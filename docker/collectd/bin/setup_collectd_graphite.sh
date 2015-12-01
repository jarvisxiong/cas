#!/bin/bash
set -e

MUSTACHE_UTIL=/opt/inmobi/commons/mustache-util/render-template
SOURCE_TEMPLATE=/opt/inmobi/usr/collectd-setup/collectd_graphite.conf.mustache
TARGET_FILE=/opt/inmobi/ops/collectd/etc/collectd.d/collectd_graphite.conf
DATA_FILE=${CAS_BASE_DIRECTORY}/.build/data/cas_prod_settings.yaml

if [ ! "$IDP_NODE" ];
then
    echo "Environment variable IDP_NODE was not set. Please rerun the docker run command with -e IDP_NODE=Suitable_Value";
    exit -1;
fi;

if [ "$IDP_ENVIRONMENT" ];
then
    echo "Detected environment: $IDP_ENVIRONMENT";
else
    echo "Environment variable IDP_ENVIRONMENT was not set. Exiting; Please rerun the docker run command with -e IDP_ENVIRONMENT=<Suitable Value>.";
    exit -1;
fi;

if [ "$IDP_CLUSTER" ];
then
    echo "Detected colo: $IDP_CLUSTER";
else
    echo "Environment variable IDP_CLUSTER was not set. Exiting; Please rerun the docker run command with -e IDP_CLUSTER=<Suitable Value>.";
    exit -1;
fi;

YAML_DATA_SECTION=$IDP_ENVIRONMENT.$IDP_CLUSTER
export CONCISE_CONTAINER_NAME=`echo ${IDP_NODE} | cut -f1 -d "."`

$MUSTACHE_UTIL --addenvvars=True --template $SOURCE_TEMPLATE --data $DATA_FILE --yaml_data_section $YAML_DATA_SECTION --output $TARGET_FILE
