#test finnish-dep-parser after building docker image locally

#build first: docker build -t finnish-dep-parser .
#then run: docker run -it --rm -p 0.0.0.0:9876:9876 finnish-dep-parser
#and then execute this script

url=http://127.0.0.1:9876

function test
{
  file=$1
  md5=$2
  md5sum=$(curl -H "Content-Type: text/plain" -s --data-binary "@${file}" ${url} | md5sum)
  if [ "$md5sum" = "$md5" ]; then
    echo "OK " $file
  else
    echo "FAILED " $file
    exit 1
  fi

}

test test/text.txt "a98ebd447c1d2a73bd8af1cc23d2b853 *-"
test test/text2.txt "82435da5e746361666bcf8ba6af8ebb8 *-"
test test/text3.txt "f6744c8a428c05e5a2b6a9ea3ce24e33 *-"
test test/text_1k.txt "404c3acfc1920fae4d8f667980ca968c *-"
