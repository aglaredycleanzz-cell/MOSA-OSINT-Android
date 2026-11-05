# WhatsApp Export Intake — Share to MOSA

## Purpose

MOSA accepts a chat export that a user or examiner explicitly selects and shares through Android's Sharesheet. This is an evidence-analysis intake path, not direct access to a WhatsApp account.

## Procedure

1. In WhatsApp, open the specific chat that is within the authorized examination scope.
2. Open the chat menu and select **More → Export chat**.
3. Choose whether to include media, according to the case scope and evidence-handling instructions.
4. In Android's share sheet, select **MOSA Digital Forensics**. If it is not immediately visible, open the full app list.
5. MOSA opens its WhatsApp Forensics module and displays the received filename.
6. Enter the case reference, acknowledge that a valid authorization exists, and tap **Analyze shared file**.
7. Review the summary and save the resulting report to the app's local report/event log.

## Accepted input

- WhatsApp plain-text chat export (.txt).
- WhatsApp export archive (.zip) containing a chat text file and optional media entries.

The existing parser and archive limits apply. A shared file is not processed automatically. If the share provider supplies a format MOSA cannot parse, the app displays an error and the examiner can use the normal file picker to select a compatible export.

## Handling notes

- MOSA receives a URI explicitly supplied by Android's share intent. It does not poll, monitor, or read WhatsApp's private app storage.
- The app does not request network access for this workflow.
- Source message text is parsed in memory and is not retained in the generated summary.
- SHA-256 is calculated for the source file and supported media entries; a hash verifies byte-level identity, not authenticity or authorship.
- The local event log in this prototype is modifiable and is not a tamper-evident or legally sufficient chain-of-custody system.
- An authorization checkbox is an examiner acknowledgement, not independent proof of legal authority. Follow the organization's evidence policy and applicable law.

## Scope boundary

This workflow does not unlock devices, bypass encryption, recover deleted messages, extract WhatsApp private databases, capture credentials, link an account covertly, or obtain chats from a phone number alone.
