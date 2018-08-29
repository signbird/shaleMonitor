@echo off
title Sync Contact To Dingding
set java_opts="-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=12345"
setlocal enableextensions enabledelayedexpansion
rem set CLASSPATH=%cd%\..\conf
for /r ./lib %%c in (*.jar) do @set CLASSPATH=!CLASSPATH!;%%c
rem echo %CLASSPATH%
java %java_opts% com.fasthink.shalemonitor.Bootstrap