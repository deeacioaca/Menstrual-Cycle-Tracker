#!/usr/bin/env bash
# Build, install and launch the app on a running emulator.
# Usage: ./run.sh            build + install + launch
#        ./run.sh emulator   boot the emulator first (separate terminal)
set -e

export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

APP_ID=com.example.arc
ACTIVITY=com.example.arc.ui.StartActivity
APK=build/outputs/apk/debug/Arc-debug.apk

if [ "$1" = "emulator" ]; then
    exec emulator -avd tema_dam -no-snapshot-save -no-boot-anim
fi

./gradlew assembleDebug
adb wait-for-device
adb install -r "$APK"
adb shell am start -n "$APP_ID/$ACTIVITY"
echo "Launched. Logs:  adb logcat -s Arc:D"
