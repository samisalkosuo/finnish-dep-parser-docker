#!/usr/bin/env bash

#generate offline packages for Finnish dependency parser Docker image and haproxy image

#require parser version when executing this script
if [[ "$1" == "" ]] ; then
	echo "Usage: $0 <DOCKER_IMAGE_VERSION> <OUTPUT_DIR>"
	echo "Check latest version: https://hub.docker.com/r/kazhar/finnish-dep-parser/tags/"  
	echo "Default <OUTPUT_DIR> is current directory."  
	exit 1
fi

if [[ "$2" == "" ]] ; then
  __workdir=$(pwd)
else
  __workdir=$2
fi  

set -o errexit
set -o pipefail
set -o nounset


__version=$1

cd $__workdir

__haproxy_image=haproxy
__haproxy_image_version=1.7-alpine
__findepparser_image=kazhar/finnish-dep-parser
__findepparser_image_version=$__version

__haproxy_img=$__haproxy_image:$__haproxy_image_version
__findep_img=$__findepparser_image:$__findepparser_image_version

#pull images
echo "Pulling images..."
docker pull $__haproxy_img
docker pull $__findep_img

__haproxy_tar=haproxy-${__haproxy_image_version}.tar
__findep_tar=findepparser-${__findepparser_image_version}.tar

#save  images as tar files
echo "Saving ${__haproxy_img}..."
docker save $__haproxy_img > $__haproxy_tar

echo "Saving ${__findep_img}..."
docker save $__findep_img > $__findep_tar

#gzip tar files
echo "gzipping ${__haproxy_img}..."
gzip -f ${__haproxy_tar}

echo "gzipping ${__findep_img}..."
gzip -f ${__findep_tar}

echo "Images are saved:"
echo "  ${__workdir}/${__haproxy_tar}.gz"
echo "  ${__workdir}/${__findep_tar}.gz"
echo " "
echo "Copy files to target host and execute:"
echo "  docker load < ${__haproxy_tar}.gz"
echo "  docker load < ${__findep_tar}.gz"
