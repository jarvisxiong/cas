#!/bin/bash

platform='unknown'
unamestr=`uname` 
if [[ "$unamestr" == 'Linux' ]]; then
   platform='linux'
elif [[ "$unamestr" == 'Darwin' ]]; then
   platform='MacOs'
fi
#echo $platform

while getopts "t:a:w:l:f:" OPTION
do
    case ${OPTION} in
        t)  targetStrings=${OPTARG}
            ;;  
        a)  adGroupId=${OPTARG} 
            ;;  
        w)  workerName=${OPTARG}
            ;; 
        l)  logFilePath=${OPTARG}
            ;; 
        f)  fullSearch=${OPTARG}
            ;; 
    esac
done

logFilePath+=debug.log

# set default value of logFilePath, in case it's not provided on the command line:
#logFilePath=${logFilePath:-"/opt/mkhoj/logs/phoenix/phoenix.log"}

# Set default value of fullSearch to "on"
# "fullSearch" searches from the start-line of taskId to the last-line of taskId in the log,
# including all intermediate lines that don't have to contain the taskId.
# This is slightly more expensive than the usual grep (takes 0.2s more), hence is not the default.

fullSearch=${fullSearch:-"off"}

ADGROUP_DELIMITER="filter ComplianceClassFilter has been called"
WORKER_DELIMITER="Inside work method of MetricManager"
TEMP_FILE=/tmp/logParser.`date +%d_%m_%y.%H-%M-%S`.tmp
IFS=
found=

if [ ! -f ${logFilePath} ]
then
    echo "FATAL: Incorrect logFilePath: ${logFilePath}"
    exit 1
fi


# first clear any and all previous temp files
rm -rf /tmp/logParser*


#lastTask=`tail -1000 ${logFilePath} | grep -Po "(?<=debug - messageReceived - \[)[A-Fa-f0-9\-]*(?=\].*)" | tail -1`
#lastTask=`tail -1000 ${logFilePath} | grep -Po "(?>[0-9a-zA-Z- ]* c.i.a.c.s.r.ThriftRequestParser: Inside parameter parser)" | sed  's/] c.i.a.c.s.r.ThriftRequestParser: Inside parameter parser//g' | tail -1`
if [[ $platform == 'linux' ]]
then 
    lastTask=`tail -20000 ${logFilePath} | grep -Po "(?>[0-9a-zA-Z\]]* c.i.a.c.s.r.ThriftRequestParser: Inside parameter parser)" | sed  's/] c.i.a.c.s.r.ThriftRequestParser: Inside parameter parser//g' | tail -1`
elif [[ $platform == 'MacOs' ]]
then
     lastTask=`tail -20000 ${logFilePath} | grep -e "[0-9a-zA-Z]*c.i.a.c.s.r.ThriftRequestParser: Inside parameter parser" | awk '{print $4}' | sed   's/\]//g' | sed 's/\[//g' | tail -1`
fi

#echo " lastTaskId is: ${lastTask}"
#echo " lastTaskId is: ${lastTask}" > /tmp/test1 #dhruba touch this
#echo " adGroupId is: $adGroupId"
#echo " workerName is: $workerName"
#echo " TEMP_FILE is: ${TEMP_FILE}"
#echo " logFilePath is: ${logFilePath}"
#echo " fullSearch is: ${fullSearch}"


# Function to get "all" log lines between the start of the lastTask and the end of the lastTask.
# Even lines not containing the lastTask will be included in ${TEMP_FILE}
getAllLogLines()
{
    totalLines=`wc -l ${logFilePath} | cut -d\  -f1`
    echo "totalLines : ${totalLines}"
    lastTacLine=`tac ${logFilePath} | grep -m1 -n -- ${lastTask} | cut -d\: -f1`
    echo "lastTacLine: ${lastTacLine}"
    let lastLineNumber=${totalLines}-${lastTacLine}
    echo "lastLineNumber: ${lastLineNumber}"
    firstLineNumber=`grep -m1 -n -- ${lastTask} ${logFilePath} | cut -d\: -f1`
    echo "firstLineNumber: ${firstLineNumber}"
    sed -n "$firstLineNumber,$lastLineNumber{p}" ${logFilePath} > ${TEMP_FILE}
    chmod 777 $TEMP_FILE
}

# get the value of the Input File Separator (IFS):
if [[ "${targetStrings}" == *@* ]]
then
    IFS='@'
elif [[ "${targetStrings}" == *\|* ]]
then
    IFS='|'
elif [[ "${targetStrings}" == *^* ]]
then    
    IFS='^'
fi

targetStringArray=(${targetStrings})



# first save the lines containing lastTask in a temp file
# so that we don't have to redo this work again and again

if [[ "${fullSearch}" == "on" ]]
then
    getAllLogLines    
else 
    grep -- ${lastTask} ${logFilePath} > ${TEMP_FILE}
fi


if [ $? -ne 0 ] 
then
    echo "FATAL: Could not grep lastTaskID from log or create temp file!!!"
    exit 1
fi

for key in "${!targetStringArray[@]}"
do
    #echo -e "key is $key"
    #echo -e "Current string to search is: ${targetStringArray[${key}]}"
    if [[ "$adGroupId" == "" && "$workerName" == "" ]]
    then
        if [[ "$IFS" == '^' && $key -lt ${#targetStringArray[@]}-1 ]] 
        then
            #echo $key ${targetStringArray[${key}]} ${targetStringArray[${key}+1]}
            grep -F -A1 ${targetStringArray[${key}]} ${TEMP_FILE} | grep -qF ${targetStringArray[${key}+1]}
            str=`grep -F -A1 ${targetStringArray[${key}]} ${TEMP_FILE} | grep -F ${targetStringArray[${key}+1]}`
	    #echo $str
	    found=$?
        else
            grep -qF "${targetStringArray[${key}]}" ${TEMP_FILE}
	    str=`grep -F "${targetStringArray[${key}]}" ${TEMP_FILE}`
            #echo $str
	    found=$?
        fi
    elif [ "$workerName" == "" ]
    then
        sed -rn "/AdGroup : $adGroupId/,/AdGroup : |${ADGROUP_DELIMITER} /p" ${TEMP_FILE} | grep -qF ${targetStringArray[${key}]}
        found=$?
    else
        sed -rn "/Worker - $workerName/,/Worker - |${WORKER_DELIMITER}/p" ${TEMP_FILE} | grep -qF ${targetStringArray[${key}]}
        found=$?
    fi

    if [[ $found -ne 0 && ( "$IFS" == '@' || "$IFS" == '^' ) ]]  # break immediately if !found && operation==AND or operation=SUBSEQUENT_AND
    then
        found=1; break
    fi

    if [[ $found -eq 0 && "$IFS" == '|' ]] # break immediately if found && operation==OR
    then
        found=0; break
    fi
    
    #echo "str is ${str}"
    #delimitor="|"
    str=${str}
done

echo ${str}
