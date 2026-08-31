#!/usr/bin/env python3
import base64, hashlib, pathlib, sys
if len(sys.argv) != 2:
    print('Usage: python apk_checksum.py LongDPC.apk')
    raise SystemExit(2)
p=pathlib.Path(sys.argv[1])
h=hashlib.sha256(p.read_bytes()).digest()
print(base64.urlsafe_b64encode(h).decode('ascii'))
