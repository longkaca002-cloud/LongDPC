from pathlib import Path
import json, re, sys, xml.etree.ElementTree as ET
root=Path(__file__).resolve().parent
errors=[]

def ok(cond,msg):
    if not cond: errors.append(msg)

# XML
for p in [root/'app/src/main/AndroidManifest.xml', *list((root/'app/src/main/res').rglob('*.xml'))]:
    try: ET.parse(p)
    except Exception as e: errors.append(f'XML {p}: {e}')

# JSON
try: json.loads((root/'qr_payload_template.json').read_text())
except Exception as e: errors.append(f'QR JSON: {e}')

# Gradle/package consistency
app_gradle=(root/'app/build.gradle').read_text()
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
java=list((root/'app/src/main/java/com/longkaca/dpc').glob('*.java'))
ok("namespace 'com.longkaca.dpc'" in app_gradle, 'namespace mismatch')
ok("applicationId 'com.longkaca.dpc'" in app_gradle, 'applicationId mismatch')
ok('android.app.action.GET_PROVISIONING_MODE' in manifest, 'missing GET_PROVISIONING_MODE')
ok('android.app.action.ADMIN_POLICY_COMPLIANCE' in manifest, 'missing ADMIN_POLICY_COMPLIANCE')
ok('android.app.action.PROVISIONING_SUCCESSFUL' in manifest, 'missing PROVISIONING_SUCCESSFUL')
ok('android.permission.BIND_DEVICE_ADMIN' in manifest, 'missing BIND_DEVICE_ADMIN')
ok('com.longkaca.dpc/com.longkaca.dpc.LongDeviceAdminReceiver' in (root/'qr_payload_template.json').read_text(), 'QR admin component mismatch')
for p in java:
    txt=p.read_text()
    ok(txt.startswith('package com.longkaca.dpc;'), f'package mismatch: {p.name}')

# Cheap syntax sanity checks that catch accidental unbalanced braces/comments/strings poorly, not a compiler replacement.
for p in java:
    txt=p.read_text()
    ok(txt.count('{')==txt.count('}'), f'unbalanced braces: {p.name}')

if errors:
    print('FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('PASS static project checks')
print(f'Java files: {len(java)}')
print('Package: com.longkaca.dpc')
print('Provisioning activities: present')
print('QR JSON/XML: valid')
