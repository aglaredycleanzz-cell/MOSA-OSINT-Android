# MOSA Roadmap

## Release 1.1.0 — Forensic workflow foundation
- Add WhatsApp TXT export analysis using local processing only.
- Calculate and display SHA-256 for the selected source file.
- Add an initial device triage record for damaged, powered-off, or locked devices.
- Attach a case reference and an examiner authorization acknowledgement to generated reports.
- Preserve the existing OSINT modules.
- Build and verify the Android debug APK in GitHub Actions.

## Release 1.2 — Evidence handling
- Add a structured case record and event log.
- Add evidence manifest with original filename, size, media type, acquisition method, SHA-256, examiner, timestamp, and notes.
- Add export-to-JSON/CSV for derived metadata.
- Add duplicate-file detection using content hashes.
- Add synthetic test fixtures for WhatsApp parser regression tests.

## Release 1.3 — WhatsApp export expansion
- Support ZIP archives produced by the official chat-export flow.
- Inventory selected images, videos, documents, and audio files without trying to reconstruct unavailable messages.
- Extract available media metadata and calculate hashes.
- Link media inventory entries to export filenames and timestamps when the export format supports it.
- Add parser confidence and format warnings.

## Release 1.4 — Laboratory integration
- Import read-only reports and forensic images from authorized acquisition tools.
- Validate manifests and hashes before analysis.
- Keep acquisition separate from analysis; MOSA does not claim to unlock every device.
- Add examiner notes, review checkpoints, and reproducible analysis summaries.

## Release 2.x — Authorized security assessment
- Lab-only phishing and device-pairing simulations with test accounts.
- Mobile configuration and patch-level assessment.
- Detection tests for suspicious links and unauthorized device-linking workflows.
- Zero-click exposure assessment based on known, verifiable advisories and device/app versions; no weaponized exploit delivery.
