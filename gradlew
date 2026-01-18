#!/bin/bash
# Gradle wrapper bootstrap script
# Run: ./gradlew <task>

GRADLE_VERSION="8.5"
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="gradle/wrapper/gradle-wrapper.properties"

# Create wrapper properties if they don't exist
if [ ! -f "$WRAPPER_PROPERTIES" ]; then
    mkdir -p gradle/wrapper
    cat > "$WRAPPER_PROPERTIES" << EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
fi

# Download wrapper jar if it doesn't exist
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Gradle wrapper..."
    curl -sL -o "$WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}/gradle/wrapper/gradle-wrapper.jar"
fi

# Run Gradle
exec java -cp "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
