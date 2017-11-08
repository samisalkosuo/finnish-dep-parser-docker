#!/bin/bash

#generate md5 from test files


#build using:
#docker build -f Dockerfile.cli -t findep-cli .

#run:
#docker run -it --rm findep-cli
#and execute this 

function generateMD5
{
  file=$1
  md5sum=$(cat ${file} | ./parser_wrapper.sh | md5sum -b | awk '{print  $1}')
  echo $file " " $md5sum  
}

