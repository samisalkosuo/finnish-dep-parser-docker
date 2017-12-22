# finnish-dep-parser-docker

Docker container and HTTP server for [Finnish dependency parser](https://github.com/TurkuNLP/Finnish-dep-parser).

Parser takes Finnish text as input and outputs [CoNLL-U format](http://universaldependencies.org/format.html).

# Implementation

Finnish dependency parser is originally used as a command line tool, but in this Docker container the parser is wrapped in HTTP server. The server code replaces some of the scripts/code with Java servlets and the result is that it is much faster to parse text.

Fortunately, Finnish dependency parser uses open source code, so I could include and modify some of the 
scripts and source code. Please see the server-directory to find out what code is used and modified.
One such code is [HFST optimized-lookup standalone library and command line tool](https://github.com/hfst/hfst-optimized-lookup) by Helsinki Finite-State Technology.

Unfortunately, the code used in Finnish dependency parser appears not to handle concurrent requests well, so
the server is currently forced to process one request at a time. Please see the code for details.

# Usage

Get container from Dockerhub:

- docker pull kazhar/finnish-dep-parser

Or build docker image using:

- docker build -t finnish-dep-parser .

Run docker container, expose port 9876 and exit using CTRL-C:

- docker run -it --rm -p 0.0.0.0:9876:9876 kazhar/finnish-dep-parser

Start docker container in detached mode and restart if container goes down:

- docker run --restart always -d -p 0.0.0.0:9876:9876 kazhar/finnish-dep-parser

Post file to parser using curl:

- curl -H "Content-Type: text/plain" --data-binary "@test/text_1k.txt" http://127.0.0.1:9876
- or
- cat test/text_1k.txt | curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876

Use any programming language to HTTP POST Finnish text to this server and get CoNLL-U format back.

Get simple statistics of the parser:

- http://127.0.0.1:9876, a normal HTTP GET

# Environment variables

Some environment variables can be used. 

- *enable_cache*, enable cache for parsed conllu documents, 'true' or 'false'
  - Default is 'true'.
  - Cache is 100MB in-memory cache.
- *server_feature*, set features to start: DEP, LEMMA, FWN or ALL. 
  - Separate features using comma to set features. For example: -e "server_feature=DEP,FWN"
  - Default is ALL to start all features.
  - DEP-feature starts dependency parser.
  - FWN-feature enables FinWordNet servlet. Context root is /finwordnet.
    - Requests are like */finwordnet?word=sana*.
    - Response is un-compliant custom JSON.
  - LEMMA-feature starts a servlet that returns only lemmas. Context root is /lemma.
- *log_level*, set level of logging. 
  - 0=no logging after starting the container.
  - 1=log elapsed time and excerpt of parsed text.
  - 2=log elapsed time, full text and some other info. 
  - Default is 1.
  - If log_level=0, you can see latest parsed text by using GET-request to parser.

# Farming

Directory *parserfarm* includes scripts to start finnish-dep-parser farm, 1 or more parser containers within single host accessible via proxy. Farm is implemented using scripts and plain containers without docker-compose or other similar stuff in order to have nothing but Docker runtime as a prereq.

Parsing of text is CPU intensive. If you use many parsers, they may consume all CPU resources. If you use few parsers, they may not take advantage of all resources. Good starting point is to have about as many parser containers as there are CPU cores in the server.

Files in parserfarm-directory:

- *start_parser_farm.sh*, this script starts parser farm. Usage: 
  - ./start_parser_farm.sh <NUMBER_OF_FINNISH_DEPENDENCY_PARSERS> <DOCKER_IMAGE_VERSION>
  - Default version is 'latest'
  - If necessary, modify this script to your needs.
- *stop_parser_farm.sh*, this script stops all containers started with previous script.
- *haproxy_template.cfg*, Config file template for haproxy. Modify to your needs. Copied to *haproxy.cfg" when starting the farm.
- *Dockerfile*, Dockerfile to build local haproxy image.

Example:

- *nproc*, command to check number of cores in the Linux server.
  - assume 4 cores
- *start_parser_farm.sh 2 0.15*, starts proxy and 2 parsers of version 0.15.

This kind of farming/scalability is not intended for production use. Use for example [IBM Cloud Private](https://www.ibm.com/cloud-computing/products/ibm-cloud-private/) for production. [Community Edition is available](https://hub.docker.com/r/ibmcom/icp-inception/) to be used to check out IBM Cloud Private.

# Disclaimer

Everything in this repo, including all code is "AS IS". No support, no warranty, no fitness for any purpose, nothing is expressed or implied, not by me (nor my employer).

# License

The Finnish dependency parsing pipeline is licensed under GPL-2.0. Other licenses may apply to other code in this repo. See files in this repo for any info. I guess this repo is also GPL-2.0 but I can not tell for sure, so I don't claim any licensing.

If you want to know more and be sure, please seek legal advice.

