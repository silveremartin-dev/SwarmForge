/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.engine.EnginePreferences;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.server.ServerConfig;
import org.swarmforge.server.SwarmForgeServer;

import java.util.Scanner;

/**
 * Command-Line Interface, Options Parser, and Interactive Console Loop
 * for {@link SwarmForgeServer}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class SwarmForgeServerCli {

    private static final Logger LOG = LoggerFactory.getLogger(SwarmForgeServerCli.class);

    private SwarmForgeServerCli() {}

    public static void main(String[] args) {
        // Parse command-line arguments
        boolean createDemo = false;
        boolean listSims = false;
        boolean listScenarios = false;
        boolean batchMode = "true".equalsIgnoreCase(System.getenv("BATCH_MODE")) || "batch".equalsIgnoreCase(System.getenv("MODE"));
        long batchTicks = 1000;
        int snapshotInterval = 50;
        String exportDir = System.getenv().getOrDefault("EXPORT_DIR", "data/results");
        try {
            String envTicks = System.getenv("TICKS");
            if (envTicks != null && !envTicks.isBlank()) batchTicks = Long.parseLong(envTicks);
        } catch (Exception ignored) {}

        boolean noGui = "true".equalsIgnoreCase(System.getenv("NOGUI")) || Boolean.getBoolean("java.awt.headless") || batchMode;
        String dbMode = (System.getenv("DB_HOST") != null && !System.getenv("DB_HOST").trim().isEmpty()) ? "postgres" : "local";
        String runSimulation = null;
        String scenarioArg = System.getenv("SCENARIO");
        long masterSeed = 42L;

        // Parse global engine preferences and acceleration options first
        EnginePreferences.applyCommandLineArgs(args);

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--ticks=")) {
                batchTicks = Long.parseLong(arg.substring("--ticks=".length()));
                batchMode = true;
                continue;
            }
            if (arg.startsWith("--scenario=")) {
                scenarioArg = arg.substring("--scenario=".length());
                continue;
            }
            if (arg.startsWith("--export-dir=")) {
                exportDir = arg.substring("--export-dir=".length());
                continue;
            }
            if (arg.startsWith("--engine=") || arg.startsWith("--accel=") || arg.startsWith("--threads=")) {
                continue;
            }

            switch (arg) {
                case "--help", "-h" -> {
                    printHelp();
                    return;
                }
                case "--batch", "--headless" -> batchMode = true;
                case "--create-demo"         -> createDemo = true;
                case "--list"               -> listSims = true;
                case "--list-scenarios"     -> listScenarios = true;
                case "--local", "--no-db"   -> dbMode = "local";
                case "--postgres"           -> dbMode = "postgres";
                case "--offline"            -> dbMode = "offline";
                case "--nogui"              -> noGui = true;
                case "--engine", "--accel", "--threads" -> {
                    if (i + 1 < args.length) i++; // skipped because processed in applyCommandLineArgs
                }
                case "--rust", "--java", "--gpu", "--cpu", "--single-core", "--multi-core", "--monocoeur", "--multicoeur" -> {
                    // processed in applyCommandLineArgs
                }
                case "--ticks" -> {
                    if (i + 1 < args.length) {
                        batchTicks = Long.parseLong(args[++i]);
                        batchMode = true;
                    }
                }
                case "--snapshot-interval" -> {
                    if (i + 1 < args.length) {
                        snapshotInterval = Integer.parseInt(args[++i]);
                    }
                }
                case "--export-dir" -> {
                    if (i + 1 < args.length) {
                        exportDir = args[++i];
                    }
                }
                case "--seed" -> {
                    if (i + 1 < args.length) {
                        try {
                            masterSeed = Long.parseLong(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seed: " + args[i]);
                        }
                    }
                }
                case "--scenario", "-s" -> {
                    if (i + 1 < args.length) {
                        scenarioArg = args[++i];
                    } else {
                        System.err.println("Error: --scenario requires a scenario ID or index (1-16)");
                        System.exit(1);
                    }
                }
                case "--run" -> {
                    if (i + 1 < args.length) {
                        runSimulation = args[++i];
                    } else {
                        System.err.println("Error: --run requires a simulation name");
                        System.exit(1);
                    }
                }
                default -> {
                    if (args[i].startsWith("-")) {
                        System.err.println("Unknown option: " + args[i]);
                        printHelp();
                        System.exit(1);
                    }
                }
            }
        }

        if (listScenarios) {
            new SwarmForgeServer(ServerConfig.offline()).listAcademicScenarios();
            return;
        }

        try {
            ServerConfig config = switch (dbMode) {
                case "postgres" -> ServerConfig.fromEnvironment();
                case "offline"  -> ServerConfig.offline();
                default         -> ServerConfig.local(); // local = localhost PG → H2 fallback
            };

            LOG.info("Server mode: {} | GUI: {} | Batch: {}", dbMode, noGui ? "disabled" : "enabled", batchMode);

            SwarmForgeServer server = new SwarmForgeServer(config);

            if (listSims) {
                server.listSimulations();
                return;
            }

            Scenario matchedScenario = null;
            if (scenarioArg != null) {
                var scenarios = AcademicScenarios.getAllAcademicScenarios(masterSeed);
                // Try index first (e.g. 1 to 16)
                try {
                    int idx = Integer.parseInt(scenarioArg);
                    if (idx >= 1 && idx <= scenarios.size()) {
                        matchedScenario = scenarios.get(idx - 1);
                    }
                } catch (NumberFormatException ignored) {}

                // Try ID or name matching
                if (matchedScenario == null) {
                    for (var sc : scenarios) {
                        if (sc.getId().equalsIgnoreCase(scenarioArg) ||
                            sc.getTitle().toLowerCase().contains(scenarioArg.toLowerCase()) ||
                            scenarioArg.toLowerCase().contains(sc.getId().toLowerCase())) {
                            matchedScenario = sc;
                            break;
                        }
                    }
                }
            }

            if (batchMode) {
                server.runBatchCampaign(matchedScenario, batchTicks, exportDir, snapshotInterval);
                System.exit(0);
                return;
            }

            if (matchedScenario != null) {
                server.loadAcademicScenario(matchedScenario);
            } else if (createDemo || runSimulation == null) {
                server.createDemoWorld();
            } else {
                server.loadWorld(runSimulation);
            }

            server.start();

            // Interactive Console Loop
            System.out.println("SwarmForge Server ready. Type 'help' for commands.");

            try (Scanner scanner = new Scanner(System.in)) {
                boolean running = true;
                while (running) {
                    System.out.print("> ");
                    String line = "";
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine().trim();
                    } else {
                        break;
                    }

                    if (line.isEmpty())
                        continue;

                    String[] parts = line.split("\\s+");
                    String cmd = parts[0].toLowerCase();

                    switch (cmd) {
                        case "help", "?" -> printHelpCommands();
                        case "shutdown", "exit", "stop" -> {
                            server.stop();
                            running = false;
                        }
                        case "status" -> server.printStatusBanner();
                        case "list" -> server.listSimulations();
                        case "scenarios" -> server.listAcademicScenarios();
                        case "save" -> server.saveWorld("console_save");
                        case "info" -> server.printSimulationInfo();
                        default -> System.out.println("Unknown command: " + cmd + " (type 'help' for usage)");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Server error: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    public static void printHelpCommands() {
        System.out.println("""
                Available Commands:
                  status      - Show server health and connection status
                  info        - Show current simulation statistics
                  list        - List available simulations in database
                  scenarios   - List all 16 academic research scenario presets
                  save        - Save current simulation state
                  stop / exit - Stop the server and exit
                  help        - Show this message
                """);
    }

    public static void printHelp() {
        System.out.println("""
                +------------------------------------------------------+
                |           SWARMFORGE SERVER - HELP                   |
                +------------------------------------------------------+
                |  Usage: java -jar swarmforge-server.jar [OPTIONS]    |
                +------------------------------------------------------+
                |  Compute Engine & Acceleration options:              |
                |    --engine <auto|java|rust> Engine backend selector |
                |    --rust / --java           Shortcut engine flags   |
                |    --accel <auto|gpu|cpu>    Hardware compute accel  |
                |    --gpu / --cpu             Shortcut accel flags    |
                |    --threads <N>             Worker thread count     |
                |    --single-core / --monocoeur Force 1 core          |
                |    --multi-core / --multicoeur Max available cores   |
                |                                                      |
                |  Database mode (choose one):                         |
                |    --local          Local mode: H2 fallback if no PG |
                |                     (DEFAULT — dev/monoposte)        |
                |    --postgres       PostgreSQL + Redis (production)  |
                |    --offline        No database at all               |
                |                                                      |
                |  Scenario & Simulation options:                      |
                |    --scenario, -s <id|1-16> Run academic scenario    |
                |    --list-scenarios         List all 16 presets      |
                |    --seed <num>             Master simulation seed   |
                |    --create-demo            Create & run demo world  |
                |    --run <name>             Run simulation from DB   |
                |    --list                   List DB simulations      |
                |    --nogui                  Disable server GUI       |
                |    --help, -h               Show this help message   |
                +------------------------------------------------------+
                """);
    }
}
