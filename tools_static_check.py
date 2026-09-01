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


main=(root/'app/src/main/java/com/longkaca/dpc/MainActivity.java').read_text()
for pkg in [
    'com.ss.android.ugc.trill',
    'com.ss.android.ugc.tiktok.lite',
    'jp.naver.line.android',
    'com.tafayor.autoscrolling',
]:
    ok(pkg in main, f'missing expected app package: {pkg}')
for old in ['com.zhiliaoapp.musically','com.zhiliaoapp.musically.go','com.truedevelopersstudio.automatictap.autoclicker']:
    ok(old not in main, f'old app package still present: {old}')

# Cheap syntax sanity checks that catch accidental unbalanced braces/comments/strings poorly, not a compiler replacement.
for p in java:
    txt=p.read_text()
    ok(txt.count('{')==txt.count('}'), f'unbalanced braces: {p.name}')

# v1.6 provisioning contract checks
get_mode=(root/'app/src/main/java/com/longkaca/dpc/GetProvisioningModeActivity.java').read_text()
compliance=(root/'app/src/main/java/com/longkaca/dpc/PolicyComplianceActivity.java').read_text()
checksum_util=(root/'app/src/main/java/com/longkaca/dpc/ChecksumUtil.java').read_text()
ok('PROVISIONING_MODE_FULLY_MANAGED_DEVICE' in get_mode, 'fully-managed mode missing')
ok('EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS' in get_mode, 'skip education missing')
ok('setResult(Activity.RESULT_OK, result)' in get_mode, 'GET_PROVISIONING_MODE must return RESULT_OK + Intent')
ok('setResult(Activity.RESULT_OK, result)' in compliance, 'ADMIN_POLICY_COMPLIANCE must return RESULT_OK + Intent')
ok('installedSigningCertSha256Base64Url' in checksum_util, 'signature checksum helper missing')
ok('PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM' in main, 'QR must use signature checksum')

if errors:
    print('FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('PASS static project checks')
print(f'Java files: {len(java)}')
print('Package: com.longkaca.dpc')
print('Provisioning activities: present')
print('QR JSON/XML: valid')
