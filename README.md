# finnish-dep-parser-docker

Docker container for Finnish dependency parser (https://github.com/TurkuNLP/Finnish-dep-parser).

Parser takes Finnish text as input and outputs CoNLL-U Format (http://universaldependencies.org/format.html).

# Usage

Get container from dockerhub:

- docker pull kazhar/finnish-dep-parser

Or build docker image using:

- docker build -t finnish-dep-parser .

Run docker container, expose port 8080 and exit using CTRL-C:

- docker run -it --rm -p 0.0.0.0:8080:8080 kazhar/finnish-dep-parser

Post file to to parser using curl

- curl -H "Content-Type: text/plain" -d "@test/text.txt" http://127.0.0.1:8080

Use any programming language to HTTP POST Finnish text to this server and get CoNNL-U format back.

# License

The Finnish dependency parsing pipeline is licensed under GPL-2.0. 
This repo does not include any code from Finnish dependency parser so it is my humble opinion that this is not GPL-2.0.
But I can not tell for sure, so I don't claim any licensing.

If you want to know more, please seek legal advice.

