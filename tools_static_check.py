from pathlib import Path
import json, sys, xml.etree.ElementTree as ET
root=Path(__file__).resolve().parent
errors=[]

def ok(cond,msg):
    if not cond: errors.append(msg)

for p in [root/'app/src/main/AndroidManifest.xml', root/'longocr/src/main/AndroidManifest.xml', root/'longswipe/src/main/AndroidManifest.xml',
          *list((root/'app/src/main/res').rglob('*.xml')),
          *list((root/'longocr/src/main/res').rglob('*.xml')),
          *list((root/'longswipe/src/main/res').rglob('*.xml'))]:
    try: ET.parse(p)
    except Exception as e: errors.append(f'XML {p}: {e}')

try: json.loads((root/'qr_payload_template.json').read_text())
except Exception as e: errors.append(f'QR JSON: {e}')

app_gradle=(root/'app/build.gradle').read_text()
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
java=list((root/'app/src/main/java/com/longkaca/dpc').glob('*.java'))
main=(root/'app/src/main/java/com/longkaca/dpc/MainActivity.java').read_text()
catalog=(root/'app/src/main/java/com/longkaca/dpc/AppCatalog.java').read_text()
all_app_text=main+'\n'+catalog

ok("namespace 'com.longkaca.dpc'" in app_gradle, 'namespace mismatch')
ok("applicationId 'com.longkaca.dpc'" in app_gradle, 'applicationId mismatch')
ok("versionCode 19" in app_gradle, 'versionCode must be 19')
ok("versionName '2.8-long-auto-swipe'" in app_gradle, 'versionName mismatch')
ok('android.app.action.GET_PROVISIONING_MODE' in manifest, 'missing GET_PROVISIONING_MODE')
ok('android.app.action.ADMIN_POLICY_COMPLIANCE' in manifest, 'missing ADMIN_POLICY_COMPLIANCE')
ok('android.app.action.PROVISIONING_SUCCESSFUL' in manifest, 'missing PROVISIONING_SUCCESSFUL')
ok('android.permission.BIND_DEVICE_ADMIN' in manifest, 'missing BIND_DEVICE_ADMIN')
ok('AutoInstallJobService' in manifest, 'AutoInstallJobService missing from manifest')
ok('BIND_JOB_SERVICE' in manifest, 'JobService permission missing')
ok('com.longkaca.dpc/com.longkaca.dpc.LongDeviceAdminReceiver' in (root/'qr_payload_template.json').read_text(), 'QR admin component mismatch')

for p in java:
    txt=p.read_text()
    ok(txt.startswith('package com.longkaca.dpc;'), f'package mismatch: {p.name}')
    ok(txt.count('{')==txt.count('}'), f'unbalanced braces: {p.name}')

for pkg in [
    'com.ss.android.ugc.trill',
    'com.ss.android.ugc.tiktok.lite',
    'jp.naver.line.android',
    'com.longkaca.autoswipe',
    'com.google.android.gm',
    'com.longkaca.ocr',
]:
    ok(pkg in all_app_text, f'missing expected app package: {pkg}')
for old in ['com.zhiliaoapp.musically','com.zhiliaoapp.musically.go','com.truedevelopersstudio.automatictap.autoclicker']:
    ok(old not in all_app_text, f'old app package still present: {old}')

get_mode=(root/'app/src/main/java/com/longkaca/dpc/GetProvisioningModeActivity.java').read_text()
compliance=(root/'app/src/main/java/com/longkaca/dpc/PolicyComplianceActivity.java').read_text()
checksum_util=(root/'app/src/main/java/com/longkaca/dpc/ChecksumUtil.java').read_text()
apk_installer=(root/'app/src/main/java/com/longkaca/dpc/ApkInstaller.java').read_text()
auto_job=(root/'app/src/main/java/com/longkaca/dpc/AutoInstallJobService.java').read_text()
install_result=(root/'app/src/main/java/com/longkaca/dpc/InstallResultReceiver.java').read_text()

ok('PROVISIONING_MODE_FULLY_MANAGED_DEVICE' in get_mode, 'fully-managed mode missing')
ok('EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS' in get_mode, 'skip education missing')
ok('setResult(Activity.RESULT_OK, result)' in get_mode, 'GET_PROVISIONING_MODE must return RESULT_OK + Intent')
ok('setResult(Activity.RESULT_OK, result)' in compliance, 'ADMIN_POLICY_COMPLIANCE must return RESULT_OK + Intent')
ok('installedSigningCertSha256Base64Url' in checksum_util, 'signature checksum helper missing')
ok('PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM' in main, 'QR must use signature checksum')

ok('.apks' in apk_installer, 'v1.8 .apks detection missing')
ok('ZipFile' in apk_installer, 'v1.8 ZipFile split reader missing')
ok('base.apk' in apk_installer, 'v1.8 base.apk validation missing')
ok('session.openWrite' in apk_installer, 'PackageInstaller session writes missing')
ok('downloadToFileResumable' in apk_installer, 'resumable downloader missing')
ok('Range' in apk_installer and 'HTTP_PARTIAL' in apk_installer, 'HTTP Range resume missing')
ok('attempt <= 8' in apk_installer, 'download reconnect attempts missing')
ok('apps-v2/tiktok.apks' in catalog, 'default TikTok APKS URL missing')
ok('apps-v2/tiktok-lite.apks' in catalog, 'default TikTok Lite APKS URL missing')
ok('apps-v2/line.apks' in catalog, 'default LINE APKS URL missing')
ok('apps-v2/long-auto-swipe.apk' in catalog, 'default Long Auto Swipe APK URL missing')
ok('com.tafayor.autoscrolling' not in catalog, 'old external Auto Scroll remains')
ok('apps-v2/gmail.apks' not in catalog, 'Gmail must be left as preserved system app')
ok('apps-v2/long-ocr-v14.apk' in catalog, 'default Long OCR 1.4 APK URL missing')
ok('apps-v3/' not in catalog, 'obsolete apps-v3 URL remains')
ok('mother_dpc_url' in main, 'mother DPC URL persistence missing')
ok('mother_wifi_ssid' in main, 'mother Wi-Fi persistence missing')
ok('mother_apk_' in main, 'mother app URL persistence missing')
ok('mother_apn_profile' in main and 'apnChoice' in main, 'mother APN selector missing')
ok('Longkaca5G' in catalog, 'new default Wi-Fi missing')
ok('ApkInstaller.downloadAndInstall' in auto_job, 'background auto installer missing')
ok('applyApnWithRetry' in auto_job, 'APN retry missing')
ok('nextAutoInstallRound' in auto_job and 'round < 4' in auto_job,
   'automatic app retry rounds missing')
