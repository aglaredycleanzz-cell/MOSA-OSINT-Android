# MOSA Digital Forensics & Intelligence — Android

MOSA is an Android-native prototype built with Kotlin and Jetpack Compose. Version 1.3.0 adds user-initiated Android Sharesheet intake for authorized WhatsApp TXT/ZIP exports.

## Version 1.3.0 modules

- **Share to MOSA:** choose MOSA from Android's share menu after exporting a chat in WhatsApp. The app opens the WhatsApp analysis module and displays the received filename.
- **Explicit review before analysis:** a shared file is not analyzed automatically. The examiner must enter a case reference, acknowledge authorization, and tap the analyze button.
- **Existing local analysis:** the same bounded TXT/ZIP parser, media inventory, source SHA-256, and media hashes are used for shared files and manually selected files.

## Version 1.2.0 modules

- **WhatsApp TXT export analysis:** locally counts timestamped messages, sender labels, attachment markers, and URLs in a selected TXT export.
- **WhatsApp ZIP export intake:** reads the TXT conversation inside a selected ZIP and inventories supported media entries (images, video, audio, and documents).
- **Evidence hashes:** calculates SHA-256 for the original selected file and each inventoried media entry. Media is streamed for hashing, not extracted to shared storage.
- **Archive safety limits:** source archive up to 200 MB; up to 2,500 entries; text export up to 20 MB; individual entries up to 64 MB; total uncompressed content up to 400 MB.
- **Device triage:** records examiner-entered observations about a device's model, Android version, power state, screen, visible lock state, and physical damage.
- **Local event log:** records the timestamp, case reference, action, filename, and available source hash when an examiner adds a WhatsApp analysis or triage record.
- **OSINT utilities:** phone-format validation, public username search links, domain information links, URL heuristic checks, business search, and a personal privacy checklist.

## Evidence handling

- The original source file is not modified.
- Selected files are read locally through Android's Storage Access Framework.
- Media files are not extracted or copied to a shared folder in this release.
- The report contains derived counts, media inventory labels, source hash, and media hashes; it does not retain raw chat text.
- The local event log uses app preferences. It is **not tamper-evident**, not digitally signed, and is not sufficient by itself as a legally defensible chain-of-custody system.

## Share intake workflow

1. In WhatsApp, open the relevant chat and use the app's own Export chat function.
2. Choose whether to include media, then select Android Share and choose MOSA.
3. MOSA shows the received file name. Enter the case reference, confirm that the examination is authorized, and tap the explicit analysis button.
4. Review the local summary and save the report to the app's report/event log.

MOSA receives only the file URI explicitly shared by the user. It does not monitor WhatsApp, query WhatsApp accounts, read private app storage, or run in the background to collect chats. Shared content remains subject to the same local file-size and ZIP-entry safety limits as manual import.

## WhatsApp analysis limitations

- MOSA does **not** read WhatsApp's private application database directly.
- MOSA does **not** bypass a device lock, decrypt an account, capture authentication codes, take over accounts, or recover deleted messages.
- The parser supports common WhatsApp TXT export layouts; locale-specific or unusual date formats may not parse.
- ZIP intake requires a TXT chat file inside the archive. It does not infer missing messages from media alone.
- Media inventory is based on file extensions in the archive. It does not identify the truth or context of an image, video, or document.

## Device triage limitations

Device triage is a manual observation form. It does not unlock, repair, root, flash, or modify the device. It is not a substitute for a validated forensic acquisition workstation.

## Privacy and security

- No Android network permission is requested by the app.
- Source files are selected through Android's Storage Access Framework.
- No account enumeration, covert collection, hidden-status lookup, credential collection, or unauthorized account linking is performed.

## Build

GitHub Actions builds the debug APK on pushes to `main` and on manual dispatch. Download the `MOSA-OSINT-debug-apk` artifact from the Actions run.

Toolchain: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Android SDK 35.

## Government-readiness status

This is a prototype, not a certified forensic tool. Operational use requires independent security testing, parser validation against synthetic fixtures, protected and signed evidence logging, documented chain-of-custody procedures, legal review, and validation against the target organization's evidence-handling standards.

See:
- [Government Edition scope](docs/GOVERNMENT_EDITION_SCOPE.md)
- [Product roadmap](docs/ROADMAP.md)
- [Authorized WhatsApp lab](docs/AUTHORIZED_WHATSAPP_LAB.md)
