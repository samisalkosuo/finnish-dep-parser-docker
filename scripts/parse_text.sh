#parse given text

url=http://127.0.0.1:9876

text=$1

echo $text | curl -H "Content-Type: text/plain" -s --data-binary @- ${url}
  