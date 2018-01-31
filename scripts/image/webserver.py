#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Simple HTTP server in python. POST finnish text to this server and get CoNNL-U back.

Usage::
    ./web-server.py [<port>]

Send a file using a POST request::
    curl -d "@wiki-test.txt" http://192.168.196.129:8080
"""
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import SocketServer
import subprocess
import io
import codecs
import sys

class S(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/plain')
        self.end_headers()

    def do_GET(self):
        self._set_headers()
        self.wfile.write("Hello from finnish-dep-parser server. Post Finnish text to this URL and get CoNLL-U back.")

    def do_HEAD(self):
        self._set_headers()

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        postedData = self.rfile.read(content_length)
        if (len(postedData) > 0):
          print >> sys.stderr, postedData
          dir="/Finnish-dep-parser"

          #write incoming data to file
          #note: if incoming data has newlines parsing may not be as expected
          #remember to remove newlines before posting text to this server.
          #TODO: write as UTF-8 or write in memory
          f=open(dir+"/data.txt", "w")
          f.write(postedData)
          f.close()

          f=open(dir+"/data.txt", "r")
          #call parser subprocess
          #write subprocess output directory to output
          self._set_headers()
          cmdArgs=[dir+"/parser_wrapper.sh"]
          proc=subprocess.Popen(cmdArgs,stdin=f,stdout=self.wfile)
          proc.wait()
          f.close()

def run(server_class=HTTPServer, handler_class=S, port=8080):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print('Starting httpd...')
    httpd.serve_forever()

if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()

