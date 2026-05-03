#!/bin/bash
echo "==================================================="
echo "             DFA Simulator - Auto Launcher"
echo "==================================================="

TOOLS_DIR="$PWD/.tools"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz"
# Sử dụng JDK bản Linux
JDK_URL="https://aka.ms/download-jdk/microsoft-jdk-21.0.2-linux-x64.tar.gz"

# 1. Kiểm tra Java
if ! command -v java &> /dev/null; then
    if [ ! -d "$TOOLS_DIR/jdk" ]; then
        read -p "[INFO] Java is missing. Do you want to download a portable JDK? (Y/n): " confirm
        if [[ "$confirm" != [yY]* && -n "$confirm" ]]; then exit 1; fi

        echo "[INFO] Downloading Portable JDK 21..."
        mkdir -p "$TOOLS_DIR/jdk_temp"
        curl -L "$JDK_URL" -o "$TOOLS_DIR/jdk.tar.gz"
        echo "[INFO] Extracting JDK..."
        tar -xzf "$TOOLS_DIR/jdk.tar.gz" -C "$TOOLS_DIR/jdk_temp" --strip-components=1
        mv "$TOOLS_DIR/jdk_temp" "$TOOLS_DIR/jdk"
        rm "$TOOLS_DIR/jdk.tar.gz"
    fi
    export JAVA_HOME="$TOOLS_DIR/jdk"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# 2. Kiểm tra Maven
if ! command -v mvn &> /dev/null; then
    if [ ! -d "$TOOLS_DIR/mvn" ]; then
        read -p "[INFO] Maven is missing. Do you want to download portable Maven? (Y/n): " confirm
        if [[ "$confirm" != [yY]* && -n "$confirm" ]]; then exit 1; fi

        echo "[INFO] Downloading Apache Maven..."
        mkdir -p "$TOOLS_DIR/mvn_temp"
        curl -L "$MAVEN_URL" -o "$TOOLS_DIR/mvn.tar.gz"
        echo "[INFO] Extracting Maven..."
        tar -xzf "$TOOLS_DIR/mvn.tar.gz" -C "$TOOLS_DIR/mvn_temp" --strip-components=1
        mv "$TOOLS_DIR/mvn_temp" "$TOOLS_DIR/mvn"
        rm "$TOOLS_DIR/mvn.tar.gz"
    fi
    export MAVEN_HOME="$TOOLS_DIR/mvn"
    export PATH="$MAVEN_HOME/bin:$PATH"
fi

# 3. Ẩn các warning rác của Java
export JAVA_TOOL_OPTIONS="-XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED"

# 4. Chạy app
echo "[INFO] Setting up dependencies and launching App. Please wait..."
mvn clean javafx:run -q