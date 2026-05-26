from __future__ import annotations

import argparse
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any


def json_response(payload: dict[str, Any], status: int = 200) -> bytes:
    body = json.dumps(payload, indent=2, sort_keys=True).encode("utf-8")
    return body


class AocamHandler(BaseHTTPRequestHandler):
    server_version = "AocamDelivery0/0.0.1"

    def do_GET(self) -> None:
        routes = {
            "/": self.root,
            "/health": self.health,
            "/api/dashboard/summary": self.dashboard_summary,
        }

        handler = routes.get(self.path)
        if handler is None:
            self.send_json({"error": "not_found", "path": self.path}, status=404)
            return

        self.send_json(handler())

    def root(self) -> dict[str, Any]:
        return {
            "service": "aocam-local-server",
            "version": "0.0.1-delivery0",
            "status": "placeholder",
            "links": {
                "health": "/health",
                "dashboard_summary": "/api/dashboard/summary",
            },
        }

    def health(self) -> dict[str, Any]:
        return {
            "ok": True,
            "component": "local-server",
            "delivery": 0,
        }

    def dashboard_summary(self) -> dict[str, Any]:
        return {
            "cameras": [],
            "alerts": [],
            "message": "No cameras are registered in Delivery 0.",
        }

    def send_json(self, payload: dict[str, Any], status: int = 200) -> None:
        body = json_response(payload, status)
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format: str, *args: Any) -> None:
        return


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Run the Aocam Delivery 0 local server placeholder.")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", default=8765, type=int)
    return parser


def main() -> None:
    args = build_parser().parse_args()
    server = ThreadingHTTPServer((args.host, args.port), AocamHandler)
    print(f"Aocam local server placeholder listening on http://{args.host}:{args.port}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    main()
