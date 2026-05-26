# Aocam Architecture Notes

## Product Shape

Aocam is a hybrid local security system:

- Android camera nodes own camera capture, motion detection, local recording, retention, heat protection, and direct LAN access.
- The local server owns registry, health polling, event aggregation, dashboard data, and optional backup.
- The browser dashboard presents a multi-camera security-room view.

The phone must never require the server for basic recording. If the server, dashboard, browser, or network fails, the phone should continue protecting its own area.

## Component Boundaries

| Component | Responsibility | Not Responsible For |
| --- | --- | --- |
| `android-camera-node` | Capture, motion detection, recording, local storage, retention, phone health, LAN API | Multi-camera aggregation, central backup policy |
| `local-server` | Camera registry, polling, event aggregation, alert logic, optional backup coordination | Direct camera capture, mandatory recording pipeline |
| `dashboard-web` | Browser UI for security-room monitoring and review | Long-running camera work |
| `shared-contracts` | API schemas, DTOs, cross-component contracts | Product documentation |
| `docs` | Architecture, setup, operations, troubleshooting | Runtime behavior |
| `docker` | Local server deployment support | Android deployment |
| `tools` | Verification and developer utilities | Production app code |

## Delivery Strategy

1. Build one reliable Android camera node.
2. Add manual local recording.
3. Add motion-triggered recording.
4. Add retention and storage safety.
5. Add single-camera LAN dashboard.
6. Harden runtime, recovery, and heat behavior.
7. Add local server and multi-camera dashboard.

The multi-camera dashboard should wait until the single-phone camera is stable enough to trust overnight.

## Initial Defaults

| Area | Default |
| --- | --- |
| Android min SDK | 26 |
| Android compile/target SDK | 36 |
| Android Gradle Plugin | 9.2.0 |
| Kotlin support | AGP built-in Kotlin |
| Compose compiler plugin | 2.3.21 |
| Compose BOM | 2026.04.01 |
| CameraX | 1.6.1 |
| Activity Compose | 1.12.4 |
| Lifecycle | 2.10.0 |
| Recording resolution | 1280 x 720 |
| Recording FPS | 15 |
| Audio | Off |
| Segment length | 60 seconds |
| Motion detection frame size | About 320 x 180 |
| Detection FPS | 2-5 |
| Retention | 7 days |
| Live view | Snapshot refresh, then MJPEG |
| Server database | SQLite first |
| Server backup | Disabled by default |
| Internet exposure | Disabled / unsupported |

## Future Delivery Dependencies

Delivery 1 introduced CameraX preview, runtime permission handling, foreground service lifecycle, device status, and local logging.

Delivery 2 should introduce Room/SQLite clip metadata and MP4 recording.

Delivery 5 should introduce the embedded phone HTTP server and authentication.

Delivery 7 should introduce the local server database and polling worker.
