# AutoTapper

AutoTapper is an Android accessibility helper that watches for Microsoft Authenticator sign-in prompts and helps you confirm them faster. It can automatically launch the authenticator app, surface an approval overlay, and even tap the final **Approve** button after you grant permission.

## What It Does
- Monitors notifications and window changes from Microsoft Authenticator (and related packages) via an AccessibilityService.
- Launches the Authenticator app or fires the pending intent embedded in the notification as soon as a sign-in request appears.
- Displays a lightweight confirmation overlay so you can approve or dismiss the request without dropping out of your current app.
- Attempts to click the Authenticator "Approve" button for you once the approval screen is visible.
- Applies a short cooldown to avoid repeated taps or accidental double approvals.

## How It Works
1. `MyAccessibilityService` listens for notification and window events from Microsoft Authenticator (`com.azure.authenticator`).
2. When a sign-in prompt is detected, AutoTapper tries to launch the Authenticator app immediately; if that fails, it shows a confirmation overlay (`ConfirmationOverlay`).
3. If you approve, the service fires the PendingIntent from the notification and searches the Authenticator UI to tap the **Approve** button automatically.

## Requirements
- Android device or emulator running Android 8.0 (API 26) or higher.
- The Microsoft Authenticator app installed (`com.azure.authenticator`).
- Permissions: Accessibility Service access and "Display over other apps" (SYSTEM_ALERT_WINDOW).
- Android Studio Flamingo / AGP 8.x or higher to build the app.

## Building & Installing
1. Clone or copy this repository to your machine.
2. Open the project in Android Studio, or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```
3. Install the generated APK (`app/build/outputs/apk/debug/app-debug.apk`) on your device.
4. Enable developer options and USB debugging if you plan to install via `adb`.

### Local Environment Setup
- Install a JDK that works with Android Gradle Plugin 8.13.0 (JDK 17+). Point `JAVA_HOME` at that installation before running Gradle, for example:
  - Windows Command Prompt: `set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17"`
  - PowerShell: `$env:JAVA_HOME='C:\Program Files\Java\jdk-17'`
  - macOS/Linux/WSL: `export JAVA_HOME=$HOME/jdks/jdk-17.0.x`
- Make sure the Android SDK is available. If you are outside Android Studio, install the command-line tools and required packages:
  ```bash
  sdkmanager "platform-tools" "platforms;android-34" "build-tools;35.0.0"
  ```
- Gradle reads the SDK path from `local.properties`, which is intentionally untracked. Create or edit it to match your setup:
  - Windows: `sdk.dir=C:\Users\<you>\AppData\Local\Android\Sdk`
  - WSL/Linux/macOS: `sdk.dir=/home/<you>/android-sdk`
- When you change machines, updating `JAVA_HOME` and `local.properties` is usually all that is required; the rest of the project stays portable.

## First-Run Setup
1. Launch AutoTapper and follow the on-screen buttons to:
   - Grant Accessibility Service access.
   - Allow the app to draw overlays.
2. (Optional) Send a test sign-in prompt from Microsoft Authenticator to verify the flow.

## Everyday Use
- When a real sign-in prompt arrives, AutoTapper will log the event and attempt to open Authenticator immediately.
- If the auto-launch fails, you will see the overlay banner asking whether to approve the request.
- Approving triggers the Authenticator PendingIntent, opens the approval screen, and automatically taps **Approve** when it becomes visible.
- A 2-second cooldown prevents repeated taps while the approval screen is changing.

## Risks & Warnings
- **Security**: This app can approve multi-factor authentication (MFA) prompts with very little user interaction. If an attacker gains control of the device (or abuses the overlay), they could approve MFA requests without you noticing. Use only on devices you fully control.
- **Privacy**: Granting Accessibility Service access allows the app to read notifications, observe UI content, and interact with other apps. This is a highly privileged capability—review your organization's policies before enabling it.
- **Overlay Abuse**: SYSTEM_ALERT_WINDOW permission lets the app draw on top of other apps. Malicious overlays could be used to trick users; only install AutoTapper from a trusted source and keep it offline when not needed.
- **Compatibility**: The service is tuned specifically for Microsoft Authenticator UI strings. Layout or text changes from Microsoft could break the automatic tap logic.
- **Logging**: The service writes verbose logs (tag `AutoTapperService`) that could reveal notification contents when USB debugging or logcat collection is enabled.

## Development Notes
- Core logic lives in `app/src/main/java/com/example/autotapper/`.
- Instrumentation and unit tests are under `app/src/androidTest` and `app/src/test`.
- Run the test suite with:
  ```bash
  ./gradlew test connectedAndroidTest
  ```
- Icon assets and overlay layouts are in `app/src/main/res`.

## Troubleshooting
- If the approval overlay never appears, confirm that overlay permission is granted in **Settings → Apps → Special access → Display over other apps**.
- If approvals stop working, disable and re-enable the Accessibility Service to restart it.
- Check `adb logcat -s AutoTapperService` for runtime diagnostics.

---
⚠️ AutoTapper is provided for educational and testing purposes. Understand the security implications before deploying it on production devices.
