#Install Maven

MVN_VERSION=$1
wget -q http://www.nic.funet.fi/pub/mirrors/apache.org/maven/maven-3/${MVN_VERSION}/binaries/apache-maven-${MVN_VERSION}-bin.zip
unzip -q apache-maven-${MVN_VERSION}-bin.zip
rm apache-maven-${MVN_VERSION}-bin.zip
mv apache-maven-${MVN_VERSION}/ maven/ 
