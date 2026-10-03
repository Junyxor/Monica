# Unified database manager — 1.0.316

## Scope

- The selected MDBX/KeePass database opens directly from the top-right menus in password lists (including both vault layouts), authenticators, Passkeys, wallet, notes and Steam.
- The root groups Monica local, MDBX2 and KeePass databases. KeePass is the KDBX backend, not a separate fourth format. Each pane independently chooses a database, folder, search, ordering, selection and horizontal/vertical scroll position.
- Long-press and vertical dragging select a range. Navigating the opposite pane preserves the source selection. Copy/move confirms the source and destination, reports progress and gives per-item results. Compact panes use smaller scalable text and tighter line heights. KeePass details reuse the native editor; MDBX/local raw disclosure is read-only and hidden by default.
- Existing snapshot browsing and restore/delete confirmations remain separate from writable database operations. No database-format migration is introduced.

## Safety and format limits

- Native MDBX copies retain schema versions, canonical payloads, labels and attachment bytes. New Monica record identities and password-group identities are mapped independently. Same-vault moves update object/attachment ownership atomically. Native object relations and unsupported identity remapping fail explicitly.
- Native KeePass entry transfer uses the existing lossless transfer path; group copies retain native properties and custom icons. Cross-vault field references are rejected rather than silently rebound.
- Cross-format transfers use the existing export/import codecs, verify destination fields and attachment names/types/hashes, then recheck source state before deletion. Source snapshot caching is revision-keyed. A source update during a batch cannot authorize deletion using an older snapshot.
- MDBX attachment uploads occur only after the native parent commits. KeePass exposes a note body in standard `Notes` while retaining its separate Monica notes in protected `MonicaItemNotes`; older files without that field keep the existing fallback. Attachment MIME inference matches the existing KeePass reconciler.
- Cross-format Passkeys, Steam accounts, native API-token schemas, external references, legacy image paths, nested local categories and unsupported native metadata are not promised to convert. Unknown fields, KeePass history/tags/expiry/custom auto-type or non-roundtrippable secure-item fields cause an explicit failure. Attachments are limited to 64 MiB per item. Failures can leave a partial destination copy; sources are retained until their verification succeeds. A batch may succeed for some items and fail for others.
- No arbitrary JSON editing or bulk destructive reset was added. Background/locked views hide disclosed content and result titles. Unrelated existing working-tree changes are retained.

## Validation status

Completed on 2026-10-03. Both editions built their production and instrumentation APKs and passed the following checks.

| Edition | JVM selection tests | API 32 repository tests | API 32 pane tests | Build |
| --- | ---: | ---: | ---: | --- |
| Main | 4 / 4 | 11 / 11 | 6 / 6 | Passed |
| F-Droid | 4 / 4 | 11 / 11 | 6 / 6 | Passed |

Main repository results are the 11 successful repository cases in `Main-emulator-5554-verified.log`. That run exposed a UI-only long-press release bug: the row click could undo the selection. The fix consumes release during the initial pointer-event pass; the subsequent `Main-emulator-5554-touch.log` passes all six UI cases, including actual cross-database copying. Repository code was unchanged by that final UI fix. F-Droid's final `Fdroid-emulator-5554-verified.log` passes all 17 cases together. JVM XML reports show zero failures, errors or skipped tests. Final builds are `build-main-touch.log` and `build-fdroid-touch.log`.

The final runner JSON records confirm restored user 0, timeout, input-method, accessibility and autofill settings for both editions. Main versionCode remains 12 and F-Droid remains 23. Shared-source/locale parity and CRLF-aware `git diff --check` passed; intentional cloud and credential-exchange differences are retained.

The device suite covers native unknown schemas and high-precision JSON with attachments; independent copied IDs and group IDs; two consecutive folder moves; stale-source deletion protection; local custom-field round trips; note bodies, separate notes and three attachments through local → MDBX → KeePass; native KeePass group properties; source edits between batch items; rejection of unmapped secure-item fields; and native cross-vault KeePass moves. UI tests cover independent vertical/horizontal scrolling, long-press range selection, state restoration, dark landscape at 140% text size, and a real screen selecting left and copying into a right-hand KeePass folder.

Tests use only synthetic fixture records in the existing isolated user 10 of `Monica_Issue136_API_32` (`emulator-5554`, API 32, x86_64). The runner checks installed APK SHA-256 values and restores the previous user, timeout, input methods, accessibility and autofill settings. The already-running shared AVD is retained. Results from the previous single-database manager task are not counted here.

## Design and evidence

- Editable local Canvas: `docs/design/database-manager-316/canvas.json`, sharing URL in `canvas-url.txt`; preview at <http://127.0.0.1:5186/database-manager.html> while the local editor is running.
- Canvas root, portrait dual-pane and wide renders are saved beside the JSON. Device screenshots are copied there after verification.
- MT Manager's official application was inspected on the shared AVD test user as an interaction reference; no official open-source application implementation was found. This implementation reuses Monica's repositories/components, without copying proprietary code.
- Logs, APK hashes, settings restoration records and parity review: `.codex-tasks/20261003-unified-database-manager/` at the workspace root.
- Both editions retain their existing differences. F-Droid receives no OneDrive entry; all 16 existing locale configurations have the manager strings.

These checks establish the tested behavior on API 32, not zero risk on every device or malformed external database. No Git commit, GitHub release or APK delivery is part of this task.
