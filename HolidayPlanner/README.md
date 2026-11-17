# Holiday & Leave Planner (Android)

A production-quality Android application built with modern architecture (MVVM, Clean Repository pattern, Room, Coroutines & Flow, Material 3) that auto-detects the user's country, lists that country's public holidays for a selected year, and enables users to plan and optimize personal leave around those holidays.

---

## 📱 Features

1. **Authentication (Firebase & Google Identity)**
   - Google Sign-In powered by Android's modern **Credential Manager** API (`GetGoogleIdOption`), exchanged for Firebase Auth credentials (no deprecated `GoogleSignInClient`).
   - Displays user profile name and photo in the toolbar.
   - One-tap **Sign Out** returns the user to the login screen.
   - Includes a **Guest Mode** fallback so the application can be explored and tested immediately without requiring an active Firebase project setup.

2. **Automatic Country Detection & Geocoding**
   - Location retrieval using Google Play Services `FusedLocationProviderClient` with `PRIORITY_BALANCED_POWER_ACCURACY`.
   - Android `Geocoder` with API 33+ asynchronous listener (`getFromLocation(lat, lng, 1, GeocodeListener)`) and API < 33 IO-dispatched fallback.
   - **Multi-tiered Fallback Chain**:
     $$\text{Fused Location + Geocoder} \longrightarrow \text{Last Saved Country} \longrightarrow \text{Device Locale} \longrightarrow \text{Manual Selection}$$
   - Seamless runtime permission handling with user rationale dialog and graceful fallback on denial.

3. **Country & Year Selectors**
   - **Country Spinner**: Populated dynamically from Nager.Date's `AvailableCountries` endpoint, sorted alphabetically. Automatically pre-selects the detected country and caches the list in-memory.
   - **Year Selector**: Dropdown spanning current year $\pm 5$ years (defaults to current year). Changing either selector immediately reloads the public holidays.

4. **Holiday Explorer (RecyclerView)**
   - Powered by `ListAdapter` with `DiffUtil`.
   - Displays formatted dates (e.g. `Mon, 26 Jan 2026`), primary name, and local name (automatically hidden if identical to English name).
   - **Long Weekend Badge**: Automatically badges holidays falling on **Friday** or **Monday** and details the span (e.g. `Fri–Sun (3 days)` or `Sat–Mon (3 days)`).
   - **Filter Toggle**: Filter chip switching between **All** holidays and **Long weekends only**.
   - **Leave Overlap Indicator**: Highlights public holidays that fall within one of the user's personal leave plans.

5. **Pinned Next Upcoming Holiday Card**
   - Pinned hero card highlighting the next upcoming holiday for the selected year.
   - Real-time days-left calculation using `java.time.temporal.ChronoUnit.DAYS.between(today, holidayDate)`. Displays `Today!`, `Tomorrow`, or `X days left`.
   - Intelligently displays `"All holidays have passed"` when viewing past years, and highlights the first holiday when viewing future years.

6. **Smart Bridge Suggestions**
   - Detects mid-week bridge opportunities (e.g. Tuesday holiday bridging Monday, or Thursday holiday bridging Friday).
   - Offers a one-tap **"Bridge this"** action button that pre-fills the Leave Plan editor with the suggested leave dates.

7. **Personal Leave Plans (CRUD with Room)**
   - Dedicated **My Leaves** tab with Material Floating Action Button (FAB).
   - Date selection with **MaterialDatePicker** (`dateRangePicker`) using UTC normalization to prevent timezone off-by-one errors.
   - Overlap warning banner warning if the chosen range overlaps an existing leave.
   - Leave cards display duration in days, notes, and a summary of included public holidays (e.g., `Includes 2 public holidays: Diwali, Govardhan Pooja`).
   - Supports editing, deletion with confirmation dialog, and swipe-to-delete with **Undo Snackbar**.
   - User data is scoped to the signed-in user's UID (`userId = FirebaseAuth.uid`).

