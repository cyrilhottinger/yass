@echo off
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_45
set version=0.0.0
set /p version=Version [ MAJOR.MINOR.PATCH or 'enter' for %version% ]?:
cmd /c C:\development\gradle-2.4\bin\gradle.bat -Pversion=%version%
pause
