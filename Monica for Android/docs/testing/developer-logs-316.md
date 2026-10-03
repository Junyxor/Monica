# Developer settings and diagnostic logs — 1.0.316

## Scope

Normal and F-Droid editions share a compact grouped developer settings page and a standalone log route. Existing developer switch callbacks are retained. No database schema, encryption, backup, authentication policy or release version change is part of this task.

## Design

Editable local M3E project: `../design/developer-logs-316/canvas.json`. Local share URL: `../design/developer-logs-316/canvas-url.txt`. The implementation reuses SettingsPanelGroup/SettingsPanelRow and SettingsSubpageTopBar. Log controls use actual text fields, chips, menus and expandable event cards.

## Data handling

The complete report is preserved independently from the currently filtered list. Source selection no longer chooses a single winner over other diagnostic sources. Multiline exception stacks are searchable and copied as an event. The preview caps text layout at 12,000 characters; complete event text stays available to copy and the full file export. Clipboard copies use the existing sensitive-data clipboard helper. Export uses the existing FileProvider, read grant and bounded share-text fallback.

Refresh and clearing are serialized by a lifecycle ViewModel; collection runs on IO and search on Default. Rotation preserves the snapshot and saved filters. Log clearing requires an explicit dialog; UI tests use an injected callback and never clear shared AVD logs or vaults. Existing environment diagnostics may regenerate after clear.

## Validation — completed 2026-10-03

| Check | Normal | F-Droid |
| --- | --- | --- |
| Production and test APK build | Passed (main-build4) | Passed (fdroid-build3) |
| Parser, source overlap, sharing and developer verification JVM regression | 15/15 | 15/15 |
| API32 device suite, 320dp viewport | 8/8 | 8/8 |
| 14 resource configurations and shared source parity | Passed | Passed |
| Original user and settings restored by runner | Verified | Verified |

Device checks cover whole-stack search and copy; source/severity combinations and honest empty results; clear cancellation and confirmation through an injected callback; loading/error actions; saved filter restoration with dark 150% text; actual settings-to-logs navigation and return; real persisted/system source collection and FileProvider content/grants; Chinese settings/log rendering. Real shares were constructed and read locally, never sent externally. The test does not clear any real log buffers. The full report was compared in full after opening the shared URI. Large clipboard payloads are rejected with a share-file alternative, and clipboard exceptions are handled.

Screenshots are in `../design/developer-logs-316/main/` and `fdroid/`; `native-preview.png` shows Chinese settings, Chinese logs and large dark text. At large fonts on narrow screens, footer buttons stack to avoid cramped wrapping. Canvas project and screenshots remain available alongside the native previews.

Build, device, identity/hash and restoration records are at workspace root `.codex-tasks/20261003-developer-logs/`. Final device runs: `Main-emulator-5554-final.log` and `Fdroid-emulator-5554-verified.log`. Scoped JVM XML is copied into `raw/main-unit/` and `raw/fdroid-unit/`.

Earlier failed attempts remain recorded: screenshot test dependency fixed; initial sleeping emulator prevented six UI cases from mounting; a language-only test context needed the existing ActivityResultRegistryOwner; one intermediate build saw tests ahead of the production source snapshot and was rerun. These failures were resolved before the final runs. Screenshot capture now waits for the compositor rather than accepting a blank frame.

This is focused diagnostic UI validation on the shared Android32 emulator, including saved-state restoration, not a claim of testing every ROM. The existing AVD and app data were retained. No commit, push, release or APK delivery was requested or performed.
