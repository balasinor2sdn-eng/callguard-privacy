# Baby Alert 👶

A one-button Android app: when the baby wakes up, tap **BABY IS AWAKE** and the
other phone gets a loud notification instantly, no matter how far away it is.

Notifications are delivered by [ntfy.sh](https://ntfy.sh), which is free and needs no account.

## Setup (5 minutes, once)

### Wife's phone: the button
1. Open the repository's **Releases** page on GitHub, open **Baby Alert (latest)** and download `BabyAlert.apk`.
2. Open the downloaded file. If Android asks, allow "Install unknown apps" for your browser/files app.
3. Open **Baby Alert**, tap **⚙ Setup** → **Share**, and send the message to your husband (WhatsApp etc.).

### Husband's phone: the receiver
1. Install the free **ntfy** app from the Play Store.
2. Open it, tap **+**, type the topic name from the shared message, tap **Subscribe**.
3. Recommended: in Android settings, allow ntfy to run without battery restrictions
   (Settings → Apps → ntfy → Battery → Unrestricted) so alerts are never delayed.

### Test
Tap the big button on the wife's phone. The husband's phone should ring within a few seconds
with **"Baby is awake 👶"**.

## Notes
- The topic name (like `baby-k3x9p2m7qa`) is randomly generated and works like a password:
  anyone who knows it can send or see alerts, so only share it with each other.
- To install an updated version you may have to uninstall the old one first (each build is
  signed with a new debug key). Then re-enter the same topic in **⚙ Setup**.

## Building
GitHub Actions builds the APK automatically on every push to `baby-alert/`
(`.github/workflows/baby-alert-apk.yml`). Locally, with the Android SDK installed:
`./gradlew assembleDebug`.
