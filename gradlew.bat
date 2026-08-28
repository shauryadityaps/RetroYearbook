@rem
@rem Retro Yearbook Gradle Execution Script
@rem
@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

setlocal

if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Users\ACER\jdk-21"
)

if not defined ANDROID_HOME (
    set "ANDROID_HOME=C:\Users\ACER\AppData\Local\Android\Sdk"
)

set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%PATH%"

"C:\Users\ACER\gradle\gradle-8.11.1\bin\gradle.bat" %*

endlocal
