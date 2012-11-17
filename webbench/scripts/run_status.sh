#!/bin/bash
#
# collect & generate system information report.
#
# usage: run_status.sh -d <duration of test in seconds> -t <number of threads -l <port> -u <db user> '
# Example: run_status.sh -d 3600 -u root -l 6431
#
# @pre:
# @post:
# output report files.
#
# @AUTHOR "LI WEIZHAO"
# @CONTACT "rickylee86#gmail.com"
# @DATE "2012-11-17"
#


#relative directory of KSAR
export KSAR_DIR=./scripts/ksar

DB_TYPE="mysql"
DB_SERVER_PORT=3306
DB_HOST="127.0.0.1"
DB_PORT=${DB_SERVER_PORT}
DB_USER=root
DB_PASSWORD=""
MYSQL_HOME="/usr"

CUR_DIR=`pwd`

# check export variables
check_export_variables() 
{
	#check KSAR_DIR
	if [ -z ${KSAR_DIR} ]; then
		echo "Error: KSAR_DIR has not been exported!"
		exit
	fi
	#check abs_top_src_dir
	if [ -z ${abs_top_srcdir} ]; then
		echo "Warning: abs_top_srcdir is not set explicityly, will be set to pwd: `pwd`."
		export abs_top_srcdir=`pwd`
	fi
}
# check if required variables exported
check_export_variables

DBDIR=mysql

DIR=`dirname $0`

EXIT=0

trap 'echo "Test was interrupted by Control-C."; \
	killall sar; killall sadc; killall vmstat; killall iostat; ${DB_COMMAND}; killall statistician.py; EXIT=1' INT
trap 'echo "Test was interrupted. Got TERM signal."; \
	killall sar; killall sadc; killall vmstat; killall iostat; ${DB_COMMAND} ; killall statistician.py; EXIT=1' TERM

do_sleep()
{
	echo "Sleeping $1 seconds"
	count=$1
	while [[ ${EXIT} == 0 && ${count} > 0 ]];do
		sleep 1
		let count=count-1
	done	
}

make_directories()
{
	COMMAND=""
	HOST=${2}
	if [ -n "${HOST}" ]; then
		COMMAND="ssh ${HOST}"
	fi
	${COMMAND} mkdir -p ${1}
}


post_process_sar()
{
	FILE=${1}
	if [ -f ${FILE} ]; then
		/usr/bin/sar -f ${FILE} -A > `dirname ${FILE}`/sar.out
	fi
}

usage()
{
	if [ "$1" != "" ]; then
		echo
		echo "error: $1"
	fi
	echo ''
	echo 'usage: run_workload.sh -d <duration of test in second> -l <port> -u <db user> -x <db password>'
	echo 'other options:'
	echo '       -d <duration of test in second.>'
	echo '       -e <table engine>'
	echo '       -o <enable oprofile data collection>'
	echo '       -r <report saved directory>'
	echo '       -D <database type>'
	echo '       -H <database host name. (default localhost)>'
	echo '       -l <database port number>'
	echo '       -u <database user>'
	echo '       -x <database password>'
	echo '       -m <mysql home, if database type is mysql>'
	echo ''
	echo 'Example: run_workload.sh -d 3600 -e ntse'
	echo ''
}

#######################################################################
# parse command line options
#######################################################################

validate_parameter()
{
	if [ "$2" != "$3" ]; then
		usage "wrong argument '$2' for parameter '-$1'"
		exit 1
	fi
}

SLEEPY=1000 # milliseconds
ENGINE="ntse"
TEST_TABLE="Blog"

while getopts "d:D:e:hH:l:m:p:r:t:u:x:" opt; do
	case $opt in
	d)
		DURATION=`echo $OPTARG | egrep "^[0-9]+$"`
		validate_parameter $opt $OPTARG $DURATION
		;;
	D)
		DB_TYPE=$OPTARG
		;;
	e)
		ENGINE=$OPTARG
		;;
	h)
		usage
		exit 1
		;;
	H)
		DB_HOST=${OPTARG}
		;;
	l)
		DB_PORT=`echo $OPTARG | egrep "^[0-9]+$"`
		validate_parameter $opt $OPTARG $DB_PORT
		;;
	m)
		MYSQL_HOME=${OPTARG}
		;;
	p)
		DB_PASSWORD=${OPTARG}
		;;
	r)
		OUTPUT_DIR=$OPTARG
		;;
	t)
		TEST_TABLE=$OPTARG
		;;
	u)      
		DB_USER=${OPTARG}
		;;
	x)
		DB_PASSWORD=${OPTARG}
		;;
	esac
done
#######################################################################
# Check parameters.
#######################################################################

if [ "$DURATION" == "" ]; then
	echo "specify the duration of the test in seconds using -d #"
	exit 1
fi

if [ "$OUTPUT_DIR" == "" ]; then
	echo "specify the output directory of the test with command line option \"-r\""
	exit 1
