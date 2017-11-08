#test finnish-dep-parser after building docker image locally

#build first: docker build -t finnish-dep-parser .
#then run: docker run -it --rm -p 0.0.0.0:9876:9876 finnish-dep-parser
#and then execute this script

url=http://127.0.0.1:9876

function test
{
  file=$1
  md5=$2
  md5sum=$(curl -H "Content-Type: text/plain" -s --data-binary "@${file}" ${url} | md5sum -b | awk '{print  $1}')
  if [ "$md5sum" = "$md5" ]; then
    echo "OK " $file
  else
    echo "FAILED " $file
    #exit 1
  fi

}

#MD5 generated 08.11.2017 using latest Finnish-dep-parser
#https://github.com/TurkuNLP/Finnish-dep-parser
test test/text_1k.txt "404c3acfc1920fae4d8f667980ca968c"
test test/text.txt "a98ebd447c1d2a73bd8af1cc23d2b853"
test test/text2.txt "82435da5e746361666bcf8ba6af8ebb8"
test test/text3.txt "85f01917071909534c503ddc4c50ca8e"
test test/text4.txt "dd98c6d8f63c82c1c26fe5874e50fa18"
test test/text5.txt "6ba38e8923f680be6bb93847c265ed2b"
test test/text6.txt "2eb23903f533a00da3b640b44a51b065"
test test/text7.txt "8a6365b227e931c51954c3d34726a407"
test test/text8.txt "943d0eaf0090726a4bc1868e4411f937"
test test/text9.txt "ec70191ae26e418939cdf3edf4462199"
test test/text10.txt "6e5258db67aeb5d8f3e52423f640cdd9"
test test/text11.txt "581a6b80c737e98603ed996d257b7ce9"
test test/text12.txt "4284e2a2c956ac859e8583ea95ae1138"
test test/text13.txt "975fcbe6bce50574d0ad85f358171c63"

