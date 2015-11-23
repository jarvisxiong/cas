#!/bin/bash

exec 2>&1

MUSTACHE_UTIL=/opt/inmobi/commons/mustache-util/render-template
DATA_FILE=${CAS_BASE_DIRECTORY}/.build/data/cas_prod_settings.yaml

if [ "$IDP_ENVIRONMENT" ];
then
    echo "Detected environment: $IDP_ENVIRONMENT";
else
    echo "Environment variable IDP_ENVIRONMENT was not set. Exiting; Please rerun the docker run command with -e IDP_ENVIRONMENT=<Suitable Value>.";
    exit -1;
fi;

if [ "$COLO" ];
then
    echo "Detected colo: $COLO";
else
    echo "Environment variable COLO was not set. Exiting; Please rerun the docker run command with -e COLO=<Suitable Value>.";
    exit -1;
fi;

YAML_DATA_SECTION=$IDP_ENVIRONMENT.$COLO
echo "Generating configs using data file: ${DATA_FILE} and yaml data section: ${YAML_DATA_SECTION}"

# Turning on globstar. This will enable no directory and recursive directory matching for **
shopt -s globstar

# For more info regarding config generation, refer to https://github.corp.inmobi.com/platform-pe/mustache-util
for FILE in ${CAS_BASE_DIRECTORY}/.build/templates/**/*.mustache
do
    echo "Merging file: $FILE";

    # Generating the output file name by removing '.build/templates/' from the file name
    TEMP=${FILE/'.build/templates/'}
    OUTPUT_FILE=${TEMP/'.mustache'}

    # Creating output directories
    mkdir -p `dirname $OUTPUT_FILE`

    # Merging the templates
    $MUSTACHE_UTIL --template $FILE --data $DATA_FILE --yaml_data_section $YAML_DATA_SECTION --output $OUTPUT_FILE --addenvvars=True
done

# Fixing permission for automation and scribe-emitter-stats
chmod 777 ${CAS_BASE_DIRECTORY}/scripts/grepLog.sh
chmod +x ${CAS_BASE_DIRECTORY}/bin/scribe-emitter/cas-emit-scribe-agent-stats

echo "Starting CAS"
. ${CAS_BASE_DIRECTORY}/bin/run_cas.bash
