#!/bin/bash

# run local Docker image
__image_name=findep-parser
docker run -it --rm -p 9876:9876 -e "server_feature=FWN,DEP" ${__image_name}

