#Docker image of Finnish dependency parser developed by Turku NLP group.
#https://github.com/TurkuNLP/Finnish-dep-parser

FROM python:2.7-alpine

RUN apk add --no-cache bash 
RUN apk update && apk add ca-certificates wget && update-ca-certificates   
RUN apk add --no-cache openjdk8
RUN apk add --no-cache git 
RUN apk add --update --no-cache curl-dev curl

WORKDIR /

#Finnish-dep-parser
RUN git clone https://github.com/TurkuNLP/Finnish-dep-parser.git


WORKDIR Finnish-dep-parser

RUN ./install.sh

#Maven
RUN wget -q http://www.nic.funet.fi/pub/mirrors/apache.org/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.zip
RUN unzip -q apache-maven-3.5.0-bin.zip

#server4dev is for development use
#when building docker, mvn dependencies are always downloaded
#using this server4dev dir and packaging it, mvn downloads dependencies once and image with dependencies is stored to docker cache
#and if you change Dockerfile after the following three lines, Docker uses cached image where dependencies are already downloaded.
#if adding new dependencies to pom.xml, copy pom.xml from server directory to server4dev directory before building docker image
RUN mkdir server4dev
ADD server4dev ./server4dev/
RUN PATH=/Finnish-dep-parser/apache-maven-3.5.0/bin:$PATH && cd server4dev && mvn package

#add server code
RUN mkdir server
ADD server ./server/

RUN PATH=/Finnish-dep-parser/apache-maven-3.5.0/bin:$PATH && cd server && mvn package

#add modified Finnish dependency parser files
ADD server/resolve_readings.py .
ADD server/omorfi_pos.py .
ADD server/omorfi_wrapper.py .
ADD server/marmot-tag.py .
ADD server/init.sh .
ADD server/my_parser_wrapper.sh .
ADD server/parse.sh .

RUN chmod 755 my_parser_wrapper.sh
RUN chmod 755 parse.sh

#add testfiles
#RUN mkdir testfiles
#ADD test ./testfiles/

#remove files not needed runtime
RUN rm -rf LIBS LIBS-LOCAL/ apache-maven-3.5.0 apache-maven-3.5.0-bin.zip /root/.m2

#Port 9876 is hardcoded servlet server port
EXPOSE 9876
CMD ["java","-Xmx2g","-jar","server/target/fin-dep-parser-server-jar-with-dependencies.jar"] 

#execute server within image: 
#java -Xmx2g -jar server/target/fin-dep-parser-server-jar-with-dependencies.jar
#CMD ["/bin/bash"]

