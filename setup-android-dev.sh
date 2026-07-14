#!/usr/bin/env bash

set -e

echo "=================================="
echo " Android + Web Dev Environment"
echo " Google Cloud Shell Setup"
echo "=================================="

sudo apt update

sudo apt install -y \
    git \
    curl \
    wget \
    unzip \
    zip \
    jq \
    tree \
    ripgrep \
    fd-find \
    tmux \
    build-essential \
    python3 \
    python3-pip \
    openjdk-21-jdk

#############################################
# NodeJS LTS
#############################################

echo
echo "Installing Node.js..."

curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -

sudo apt install -y nodejs

#############################################
# Gradle
#############################################

echo
echo "Installing Gradle..."

sudo apt install -y gradle

############################################
# Android CLI
############################################

echo
echo "Installing Android CLI..."

mkdir -p ~/.local/bin

# Replace with latest Linux installer URL from Android Developers.
curl -fsSL https://dl.google.com/android/cli/latest/linux_x86_64/install.sh | bash

echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
export PATH="$HOME/.local/bin:$PATH"

############################################
# Initialize Android CLI
############################################

android init

############################################
# Install SDK Packages
############################################

android sdk install \
    platform-tools \
    build-tools/36.0.0 \
    platforms/android-36

#############################################
# Verify
#############################################

echo
echo "Versions"
echo "--------------------------------"

java -version
node -v
npm -v
gradle -v | head -n 3
adb version

echo
echo "Done!"
echo
echo "Run:"
echo
echo "source ~/.bashrc"