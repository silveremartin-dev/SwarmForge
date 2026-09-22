#!/usr/bin/env python3
"""
SwarmForge - Serveur de Test & Mock Autonome (Zero Dependency)
============================================================
Ce script lance :
 1. Un serveur HTTP pour distribuer le client Web (port 5173)
 2. Un serveur WebSocket interactif (port 8081) diffusant en temps réel
    l'état d'une simulation vivante (colonies, fourmis, nids, météo, cycle jour/nuit)

Utilisation :
    py scripts/mock_server.py
    (ou scripts\\run-web.bat / scripts\\run-web.ps1)
"""

import os
import sys
import json
import math
import time
import base64
import hashlib
import struct
import socket
import select
import random
import threading
import webbrowser
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

# Configuration
HTTP_PORT = 5173
WS_PORT = 8081
ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
WEB_DIST_DIR = os.path.join(ROOT_DIR, "swarmforge-web", "dist")

# ANSI Colors for terminal output
CYAN = "\033[96m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
RED = "\033[91m"
MAGENTA = "\033[95m"
BOLD = "\033[1m"
RESET = "\033[0m"

# WebSocket RFC 6455 Helper functions (Zero-dependency pure Python)
WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

def create_ws_accept_key(client_key):
    sha1 = hashlib.sha1((client_key.strip() + WS_GUID).encode('utf-8')).digest()
    return base64.b64encode(sha1).decode('utf-8')

def build_ws_text_frame(message: str) -> bytes:
    data = message.encode('utf-8')
    length = len(data)
    if length <= 125:
        header = struct.pack('!BB', 0x81, length)
    elif length <= 65535:
        header = struct.pack('!BBH', 0x81, 126, length)
    else:
        header = struct.pack('!BBQ', 0x81, 127, length)
    return header + data

def decode_ws_frame(data: bytes):
    if len(data) < 2:
        return None, 0
    first_byte, second_byte = data[0], data[1]
    opcode = first_byte & 0x0F
    is_masked = bool(second_byte & 0x80)
    payload_len = second_byte & 0x7F

    offset = 2
    if payload_len == 126:
        if len(data) < 4:
            return None, 0
        payload_len = struct.unpack('!H', data[2:4])[0]
        offset = 4
    elif payload_len == 127:
        if len(data) < 10:
            return None, 0
        payload_len = struct.unpack('!Q', data[2:10])[0]
        offset = 10

    if is_masked:
        mask = data[offset:offset+4]
        offset += 4
        if len(data) < offset + payload_len:
            return None, 0
        raw_payload = data[offset:offset+payload_len]
        decoded = bytes(b ^ mask[i % 4] for i, b in enumerate(raw_payload))
    else:
        if len(data) < offset + payload_len:
            return None, 0
        decoded = data[offset:offset+payload_len]

    return (opcode, decoded), offset + payload_len


