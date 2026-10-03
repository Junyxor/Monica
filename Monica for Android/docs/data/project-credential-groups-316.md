# Project credential groups (Android 1.0.316)

Status: implemented in the ordinary and F-Droid Android applications. This is an additive contract; no Room schema migration is required. It does not turn independent bulk creation into one project.

## Storage contract

Every password remains a real `PasswordEntry` row. Its `username`, encrypted `password`, and encrypted `authenticatorKey` belong to that account. A group with two passwords has two rows with the same username and OTP payload. Use the existing encryption and native backend writers; never put passwords or OTP seeds into custom-field JSON.

Rows in one project have the same explicit `passwordGroupId`. Grouping is scoped to the database: local, a particular MDBX database, a particular KeePass database, or a particular Bitwarden vault. Similar title, website, or username is never sufficient to merge projects. Copies into two folders of one database have distinct project identities.

Each row carries exactly one custom field named `monica.content.credential`, with a JSON object:

```json
{
  "version": 1,
  "projectId": "a1f087f7-c030-4458-a844-99dce91513ea",
  "groupId": "b1729a60-92c4-462b-bd17-faa3dfc9810b",
  "passwordId": "c62ecc7d-34bb-490e-99d9-7eca978d8db4",
  "label": "Work",
  "primary": false,
  "groupOrder": 1,
  "passwordOrder": 0
}
```

`projectId`, `groupId` and `passwordId` are canonical UUID strings. `groupId` identifies the account, `passwordId` identifies one password within it. Orders are zero-based non-negative integers. The first group is primary. IDs survive edits and ordering changes. The JSON contains only grouping metadata, never passwords or OTP seeds. Preserve unknown JSON members exactly when rewriting known fields.

The reader accepts older version-1 metadata without `projectId`; new writes include it. Bitwarden download restores `passwordGroupId` from this metadata so a fresh install can reconstruct the same project. Preserve the full custom-field transport, including its existing chunking and encryption mechanisms.

The first password of the primary group owns common custom fields and attachments. If it is removed, transfer those assets to the new owner before deleting the previous row. Secondary rows can have legacy per-row fields; preserve them. A failure to copy an attachment must retain the source. The detail page resolves common content through the primary owner even when opened by a secondary password row.

## Reading, editing and filling

1. Find siblings by explicit project identity within the selected database.
2. Read grouping metadata and decrypt secrets through that backend's existing path.
3. Partition by `groupId`, then sort by `groupOrder` and `passwordOrder`.
4. Reject ambiguous duplicate password IDs, unsupported metadata, or conflicting usernames/OTP values within an account before editing. Preserve the stored rows for recovery instead of silently normalizing them.
5. Render each account as its own username/password block, with its own OTP field and preview. Adding or deleting account groups does not change the independent bulk-creation command.
6. Autofill candidates reference actual row IDs. Fill the selected row's username, password and OTP; never fall back to the primary account after a stale selection. Revalidate availability and unlock state on use. Keyboard fills reread the selected row without decrypting the whole vault.

## Backup and compatibility

Monica full backups retain all real password rows, explicit project IDs, custom fields and attachment ownership. MDBX and KeePass keep each password in their native entry representation. Bitwarden uses one cipher per password; metadata reconstructs the project in Monica. Other clients may display these ciphers separately. Clients that discard the metadata cannot preserve this grouped presentation.

Older Monica versions still have the actual password rows, but do not implement the new account-group editor. Do not promise that editing a multi-account project in an older client preserves its account boundaries. Upgrading requires no bulk rewrite of existing unrelated projects.

## Verification

See `ProjectCredentialGroupTest`, `ProjectCredentialStorageTest`, `ProjectCredentialEditorTest`, and `MonicaImeSystemTest`. Fixtures are synthetic and scoped; they do not clear the shared emulator. Coverage includes separate account OTPs, edit from a secondary row, stable password IDs, encrypted full-backup round trips, MDBX/KeePass native rereads, Bitwarden encrypted upload/fresh download, failed-destination rollback, multiple folder copies and shared attachment bytes after replacing the primary password and moving to MDBX.

Bitwarden transport testing uses a local protocol fixture, not an external user's server. These tests do not establish compatibility with every third-party client or every Android form.

## Android app-to-app export

The normal distribution exposes Google's credential-exchange entry. F-Droid intentionally has no Google Provider Events entry; keep that packaging distinction.

For an importing app requesting `basic-auth`, export **one CXF Item per real password row**, with exactly one basic-auth credential. Never flatten multiple usernames/passwords into one credential or collapse equal usernames. A receiver may display these as separate entries and cannot be expected to recreate Monica's project presentation.

If the receiver also requests `totp`, include that row's embedded standard TOTP in the same Item (CXF 1.0 §3.3.16: Base32 secret, lowercase sha1/sha256/sha512, numeric period/digits, optional username/issuer). Read native OTP payloads from MDBX/KeePass and decrypt Room-backed credentials. Never convert HOTP/Steam/MOTP or unsupported algorithms into TOTP. Show an exclusion count before confirmation when linked OTP cannot be sent, including receivers that do not request TOTP. Export never deletes or rewrites source records.

This is password-associated TOTP **export** support. The app's registered/import-request capabilities remain basic-auth/passkey; standalone authenticator export/import, arbitrary attachments, and all Monica custom content are not claimed as CXF capabilities. Use Monica's full backup for complete application data. Sharing no unsupported type when the receiver did not request it is deliberate.

Manual picker and platform suggestions use the same per-password ordinal for otherwise identical account rows. Display labels do not modify the stored username or any value passed to the target form.

New native MDBX password writes set `monica_password_encoding: "plaintext-v1"`. This marks `password_plain` as authoritative user content, even when its text resembles a Monica encryption envelope. Preserve this marker when editing native JSON. Legacy native repair skips marked values; import batches receive freshly normalized single-layer encrypted Room values. This optional payload member does not change the Room schema or MDBX engine format.