ok('isPackageInstalled' in auto_job, 'installed package skip missing')
ok('getAutoInstallRound' in main and '>= 4' in main, 'managed-screen retry resume missing')
ok('PERMISSION_GRANT_STATE_GRANTED' in install_result and 'com.longkaca.ocr' in install_result,
   'Long OCR automatic camera permission grant missing')
apn=(root/'app/src/main/java/com/longkaca/dpc/ApnAdmin.java').read_text()
ok('addOverrideApn' in apn and 'setOverrideApnsEnabled' in apn, 'override APN support missing')
ok('plus.4g' in apn and 'line.me' in apn, 'APN profiles missing')

ocr_manifest=(root/'longocr/src/main/AndroidManifest.xml').read_text()
ocr_main=(root/'longocr/src/main/java/com/longkaca/ocr/MainActivity.java').read_text()
ocr_extractor=(root/'longocr/src/main/java/com/longkaca/ocr/EmailExtractor.java').read_text()
ok('android.permission.CAMERA' in ocr_manifest, 'Long OCR camera permission missing')
ok('TextRecognition.getClient' in ocr_main, 'ML Kit OCR client missing')
ok('ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY' in ocr_main, 'Long OCR quality capture missing')
ok('CHỤP VÀ QUÉT' in ocr_main, 'Long OCR capture button missing')
ok('startFocusAndMetering' in ocr_main, 'Long OCR tap-to-focus missing')
ok("versionName '1.4-wrapped-us-email'" in (root/'longocr/build.gradle').read_text(), 'Long OCR version mismatch')
ok('getBoundingBox' in ocr_main and 'thenComparingInt' in ocr_main,
   'Long OCR coordinate line sorting missing')
ok('usEmailsFromLines' in ocr_main and 'usEmailsFromLines' in ocr_extractor,
   'Long OCR wrapped .us email extraction missing')
ok('Đã ghép và nhận " + rowCount' in ocr_main, 'Long OCR selectable .us rows missing')
ok('SAO CHÉP' in ocr_main, 'Long OCR copy button missing')
ok('ClipboardManager' in ocr_main, 'Long OCR clipboard support missing')
ok('Pattern.compile' in ocr_extractor, 'Long OCR email extractor missing')
ok('allEmails' in ocr_extractor and 'ĐÃ COPY' in ocr_main,
   'Long OCR stable email list/copy state missing')

swipe_gradle=(root/'longswipe/build.gradle').read_text()
swipe_manifest=(root/'longswipe/src/main/AndroidManifest.xml').read_text()
swipe_service=(root/'longswipe/src/main/java/com/longkaca/autoswipe/SwipeAccessibilityService.java').read_text()
swipe_main=(root/'longswipe/src/main/java/com/longkaca/autoswipe/MainActivity.java').read_text()
swipe_config=(root/'longswipe/src/main/res/xml/accessibility_service_config.xml').read_text()
ok("applicationId 'com.longkaca.autoswipe'" in swipe_gradle, 'Long Auto Swipe package mismatch')
ok('BIND_ACCESSIBILITY_SERVICE' in swipe_manifest, 'Long Auto Swipe accessibility permission missing')
ok('canPerformGestures="true"' in swipe_config, 'Long Auto Swipe gesture capability missing')
ok('canRetrieveWindowContent="true"' in swipe_config and 'flagRetrieveInteractiveWindows' in swipe_config,
   'Long Auto Swipe foreground-window access missing')
ok('com.ss.android.ugc.tiktok.lite' in swipe_config and 'TIKTOK_LITE' in swipe_service,
   'Long Auto Swipe must be restricted to TikTok Lite')
ok('10_000L' in swipe_service and 'nextInt(5_001)' in swipe_service,
   'Long Auto Swipe random 10–15 second timing missing')
ok('dispatchGesture' in swipe_service and 'path.lineTo' in swipe_service,
   'Long Auto Swipe upward gesture missing')
ok('onAccessibilityEvent' in swipe_service and 'lastTikTokEventAt' in swipe_service,
   'Long Auto Swipe event-based TikTok detection missing')
ok('ACTION_ACCESSIBILITY_SETTINGS' in swipe_main, 'Accessibility settings button missing')
ok('<queries>' in swipe_manifest and 'com.ss.android.ugc.tiktok.lite' in swipe_manifest,
   'Android 11 package visibility declaration missing')
ok('android.permission.INTERNET' not in swipe_manifest, 'Long Auto Swipe must not request Internet')

if errors:
    print('FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('PASS static project checks')
print(f'Java files: {len(java)}')
print('Package: com.longkaca.dpc')
print('Version: 2.8-long-auto-swipe (19)')
print('Split APK/APKS installer: present')
print('Persistent mother defaults: present')
print('Background auto-install JobService: present')
