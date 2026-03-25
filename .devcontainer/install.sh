#!/usr/bin/env bash
set -e

# Ensure ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
  echo "ANDROID_HOME is not set"
  exit 1
fi

SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"

# Verify sdkmanager exists
if [ ! -f "$SDKMANAGER" ]; then
  echo "sdkmanager not found at $SDKMANAGER"
  exit 1
fi

# Accept all SDK licenses
yes | "$SDKMANAGER" --licenses