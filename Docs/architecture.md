# Project Architecture

## Design Pattern: Clean Architecture + MVVM
The app follows a strict separation of concerns using Clean Architecture principles combined with the MVVM (Model-View-ViewModel) pattern in the presentation layer.

### 1. Presentation Layer (`presentation/`)
- **UI (Composables)**: Jetpack Compose for declarative UI.
- **ViewModels**: Manage UI state and handle user interactions. They communicate with the Domain layer via UseCases.
- **State Management**: Uses `State` objects in ViewModels to represent the single source of truth for the UI (Unidirectional Data Flow).

### 2. Domain Layer (`domain/`)
- **Models**: Plain data classes representing business entities (e.g., `Product`, `Review`).
- **UseCases**: Simple classes that encapsulate a single piece of business logic (e.g., `AddToCartUseCase`).
- **Repositories (Interfaces)**: Define the contract for data operations, abstracted away from the implementation details.

### 3. Data Layer (`data/`)
- **Models**: Data Transfer Objects (DTOs) for Firebase and other sources.
- **Repositories (Implementations)**: Concrete implementations of the domain repositories. They coordinate between different data sources.
- **Remote Service (`data/remote/`)**: Direct interaction with Firebase (Firestore, Auth, Storage).
- **Local Service (`data/preferences/`)**: DataStore for local persistence.

## Dependency Injection
- Powered by **Hilt** (Dagger).
- Modules are defined in `com/opticalshop/di/AppModule.kt`.
