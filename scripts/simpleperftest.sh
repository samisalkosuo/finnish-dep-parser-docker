#https://gist.github.com/bigomega/4de7dbc395be0e0f33b4#file-load-test-sh

start_time=$(date +%s%N)

function usage()
{
	echo "Usage: $0 <TOTAL_REQUESTS_TO_DO> <PARSER_URL>"
}

if [[ "$1" == "" ]] ; then
  usage
	exit 1
fi  

if [[ "$2" == "" ]] ; then
	usage
	exit 1
fi  


__total_requests="$1"
echo "url: $2
total requests: ${__total_requests}"
START=$(date +%s);

__test_file=../test/text2.txt
rm -f perf-test.log

get () {
#  curl -s -v "$1" 2>&1 | tr '\r\n' '\\n' | awk -v date="$(date +'%r')" '{print $0"\n-----", date}' >> perf-test.log
  curl -o /dev/null -w %{time_total}\\n  -H "Content-Type: text/plain" -s --data-binary "@${__test_file}" $1 >> perf-test.log
}

#echo $(($(date +%s) - START)) | awk '{print int($1/60)":"int($1%60)}'

for i in `seq 1 $__total_requests`
do
  get $2 &
done

__requests_done=$(wc perf-test.log | awk '{print $1}')
echo -n "Working"
while [ $__requests_done -ne $__total_requests ]
do
  sleep 1
  #echo "Still working..."
  echo -n "."
  __requests_done=$(wc perf-test.log | awk '{print $1}')
done
echo "Done."

end_time=$(date +%s%N)
elapsed=$(python -c "print (${end_time}-${start_time})/1000000.0")

echo "Total requests  : ${__total_requests}"
__avg_response=$(awk '{s+=$1}END{print "",s/NR}' RS="\n" perf-test.log)
echo "Avg respons time:${__avg_response}"
echo "Elapsed         : $elapsed milliseconds"


