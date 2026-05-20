#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE="-f $REPO_ROOT/api/docker-compose.yml -f $REPO_ROOT/apps/mobile/docker-compose.yml"

teardown() {
  echo "--- tearing down stack ---"
  docker compose $COMPOSE down --volumes
  pkill -f "appium --port 4723" || true
}
trap teardown EXIT

echo "--- building images ---"
docker build -t ssv-api:latest -f api/Dockerfile api

echo "--- building APK ---"
(cd apps/mobile && pnpm install --frozen-lockfile && pnpm build && pnpm exec cap sync android)
docker run --rm \
  -v "$REPO_ROOT:/repo" \
  -v "${ANDROID_HOME}:/android-sdk:ro" \
  -e ANDROID_HOME=/android-sdk \
  -e GRADLE_USER_HOME=/repo/.gradle-docker \
  -e HOME=/tmp \
  --user "$(id -u):$(id -g)" \
  -w /repo/apps/mobile/android \
  eclipse-temurin:21-jdk-jammy \
  sh -c "chmod +x gradlew && ./gradlew assembleDebug --no-daemon"
cp apps/mobile/android/app/build/outputs/apk/debug/app-debug.apk apps/mobile/app-debug.apk
docker build -t ssv-mobile:latest -f apps/mobile/Dockerfile apps/mobile

echo "--- starting stack ---"
API_IMAGE=ssv-api:latest \
MOBILE_IMAGE=ssv-mobile:latest \
docker compose $COMPOSE up -d

echo "--- connecting ADB and waiting for emulator boot ---"
adb connect localhost:5555
adb wait-for-device
echo "waiting for emulator to fully boot..."
until adb shell getprop sys.boot_completed 2>/dev/null | grep -q "1"; do
  sleep 5
done
echo "emulator ready"

echo "--- starting Appium ---"
appium --port 4723 &
sleep 3

echo "--- running Appium tests ---"
cd apps/mobile
pnpm test:e2e