class SimulationState:
    def __init__(self):
        self.tick = 0
        self.running = True
        self.speed = 1.0
        self.start_time = time.time()
        self.colonies = [
            {"id": "COLONY_1", "name": "Colonie #1 (Formica fusca)", "species": "Formica fusca", "color": "#38bdf8", "foodStored": 320, "queenCount": 1, "workerCount": 50},
            {"id": "COLONY_2", "name": "Colonie #2 (Linepithema humile)", "species": "Linepithema humile", "color": "#f43f5e", "foodStored": 240, "queenCount": 2, "workerCount": 40},
        ]
        self.ants = []
        self.food_sources = [
            {"id": "food_1", "x": 40.0, "y": 60.0, "quantity": 250, "type": "SUGAR_NECTAR"},
            {"id": "food_2", "x": 65.0, "y": 35.0, "quantity": 180, "type": "SEEDS"},
            {"id": "food_3", "x": 50.0, "y": 80.0, "quantity": 120, "type": "INSECT_PREY"}
        ]
        self.predators = [
            {"id": "pred_1", "x": 50.0, "y": 50.0, "type": "SPIDER", "state": "PATROLLING", "heading": 0.0}
        ]
        self.init_ants()

    def init_ants(self):
        self.ants = []
        for col_idx, col in enumerate(self.colonies):
            base_x = 35.0 if col_idx == 0 else 65.0
            base_y = 35.0 if col_idx == 0 else 65.0
            
            # Reine
            self.ants.append({
                "id": f"ant_{col['id']}_queen",
                "colonyId": col["id"],
                "colonyName": col["name"],
                "species": col["species"],
                "caste": "QUEEN",
                "job": "LAYING_EGGS",
                "x": base_x,
                "y": base_y,
                "z": 0.0,
                "heading": random.uniform(0, math.pi * 2),
                "health": 100,
                "energy": 100,
                "ageInDays": 380,
                "maxLifespanDays": 3650,
                "bodyLengthMm": 12.0,
                "carriedItem": "NONE",
                "diseaseState": "HEALTHY",
                "isAlive": True
            })

            # Ouvrières et soldats
            for i in range(35):
                is_soldier = (i < 8)
                angle = random.uniform(0, math.pi * 2)
                dist = random.uniform(1.0, 20.0)
                self.ants.append({
                    "id": f"ant_{col['id']}_{i}",
                    "colonyId": col["id"],
                    "colonyName": col["name"],
                    "species": col["species"],
                    "caste": "SOLDIER" if is_soldier else "WORKER",
                    "job": "GUARDING" if is_soldier else random.choice(["FORAGING", "EXPLORING", "NURSING", "EXCAVATING"]),
                    "x": max(5.0, min(95.0, base_x + math.cos(angle) * dist)),
                    "y": max(5.0, min(95.0, base_y + math.sin(angle) * dist)),
                    "z": 0.0,
                    "heading": angle,
                    "health": random.randint(90, 100),
                    "energy": random.randint(70, 100),
                    "ageInDays": random.randint(4, 25),
                    "maxLifespanDays": 180 if is_soldier else 120,
                    "bodyLengthMm": 7.5 if is_soldier else 5.0,
                    "carriedItem": random.choice(["NONE", "NONE", "SEEDS", "SUGAR_NECTAR"]),
                    "diseaseState": "HEALTHY",
                    "isAlive": True
                })

    def update_tick(self):
        if not self.running:
            return

        self.tick += 1
        sim_sec = self.tick * self.speed
        
        # Déplacement vivant des fourmis
        for ant in self.ants:
            if ant["caste"] == "QUEEN":
                continue
            
            step = random.uniform(0.3, 0.7) * min(self.speed, 3.0)
            ant["heading"] += random.uniform(-0.35, 0.35)
            
            # Attirance vers la nourriture si butineuse
            if ant["job"] == "FORAGING" and ant["carriedItem"] == "NONE":
                target_food = self.food_sources[0]
                dx = target_food["x"] - ant["x"]
                dy = target_food["y"] - ant["y"]
                dist = math.hypot(dx, dy)
                if dist < 3.0:
                    ant["carriedItem"] = target_food["type"]
                elif dist > 0.1:
                    ant["heading"] = math.atan2(dy, dx) + random.uniform(-0.2, 0.2)
            
            # Retour au nid si elle porte de la nourriture
            elif ant["carriedItem"] != "NONE":
                col_base_x = 35.0 if "COLONY_1" in ant["colonyId"] else 65.0
                col_base_y = 35.0 if "COLONY_1" in ant["colonyId"] else 65.0
                dx = col_base_x - ant["x"]
                dy = col_base_y - ant["y"]
                dist = math.hypot(dx, dy)
                if dist < 4.0:
                    ant["carriedItem"] = "NONE"
                else:
                    ant["heading"] = math.atan2(dy, dx) + random.uniform(-0.1, 0.1)

            nx = ant["x"] + math.cos(ant["heading"]) * step
            ny = ant["y"] + math.sin(ant["heading"]) * step

            if nx < 3.0 or nx > 97.0:
                ant["heading"] = math.pi - ant["heading"]
                nx = max(3.0, min(97.0, nx))
            if ny < 3.0 or ny > 97.0:
                ant["heading"] = -ant["heading"]
                ny = max(3.0, min(97.0, ny))

            ant["x"] = nx
            ant["y"] = ny
            ant["energy"] = max(10, ant["energy"] - 0.01)

        # Mouvement du prédateur
        for pred in self.predators:
            pred["heading"] += random.uniform(-0.2, 0.2)
            pred["x"] = max(10.0, min(90.0, pred["x"] + math.cos(pred["heading"]) * 0.4))
            pred["y"] = max(10.0, min(90.0, pred["y"] + math.sin(pred["heading"]) * 0.4))

    def get_full_state(self):
        # Calcul du cycle jour/nuit (période = 240 secondes)
        cycle_progress = ((self.tick * 0.02) % 1.0)
        light_level = max(0.1, math.sin(cycle_progress * math.pi * 2) * 0.9 + 0.1)
        time_of_day = "DAY" if light_level > 0.4 else ("DAWN" if cycle_progress < 0.25 else ("DUSK" if cycle_progress < 0.75 else "NIGHT"))
        temperature = 18.0 + math.sin(cycle_progress * math.pi * 2) * 7.0

        return {
            "type": "STATE",
            "tick": self.tick,
            "running": self.running,
            "speed": self.speed,
            "colonies": self.colonies,
            "individuals": self.ants,
            "foodSources": self.food_sources,
            "predators": self.predators,
            "environment": {
                "lightLevel": round(light_level, 2),
                "timeOfDay": time_of_day,
                "sunAngle": round(cycle_progress, 3),
                "temperature": round(temperature, 1),
                "humidity": 65,
                "rainIntensity": 0.0,
                "windSpeed": 3.8,
                "weatherState": "CLEAR",
                "season": "SUMMER"
            }
        }


