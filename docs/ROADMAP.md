# MOSA Roadmap

## Release 1.1.0 — Forensic workflow foundation
- WhatsApp TXT export parsing and local summary.
- SHA-256 of selected text source.
- Device triage for damaged, powered-off, or locked devices.
- Case reference and authorization acknowledgement on reports.

## Release 1.2.0 — ZIP intake and evidence event log
- Support ZIP archives produced by official WhatsApp chat export.
- Parse the chat TXT file inside the selected archive.
- Inventory supported image, video, audio, and document entries without extracting them to shared storage.
- Calculate SHA-256 for the archive and each supported media item.
- Add safe limits for archive size, entry count, individual item size, and total decompressed content.
- Add a local event log for case reference, action, filename, timestamp, and source hash.
- Build and verify the Android debug APK.

## Release 1.3 — Structured evidence management
- Replace the prototype preferences log with a structured encrypted case database.
- Add examiner identity, acquisition method, source device details, and authorization record.
- Add a signed, append-only evidence event ledger with integrity verification.
- Add JSON/CSV evidence manifests and duplicate-file detection.
- Add synthetic parser fixtures and automated regression tests.

## Release 1.4 — Media and export expansion
- Parse metadata available in selected media files without altering originals.
- Add optional OCR on examiner-selected images, with explicit review of extracted text.
- Correlate media filenames with export references when the format supports it.
- Add parser confidence, locale detection, and format warnings.

## Release 1.5 — Laboratory integration
- Import read-only reports and forensic images produced by authorized acquisition tools.
- Validate manifests and hashes before analysis.
- Keep acquisition separate from analysis; MOSA does not claim to unlock every device.
- Add examiner review checkpoints and reproducible analysis summaries.

## Release 2.x — Authorized security assessment
- Lab-only phishing and device-pairing simulations with test accounts.
- Mobile configuration and patch-level assessment.
- Detection tests for suspicious links and unauthorized device-linking workflows.
- Zero-click exposure assessment based on known, verifiable advisories and device/app versions; no weaponized exploit delivery.
