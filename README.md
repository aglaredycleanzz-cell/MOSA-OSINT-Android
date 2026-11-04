# MOSA OSINT — Android Native

MOSA OSINT is a native Android application built with Kotlin and Jetpack Compose.

## Version 1.0.0 modules

- **Phone format check:** local E.164 syntax validation only. It does not identify the owner or check WhatsApp registration.
- **Public username research:** creates user-initiated search links to public web results. It does not scrape platforms or access private profiles.
- **Domain analysis:** local hostname normalization plus links to public RDAP and certificate-transparency records.
- **URL risk indicators:** local heuristic checks for scheme, user-info, punycode, IP literals, shortened URLs, length, and unusual ports. It does not fetch or open the submitted URL.
- **Business research:** user-initiated web and maps searches. Results are not legal proof of business registration.
- **Privacy audit:** locally saved self-checklist for the user's own WhatsApp/account security settings.
- **Reports:** latest analysis result is stored locally and can be copied/shared.

## Privacy and security

- No WhatsApp private-data access, scraping, account enumeration, hidden-status lookup, device fingerprinting, or privacy bypass.
- No app network permission is requested in this version. Web searches open in the user's browser only when tapped.
- Search links are created locally. Reports and checklist state stay on the device's app preferences.

## Build

GitHub Actions builds the debug APK on pushes to `main` and on manual dispatch. Download the `MOSA-OSINT-debug-apk` artifact from the Actions run.

Toolchain: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Android SDK 35.
