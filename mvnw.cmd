@ECHO OFF
SETLOCAL

SET BASE_DIR=%~dp0
SET WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
SET JAR=%WRAPPER_DIR%\maven-wrapper.jar
SET PROPS=%WRAPPER_DIR%\maven-wrapper.properties

IF NOT EXIST "%PROPS%" (
  ECHO Missing %PROPS%
  EXIT /B 1
)

IF NOT EXIST "%JAR%" (
  ECHO Missing %JAR%. Please run bootstrap to download it.
  EXIT /B 1
)

java -jar "%JAR%" %*

ENDLOCAL

