#!/usr/bin/env bash
# Compila el APK de KEVANIX localmente.
# Requisitos: JDK 17+ y ANDROID_HOME/ANDROID_SDK_ROOT apuntando al Android SDK.
set -e
cd "$(dirname "$0")"
chmod +x ./gradlew
echo "==> Compilando APK debug..."
./gradlew assembleDebug --no-daemon
echo ""
echo "✅ Listo. APK generado en:"
echo "   app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "Para una versión de producción (sin firmar): ./gradlew assembleRelease"
