#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

required_paths=(
  "android-camera-node/build.gradle.kts"
  "android-camera-node/src/main/AndroidManifest.xml"
  "android-camera-node/src/main/java/com/aocam/cameranode/MainActivity.kt"
  "local-server/src/aocam_server/main.py"
  "dashboard-web/index.html"
  "shared-contracts/openapi/camera-node.v0.yaml"
  "shared-contracts/openapi/server.v0.yaml"
  "docs/architecture.md"
  "docker/compose.yaml"
)

for path in "${required_paths[@]}"; do
  if [[ ! -e "$path" ]]; then
    echo "missing: $path"
    exit 1
  fi
done

echo "structure: ok"

PYTHONPYCACHEPREFIX="${TMPDIR:-/tmp}/aocam-delivery0-pycache" python3 -m py_compile local-server/src/aocam_server/main.py
echo "local-server syntax: ok"

PORT="${AOCAM_VERIFY_PORT:-8765}"
LOG_FILE="${TMPDIR:-/tmp}/aocam-delivery0-server.log"
python3 local-server/src/aocam_server/main.py --host 127.0.0.1 --port "$PORT" > "$LOG_FILE" 2>&1 &
SERVER_PID="$!"
trap 'kill "$SERVER_PID" >/dev/null 2>&1 || true' EXIT

for _ in {1..20}; do
  if curl -fsS "http://127.0.0.1:$PORT/health" >/dev/null 2>&1; then
    echo "local-server runtime: ok"
    break
  fi
  sleep 0.2
done

if ! curl -fsS "http://127.0.0.1:$PORT/api/dashboard/summary" >/dev/null; then
  echo "local-server dashboard summary: failed"
  exit 1
fi

echo "local-server dashboard summary: ok"

if command -v gradle >/dev/null 2>&1; then
  if [[ -n "${ANDROID_HOME:-}" || -n "${ANDROID_SDK_ROOT:-}" ]]; then
    gradle :android-camera-node:assembleDebug
    echo "android build: ok"
  else
    echo "android build: skipped - ANDROID_HOME or ANDROID_SDK_ROOT is not set"
  fi
else
  echo "android build: skipped - gradle command is not installed"
fi

if command -v adb >/dev/null 2>&1; then
  echo "android launch check: adb available, run after building APK and connecting a device"
else
  echo "android launch check: skipped - adb is not installed"
fi

echo "delivery 0 verification complete"
