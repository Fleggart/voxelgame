#!/bin/sh

APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_BASE_NAME=$(basename "$0")
DEFAULT_JVM_OPTS="-Xmx512m -Xms128m"

# 检查 Java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo "ERROR: Java not found"
    exit 1
fi

# 直接执行
exec "$JAVACMD" $DEFAULT_JVM_OPTS \
    -Dorg.gradle.appname="$APP_BASE_NAME" \
    -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    "$@"
