# APK signing

All release builds use one stable signing key so updates can be installed over an existing
installation without uninstalling it or losing app data.

## Key

- Keystore: `afnon-release.jks` (kept outside this repository)
- Alias: `afnon`
- Certificate SHA-256: `9F:92:9B:41:0A:B4:6F:F0:9F:47:82:22:11:87:15:E1:A3:A6:E9:25:69:ED:D1:51:72:95:56:CC:0E:27:AD:B1`
- Validity: 10,000 days

Keep an encrypted backup of the keystore and its passwords. If the key is lost, the app can
only be updated by uninstalling it first.

## GitHub Actions secrets

The workflow reads these repository secrets:

- `AFNON_KEYSTORE_BASE64` — base64 of `afnon-release.jks`
- `AFNON_KEYSTORE_PASSWORD` — keystore password
- `AFNON_KEY_ALIAS` — `afnon`
- `AFNON_KEY_PASSWORD` — key password

The workflow writes a temporary `keystore.properties` and deletes the key with the runner.

## Local builds

Create a git-ignored `keystore.properties` in the repository root:

```properties
storeFile=/absolute/path/to/afnon-release.jks
storePassword=...
keyAlias=afnon
keyPassword=...
```

Then build with `./gradlew :app:assembleRelease`. Both debug and release builds use the stable
key when this file is present; otherwise Gradle falls back to its normal debug key.

## Full audio build

The audio files are intentionally not committed. To include adhan and announcement audio in a
CI build, run the workflow manually with `include_audio` enabled:

```bash
gh workflow run android-build.yml \
  -R app87854/afnon-android-tv-launcher \
  --ref manus/android-build \
  -f include_audio=true
```

## One-time migration

The previously installed APK was signed with a temporary CI debug key. Switching to this stable
key requires one uninstall/reinstall. Back up app data first, then restore it after installing.
After that, normal updates use the same key and can be installed with `adb install -r`.
