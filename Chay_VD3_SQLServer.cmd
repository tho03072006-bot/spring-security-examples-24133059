@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run.ps1" -Example 3 -Profile sqlserver
pause
