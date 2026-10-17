#!/bin/sh
#
# Gradle start up script for POSIX generated for DRAPE Android project.
#
APP_HOME=$( cd "${0%/*}" && pwd -P ) || exit
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Gradle wrapper JAR not found at $WRAPPER_JAR." >&2
    echo "In CI/GitHub Actions, use gradle/actions/setup-gradle@v4 or run 'gradle wrapper'." >&2
    exit 1
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$WRAPPER_JAR" \
  org.gradle.wrapper.GradleWrapperMain "$@"
