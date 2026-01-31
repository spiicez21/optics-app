# Optical Specs E-Commerce App

A modern Android e-commerce application for purchasing optical glasses and accessories, built with Jetpack Compose and following Clean Architecture principles.

## 🚀 Overview

This app provides a seamless shopping experience for eyewear. It utilizes Firebase for its backend (Auth, Firestore, Storage) and implements a robust architecture to ensure scalability and maintainability.

### Key Features
- **Product Catalog**: Browse and search through a wide variety of optical frames.
- **Real-time Updates**: Powered by Firestore for live data synchronization.
- **Authentication**: Secure user login and registration via Firebase Auth.
- **Shopping Cart**: Manage selected items and proceed to checkout.
- **Pull-to-Refresh & Animations**: Smooth UI interactions with Lottie animations.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI)
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Architecture**: Clean Architecture + MVVM + MVI
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Backend**: [Firebase](https://firebase.google.com/) (Firestore, Auth, Storage)
- **Asynchrony**: [Kotlin Coroutines & Flows](https://kotlinlang.org/docs/flow.html)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Local Storage**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **Animations**: [Lottie](https://github.com/airbnb/lottie-android)

## 📂 Project Documentation

Detailed documentation can be found in the [Docs](./Docs) directory:

- [**Project Architecture**](./Docs/PROJECT_ARCHITECTURE.md): Detailed breakdown of modules, packages, external libraries, and the CI/CD pipeline.
- [**Data Flow**](./Docs/data_flow.md): Visualization of how data moves through the app.
- [**Architecture Overview (Legacy)**](./Docs/architecture.md): High-level overview of design patterns.
- [**Roadmap / TODO**](./Docs/TODO.md): Current status and upcoming features.

## ⚙️ CI/CD

The project uses **GitHub Actions** for automated builds.
- **Workflow**: `Android CI` (runs on push/pull requests to `main` and `develop`).
- **Artifacts**: Each build generates a debug APK available in the GitHub Actions run details.

## 🏗 Getting Started

### Prerequisites
- Android Studio Iguana or newer.
- JDK 17.

### Running the App
1. Clone the repository.
2. Open the project in Android Studio.
3. Sync project with Gradle files.
4. Run the `:app` module on an emulator or physical device.

---
*Created and maintained by @spiicez21.*
