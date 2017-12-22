#!/usr/bin/env bash

#start number of parsers

#require a number of parsers when executing this script
if [[ "$1" == "" ]] ; then
	echo "Usage: $0 <NUMBER_OF_FINNISH_DEPENDENCY_PARSERS> <DOCKER_IMAGE_VERSION>"
	echo "Default <DOCKER_IMAGE_VERSION> is 'latest'"    
	exit 1
fi  
__number_of_parsers=$1

if [[ "$2" == "" ]] ; then
	__findep_image_version=latest
else
	__findep_image_version=$2
fi  

__parser_port=9876
__findep_parser_image=kazhar/finnish-dep-parser:$__findep_image_version
#colon separated environment variables for findep parser container
#comment out if not needed
__findep_env_vars=server_feature=DEP,FWN
#:enable_cache=false

#extract env vars to docker -e options
if [ ! -z "$__findep_env_vars"  ] ; then
  __findep_env_var_opts=$(echo ${__findep_env_vars} | awk '{n=split($0,a,",");for (i = 1; i <= n; ++i) printf " -e " a[i] " " }')
fi

function startParser
{
  __parser_name=$1
  __expose_port=$2
  docker run --restart always -d ${__findep_env_var_opts} --name=${__parser_name} -p 0.0.0.0:$__expose_port:9876 ${__findep_parser_image}
}

echo "Starting ${__number_of_parsers} parsers..."

__prefix=findepparser
for (( c=1; c<=${__number_of_parsers}; c++ ))
do
  __port=$((c+9875))
  startParser ${__prefix}${c} $__port
done

echo ""
echo "Parsers are starting... wait a while..."
echo "Or use: "
echo ""
for (( c=1; c<=${__number_of_parsers}; c++ ))
do
  echo "docker logs ${__prefix}${c}"
done
echo ""
echo "to view logs."
echo ""
echo "Parser container info..."
docker ps | grep ${__prefix}
