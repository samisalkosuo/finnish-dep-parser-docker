#!/bin/bash

#start finnish parser server


#slf4j_log_level as env variable, if not then it is "info"
if [[ "${slf4j_log_level}" == "" ]] ; then
    LOG_LEVEL=info
  else
    LOG_LEVEL=${slf4j_log_level}
fi

#Jetty default log level is info. Jetty debug-level prints out a lot of debug messages.
#set jetty log level to match given log level, if given log level is higher than 'info'.
JETTY_LOG_LEVEL=info
if [[ "${slf4j_log_level}" == "warn" ]] ; then
    JETTY_LOG_LEVEL=warn
fi
if [[ "${slf4j_log_level}" == "error" ]] ; then
    JETTY_LOG_LEVEL=error
fi
if [[ "${slf4j_log_level}" == "off" ]] ; then
    JETTY_LOG_LEVEL=off
fi

MAX_HEAP_SIZE=2g


#system properties for SLF4J simple logging
#see: https://www.slf4j.org/apidocs/org/slf4j/impl/SimpleLogger.html

java -Xmx${MAX_HEAP_SIZE} \
-Dorg.slf4j.simpleLogger.defaultLogLevel=${LOG_LEVEL} \
-Dorg.slf4j.simpleLogger.showDateTime=true \
-Dorg.slf4j.simpleLogger.dateTimeFormat="yyyy-MM-dd HH.mm.ss.SSS" \
-Dorg.slf4j.simpleLogger.levelInBrackets=true \
-Dorg.slf4j.simpleLogger.log.org.eclipse.jetty=${JETTY_LOG_LEVEL} \
-jar fin-dep-parser-server-jar-with-dependencies.jar
