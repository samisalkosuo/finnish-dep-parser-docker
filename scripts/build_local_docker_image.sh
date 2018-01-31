#!/bin/bash

# build local Docker image
__image_name=findep-parser
echo Building Docker image \"${__image_name}\"...
docker build -t ${__image_name} .

echo " "
echo Docker image \"${__image_name}\" has been built.

