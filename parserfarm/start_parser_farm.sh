#!/bin/bash

#start local finnish dependency parser farm

#check if docker network exist
#if yes ask to stop farm
docker network inspect ${__network_name} > /dev/null 2> /dev/null
if [ $? -eq 0 ]; then
	echo "Farm may be running. Please stop it using stop_parser_farm.sh"
	exit 1
fi

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

#modify variables if you want
__prefix=findepparser
__network_name=${__prefix}network
__proxy_port=9876
__findep_parser_image=kazhar/finnish-dep-parser:$__findep_image_version
#comma separated environment variables for findep parser container
#comment out if not needed
__findep_env_vars=server_feature=DEP
#,conllu_cache_size=100



if [ ! -f haproxy_template.cfg ]; then
    echo "haproxy_template.cfg not found."
    echo "run this script in parserfarm-directory."
    exit 1
fi

__proxy_cfg_file=haproxy.cfg
cp haproxy_template.cfg ${__proxy_cfg_file}

#extract env vars to docker -e options
if [ ! -z "$__findep_env_vars"  ] ; then
  __findep_env_var_opts=$(echo ${__findep_env_vars} | awk '{n=split($0,a,",");for (i = 1; i <= n; ++i) printf " -e " a[i] " " }')
fi

function startParser
{
  __parser_name=$1
  docker run --net ${__network_name} --restart always -d ${__findep_env_var_opts} --name=${__parser_name} ${__findep_parser_image}
  echo "  server ${__parser_name} ${__parser_name}:9876 maxconn 32" >> ${__proxy_cfg_file}
}

echo "Creating parser network..."
docker network create ${__network_name}

echo "Starting ${__number_of_parsers} parsers..."

for (( c=1; c<=${__number_of_parsers}; c++ ))
do
  startParser ${__prefix}${c}     
done

echo "Building haproxy container..."
docker build -t findep-parser-haproxy .

echo "Starting haproxy container..."
docker run --net ${__network_name} -d --restart always --name=${__prefix}proxy -p 0.0.0.0:${__proxy_port}:9876 findep-parser-haproxy

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
echo "Parser farm container info..."
docker ps | grep ${__prefix}
