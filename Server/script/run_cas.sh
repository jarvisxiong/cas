#!/bin/bash
java -jar $1 -Dcom.sun.management.jmxremote\
 -Dcom.sun.management.jmxremote.local.only=false\
 -Dcom.sun.management.jmxremote.authenticate=false\
 -Dcom.sun.management.jmxremote.port=9004\
 -Dcom.sun.management.jmxremote.ssl=false\
 -Djava.rmi.server.hostname=$(hostname)

