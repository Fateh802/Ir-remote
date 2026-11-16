# Custom TV Remote

A small offline Android IR remote UI modeled on the uploaded physical remote.

## Current capabilities
- Full button layout matching the photographed remote.
- Android `ConsumerIrManager` hardware check and IR transmit.
- NEC 32-bit code encoder (38 kHz).
- Raw alternating mark/space pattern support with configurable carrier frequency.
- Long-press any key to configure or clear its code.
- Import/export profile JSON; profiles are stored locally.
- Unconfigured buttons intentionally do not transmit.

## Important limitation
The uploaded OnePlus IR Remote APK contains a generic Kookong database with many devices. The APK itself does not identify which remote profile the user selected for their assembled TV. This project does not copy or redistribute that proprietary database, and it does not invent TV codes. The matching profile or codes must be transferred/identified before all keys can control the TV.

## Build
Requires Android SDK Platform 35, Build Tools 35, JDK 17, and Gradle 8.9+ (AGP 8.7.3).

```bash
gradle assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

A GitHub Actions workflow is included at `.github/workflows/build-apk.yml` to build the APK and publish it as an artifact.

## Profile JSON format
```json
{
  "format": "custom-tv-remote-v1",
  "buttons": {
    "POWER": { "nec": "20DF10EF", "frequency": 38000 },
    "VOL_UP": { "raw": "9000,4500,560,560,560,1690,560,560", "frequency": 38000 }
  }
}
```
`nec` is a 32-bit NEC hex value. `raw` is a comma- or whitespace-separated list of alternating mark/space durations in microseconds. Raw profiles should be obtained from a known-working code source, not guessed.
