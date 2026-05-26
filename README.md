# Aocam

Aocam is an offline-first Android security camera system. The project starts with one reliable Android phone camera node, then grows into a LAN-only multi-camera dashboard.

The first hard rule is that a phone must keep recording without the local server. The server is for visibility, coordination, health monitoring, and optional backup.

## Current Status

Delivery 1 is implemented as the Android camera-node baseline.

Implemented now:

- Compose-based Android status screen.
- Runtime camera permission request.
- CameraX preview bound to the visible Activity lifecycle.
- Camera foreground service with persistent notification and stop action.
- Manifest declarations for camera foreground-service type.
- Device status display: service state, camera state, permissions, battery, temperature, storage, LAN IP, app version, last error.
- App-specific local log file at runtime.
- Delivery 1 verifier for file/dependency/manifest checks.

Important limitation:

- This machine does not currently have Android SDK, Gradle, or `adb`, so the Android build and device launch checks cannot be completed locally yet.

## Delivery 0 Status

This repository now has the project skeleton and component boundaries:

```text
aocam/
  android-camera-node/
  local-server/
  dashboard-web/
  shared-contracts/
  docs/
  tools/
  docker/
```

Included in this delivery:

- Android camera-node Gradle project boundary.
- Empty Android app entry point.
- Local server placeholder that can start without external Python packages.
- Dashboard static placeholder.
- Initial camera-node and server API contract stubs.
- Architecture notes and delivery-0 checklist.
- Verification script for skeleton health.

## Current Technology Defaults

| Area | Default |
| --- | --- |
| Android language | Kotlin |
| Android build | Gradle + Android Gradle Plugin |
| Android UI | Jetpack Compose from Delivery 1 onward |
| Camera | CameraX |
| Local database | Room / SQLite |
| Settings | Jetpack DataStore |
| Phone LAN API | Ktor embedded server or a lightweight local HTTP server |
| Local server | FastAPI + SQLite first, PostgreSQL later only if needed |
| Dashboard | Simple HTML/CSS/JS first, React + TypeScript later |
| Live view | Snapshot refresh first, MJPEG next, WebRTC later |
| Video format | MP4 |
| Default recording | 720p, 15 FPS, audio off |
| Retention | 7 days, oldest unprotected clips first |

The current server placeholder intentionally uses Python's standard library so Delivery 0 can be run before FastAPI is installed.

## Verify Delivery 1

Run:

```bash
./tools/verify_delivery1.sh
```

Expected right now:

- Delivery 0 checks pass.
- Delivery 1 Android source, manifest, and dependency checks pass.
- Android build/launch checks are skipped until Android SDK/Gradle/`adb` are installed.

## Verify Delivery 0

Run:

```bash
./tools/verify_delivery0.sh
```

Expected right now:

- File structure checks pass.
- Local server placeholder starts and returns JSON.
- Android tooling check reports whether Android SDK/Gradle are available.

This machine currently needs Android SDK/Gradle tooling before the Android build/launch acceptance test can pass.

## Run Local Server Placeholder

```bash
python3 local-server/src/aocam_server/main.py --host 127.0.0.1 --port 8765
```

Then open:

```text
http://127.0.0.1:8765/
http://127.0.0.1:8765/health
http://127.0.0.1:8765/api/dashboard/summary
```

## Android Project

The Android skeleton is in `android-camera-node/`. It is meant to be opened in Android Studio or built with Gradle after Android SDK tooling is installed.

The app now has the Delivery 1 baseline: permission flow, CameraX preview, foreground camera service, status view, and local logging.
