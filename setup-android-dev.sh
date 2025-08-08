#!/bin/bash

set -e

ANDROID_SDK_ROOT="$HOME/Android/Sdk"
ANDROID_CMDLINE_ZIP="commandlinetools-linux-11076708_latest.zip"
ANDROID_STUDIO_URL="https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2022.3.1.21/android-studio-2022.3.1.21-linux.tar.gz"

echo "▶ Updating packages..."
sudo apt update && sudo apt upgrade -y

echo "▶ Installing dependencies..."
sudo apt install -y wget unzip git curl zip zsh lib32stdc++6 lib32z1 libc6-i386 lib32gcc-s1 libgl1-mesa-dev libx11-dev software-properties-common

echo "▶ Installing latest default OpenJDK..."
sudo apt install -y default-jdk
JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
export JAVA_HOME=$JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

echo "▶ Creating Android SDK directory..."
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
cd "$ANDROID_SDK_ROOT"

echo "▶ Downloading Android SDK command line tools..."
wget -q https://dl.google.com/android/repository/${ANDROID_CMDLINE_ZIP} -O cmdline.zip
unzip -q cmdline.zip -d cmdline-temp
mv cmdline-temp/cmdline-tools cmdline-tools/latest
rm -rf cmdline.zip cmdline-temp

echo "▶ Setting up environment variables..."
{
  echo "export ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
  echo "export PATH=\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$PATH"
  echo "export PATH=\$ANDROID_SDK_ROOT/platform-tools:\$PATH"
} >> ~/.bashrc
source ~/.bashrc

echo "▶ Accepting licenses and installing SDK packages..."
yes | $ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager --licenses

$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0" \
  "emulator" \
  "cmdline-tools;latest"

read -p "Do you want to install Android Studio? (y/n): " INSTALL_STUDIO
if [[ "$INSTALL_STUDIO" == "y" ]]; then
  echo "▶ Downloading Android Studio..."
  cd /tmp
  wget -q "$ANDROID_STUDIO_URL" -O studio.tar.gz
  tar -xf studio.tar.gz
  sudo mv android-studio /opt/
  sudo ln -sf /opt/android-studio/bin/studio.sh /usr/local/bin/android-studio
  echo "✅ Run Android Studio using: android-studio"
fi

source ~/.bashrc

echo "✅ Android development environment setup complete."
echo "📝 Please restart your terminal or run: source ~/.bashrc"
