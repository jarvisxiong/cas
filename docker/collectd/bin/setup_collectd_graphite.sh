#!/bin/bash
set -e

TARGET_FILE=/opt/inmobi/ops/collectd/etc/collectd.d/collectd_graphite.conf
TEMPLATE_DIR=/opt/inmobi/usr/collectd-setup/

if [ ! "$CONTAINER_NAME" ];
then
    echo "Environment variable CONTAINER_NAME was not set. Please rerun the docker run command with -e CONTAINER_NAME=Suitable_Value";
    exit -1;
fi;

export CONCISE_CONTAINER_NAME=`echo ${CONTAINER_NAME} | cut -f1 -d "."`

/opt/inmobi/commons/mustache-util/render-template --addenvvars=True --template $TEMPLATE_DIR/collectd_graphite.conf.mustache --output $TARGET_FILE
