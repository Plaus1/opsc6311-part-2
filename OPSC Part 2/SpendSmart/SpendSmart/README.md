# SpendSmart – Personal Budget Tracker

**Module:** OPSC6311 – Open-Source Coding  
**Students:** Tendani John Tshishonga (ST10451749), Plausible Tshishonga (ST10449619)  
**POE:** Part 2 – App Prototype Development

---

## 📱 About SpendSmart

SpendSmart is an Android personal budget tracker application that helps users manage their finances by tracking expenses, setting budget goals, and monitoring spending behaviour through visual insights and gamification.

---

## ✅ Features

| Feature | Status |
|---|---|
| User Registration & Login | ✅ |
| Custom Expense Categories (CRUD) | ✅ |
| Add/Edit/Delete Expenses | ✅ |
| Date, Start Time & End Time per Expense | ✅ |
| Optional Receipt Photo (Camera or Gallery) | ✅ |
| Monthly Min/Max Budget Goals with SeekBar | ✅ |
| Expense List with Date Range Filter | ✅ |
| Total Spending per Category | ✅ |
| Pie Chart & Bar Chart Reports | ✅ |
| Visual Dashboard with Progress Bar | ✅ |
| Gamification (Streaks, Badges, Achievements) | ✅ |
| Smart Budget Alerts (80% and 100% thresholds) | ✅ |
| RoomDB Local Persistence | ✅ |
| Input Validation & Error Handling | ✅ |
| GitHub Actions CI/CD | ✅ |

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Database:** Room (SQLite)
- **Charts:** MPAndroidChart
- **Image Loading:** Glide
- **Architecture:** Single-activity per screen + ViewBinding
- **Testing:** JUnit4, Espresso, Room in-memory DB tests
- **CI:** GitHub Actions

---

## 🗂️ Project Structure

```
app/src/main/java/com/spendsmart/
├── data/
│   ├── dao/          # Room DAOs
│   ├── database/     # SpendSmartDatabase
│   └── entities/     # User, Category, Expense, BudgetGoal
├── ui/
│   ├── auth/         # LoginActivity, RegisterActivity
│   ├── dashboard/    # MainActivity
│   ├── expenses/     # Add/List/Detail + Adapter
│   ├── categories/   # CategoryActivity
│   ├── goals/        # GoalsActivity
│   ├── reports/      # ReportsActivity
│   └── achievements/ # AchievementsActivity
└── utils/            # Extensions, SessionManager
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- JDK 17

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/SpendSmart.git
   ```
2. Open in Android Studio
3. Add the JitPack repository to your `settings.gradle`:
   ```gradle
   maven { url 'https://jitpack.io' }
   ```
4. Sync Gradle and run on an emulator or physical device (API 24+)

---

## 🧪 Running Tests

**Unit Tests:**
```bash
./gradlew test
```

**Instrumented Tests:**
```bash
./gradlew connectedAndroidTest
```

---

## 📊 GitHub Actions

The CI pipeline runs automatically on every push and pull request:
1. **Unit Tests** – runs first
2. **Build APK** – only if tests pass
3. **Instrumented Tests** – runs on Android emulator

See `.github/workflows/android.yml` for full configuration.

---

## 📸 App Screenshots

| Login | Dashboard | Add Expense |
|---|---|---|
| _See app_ | _See app_ | _See app_ |

---

## 📚 References

- Brown, L., 2021. *Mobile Applications for Financial Management.* Tech Publications.
- Forbes Advisor, 2024. YNAB & PocketGuard reviews. https://www.forbes.com
- SparkTrail, 2026. Budgeting apps comparison. https://sparktrail.site
- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
- Android Room Documentation: https://developer.android.com/training/data-storage/room
