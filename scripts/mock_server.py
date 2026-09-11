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
        msg_type = msg.get("type")
        if msg_type == "CONTROL":
            action = msg.get("action")
            if action == "PLAY":
                self.sim_state.running = True
                print(f"{CYAN}[Commande] Reprise de la simulation (PLAY){RESET}")
            elif action == "PAUSE":
                self.sim_state.running = False
                print(f"{CYAN}[Commande] Pause de la simulation (PAUSE){RESET}")
            elif action == "SPEED":
                self.sim_state.speed = float(msg.get("speed", 1.0))
                print(f"{CYAN}[Commande] Vitesse réglée à {self.sim_state.speed}x{RESET}")
        elif msg_type == "SUBSCRIBE":
            # Renvoie l'état complet au client abonné
            full_state = json.dumps(self.sim_state.get_full_state())
            try:
                client_sock.sendall(build_ws_text_frame(full_state))
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
