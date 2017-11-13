#!/bin/bash

__prefix=findepparser

#stop all containers
echo "Stopping all parser containers..."
docker ps | grep ${__prefix} | awk '{print "docker stop " $1}'| sh

#remove all containers
echo "Removing all parser containers..."
docker ps --all | grep ${__prefix} | awk '{print "docker rm " $1}'| sh

#remove network
echo "Removing parser network..."
docker network rm ${__prefix}network
