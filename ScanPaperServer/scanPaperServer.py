#!/usr/bin/env python3
from http.server import BaseHTTPRequestHandler, HTTPServer
import psutil
import sys
import numpy as np
import cv2
import io
from PIL import Image


class HandleRequests(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        self._set_headers()
        self.wfile.write("received get request")

    def do_POST(self):
        self._set_headers()
        content_len = int(self.headers.get('content-length', 0))
        self.process_request(self.rfile.read(content_len))

    def do_PUT(self):
        self.do_POST()

    def process_request(self, body):
        img = np.array(Image.open(io.BytesIO(body)), dtype=np.uint8)
        opencv_array = cv2.cvtColor(img, code=cv2.COLOR_RGB2GRAY)
        process_image(opencv_array)


def process_image(image):
    pass


if __name__ == '__main__':
    if len(sys.argv) == 3:
        interface = sys.argv[1]
        host = psutil.net_if_addrs()[interface][0].address
        port = int(sys.argv[2])
        print(f'Listening on {host}:{port}')
        HTTPServer((host, port), HandleRequests).serve_forever()
    else:
        print('Insert as first arg interface and as second port number')
