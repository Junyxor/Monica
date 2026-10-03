# MDBX native manager and snapshot layout — 1.0.316

## Scope

- Added an MDBX2 original-information entry in database management. Folder creation, rename and move reuse existing native repository transactions. Object rename checks the original identity, collection, schema and revision before changing the title.
- Summary-only list browsing; raw payload disclosure occurs only after selecting an object. Raw fields remain read-only and individually hidden until revealed. Leaving the foreground or locking clears loaded content. No arbitrary JSON editing, bulk deletion or database migration was added.
- Current content and read-only snapshots share `MdbxFolderBrowser`, grouped settings shapes, breadcrumbs, localized type names, title sorting and search. Each landscape pane owns its path and lazy list state. Snapshot restore/delete confirmations are unchanged.
- The new strings cover all 16 existing language configurations. F-Droid retains its separate cloud integration restrictions.

## Verification

Both editions passed the final checks:

| Edition | JVM checks | API 32 device checks | Build |
| --- | ---: | ---: | --- |
| Main | 9 / 9 | 10 / 10 | Production and instrumentation APKs passed |
| F-Droid | 9 / 9 | 10 / 10 | Production and instrumentation APKs passed |

The final device logs and installed APK SHA-256 records are in `.codex-tasks/20261003-mdbx-native-manager/{Main,Fdroid}-emulator-5554-verified.log[.json]`. The prior active user (0), input/autofill settings and screen timeout were restored. The already running shared AVD remains available. No APK was copied to a delivery directory.

The native fixture starts from a CLI-authored vault containing an unknown custom type, API-token schema versions 1 and 7, login schema version 9 and real synthetic attachments. Thirteen additional object categories contain multiple credentials, multiple passwords, OTP data, nested unknown objects, booleans, nulls and a high-precision decimal. All 17 objects are renamed and reopened; object type, schema version, collection, JSON values and attachment bytes must remain unchanged. Stale revisions must be rejected, and repeated browsing must not create commits.

UI checks cover 241-entry folder scrolling, deep breadcrumbs, saved-state restoration, searching a future type, masked raw values, dark mode at 140% font scale, independent landscape scrolling and read-only snapshot details. A real repository-backed manager test creates a folder, renames a login, verifies the refreshed Room projection, then verifies the original native payload.

The existing conflict, snapshot confirmation and history-detail tests are included. Their legacy test-window setup was updated to the supported Compose test rule and an explicit visible test Activity. An ambiguous assertion that matched both summary and full history message was scoped to the technical value.

## Environment and evidence

Shared AVD: `Monica_Issue136_API_32`, API 32, x86_64, `emulator-5554`. Existing synthetic user 10 is used. The runner verifies installed APK hashes and restores the prior user and relevant settings. Only newly created synthetic fixture databases and projections are removed; existing user databases and the AVD data disk are retained.

Editable Canvas: `docs/design/mdbx-native-manager-316/canvas.json`; the saved local sharing URL is `canvas-url.txt`. The same directory contains Canvas renders and native screenshots.

Tests validate these paths on API 32 and packaged native libraries; they do not establish zero risk on every device or malformed third-party database. No database or backup format changes are required by this UI feature.
