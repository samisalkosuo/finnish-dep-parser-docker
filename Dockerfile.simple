#Docker image of Finnish dependency parser developed by Turku NLP group.
#https://github.com/TurkuNLP/Finnish-dep-parser

FROM python:2.7-alpine

RUN apk add --no-cache bash 
RUN apk update && apk add ca-certificates wget && update-ca-certificates   
RUN apk add --no-cache openjdk8
RUN apk add --no-cache git 

#Finnish-dep-parser
WORKDIR /
RUN git clone https://github.com/TurkuNLP/Finnish-dep-parser.git
WORKDIR Finnish-dep-parser

RUN ./install.sh

ADD webserver.py .

CMD ["python","webserver.py"]
