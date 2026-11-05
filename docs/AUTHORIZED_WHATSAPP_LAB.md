# Authorized WhatsApp Security Lab

## Purpose
Test an organization's controls against common account-takeover and mobile-messaging threats using devices and accounts owned by, or explicitly controlled by, the organization.

## Safe test scenarios
- Phishing-awareness simulation using a benign landing page that records only synthetic test events.
- Device-linking awareness simulation that never requests or captures a real WhatsApp pairing code.
- Review of account security settings and linked-device inventory by the account owner or authorized administrator.
- Mobile patch and configuration checks.
- Triage of user-submitted suspicious URLs without visiting or detonating them.

## Required controls
- Written scope, dates, target test accounts/devices, responsible owner, and stop conditions.
- Synthetic credentials and test data only.
- No collection of real authentication codes, session tokens, passwords, or private message content.
- No exploit payloads against production devices.
- Findings are reproducible, time-stamped, and linked to evidence hashes.
