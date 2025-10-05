@echo off
echo Limpando projeto...
rmdir /s /q target 2>nul

echo Compilando projeto...
mvn clean compile

echo Pronto! Agora execute o projeto pelo VS Code.
pause
