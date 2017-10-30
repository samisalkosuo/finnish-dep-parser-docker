#package parser server

PATH=/maven/bin:$PATH
cd server
mvn package
mv target/fin-dep-parser-server-jar-with-dependencies.jar ..
cd ..

#If building this image on RedHat7 this may not work
#apparently related to OverlayFS file system
#that's why next line includes || true
rm -rf server/ || true
