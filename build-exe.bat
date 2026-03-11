@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: ==========================================
:: 1. 配置区（请根据你的项目实际情况修改）
:: ==========================================
set APP_NAME=PurePulseApp
set MAIN_CLASS=org.purepulse.PurePulseApp
set VERSION=1.0.0
set JAR_NAME=pure-pulse-app-1.0.0.jar
set ICON_PATH=%CD%\src\main\resources\icon\app-icon.ico
set MODULE_PATH=D:\java\jdk-21.0.4+7\jmods;target/libs
set ADD_MODULES=javafx.controls,javafx.fxml,java.base,java.desktop,java.management,jdk.unsupported,java.logging,java.sql
set JVM_OPTIONS=-Xms20m -Xmx60m

:: ==========================================
:: 2. 环境清理
:: ==========================================
echo [Step 1] Cleaning old artifacts...
if exist dist rmdir /s /q dist
if exist target rmdir /s /q target
:: 关键：清理 jpackage 可能残留在系统 Temp 里的临时文件，解决 311 错误
del /q /f /s "%TEMP%\jdk.jpackage*" >nul 2>&1

:: ==========================================
:: 3. 编译与准备依赖
:: ==========================================
echo [Step 2] Building project with Maven...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo Maven build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [Step 3] Copying dependencies...
call mvn dependency:copy-dependencies -DoutputDirectory=target/libs

:: ==========================================
:: 4. 执行打包 (针对 311 错误进行了优化)
:: ==========================================
echo [Step 4] Starting jpackage...
echo This might take a minute, please wait...

:: 注意：添加了 --about-url 等信息，并强制指定了一些 Windows 参数
jpackage ^
  --type exe ^
  --dest dist ^
  --name "%APP_NAME%" ^
  --app-version "%VERSION%" ^
  --input target/libs ^
  --input target ^
  --main-jar %JAR_NAME% ^
  --main-class %MAIN_CLASS% ^
  --module-path "%MODULE_PATH%" ^
  --add-modules %ADD_MODULES% ^
  --win-shortcut ^
  --win-menu ^
  --win-menu-group "%APP_NAME%" ^
  --win-dir-chooser ^
  --icon "%ICON_PATH%" ^
  --vendor "Aidan Studio" ^
  --copyright "Copyright 2026" ^
  --description "PurePulseApp" ^
  --java-options "%JVM_OPTIONS%" ^
  --verbose ^
  --temp temp_build

:: 注释：--temp temp_build 会在当前目录创建临时文件夹而非系统 Temp，有助于避开权限和编码问题

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] jpackage failed with exit code %ERRORLEVEL%.
    echo If you see 311, ensure .NET Framework 3.5 is installed and WiX v3.11 is in PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ==========================================
echo 打包成功！安装包位于: %CD%\dist
echo ==========================================
pause