8. **Offline Caching & Resilience**
   - Room database cache for holidays keyed by `(countryCode, year)` for full offline support.
   - Graceful handling of HTTP 204 (No Content), HTTP 404 (unsupported country/year), timeouts, and network disconnects.

---

## 🛠 Tech Stack & Architecture

- **Language:** Kotlin 2.2.10
- **Android Target:** minSdk 24 (with Java 8+ core library desugaring), targetSdk 35, compileSdk 36.
- **UI:** XML Layouts, ViewBinding, Material Components 3 (`Theme.Material3.DayNight.NoActionBar`). No Jetpack Compose.
- **Architecture:** Clean Architecture + MVVM + Repository pattern.
- **Async & Concurrency:** Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`) & `StateFlow` / `Flow` with `repeatOnLifecycle`.
- **Database:** Room 2.7.2 with KSP (`ksp("androidx.room:room-compiler")`).
- **Networking:** Retrofit 2.11.0 + OkHttp 4.12.0 + Gson + HttpLoggingInterceptor.
- **Authentication:** Firebase Auth 33.7.0 + Credential Manager 1.3.0 + Google Identity `googleid:1.1.1`.
- **Location:** Google Play Services Location 21.3.0 (`FusedLocationProviderClient`) + Android `Geocoder`.
- **Dependency Injection:** Lightweight manual DI via `AppContainer` in `HolidayApplication`.

---

## 📁 Project Structure

```
app/src/main/java/com/ashok/holidayplanner/
├── HolidayApplication.kt               # Application class with AppContainer initialization
├── MainActivity.kt                     # Single Activity host managing auth & main fragments
├── di/
│   └── AppContainer.kt                 # Manual Dependency Injection container & ViewModel factory
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt              # Room Database singleton
│   │   ├── converters/Converters.kt    # TypeConverters for LocalDate & String lists
│   │   ├── dao/
│   │   │   ├── HolidayCacheDao.kt      # Flow & suspend DAO for cached holidays
│   │   │   └── LeavePlanDao.kt         # Flow & suspend DAO for user-scoped leave plans
│   │   └── entities/
│   │       ├── CachedHoliday.kt        # Room entity for offline holiday caching
│   │       └── LeavePlan.kt            # Room entity for personal leave plans
│   ├── prefs/
│   │   └── SettingsStore.kt            # SharedPreferences manager for country, year, & guest state
│   ├── remote/
│   │   ├── NagerApi.kt                 # Retrofit interface for Nager.Date API
│   │   ├── RetrofitProvider.kt         # OkHttpClient & Retrofit factory
│   │   └── dto/
│   │       ├── CountryDto.kt           # DTO for AvailableCountries
│   │       └── HolidayDto.kt           # DTO for PublicHolidays
│   └── repository/
│       ├── AuthRepository.kt           # Firebase Auth & Credential Manager repository
│       ├── CountryRepository.kt        # Country caching & retrieval repository
│       ├── HolidayRepository.kt        # Remote fetch & Room cache repository
│       └── LeaveRepository.kt          # Leave plan CRUD & overlap calculation repository
├── domain/
│   ├── LongWeekendCalculator.kt        # 3-day weekend & bridge suggestion calculator
│   ├── UpcomingHolidayFinder.kt        # Next upcoming holiday & days-left finder
│   ├── mapper/
│   │   └── HolidayMapper.kt            # DTO <-> Domain <-> Room Entity mappers
│   └── model/
│       ├── Country.kt                  # Domain country model
│       ├── Holiday.kt                  # Domain holiday model
│       └── LeaveWithHolidays.kt        # Combined LeavePlan + included holidays model
├── location/
│   ├── CountryResolver.kt              # Fallback chain & Geocoder implementation
│   └── LocationProvider.kt             # FusedLocationProviderClient wrapper
├── ui/
│   ├── auth/
│   │   ├── AuthFragment.kt             # Google sign-in & Guest mode UI
│   │   └── AuthViewModel.kt            # Auth state management
│   ├── common/
│   │   └── UiState.kt                  # Standard sealed interface for screen states
│   ├── holidays/
│   │   ├── HolidayAdapter.kt           # ListAdapter + DiffUtil for holidays
│   │   ├── HolidaysFragment.kt         # Holidays list, selectors, & upcoming card
│   │   └── HolidaysViewModel.kt        # Holiday state & leave overlap combining
│   ├── home/
│   │   └── HomeFragment.kt             # Toolbar with profile/sign-out & BottomNavigation
│   └── leaves/
│       ├── LeaveAdapter.kt             # ListAdapter + DiffUtil for leave plans
│       ├── LeaveEditorDialogFragment.kt# MaterialDatePicker range editor & validator
│       ├── LeavesFragment.kt           # My Leaves list, swipe-to-delete, & FAB
│       └── LeavesViewModel.kt          # Leave CRUD & overlap checking
└── util/
    └── DateUtils.kt                    # Safe java.time formatting & UTC conversions
