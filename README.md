# Learning Dashboard (Android · Kotlin · Jetpack Compose)

Login → Course Dashboard → Course Details, with an offline cache. Demo login: `aman@gmail.com` / `123456`.
Run the app from Android Studio. Run the tests with `./gradlew :app:testDebugUnitTest`.

## 1. Architecture
The app uses **MVVM with Clean Architecture layers**, with **Hilt** for dependency injection:
`ui` (Compose screens) → `ViewModel` (`StateFlow` UI state) → `domain` (models, repository interfaces, `ProgressCalculator`) → `data` (`CourseRepositoryImpl` → `CourseApi` + Room `CourseDao`).
- The UI is a pure function of a sealed `UiState`. Each screen handles Loading, Success, Empty and Error.
- The domain layer has no Android dependencies. The interfaces let me swap the mock API for Retrofit/Ktor without touching the UI. Tests run on the JVM with simple fakes and don't need mocking frameworks.
- I went with this structure because Google recommends it for Android and it scales well into feature modules. For an app this size, a full use-case layer would have added boilerplate without much benefit.

## 2. Offline support
**Room is the single source of truth.** Screens only observe Room `Flow`s. `refreshCourses()` fetches from the API and atomically upserts courses and lessons in a `@Transaction`.
- If the refresh fails and cached data exists, the dashboard still shows the cached courses with an "offline" banner. If nothing is cached, it shows an error with a Retry button.
- Marking a lesson complete writes to Room, and progress is calculated from the lessons, so every screen updates right away. The refresh merge (`remote || local`) keeps completions made offline.
- The mock API (`assets/courses.json`) throws when the device has no network, so you can test offline mode by turning on airplane mode. The session is saved in DataStore, so the app opens straight to the cached dashboard after a restart.

## 3. Security (tokens)
- Keep short-lived **access tokens in memory**. Store the **refresh token encrypted** with an **Android Keystore**-backed AES key (for example Tink with DataStore). Never use plain SharedPreferences, logs or the Room DB for tokens.
- Exclude token and DB files from backups (`dataExtractionRules`), enforce HTTPS with certificate pinning, and clear all user data on logout. This app already does that last step.

## 4. Scale (1M users, hundreds of courses)
1. **Pagination and lazy detail loading**: page the course list and fetch lessons on demand. Use Paging 3 with a `RemoteMediator` over Room.
2. **Server-side progress sync**: queue lesson completions in an outbox table and sync them with **WorkManager**, using idempotent requests and conflict resolution.
3. **Efficient networking**: Retrofit/OkHttp with ETag/HTTP caching, a CDN for static content, gzip, and exponential-backoff retries.
4. **Modularization**: `:core:*` and `:feature:*` modules for faster builds and team ownership, plus Baseline Profiles for startup performance.
5. **Observability and safe rollout**: Crashlytics and analytics, remote feature flags, staged rollouts, and CI running unit, UI and screenshot tests.

## 5. Second platform (iOS/macOS)
- I'd use the same layers in **SwiftUI + MVVM**: `ObservableObject`/`@Observable` view models that publish an enum state, and `async/await` with `URLSession` for the API.
- The offline cache would use **SwiftData/Core Data** as the source of truth. Tokens would go in the **Keychain**, and background sync would use `BGTaskScheduler`.
- Alternatively, I'd use **Kotlin Multiplatform** to share the domain and data layers (repository, Ktor, SQLDelight/Room KMP) and keep the UI native in SwiftUI. That way the offline and progress logic only has to be written and tested once.
