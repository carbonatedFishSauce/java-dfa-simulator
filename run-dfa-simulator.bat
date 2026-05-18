@echo off
setlocal enabledelayedexpansion
title DFA Simulator Launcher

echo ===================================================
echo             DFA Simulator - Auto Launcher
echo ===================================================

set TOOLS_DIR=%CD%\.tools
set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
set JDK_URL=https://aka.ms/download-jdk/microsoft-jdk-21.0.2-windows-x64.zip

:: 1. Kiểm tra Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if not exist "%TOOLS_DIR%\jdk" (
        set /p user_input="[INFO] Java is missing. Do you want to download a portable JDK to run this app? (Y/N): "
        if /i "!user_input!" neq "Y" exit /b

        echo [INFO] Downloading Portable JDK 21...
        if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%JDK_URL%' -OutFile '%TOOLS_DIR%\jdk.zip' -UserAgent 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'"
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
        set /p user_input="[INFO] Maven is missing. Do you want to download portable Maven? (Y/N): "
        if /i "!user_input!" neq "Y" exit /b

        echo [INFO] Downloading Apache Maven...
        if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TOOLS_DIR%\mvn.zip' -UserAgent 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'"
        echo [INFO] Extracting Maven...
        powershell -Command "Expand-Archive -Path '%TOOLS_DIR%\mvn.zip' -DestinationPath '%TOOLS_DIR%\mvn_temp' -Force"
        for /d %%I in ("%TOOLS_DIR%\mvn_temp\*") do move "%%I" "%TOOLS_DIR%\mvn" >nul
        rmdir /s /q "%TOOLS_DIR%\mvn_temp"
        del "%TOOLS_DIR%\mvn.zip"
    )
    set "MAVEN_HOME=%TOOLS_DIR%\mvn"
    set "PATH=%TOOLS_DIR%\mvn\bin;%PATH%"
)

:: 3. Ẩn các warning rác của Java Modules (Unnamed Error)
set JAVA_TOOL_OPTIONS=-XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED

:: 4. Chạy ứng dụng (Quiet mode để ẩn log tải dependency của Maven)
echo [INFO] Setting up dependencies and launching App. Please wait...
call mvn clean javafx:run -q

pause
