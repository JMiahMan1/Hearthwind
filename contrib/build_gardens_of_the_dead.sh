#!/usr/bin/env bash
set -euo pipefail

# Build automation for Gardens of the Dead 26.2 port
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BUILD_TMP="${ROOT_DIR}/.tmp/contrib-build/gardens-of-the-dead"
PATCH_FILE="${SCRIPT_DIR}/gardens-of-the-dead/patches/26.2-port.patch"
TARGET_JAR="${ROOT_DIR}/conversion/vendored/gardens-of-the-dead-fabric-5.0.2+26.2.jar"

mkdir -p "$(dirname "${BUILD_TMP}")" "${ROOT_DIR}/conversion/vendored"

if [ ! -d "${BUILD_TMP}/.git" ]; then
    echo "== cloning ochotonida/gardens-of-the-dead =="
    git clone --branch 1.21.x https://github.com/ochotonida/gardens-of-the-dead.git "${BUILD_TMP}"
else
    echo "== resetting gardens-of-the-dead repo =="
    git -C "${BUILD_TMP}" fetch origin
    git -C "${BUILD_TMP}" reset --hard origin/1.21.x
    git -C "${BUILD_TMP}" clean -fd
fi

echo "== applying 26.2 port patch =="
git -C "${BUILD_TMP}" apply "${PATCH_FILE}"

echo "== building fabric jar =="
cd "${BUILD_TMP}"
./gradlew :fabric:build --no-daemon

BUILT_JAR="$(find "${BUILD_TMP}/fabric/build/libs" -name "gardens-of-the-dead-fabric-5.0.2.jar" | head -1)"
if [ -z "${BUILT_JAR}" ] || [ ! -f "${BUILT_JAR}" ]; then
    echo "ERROR: Built jar not found!"
    exit 1
fi

cp "${BUILT_JAR}" "${TARGET_JAR}"
echo "== successfully built and deployed ${TARGET_JAR} =="
