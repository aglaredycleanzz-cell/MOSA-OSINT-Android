# MOSA Government Edition — Product Scope

## Product objective
MOSA is an Android-first digital-forensics intake, analysis, and authorized security-assessment workspace. The initial government-facing build is a prototype, not a certified forensic acquisition suite.

## Operating principles
- Written authority, defined case scope, examiner identity, and evidence provenance are mandatory project requirements.
- No covert installation, persistence, credential harvesting, unauthorized account linking, or extraction from a person's account without lawful authority.
- No claims of universal phone unlocking, WhatsApp decryption, deleted-message recovery, or zero-click capability.
- Evidence handling must preserve originals, record hashes, and distinguish source evidence from derived analysis.
- Where a source is user-selected, use Android's Storage Access Framework and least-privilege access.

## Initial modules
1. **WhatsApp export analysis** — local analysis of a user-selected WhatsApp TXT export; message counts, parsed sender labels, date range, URL and attachment-marker counts, and SHA-256 of the source file. Raw conversation text is not retained in the app report.
2. **Device triage** — examiner-entered observations about make/model, visible state, damage, and lock state. It does not attempt to unlock or modify the device.
3. **Case reporting** — local report generation with case reference and explicit technical limitations.
4. **OSINT utilities** — retained from the 1.0 prototype.

## Planned research tracks
- Validated parsers for multiple WhatsApp export formats, with regression fixtures containing synthetic data.
- ZIP export intake and user-selected media inventory, preserving source hashes.
- Evidence manifest, chain-of-custody events, examiner and authorization records.
- Read-only ingest of forensic images and exports produced by authorized, established acquisition tools.
- Android device health and compatibility triage for a laboratory workflow.
- Red-team assessment in an isolated lab using test accounts and devices owned or explicitly controlled by the organization.
- Detection engineering for phishing, malicious pairing attempts, and mobile exploit exposure.

## Out of scope
- A link or payload that takes over WhatsApp accounts.
- Silent collection of private messages, contacts, SMS, microphone, camera, or location.
- Bypassing screen locks, secure hardware, encryption, or platform access controls on third-party devices.
- Exploit development or deployment against real targets.
- Claims that a checkbox alone establishes legal authority.

## Prototype disclaimer
The current Android prototype is a field-workflow and analysis proof of concept. It must undergo security review, parser validation, evidence-handling review, and legal review before operational government use.
