#!/usr/bin/env python3
"""
SwarmForge - Serveur HTTP Client Web
===================================
Ce script distribue l'interface utilisateur Web compilée (Vite SPA) sur le port 5173.
Il permet au client léger de se connecter au véritable serveur Java SwarmForge (port 8081 / 50051).

Utilisation :
    py scripts/run/web_server.py [--port 5173]
"""

import os
import sys
import time
import socket
import argparse
import threading
import webbrowser
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

HTTP_PORT = 5173
WS_PORT = 8081
GRPC_PORT = 50051

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
WEB_DIST_DIR = os.path.join(ROOT_DIR, "swarmforge-web", "dist")

CYAN = "\033[96m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
RED = "\033[91m"
MAGENTA = "\033[95m"
BOLD = "\033[1m"
RESET = "\033[0m"


def is_port_open(host="127.0.0.1", port=8081, timeout=0.5):
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except (socket.timeout, ConnectionRefusedError, OSError):
        return False


class SPAHTTPHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIST_DIR, **kwargs)

    def do_GET(self):
        # SPA routing fallback: serve index.html for non-asset routes
        path = self.translate_path(self.path)
        if not os.path.exists(path) and not self.path.startswith(("/assets/", "/sounds/", "/3d/")):
            self.path = "/index.html"
        return super().do_GET()

    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        super().end_headers()

    def log_message(self, format, *args):
        pass


def run_web_server(port=HTTP_PORT, open_browser=True):
    print(f"{BOLD}{CYAN}======================================================{RESET}")
    print(f"{BOLD}{CYAN}      🐝 SwarmForge - Serveur HTTP Client Web 🌿      {RESET}")
    print(f"{BOLD}{CYAN}======================================================{RESET}")
    print()

    if not os.path.isdir(WEB_DIST_DIR):
        print(f"{RED}[ERREUR] Le dossier Web dist n'existe pas : {WEB_DIST_DIR}{RESET}")
        print(f"{YELLOW}Exécutez 'npm run build' dans swarmforge-web ou vérifiez les fichiers.{RESET}")
        sys.exit(1)

    # Vérification de l'état du serveur Java SwarmForge
    java_ws_running = is_port_open("127.0.0.1", WS_PORT)
    java_grpc_running = is_port_open("127.0.0.1", GRPC_PORT)

    if java_ws_running or java_grpc_running:
        print(f"{GREEN}✓ Serveur Java SwarmForge DÉTECTÉ ET ACTIF :{RESET}")
        if java_grpc_running:
            print(f"  • gRPC Engine      : {BOLD}localhost:{GRPC_PORT}{RESET}")
        if java_ws_running:
            print(f"  • WebSocket Stream : {BOLD}ws://localhost:{WS_PORT}{RESET}")
    else:
        print(f"{YELLOW}[!] Serveur Java SwarmForge non détecté actuellement.{RESET}")
        print(f"{YELLOW}    Pour exécuter la simulation réelle complète :{RESET}")
        print(f"    → Lancez {BOLD}scripts\\run\\run-server.bat{RESET} ou {BOLD}scripts\\run\\run-web-and-server.bat{RESET}")
        print(f"    Le client Web restera en attente de connexion sur le serveur Java.")

    print()

    # Démarrer le serveur HTTP pour le client Web
    ThreadingHTTPServer.allow_reuse_address = True
    httpd = ThreadingHTTPServer(("0.0.0.0", port), SPAHTTPHandler)
    http_thread = threading.Thread(target=httpd.serve_forever, daemon=True, name="http-web-server")
    http_thread.start()

    url = f"http://localhost:{port}"
    print(f"{GREEN}✓ Client Web accessible sur  :{RESET} {BOLD}{url}{RESET}")
    print()

    if open_browser:
        print(f"{MAGENTA}→ Ouverture automatique du navigateur sur {url}...{RESET}")
        try:
            webbrowser.open(url)
        except Exception:
            pass

    print(f"{CYAN}Appuyez sur {BOLD}Ctrl+C{RESET}{CYAN} dans ce terminal pour arrêter le serveur Web.{RESET}")
    print(f"{CYAN}------------------------------------------------------{RESET}")

    try:
        last_check = 0
        last_state = java_ws_running
        while True:
            time.sleep(1)
            now = time.time()
            if now - last_check > 5:
                last_check = now
                current_state = is_port_open("127.0.0.1", WS_PORT)
                if current_state != last_state:
                    last_state = current_state
                    if current_state:
                        print(f"\n{GREEN}✓ Connexion au serveur Java SwarmForge disponible sur ws://localhost:{WS_PORT}{RESET}")
                    else:
                        print(f"\n{YELLOW}[!] Serveur Java SwarmForge déconnecté.{RESET}")
    except KeyboardInterrupt:
        print(f"\n{YELLOW}Arrêt du serveur Web SwarmForge...{RESET}")
        httpd.shutdown()
        print(f"{GREEN}Serveur Web arrêté. À bientôt !{RESET}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="SwarmForge Web Client HTTP Server")
    parser.add_argument("--port", type=int, default=HTTP_PORT, help="Port HTTP (défaut: 5173)")
    parser.add_argument("--no-browser", action="store_true", help="Ne pas ouvrir le navigateur automatiquement")
    args = parser.parse_args()

    run_web_server(port=args.port, open_browser=not args.no_browser)
