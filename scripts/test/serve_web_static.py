#!/usr/bin/env python3
"""
SwarmForge - Serveur HTTP Statique pour Client Web (Port 5173)
=============================================================
Sert les fichiers compiles de swarmforge-web/dist/ pour les tests multi-clients.
Ne demarre PAS de WebSocket (le WebSocket est gere par SwarmForgeServer Java sur le port 8081).
"""

import os
import sys
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

HTTP_PORT = 5173
ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
WEB_DIST_DIR = os.path.join(ROOT_DIR, "swarmforge-web", "dist")


class SPAStaticHTTPHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIST_DIR, **kwargs)

    def do_GET(self):
        # Support du SPA routing (redirige les chemins non-fichiers vers index.html)
        path = self.translate_path(self.path)
        if not os.path.exists(path) and not os.path.splitext(self.path)[1]:
            self.path = "/index.html"
        return super().do_GET()

    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        super().end_headers()

    def log_message(self, format, *args):
        # Silencieux pour eviter de polluer la console
        pass


def run_static_server(port=HTTP_PORT):
    if not os.path.isdir(WEB_DIST_DIR):
        print(f"[ERREUR] Le dossier Web dist n'existe pas : {WEB_DIST_DIR}")
        print("Veuillez d'abord compiler le client web (npm run build).")
        sys.exit(1)

    print(f"OK Serveur HTTP Statique Web actif sur : http://localhost:{port}")
    httpd = ThreadingHTTPServer(("0.0.0.0", port), SPAStaticHTTPHandler)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nArret du serveur HTTP Statique.")
        httpd.shutdown()


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else HTTP_PORT
    run_static_server(port)
