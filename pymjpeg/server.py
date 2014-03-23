import pymjpeg
from glob import glob
import sys
import time

from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler

class MyHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        # Response headers (multipart)
        for k, v in pymjpeg.request_headers().items():
            self.send_header(k, v) 
        # Multipart content
        filename = '../img/camera0.jpg'
        while True:
            # Part boundary string
            self.end_headers()
            self.wfile.write(pymjpeg.boundary)
            self.end_headers()
            # Part headers
            for k, v in pymjpeg.image_headers(filename).items():
                self.send_header(k, v) 
            self.end_headers()
            # Part binary
            for chunk in pymjpeg.image(filename):
                self.wfile.write(chunk)
            time.sleep(0.2)
    def log_message(self, format, *args):
        return

httpd = HTTPServer(('', 8001), MyHandler)
httpd.serve_forever()
