#!/bin/bash

#start finnish parser server
#and set java system props from env variables

#system properties for SLF4J simple logging
#see: https://www.slf4j.org/apidocs/org/slf4j/impl/SimpleLogger.html

#slf4j_log_level as env variable, if not then it is "info"
if [[ "${slf4j_log_level}" == "" ]] ; then
    LOG_LEVEL=info
  else
    LOG_LEVEL=${slf4j_log_level}
fi

MAX_HEAP_SIZE=2g

java -Xmx${MAX_HEAP_SIZE} \
-Dorg.slf4j.simpleLogger.defaultLogLevel=${LOG_LEVEL} \
-Dorg.slf4j.simpleLogger.showDateTime=true \
-Dorg.slf4j.simpleLogger.dateTimeFormat="yyyy-MM-dd hh.mm.ss.SSS" \
-Dorg.slf4j.simpleLogger.levelInBrackets=true \
-Dorg.slf4j.simpleLogger.log.org.eclipse.jetty=info \
-jar fin-dep-parser-server-jar-with-dependencies.jar
