@echo off
setlocal enabledelayedexpansion
title DFA Simulator - Builder

echo ===================================================
echo             DFA Simulator - Fat JAR Builder
echo ===================================================

set "TOOLS_DIR=%CD%\.tools"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
set "JDK_URL=https://aka.ms/download-jdk/microsoft-jdk-21.0.2-windows-x64.zip"

:: 1. Kiểm tra Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if not exist "%TOOLS_DIR%\jdk" (
        echo [INFO] Downloading Portable JDK 21...
        if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.Headers.Add('User-Agent', 'Mozilla/5.0'); $wc.DownloadFile('%JDK_URL%', '%TOOLS_DIR%\jdk.zip')"
        echo [INFO] Extracting JDK...
        powershell -Command "Expand-Archive -Path '%TOOLS_DIR%\jdk.zip' -DestinationPath '%TOOLS_DIR%\jdk_temp' -Force"
        for /d %%I in ("%TOOLS_DIR%\jdk_temp\*") do move "%%I" "%TOOLS_DIR%\jdk" >nul
        rmdir /s /q "%TOOLS_DIR%\jdk_temp"
        del "%TOOLS_DIR%\jdk.zip"
    )
    set "JAVA_HOME=%TOOLS_DIR%\jdk"
    set "PATH=%TOOLS_DIR%\jdk\bin;%PATH%"
)

:: 2. Kiểm tra Maven
call mvn -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if not exist "%TOOLS_DIR%\mvn" (
        echo [INFO] Downloading Apache Maven...
        if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.Headers.Add('User-Agent', 'Mozilla/5.0'); $wc.DownloadFile('%MAVEN_URL%', '%TOOLS_DIR%\mvn.zip')"
        echo [INFO] Extracting Maven...
        powershell -Command "Expand-Archive -Path '%TOOLS_DIR%\mvn.zip' -DestinationPath '%TOOLS_DIR%\mvn_temp' -Force"
        for /d %%I in ("%TOOLS_DIR%\mvn_temp\*") do move "%%I" "%TOOLS_DIR%\mvn" >nul
        rmdir /s /q "%TOOLS_DIR%\mvn_temp"
        del "%TOOLS_DIR%\mvn.zip"
    )
    set "MAVEN_HOME=%TOOLS_DIR%\mvn"
    set "PATH=%TOOLS_DIR%\mvn\bin;%PATH%"
)

:: 3. Chạy lệnh đóng gói (Thay vì lệnh chạy app)
echo [INFO] Building Fat JAR...
call mvn clean package

echo ===================================================
echo [SUCCESS] Build process completed. Check your 'target' folder.
echo ===================================================
pause
