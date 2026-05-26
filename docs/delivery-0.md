# Delivery 0: Project Skeleton

## Goal

Create the repository structure and component boundaries for Aocam.

## Included

- `android-camera-node/`
- `local-server/`
- `dashboard-web/`
- `shared-contracts/`
- `docs/`
- `docker/`
- `tools/`
- Root README
- Architecture notes
- Initial API contract stubs
- Runnable local server placeholder

## Acceptance Checks

| Check | Current Status |
| --- | --- |
| Repository structure exists | Done |
| Android project boundary exists | Done |
| Empty Android app source exists | Done |
| Local server placeholder can start | Done |
| Shared API contracts exist | Done |
| Architecture notes exist | Done |
| Android build can run | Requires Android SDK and Gradle tooling |
| Empty Android app can launch | Requires Android SDK/emulator/device |

## Next Delivery

Delivery 1 should make `android-camera-node` open the device camera through CameraX and run camera work through a foreground service.