fi

echo "data base type is : ${DB_TYPE}" 
if [ "${DB_TYPE}" != "mysql" -a "${DB_TYPE}" != "oracle" -a "${DB_TYPE}" != "postgresql" ]; then
	echo "Invalid database type: ${DB_TYPE}"
	exit 1
fi

#######################################################################
#end check parameters
#######################################################################

STATISTICIAN="${abs_top_srcdir}/scripts/statistician/statistician.py"
STATISTICIAN_ARG=" -H ${DB_HOST} -u ${DB_USER} -P ${DB_PORT}"
if [ ! -z "${DB_PASSWORD}" ]; then
	STATISTICIAN_ARG="${STATISTICIAN_ARG} -p ${DB_PASSWORD}"
fi

#
# Create the directories we will need.
#
make_directories $OUTPUT_DIR/tmp


#######################################################################
#start stage
#######################################################################

# Start collecting data before we start the test.

SAMPLE_LENGTH=60
ITERATIONS=$(( ($DURATION/$SAMPLE_LENGTH)+1 ))
${abs_top_srcdir}/scripts/sysstats.sh \
		--iter ${ITERATIONS} \
		--sample ${SAMPLE_LENGTH} \
		--outdir ${OUTPUT_DIR}/tmp > ${OUTPUT_DIR}/tmp/stats.out 2>&1 &

if [ "${DB_TYPE}" == "mysql" ]; then
  echo "Starting collecting ntse status at back end. "
  if [ "${DB_PASSWORD}" == "" ]; then
		${STATISTICIAN} create ${STATISTICIAN_ARG} -t test:${TEST_TABLE} -m ${MYSQL_HOME} -e ${ENGINE} -o ${OUTPUT_DIR} -H ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} >> /dev/null &
	else
		${STATISTICIAN} create ${STATISTICIAN_ARG} -t test:${TEST_TABLE} -m ${MYSQL_HOME} -e ${ENGINE} -o ${OUTPUT_DIR} -H ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} -p ${DB_PASSWORD}>> /dev/null &
	fi
	sleep 2
	${STATISTICIAN} start -i ${ITERATIONS} -o ${OUTPUT_DIR} >> /dev/null &
	mysql_info_pid=$!
fi

# Sleep for the duration of the run.
echo -n "estimated steady state time: "
#
do_sleep $DURATION

######################################################################
#stop stage
######################################################################

echo ''
echo "Stage 3. Processing of results..."

# Postprocessing of Database Statistics
post_process_sar ${OUTPUT_DIR}/tmp/sar_raw.out

${abs_top_srcdir}/scripts/generate-report --indir ${OUTPUT_DIR}/tmp  --outdir ${OUTPUT_DIR} > /dev/null 2>&1


if [ -f ${OUTPUT_DIR}/index.html ]; then
	echo "report file generated."
else
	echo "Error: Failed to find report file ${OUTPUT_DIR}/index.html"
fi

# post process sar
if [ -f ${KSAR_DIR}/kSar.jar ]; then
	mkdir -p ${OUTPUT_DIR}/sar
	java -jar ${KSAR_DIR}/kSar.jar -input file://${OUTPUT_DIR}/tmp/sar.out -outputPDF ${OUTPUT_DIR}/sar/sar_report.pdf > /dev/null 2>&1
	# append sar report file
	echo '<p><a href="sar/sar_report.pdf"> sar report</a></p>' >>${OUTPUT_DIR}/index.html
else
	echo "Error: failed to find kSar.jar at ${KSAR_DIR}/kSar.jar, please export KSAR_DIR"
fi

if [ "${DB_TYPE}" == "mysql" ]; then
	# post add ntse status report file link
	# 1. wait mysql info collecting shell ends
	echo "Waiting mysql info collecting shell ends (with pid ${mysql_info_pid}) ....(within 60 seconds)"
	wait $mysql_info_pid
	echo "wait finished, mysql info printing task completed."

	#collect mysql information the last time and delete temp file
	${STATISTICIAN} stop -o ${OUTPUT_DIR} >> /dev/null &
	${STATISTICIAN} drop -o ${OUTPUT_DIR} >> /dev/null &
fi

#parse mms shootings
if [ "$DB_TYPE" == "mysql" -a "${ENGINE}" == "ntse" ]; then
	./scripts/transform_ntse_mms.pl -i ${OUTPUT_DIR}/statistician-ntse/information_schema.run -o ${OUTPUT_DIR}/ntse_status/
	./scripts/generate_ntse_report.pl -i ${OUTPUT_DIR}/statistician-ntse/status.run -o ${OUTPUT_DIR}
fi


#remove temp directory
rm -rf $OUTPUT_DIR/tmp >> /dev/null 

echo "Test report completed!! Report fold is ${OUTPUT_DIR}"

