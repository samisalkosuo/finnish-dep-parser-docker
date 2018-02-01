#parse given file

url=http://127.0.0.1:9876

curl -H "Content-Type: text/plain" -s --data-binary "@${1}" ${url}
  