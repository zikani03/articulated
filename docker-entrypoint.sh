#!/bin/sh
/quickprosener &
java -Dlogging.fileName=/logs/api.log -Dserver.host=0.0.0.0 -jar /articulated.jar
