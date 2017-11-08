#install Finnish dependency parser and Maven
#and delete unnecessary files

#Finnish dependency parser
#download specific commit of fork of Finnish dependency parser
COMMIT_ID=$1
wget -q https://github.com/samisalkosuo/Finnish-dep-parser/archive/${COMMIT_ID}.zip
unzip -q ${COMMIT_ID}.zip && rm -rf ${COMMIT_ID}.zip
mv /Finnish-dep-parser-${COMMIT_ID} /Finnish-dep-parser

cd /Finnish-dep-parser
chmod -R 755 *

./install.sh 

#test installation
#cat data/wiki-test.txt | ./parser_wrapper.sh > wiki-test-parsed.txt

rm -rf LIBS LIBS-LOCAL/

