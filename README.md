
# Smart Car Parking

Smart Car Parking is an Android app that provides parking management features including lot maps, bookings, admin tools, and QR-based check-in. This repository contains the Android app source, build scripts, and a short demo video in the `Demo/` folder.

**Main Features:**
- Map view with parking markers
- Booking and check-in flows
- Admin screens for lots, users and announcements
- QR scanner and history

**Prerequisites**
- Java JDK (version 11+ recommended)
- Android Studio (or Android SDK + Gradle)
- An Android device or emulator for testing

**Quick setup**
1. Clone the repository:

```
git clone https://github.com/Diya570/smart_car_parking.git
cd smart_car_parking
```

2. Open the project in Android Studio (recommended) and let it download Gradle dependencies.

3. Build from the command line (Windows PowerShell):

```
.\gradlew assembleDebug
.\gradlew installDebug   # installs on a connected device/emulator
```

**Project layout (high level)**
- `app/` — Android app module (source, resources, manifest)
- `Demo/` — demo assets (contains `demo.mp4`)
- Gradle files at project root for build configuration

## Demo

Watch the app in action:

https://github.com/Diya570/smart_car_parking/assets/Demo/demo.mp4

*Note: The demo video is located in the `Demo/` folder of this repository.***Notes & Next steps**
- If you expect to run the app against Firebase or other services, ensure `google-services.json` is present in `app/` (it is in this repo but verify credentials).
- Open an issue if you encounter build problems or want help running the demo.

---
Repository maintained by the project contributors. See the repository for license and contributor information.