class MockWebSocketServer:
    def __init__(self, host="0.0.0.0", port=8081, sim_state=None):
        self.host = host
        self.port = port
        self.sim_state = sim_state or SimulationState()
        self.clients = set()
        self.lock = threading.Lock()
        self.server_sock = None
        self.running = False
        self.lobby_status = "LOBBY_WAITING"  # LOBBY_WAITING, ACTIVE, INACTIVE
        self.selected_scenario_id = "ACAD_01_LEVY_BROWNIAN"
        self.lobby_players = []

    def start(self):
        self.server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_sock.bind((self.host, self.port))
        self.server_sock.listen(10)
        self.running = True

        threading.Thread(target=self._accept_loop, daemon=True, name="ws-accept").start()
        threading.Thread(target=self._simulation_loop, daemon=True, name="ws-broadcast").start()

    def _accept_loop(self):
        while self.running:
            try:
                client_sock, addr = self.server_sock.accept()
                threading.Thread(target=self._handle_client, args=(client_sock, addr), daemon=True).start()
            except Exception:
                break

    def _handle_client(self, client_sock, addr):
        try:
            # 1. WebSocket Handshake
            request = client_sock.recv(4096).decode('utf-8', errors='ignore')
            if "Upgrade: websocket" not in request and "Upgrade: WebSocket" not in request:
                client_sock.close()
                return

            sec_key = None
            for line in request.split("\r\n"):
                if line.lower().startswith("sec-websocket-key:"):
                    sec_key = line.split(":", 1)[1].strip()
                    break

            if not sec_key:
                client_sock.close()
                return

            accept_key = create_ws_accept_key(sec_key)
            handshake_response = (
                "HTTP/1.1 101 Switching Protocols\r\n"
                "Upgrade: websocket\r\n"
                "Connection: Upgrade\r\n"
                f"Sec-WebSocket-Accept: {accept_key}\r\n"
                "\r\n"
            )
            client_sock.sendall(handshake_response.encode('utf-8'))

            with self.lock:
                self.clients.add(client_sock)
            print(f"{GREEN}[WebSocket] Nouveau client connecté : {addr[0]}:{addr[1]} (Total: {len(self.clients)}){RESET}")

            # Envoi du message initial d'accueil et de l'état complet
            welcome = json.dumps({"type": "WELCOME", "message": "Connecté au serveur Mock SwarmForge"})
            client_sock.sendall(build_ws_text_frame(welcome))
            
            init_state = json.dumps(self.sim_state.get_full_state())
            client_sock.sendall(build_ws_text_frame(init_state))

            # 2. Message Receive Loop
            buffer = b""
            while self.running:
                chunk = client_sock.recv(4096)
                if not chunk:
                    break
                buffer += chunk
                while buffer:
                    res = decode_ws_frame(buffer)
                    if res[0] is None:
                        break
                    (opcode, payload), consumed = res
                    buffer = buffer[consumed:]
                    
                    if opcode == 0x8:  # Close
                        return
                    elif opcode == 0x9:  # Ping
                        client_sock.sendall(struct.pack('!BB', 0x8A, 0))
                    elif opcode == 0x1:  # Text frame
                        try:
                            msg = json.loads(payload.decode('utf-8'))
                            self._handle_client_message(msg, client_sock)
                        except Exception as e:
                            pass
        except Exception as e:
            pass
        finally:
            with self.lock:
                self.clients.discard(client_sock)
            try:
                client_sock.close()
            except Exception:
                pass
            print(f"{YELLOW}[WebSocket] Client déconnecté : {addr[0]}:{addr[1]}{RESET}")

    def _handle_client_message(self, msg, client_sock):
        msg_type = (msg.get("type") or "").upper()
        if msg_type == "PING":
            pong = json.dumps({"type": "PONG", "timestamp": int(time.time() * 1000)})
            try:
                client_sock.sendall(build_ws_text_frame(pong))
            except Exception:
                pass
        elif msg_type in ("CONTROL", "CONTROL_COMMAND"):
            action = (msg.get("action") or "").upper()
            if action in ("PLAY", "START"):
                self.sim_state.running = True
                print(f"{CYAN}[Commande] Reprise de la simulation (PLAY){RESET}")
            elif action == "PAUSE":
                self.sim_state.running = False
                print(f"{CYAN}[Commande] Pause de la simulation (PAUSE){RESET}")
            elif action == "STEP":
                self.sim_state.update_tick()
                print(f"{CYAN}[Commande] Avance d'un pas (STEP -> Tick #{self.sim_state.tick}){RESET}")
            elif action in ("SPEED", "SET_SPEED"):
                self.sim_state.speed = float(msg.get("value") or msg.get("speed", 1.0))
                print(f"{CYAN}[Commande] Vitesse réglée à {self.sim_state.speed}x{RESET}")
            elif action == "RESET":
                self.sim_state.tick = 0
                self.sim_state.init_ants()
                print(f"{CYAN}[Commande] Réinitialisation de la simulation (RESET){RESET}")
        elif msg_type == "DEPLOY_SCENARIO":
            scenario = msg.get("scenario", {})
            print(f"{GREEN}[Scénario] Déploiement d'un nouveau scénario maître depuis le client distant !{RESET}")
            cards = scenario.get("speciesCards")
            if cards:
                self.sim_state.colonies = [
                    {
                        "id": c.get("id", f"COL_{idx}"),
                        "name": c.get("name", f"Colonie #{idx+1}"),
                        "species": c.get("speciesId", "Formica fusca"),
                        "color": c.get("color", "#38bdf8"),
                        "foodStored": 250,
                        "queenCount": c.get("initialQueens", 1),
                        "workerCount": c.get("initialWorkers", 40)
                    }
                    for idx, c in enumerate(cards)
                ]
            self.sim_state.tick = 0
            self.sim_state.init_ants()
            
            # Répond avec la confirmation de scénario
            sc_state = json.dumps({"type": "SCENARIO_STATE", "scenario": scenario})
            try:
                client_sock.sendall(build_ws_text_frame(sc_state))
            except Exception:
                pass
        elif msg_type == "LIST_SERVER_SCENARIOS" or msg_type == "GET_SCENARIOS":
            sc_list = self._build_scenarios_list()
            payload = json.dumps({"type": "SERVER_SCENARIOS_LIST", "scenarios": sc_list})
            try:
                client_sock.sendall(build_ws_text_frame(payload))
            except Exception:
                pass
        elif msg_type == "PLAYER_READY":
            ready = bool(msg.get("ready", True))
            for p in self.lobby_players:
                if p.get("sock") == client_sock:
                    p["isReady"] = ready
            self._broadcast_lobby_state()
        elif msg_type == "START_MATCH":
            if self.lobby_status != "ACTIVE":
                self.lobby_status = "ACTIVE"
                self.sim_state.running = True
                print(f"{GREEN}[Matchmaking] Partie démarrée par l'Hôte ! Simulation active.{RESET}")
                self._broadcast_lobby_state()
        elif msg_type == "SELECT_SCENARIO":
            if self.lobby_status != "ACTIVE":
                sc_id = msg.get("scenarioId")
                if sc_id:
                    self.selected_scenario_id = sc_id
                    print(f"{CYAN}[Catalogue] Scénario sélectionné : {sc_id}{RESET}")
                    self._broadcast_lobby_state()
                    self._broadcast_scenarios_list()
        elif msg_type in ("SUBSCRIBE", "JOIN_SESSION"):
            alias = msg.get("participantTag", "Joueur")
            species = msg.get("species", "Formica fusca")
            role = msg.get("role", "JOIN")
            
            # Register player in lobby
            existing = [p for p in self.lobby_players if p.get("sock") == client_sock]
            if not existing:
                self.lobby_players.append({
                    "sock": client_sock,
                    "tag": alias,
                    "species": species,
                    "role": role,
                    "isReady": False,
                    "joinedAt": int(time.time() * 1000)
                })

            # Send full state, scenario catalog, and lobby state
            full_state = json.dumps(self.sim_state.get_full_state())
            sc_list = json.dumps({"type": "SERVER_SCENARIOS_LIST", "scenarios": self._build_scenarios_list()})
            try:
                client_sock.sendall(build_ws_text_frame(full_state))
                client_sock.sendall(build_ws_text_frame(sc_list))
            except Exception:
                pass
            self._broadcast_lobby_state()
        elif msg_type == "GOD_MODE_INTERVENTION":
            intervention = msg.get("intervention", {})
            cat = intervention.get("category", "SYSTEM")
            typ = intervention.get("type", "Action")
            print(f"{MAGENTA}[God Mode] Intervention divine reçue : [{cat}] {typ}{RESET}")
        elif msg_type == "SCHEDULE_EVENT":
            evt = msg.get("scheduledEvent", {})
            target = evt.get("targetTick", 0)
            desc = evt.get("description", "Événement planifié")
            print(f"{MAGENTA}[God Mode] Événement programmé pour le tick #{target} : {desc}{RESET}")

    def _build_scenarios_list(self):
        catalog = [
            {
                "id": "ACAD_01_LEVY_BROWNIAN",
                "title": "Exploration Strategy: Lévy Flights vs Brownian Walk",
                "description": "Comparative study of foraging harvesting efficiency between Neural/RL and Brownian FSM ants.",
                "academicCategory": "Ethology / Optimal Foraging Theory",
                "biomeName": "TEMPERATE_FOREST",
                "requiredPlayerCount": 2,
                "isMultiplayerOnly": False,
                "maxDurationValue": 100.0,
                "maxDurationUnit": "Days (d)",
                "status": self.lobby_status if self.selected_scenario_id == "ACAD_01_LEVY_BROWNIAN" else "INACTIVE"
            },
            {
                "id": "ACAD_02_POLYETHISM_BDI",
                "title": "Polyethism and Division of Labor (BDI)",
                "description": "Analysis of the emergence of division of labor driven by adaptive BDI cognitive engines.",
                "academicCategory": "Sociobiology / Division of Labor",
                "biomeName": "MEDITERRANEAN",
                "requiredPlayerCount": 1,
                "isMultiplayerOnly": False,
                "maxDurationValue": 100.0,
                "maxDurationUnit": "Days (d)",
                "status": self.lobby_status if self.selected_scenario_id == "ACAD_02_POLYETHISM_BDI" else "INACTIVE"
            },
            {
                "id": "MP_BATTLE_ARENA_1V1",
                "title": "Multiplayer Competitive Arena (1v1)",
                "description": "Direct territorial competition between two equal colonies with resource contested hotspots.",
                "academicCategory": "Competition / Game Theory",
                "biomeName": "TEMPERATE_FOREST",
                "requiredPlayerCount": 2,
                "isMultiplayerOnly": True,
                "maxDurationValue": 30.0,
                "maxDurationUnit": "Days (d)",
                "status": self.lobby_status if self.selected_scenario_id == "MP_BATTLE_ARENA_1V1" else "INACTIVE"
            },
            {
                "id": "MP_COOP_TRIBUTE_TRADE",
                "title": "Multiplayer Cooperative Tributary Trade (Co-op)",
                "description": "Cooperative ecosystem management with complementary ecological niches and symbiotic exchanges.",
                "academicCategory": "Symbiosis / Mutualism",
                "biomeName": "TROPICAL_RAINFOREST",
                "requiredPlayerCount": 2,
                "isMultiplayerOnly": True,
                "maxDurationValue": 60.0,
                "maxDurationUnit": "Days (d)",
                "status": self.lobby_status if self.selected_scenario_id == "MP_COOP_TRIBUTE_TRADE" else "INACTIVE"
            },
            {
                "id": "MP_MEGATERRARIUM_SHARDED_4NODE",
                "title": "Megaterrarium 4-Node Sharded Federation (4 Players)",
                "description": "Large-scale sharded simulation across 4 contiguous spatial nodes for high-density multi-colony colonies.",
                "academicCategory": "Distributed Systems / Spatial Ecology",
                "biomeName": "TEMPERATE_FOREST",
                "requiredPlayerCount": 4,
                "isMultiplayerOnly": True,
                "maxDurationValue": 180.0,
                "maxDurationUnit": "Days (d)",
                "status": self.lobby_status if self.selected_scenario_id == "MP_MEGATERRARIUM_SHARDED_4NODE" else "INACTIVE"
            }
        ]
        return catalog

    def _broadcast_lobby_state(self):
        players_payload = [
            {
                "tag": p["tag"],
                "species": p["species"],
                "role": p["role"],
                "isReady": p["isReady"],
                "joinedAt": p["joinedAt"]
            }
            for p in self.lobby_players
        ]
        msg = json.dumps({
            "type": "LOBBY_STATE",
            "status": self.lobby_status,
            "selectedScenarioId": self.selected_scenario_id,
            "players": players_payload,
            "playerCount": len(players_payload)
        })
        frame = build_ws_text_frame(msg)
        with self.lock:
            for c in list(self.clients):
                try:
                    c.sendall(frame)
                except Exception:
                    pass

    def _broadcast_scenarios_list(self):
        sc_list = self._build_scenarios_list()
        msg = json.dumps({"type": "SERVER_SCENARIOS_LIST", "scenarios": sc_list})
        frame = build_ws_text_frame(msg)
        with self.lock:
            for c in list(self.clients):
                try:
                    c.sendall(frame)
                except Exception:
                    pass

    def _simulation_loop(self):
        fps = 20
        frame_time = 1.0 / fps
        while self.running:
            t0 = time.time()
            self.sim_state.update_tick()

            # Diffuse à tous les clients connectés
            with self.lock:
                clients_copy = list(self.clients)

            if clients_copy:
                payload = json.dumps(self.sim_state.get_full_state())
                frame = build_ws_text_frame(payload)
                for c in clients_copy:
                    try:
                        c.sendall(frame)
                    except Exception:
                        pass

            elapsed = time.time() - t0
            sleep_time = max(0.005, frame_time - elapsed)
            time.sleep(sleep_time)


class CustomHTTPHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIST_DIR, **kwargs)

    def do_GET(self):
        # SPA routing fallback: serve index.html for non-asset routes
        path = self.translate_path(self.path)
        if not os.path.exists(path) and not self.path.startswith("/assets/") and not self.path.startswith("/sounds/") and not self.path.startswith("/3d/"):
            self.path = "/index.html"
        return super().do_GET()

    def end_headers(self):
        # Enable CORS and caching headers
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        super().end_headers()

    def log_message(self, format, *args):
        # Filtrer le bruit des requêtes statiques normales
        pass


def run_server():
    print(f"{BOLD}{CYAN}======================================================{RESET}")
    print(f"{BOLD}{CYAN}      🐝 SwarmForge - Serveur Démo & Test Web 🌿     {RESET}")
    print(f"{BOLD}{CYAN}======================================================{RESET}")
    print()

    if not os.path.isdir(WEB_DIST_DIR):
        print(f"{RED}[ERREUR] Le dossier Web dist n'existe pas : {WEB_DIST_DIR}{RESET}")
        print(f"{YELLOW}Exécutez d'abord la compilation ou vérifiez les fichiers.{RESET}")
        sys.exit(1)

    # 1. Démarrer le serveur WebSocket (8081)
    sim_state = SimulationState()
    ws_server = MockWebSocketServer(host="0.0.0.0", port=WS_PORT, sim_state=sim_state)
    ws_server.start()
    print(f"{GREEN}✓ Serveur WebSocket actif :{RESET} {BOLD}ws://localhost:{WS_PORT}{RESET} (Simulation vivante 20 FPS)")

    # 2. Démarrer le serveur HTTP (5173)
    ThreadingHTTPServer.allow_reuse_address = True
    httpd = ThreadingHTTPServer(("0.0.0.0", HTTP_PORT), CustomHTTPHandler)
    http_thread = threading.Thread(target=httpd.serve_forever, daemon=True, name="http-server")
    http_thread.start()
    url = f"http://localhost:{HTTP_PORT}"
    print(f"{GREEN}✓ Serveur Web HTTP actif  :{RESET} {BOLD}{url}{RESET}")
    print()
    print(f"{MAGENTA}→ Ouverture automatique du navigateur sur {url}...{RESET}")
    print(f"{CYAN}Appuyez sur {BOLD}Ctrl+C{RESET}{CYAN} dans ce terminal pour arrêter le serveur.{RESET}")
    print(f"{CYAN}------------------------------------------------------{RESET}")

    # 3. Ouvrir le navigateur
    try:
        webbrowser.open(url)
    except Exception:
        pass

    try:
        while True:
            time.sleep(1)
            # Affichage périodique d'un heartbeat en console
            if sim_state.tick % 40 == 0 and sim_state.tick > 0:
                print(f"  [Simulation] Tick #{sim_state.tick} | Fourmis actives: {len(sim_state.ants)} | Clients connectés: {len(ws_server.clients)}", end="\r")
    except KeyboardInterrupt:
        print(f"\n{YELLOW}Arrêt des serveurs SwarmForge...{RESET}")
        ws_server.running = False
        httpd.shutdown()
        print(f"{GREEN}Serveurs arrêtés. À bientôt !{RESET}")


if __name__ == "__main__":
    run_server()
