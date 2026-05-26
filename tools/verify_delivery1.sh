#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

./tools/verify_delivery0.sh

required_paths=(
  "android-camera-node/src/main/java/com/aocam/cameranode/MainActivity.kt"
  "android-camera-node/src/main/java/com/aocam/cameranode/ui/CameraPreviewPanel.kt"
  "android-camera-node/src/main/java/com/aocam/cameranode/service/CameraNodeService.kt"
  "android-camera-node/src/main/java/com/aocam/cameranode/runtime/AocamLogger.kt"
  "android-camera-node/src/main/java/com/aocam/cameranode/runtime/CameraNodeState.kt"
  "android-camera-node/src/main/java/com/aocam/cameranode/runtime/CameraNodeStatusStore.kt"
  "android-camera-node/src/main/java/com/aocam/cameranode/runtime/DeviceStatusReader.kt"
  "android-camera-node/src/main/res/drawable/ic_aocam_notification.xml"
  "docs/delivery-1.md"
)

for path in "${required_paths[@]}"; do
  if [[ ! -e "$path" ]]; then
    echo "missing: $path"
    exit 1
  fi
done

echo "delivery1 files: ok"

rg -q 'val cameraXVersion = "1\.6\.1"' android-camera-node/build.gradle.kts
rg -q 'androidx.camera:camera-view:\$cameraXVersion' android-camera-node/build.gradle.kts
rg -q 'androidx.activity:activity-compose:1\.12\.4' android-camera-node/build.gradle.kts
rg -q 'androidx.compose:compose-bom:2026\.04\.01' android-camera-node/build.gradle.kts
rg -q 'org.jetbrains.kotlin.plugin.compose' build.gradle.kts android-camera-node/build.gradle.kts
if rg -q 'org.jetbrains.kotlin.android' build.gradle.kts android-camera-node/build.gradle.kts; then
  echo "obsolete kotlin android plugin still present"
  exit 1
fi
echo "delivery1 dependencies: ok"

rg -q 'android:foregroundServiceType="camera"' android-camera-node/src/main/AndroidManifest.xml
rg -q 'android.permission.FOREGROUND_SERVICE_CAMERA' android-camera-node/src/main/AndroidManifest.xml
rg -q 'android.permission.CAMERA' android-camera-node/src/main/AndroidManifest.xml
echo "delivery1 manifest: ok"

rg -q 'ProcessCameraProvider' android-camera-node/src/main/java/com/aocam/cameranode/ui/CameraPreviewPanel.kt
rg -q 'PreviewView' android-camera-node/src/main/java/com/aocam/cameranode/ui/CameraPreviewPanel.kt
rg -q 'ServiceCompat.startForeground' android-camera-node/src/main/java/com/aocam/cameranode/service/CameraNodeService.kt
rg -q 'BatteryManager' android-camera-node/src/main/java/com/aocam/cameranode/runtime/DeviceStatusReader.kt
echo "delivery1 source checks: ok"

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
  echo "android launch check: adb available, run install/launch after assembleDebug"
else
  echo "android launch check: skipped - adb is not installed"
fi

echo "delivery 1 verification complete"
