
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

**Demo**

A short demonstration video is included in the repository. Open the file at `Demo/demo.mp4` or play it directly on GitHub by following the link below:

[Demo video — Demo/demo.mp4](Demo/demo.mp4)

Repository maintained by the project contributors. See the repository for license and contributor information.
