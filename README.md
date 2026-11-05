# MOSA Digital Forensics & Intelligence — Android

MOSA is an Android-native prototype built with Kotlin and Jetpack Compose. Release 1.1.0 adds an authorized digital-forensics workflow foundation while retaining the earlier OSINT utilities.

## Release 1.1.0 modules

- **WhatsApp TXT export analysis:** imports a user-selected text export through Android's document picker, then locally counts timestamped messages, sender labels, attachment markers, and URLs.
- **Evidence integrity:** calculates SHA-256 for the selected export file. The report records the case reference, source filename, file size, summary counts, and hash.
- **Device triage:** records examiner-entered observations about a seized device's model, Android version, power state, screen, visible lock state, and physical damage.
- **Local reports:** stores the derived summary locally for copying or sharing.
- **OSINT utilities:** phone-format validation, public username search links, domain information links, URL heuristic checks, business search, and personal privacy checklist.

## WhatsApp analysis limitations

- MOSA does **not** read WhatsApp's private application database directly.
- MOSA does **not** bypass a device lock, decrypt an account, capture authentication codes, or recover deleted messages.
- The current parser supports common WhatsApp TXT export layouts only; unusual locale/date formats may not parse correctly.
- Only summary metadata is put in the report. Raw conversation text is processed locally and is not retained by the application.
- Attachment count is based on export placeholders; this release does not extract or analyze media files.

## Device triage limitations

Device triage is a manual observation form. It does not unlock, repair, root, flash, or modify the device. It is not a substitute for a validated forensic acquisition workstation.

## Privacy and security

- No Android network permission is requested by the app.
- Source files are selected through Android's Storage Access Framework.
- The selected file is read locally, with a 20 MB size limit in this release.
- No account enumeration, covert collection, hidden-status lookup, credential collection, or unauthorized account linking is performed.

## Build

GitHub Actions builds the debug APK on pushes to `main` and on manual dispatch. Download the `MOSA-OSINT-debug-apk` artifact from the Actions run.

Toolchain: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Android SDK 35.

## Government-readiness status

This is a prototype, not a certified forensic tool. Operational use requires independent security testing, parser validation against synthetic fixtures, documented chain-of-custody procedures, legal review, and validation against the target organization's evidence-handling standards.

See:
- [Government Edition scope](docs/GOVERNMENT_EDITION_SCOPE.md)
- [Product roadmap](docs/ROADMAP.md)
- [Authorized WhatsApp lab](docs/AUTHORIZED_WHATSAPP_LAB.md)
