#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
: "${SDK_ROOT:?Set ANDROID_SDK_ROOT to your Android SDK directory}"
BUILD_TOOLS="${BUILD_TOOLS_VERSION:-36.0.0}"
TOOLS="$SDK_ROOT/build-tools/$BUILD_TOOLS"
PLATFORM="$SDK_ROOT/platforms/android-36/android.jar"
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
rm -rf build/classes build/dex build/generated
mkdir -p build/classes build/dex build/generated dist .local
"$TOOLS/aapt2" compile --dir app/src/main/res -o build/resources.zip
"$TOOLS/aapt2" link -o build/unsigned.apk --manifest app/src/main/AndroidManifest.xml -I "$PLATFORM" --min-sdk-version 30 --target-sdk-version 36 --version-code 16 --version-name 0.8.2 --java build/generated build/resources.zip
javac -encoding UTF-8 -source 8 -target 8 -bootclasspath "$PLATFORM:$TOOLS/core-lambda-stubs.jar" -d build/classes app/src/main/java/top/suzuki/sukawallet/*.java build/generated/top/suzuki/sukawallet/R.java
jar cf build/classes.jar -C build/classes .
"$TOOLS/d8" --lib "$PLATFORM" --min-api 30 --output build/dex build/classes.jar
cp build/unsigned.apk build/with-dex.apk
(cd build/dex && zip -q ../with-dex.apk classes.dex)
"$TOOLS/zipalign" -f -p 4 build/with-dex.apk build/aligned.apk
KEYSTORE="${MINICARD_KEYSTORE:-.local/development.p12}"
KEY_ALIAS="${MINICARD_KEY_ALIAS:-minicard}"
export MINICARD_STORE_PASSWORD="${MINICARD_STORE_PASSWORD:-android}"
if [[ ! -f "$KEYSTORE" ]]; then
    if [[ "$KEYSTORE" != .local/development.p12 ]]; then echo 'Specified signing key is missing.' >&2; exit 1; fi
    keytool -genkeypair -keystore "$KEYSTORE" -storepass:env MINICARD_STORE_PASSWORD -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=MiniCard Local Development' >/dev/null 2>&1
fi
"$TOOLS/apksigner" sign --ks "$KEYSTORE" --ks-key-alias "$KEY_ALIAS" --ks-pass env:MINICARD_STORE_PASSWORD --out dist/SukaWallet-0.8.2.apk build/aligned.apk
"$TOOLS/apksigner" verify dist/SukaWallet-0.8.2.apk
printf 'Built: %s/dist/SukaWallet-0.8.2.apk\n' "$PWD"
