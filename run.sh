#!/usr/bin/env bash
# Build, install and launch Arc on a connected device or running emulator.
#
#   ./run.sh              build + install + launch
#   ./run.sh emulator     boot an emulator first (run in its own terminal)
#
# Honours JAVA_HOME and ANDROID_HOME if you already have them set; otherwise it
# looks in the usual places. Override the emulator with AVD=<name> ./run.sh emulator
set -e

# --- toolchain -------------------------------------------------------------
if [ -z "$JAVA_HOME" ]; then
    for candidate in \
        /opt/homebrew/opt/openjdk@17 \
        /usr/local/opt/openjdk@17 \
        /usr/lib/jvm/java-17-openjdk-amd64 \
        "$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
    do
        if [ -n "$candidate" ] && [ -x "$candidate/bin/java" ]; then
            export JAVA_HOME="$candidate"
            break
        fi
    done
fi
if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set and no JDK 17 was found. Install one, or set JAVA_HOME." >&2
    exit 1
fi

if [ -z "$ANDROID_HOME" ]; then
    for candidate in "$ANDROID_SDK_ROOT" "$HOME/Library/Android/sdk" "$HOME/Android/Sdk"; do
        if [ -n "$candidate" ] && [ -d "$candidate" ]; then
            export ANDROID_HOME="$candidate"
            break
        fi
    done
fi
if [ -z "$ANDROID_HOME" ]; then
    echo "ANDROID_HOME is not set and no Android SDK was found." >&2
    exit 1
fi
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

APP_ID=com.example.arc
ACTIVITY=com.example.arc.ui.StartActivity
APK=build/outputs/apk/debug/Arc-debug.apk

# --- emulator --------------------------------------------------------------
if [ "$1" = "emulator" ]; then
    NAME="${AVD:-$(emulator -list-avds | head -1)}"
    if [ -z "$NAME" ]; then
        echo "No AVD found. Create one first - see the README." >&2
        exit 1
    fi
    echo "Booting $NAME ..."
    exec emulator -avd "$NAME" -no-snapshot-save -no-boot-anim
fi

# --- build, install, launch ------------------------------------------------
./gradlew assembleDebug
adb wait-for-device
adb install -r "$APK"
adb shell am start -n "$APP_ID/$ACTIVITY"
echo "Launched. Logs:  adb logcat -s Arc:D"
