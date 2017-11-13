# finnish-dep-parser-docker

Docker container and server for [Finnish dependency parser](https://github.com/TurkuNLP/Finnish-dep-parser).

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

Run parser using cache:

- docker run -it --rm -p 0.0.0.0:9876:9876 -e "conllu_cache_size=100" kazhar/finnish-dep-parser

Post file to parser using curl:

- curl -H "Content-Type: text/plain" --data-binary "@test/text_1k.txt" http://127.0.0.1:9876
- or
- cat test/text_1k.txt | curl -H "Content-Type: text/plain" --data-binary @- http://127.0.0.1:9876

Use any programming language to HTTP POST Finnish text to this server and get CoNLL-U format back.

Get simple statistics of the parser:

- http://127.0.0.1:9876, a normal HTTP GET

# Environment variables

Supported environment variables.

- *conllu_cache_size*, set cache size for parsed conllu documents. Default is no cache.
- *server_feature*, set features to start: DEP, LEMMA or ALL. Default is ALL to start all features.

# Farming

Directory 'parserfarm' includes scripts to start finnish-dep-parser farm, 1 or more parser containers within single host accessible via proxy. Farm is implemented using scripts and plain containers without docker-compose or other similar stuff in order to have nothing but Docker runtime as a prereq.

Files in parserfarm-directory:

- *start_parser_farm.sh*, this script starts parser farm. Usage: 
  - ./start_parser_farm.sh <NUMBER_OF_FINNISH_DEPENDENCY_PARSERS> <DOCKER_IMAGE_VERSION>
  - Default version is 'latest'
  - If necessary, modify this script to your needs.
- *stop_parser_farm.sh*, this script stops all containers started with previous script.
- *haproxy_template.cfg*, Config file template for haproxy. Modify to your needs. Copied to *haproxy.cfg" when starting the farm.
- *Dockerfile*, Dockerfile to build local haproxy image.

# Disclaimer

Everything in this repo, including all code is "AS IS". No support, no warranty, no fitness for any purpose, nothing is expressed or implied, not by me (nor my employer).

# License

The Finnish dependency parsing pipeline is licensed under GPL-2.0. Other licenses may apply to other code in this repo. See files in this repo for any info. I guess this repo is also GPL-2.0 but I can not tell for sure, so I don't claim any licensing.

If you want to know more and be sure, please seek legal advice.

