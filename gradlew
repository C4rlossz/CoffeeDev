#!/usr/bin/env sh
# Transparent Gradle bootstrap. Downloads only the pinned official distribution.
set -eu
VERSION=8.11.1
SHA256=f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6
CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/coffeedev-bootstrap"
GRADLE="$CACHE/gradle-$VERSION/bin/gradle"
if [ ! -x "$GRADLE" ]; then
  mkdir -p "$CACHE"
  ARCHIVE="$CACHE/gradle-$VERSION-bin.zip"
  curl --fail --location --retry 2 "https://downloads.gradle.org/distributions/gradle-$VERSION-bin.zip" --output "$ARCHIVE"
  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL=$(sha256sum "$ARCHIVE" | cut -d ' ' -f 1)
  else
    ACTUAL=$(shasum -a 256 "$ARCHIVE" | cut -d ' ' -f 1)
  fi
  [ "$ACTUAL" = "$SHA256" ] || { echo "Gradle checksum mismatch" >&2; exit 1; }
  unzip -q -o "$ARCHIVE" -d "$CACHE"
fi
exec "$GRADLE" "$@"
