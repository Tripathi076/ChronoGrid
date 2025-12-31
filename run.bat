@echo off
cd /d "%~dp0"
echo Compiling ChronoGrid...
cd src
del /q sources.txt 2>nul
for /R %%f in (*.java) do @echo %%f >> sources.txt
javac -d ../out --module-path "../javafx-sdk-21.0.4/lib" --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.swing,javafx.web @sources.txt
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
del sources.txt
cd ..
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Starting ChronoGrid...
java --module-path "javafx-sdk-21.0.4\lib" --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.swing,javafx.web -cp out game.Main
exit