#!/bin/sh

exec 2>&1

export CLASSPATH=`ls /usr/lib/hadoop/*jar /usr/lib/hadoop/lib/*jar | tr "\n" :`;
DAEMON=/usr/bin/scribed
CONF=/opt/inmobi/cas/conf/scribe/scribe.conf
SLEEP=`perl -e "print int(rand(4))"`

echo "Starting in $SLEEP seconds..."
sleep $SLEEP

if [ ! -f $CONF ]; then
        echo "$CONF is missing..."
        exit 1
fi

exec setuidgid nobody ${DAEMON} -c ${CONF}