```

---

## 🔑 Firebase & Google Sign-In Setup Guide

The repository includes a template `google-services.json` so that the project compiles out-of-the-box (`./gradlew assembleDebug`). To connect your live Firebase project with Google Authentication, follow these steps:

### 1. Create a Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** and follow the on-screen steps.

### 2. Register Your Android App
1. In Project Overview, click the **Android** icon to add an Android app.
2. Enter the package name: `com.ashok.holidayplanner` (must match `applicationId` in `app/build.gradle.kts`).
3. Enter an App nickname (e.g. `Holiday Planner`).

### 3. Generate & Add SHA-1 Fingerprint
To allow Google Sign-In with Credential Manager:
1. In your terminal, run:
   ```bash
   ./gradlew signingReport
   ```
2. Copy the **SHA-1** and **SHA-256** fingerprints for the `debug` variant (under `task ':app:signingReport'`).
3. In the Firebase App Settings, paste both fingerprints into the **SHA certificate fingerprints** section.

### 4. Enable Google Sign-In Provider
1. In the Firebase Console, go to **Build > Authentication > Sign-in method**.
2. Click on **Google**, toggle **Enable**, select a support email, and click **Save**.

### 5. Download `google-services.json`
1. Download the updated `google-services.json` file from your Firebase project settings.
2. Replace the file at `app/google-services.json`.
3. In the Firebase Console under **Authentication > Sign-in method > Google > Web SDK configuration**, copy the **Web client ID**.
4. The Google Services Gradle plugin will automatically inject this client ID into `R.string.default_web_client_id` during build.

*(Note: If you run without Firebase configuration, tap **"Continue as Guest"** on the login screen to test all app features with local data persistence).*

---

## 🧪 Testing

The test suite covers all core domain logic, algorithms, date conversions, and data isolation:

- **LongWeekendCalculatorTest**: Verifies Friday/Monday detection, span formatting, and bridge day calculations.
- **UpcomingHolidayFinderTest**: Tests next upcoming holiday selection, today/tomorrow formatting, and past/future year boundaries.
- **HolidayMapperTest**: Verifies DTO-to-domain conversion, resilience to missing fields/unknown keys, and date parsing.
- **DateValidationTest**: Tests date-range validation, inclusive day counts, and UTC millis conversion.
- **LeavePlanDaoTest**: Instrumented in-memory Room test verifying CRUD operations and user-level data isolation.

To run the unit tests:
```bash
./gradlew test
```

To build the APK:
```bash
./gradlew assembleDebug
```

The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🛡️ Edge Cases Handled

1. **No Internet Connection**: Reads from the local Room database cache and displays previously fetched holidays.
2. **Location Permission Denied**: Gracefully falls back to the last saved country, then to the system's `Locale.getDefault().country`, or lets the user select manually.
3. **Unsupported Country or Year**: Safely handles HTTP 404 and HTTP 204 without crashing; displays a user-friendly notice and enables switching via the Spinner.
4. **Timezone Off-By-One Errors**: Uses UTC instant conversion (`Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()`) for `MaterialDatePicker` selections.
5. **Multi-User Scoping**: Room entity queries are filtered by `userId = authRepository.getUserId()`, ensuring complete privacy across different accounts.
