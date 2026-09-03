# Build LongDPC v2.4 + Long OCR 1.3 on phone / Codespaces

From `/workspaces/LongDPC` after uploading the patch ZIP:

```bash
git pull
unzip -o LongDPC_v2.0_LONG_OCR_PATCH.zip
rm LongDPC_v2.0_LONG_OCR_PATCH.zip
python3 tools_static_check.py
grep -n "versionCode\\|versionName" app/build.gradle
git add -A
git commit -m "LongDPC v2.0 with offline Long OCR"
git push origin main
```

Expected version:
- `versionCode 9`
- `versionName '1.8-split-persist'`
