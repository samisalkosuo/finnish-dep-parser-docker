#Docker image of Finnish dependency parser developed by Turku NLP group.
#https://github.com/TurkuNLP/Finnish-dep-parser

FROM python:2.7-alpine

RUN apk add --update --no-cache bash openjdk8 curl-dev curl wget zip
RUN apk update && apk add ca-certificates && update-ca-certificates   

WORKDIR /

#Install Maven
ADD scripts/install_maven.sh .
RUN ["/bin/bash" ,"install_maven.sh","3.5.2"]

#server4dev is for development use
#Docker uses layered filesystem and each line of Dockerfile is maintained in the image
#TODO: more refactoring for image size http://blog.replicated.com/refactoring-a-dockerfile-for-image-size/
#when building docker, mvn dependencies are always downloaded
#using this server4dev dir and packaging it, mvn downloads dependencies once and image with dependencies is stored to docker cache
#and if you change Dockerfile after the following three lines, Docker uses cached image where dependencies are already downloaded.
#if adding new dependencies to pom.xml, copy pom.xml from server directory to server4dev directory before building docker image
RUN mkdir server4dev
ADD server4dev ./server4dev/
RUN PATH=/maven/bin:$PATH && cd server4dev && mvn package

ADD scripts/convert_vocab_fi.py .
ADD scripts/install_findepparser.sh .
#Install Finnish-dep-parser
#Uses fork: https://github.com/samisalkosuo/Finnish-dep-parser
#uses specific commit ID as parameter
RUN ["/bin/bash" ,"install_findepparser.sh","fc8511cd16541e3b07072352d5801b54a5c05cf3"]

WORKDIR /Finnish-dep-parser

#add server code
RUN mkdir server
ADD server ./server/
ADD scripts/package_parserserver.sh .
RUN ["/bin/bash" ,"package_parserserver.sh"]

#add modified Finnish dependency parser files
ADD server/resolve_readings.py .
ADD server/omorfi_pos.py .
ADD server/omorfi_wrapper.py .
ADD server/marmot-tag.py .
ADD server/init.sh .
ADD server/my_parser_wrapper.sh .
ADD server/tag.sh .

RUN chmod 755 my_parser_wrapper.sh tag.sh

#add testfiles
#RUN mkdir testfiles
#ADD test ./testfiles/

#Port 9876 is hardcoded servlet server port
EXPOSE 9876

CMD ["java","-Xmx2g","-jar","fin-dep-parser-server-jar-with-dependencies.jar"] 

#execute server within image: 
#java -Xmx2g -jar fin-dep-parser-server-jar-with-dependencies.jar
#CMD ["/bin/bash"]

