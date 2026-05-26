# Delivery 1: Android Camera Node Baseline

## Goal

Build the minimum Android foundation that can open the camera, show a preview, run a camera foreground service, and expose basic device/service status.

## Included

- Kotlin Android module using Compose.
- CameraX preview using `PreviewView`.
- Runtime camera permission request.
- Optional Android 13+ notification permission request.
- Foreground service declared with `android:foregroundServiceType="camera"`.
- Persistent foreground-service notification.
- Basic status screen.
- Battery, charging, temperature, storage, LAN IP, and app-version status.
- Local app-specific log file.

## Current Implementation Boundary

The Activity owns the visible CameraX preview. The foreground service owns the long-running service/notification lifecycle and establishes the correct Android runtime boundary for camera work.

The service does not yet perform recording or motion analysis. Delivery 2 adds local MP4 recording; Delivery 3 adds motion-triggered recording. When those arrive, the camera pipeline should move toward the service-owned long-running runtime instead of being only a visible preview.

## Acceptance Checks

| Check | Current Status |
| --- | --- |
| Camera permission flow exists | Done |
| CameraX preview source exists | Done |
| Foreground service source exists | Done |
| Manifest declares camera foreground-service type | Done |
| Status screen source exists | Done |
| Device status reader exists | Done |
| Local logger exists | Done |
| Android build can run | Requires Android SDK and Gradle tooling |
| Empty app can launch | Requires Android SDK/emulator/device |
| Camera preview can be visually confirmed | Requires physical/emulated Android device with camera |
| Screen-off service test | Requires physical Android device |

## Manual Device Test Plan

1. Install and open Aocam on an Android phone.
2. Grant camera permission.
3. Confirm the camera preview appears.
4. Start the camera service.
5. Confirm the persistent notification appears.
6. Turn the screen off for at least one minute.
7. Unlock the phone and confirm the service still shows as running.
8. Stop the service from the app or notification action.
9. Confirm the status screen and log file show lifecycle events.

## Next Delivery

Delivery 2 should add reliable manual MP4 recording, app-specific clip storage, and clip metadata.
