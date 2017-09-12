#install Finnish dependency parser and Maven
#and delete unnecessary files

#Finnish dependency parser
git clone https://github.com/TurkuNLP/Finnish-dep-parser.git
cd /Finnish-dep-parser
rm -rf .git
./install.sh 
rm -rf LIBS LIBS-LOCAL/

#Maven
wget -q http://www.nic.funet.fi/pub/mirrors/apache.org/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.zip
unzip -q apache-maven-3.5.0-bin.zip
rm apache-maven-3.5.0-bin.zip
