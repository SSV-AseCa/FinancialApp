# Build context is apps/ (so the in-image build can reach ui-core), mirroring
# apps/web/Dockerfile. Stage 1 builds the debug APK from source on demand —
# ui-core -> mobile web bundle -> cap sync -> gradle assembleDebug — so no
# pre-built app-debug.apk needs to exist in the context.

# ── build stage: compile the debug APK from source ────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS build

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$PATH:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools

# Node 24 + pnpm 9, plus tooling needed to fetch the Android SDK.
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl unzip ca-certificates \
  && curl -fsSL https://deb.nodesource.com/setup_24.x | bash - \
  && apt-get install -y --no-install-recommends nodejs \
  && rm -rf /var/lib/apt/lists/* \
  && corepack enable && corepack prepare pnpm@9 --activate

# Android SDK: cmdline-tools + platform/build-tools matching android/variables.gradle
# (compileSdk 35, build-tools 35.0.0). Gradle itself comes from the project wrapper.
RUN mkdir -p $ANDROID_HOME/cmdline-tools \
  && curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/cmdline-tools.zip \
  && unzip -q /tmp/cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools \
  && mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest \
  && rm /tmp/cmdline-tools.zip \
  && yes | sdkmanager --licenses > /dev/null \
  && sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" > /dev/null

WORKDIR /app

# Workspace manifests — install deps for mobile and its workspace deps (ui-core).
COPY pnpm-workspace.yaml package.json pnpm-lock.yaml ./
COPY ui-core/package.json ui-core/
COPY mobile/package.json mobile/
RUN pnpm install --frozen-lockfile --filter ssv-mobile...

# Sources
COPY ui-core ui-core
COPY mobile mobile

ARG VITE_AUTH0_DOMAIN
ARG VITE_AUTH0_CLIENT_ID
ARG VITE_AUTH0_AUDIENCE
ARG VITE_API_BASE_URL
ARG VITE_USE_MOCK_AUTH=false

ENV VITE_AUTH0_DOMAIN=$VITE_AUTH0_DOMAIN \
  VITE_AUTH0_CLIENT_ID=$VITE_AUTH0_CLIENT_ID \
  VITE_AUTH0_AUDIENCE=$VITE_AUTH0_AUDIENCE \
  VITE_API_BASE_URL=$VITE_API_BASE_URL \
  VITE_USE_MOCK_AUTH=$VITE_USE_MOCK_AUTH

# Build ui-core first so dist/index.js and dist/index.d.ts exist, then the mobile
# web bundle, copy it into the native project, and assemble the debug APK.
RUN pnpm --filter @ssv/ui-core build
RUN pnpm --filter ssv-mobile build
RUN pnpm --filter ssv-mobile exec cap sync android
RUN cd mobile/android && ./gradlew assembleDebug --no-daemon

# ── runtime stage: Android emulator image with the freshly built APK ──────────
FROM budtmo/docker-android:emulator_13.0

ENV EMULATOR_DEVICE="Samsung Galaxy S10"
ENV WEB_VNC=false

COPY --from=build /app/mobile/android/app/build/outputs/apk/debug/app-debug.apk /root/tmp/
