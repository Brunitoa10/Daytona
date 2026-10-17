#!/bin/sh
cd "$(dirname "$0")"
JAR=daytona.jar; [ -f "$JAR" ] || JAR=dist/daytona.jar
JAVA=java; [ -x jre/bin/java ] && JAVA=jre/bin/java
exec "$JAVA" -Xms256m -Xmx768m -jar "$JAR"
