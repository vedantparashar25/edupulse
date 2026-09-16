# EduPulse: Smart Campus Academic & Student Analytics System
> **VITyarthi Evaluated Course Project - Flipped Course Evaluation**  
> **Student**: Vedant Parashar | **Reg. No**: 25BAI11290 | **Course**: Object-Oriented Programming with Java

[![Java](https://img.shields.io/badge/Java-17%2B%20%2F%2026-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)]()
[![Tests](https://img.shields.io/badge/Tests-31%2F31%20Passed-brightgreen?style=for-the-badge)]()
[![License](https://img.shields.io/badge/License-Academic-blue?style=for-the-badge)]()

---

## 1. Project Title & Overview
**EduPulse** is a production-grade, modular, command-line terminal application engineered in Java SE to streamline campus academic management, course registration, attendance monitoring, and performance analytics.

Built specifically to align with the **VITyarthi flipped course evaluation criteria**, EduPulse incorporates industry-standard Object-Oriented Programming (OOP) paradigms, multiple Gang-of-Four (GoF) design patterns, multithreaded concurrency, modern Java Stream APIs, and zero external runtime dependencies.

---

## 2. Key Features

### 🔐 1. Role-Based Identity & Authentication (RBAC)
- Salted **SHA-256 cryptographic password hashing** for secure storage.
- Context-sensitive terminal portals for **Admin**, **Faculty**, and **Student**.
- Factory Design Pattern (`UserFactory`) for polymorphic user instantiation.

### 📚 2. Academic Curriculum & Course Enrollment Engine
- Course catalog with credit weighting, prerequisites, and max seat capacity.
- Thread-safe seat allocation preventing over-enrollment.
- Course add/drop workflow with dynamic seat reclamation.

### ⚠️ 3. Attendance Tracking & Debarment Alerting (Observer Pattern)
- Lecture-by-lecture attendance recording with topic logging.
- Concrete **Observer Pattern** (`AttendanceAlertNotifier`) continuously monitoring student attendance.
- Automatically triggers terminal warnings and audit events when attendance drops below the mandatory **75% cutoff**.

### 📊 4. Dual Strategy Grading Engine (Strategy Pattern)
- **Absolute Cutoff Strategy**: Fixed 10-point letter grading (S, A, B, C, D, E, F).
- **Relative Bell-Curve Strategy**: Dynamically computes letter grades using cohort mean and standard deviation.
- Real-time **Cumulative GPA (CGPA)** computation weighted by earned course credits.

### 💰 5. Tuition Assessment & Scholarship Engine (Strategy Pattern)
- Per-credit tuition assessment combined with campus facilities fees.
- **Merit Scholarship Strategy**: Up to 40% tuition fee waiver for academic excellence (CGPA ≥ 9.5).
- Instant ASCII invoice and itemized fee receipt generation.

### 🧵 6. Concurrency & Asynchronous Audit Logging
- Dedicated daemon thread executor (`AsyncAuditLogger`) processing non-blocking event queues (`BlockingQueue`).
- Real-time append-only security and operational audit trail written to `data/audit.log`.

### 💾 7. Zero-Dependency Persistent Storage
- Lightweight, resilient file persistence engine (`JsonStorageManager`) saving database state to `data/`.
- Automatic demo data seeding on initial run.

---

## 3. Technologies & Architecture

- **Language**: Java SE (Compatible with JDK 17, 21, and 26)
- **Design Patterns Implemented**:
  - **Factory Pattern**: `UserFactory`
  - **Strategy Pattern**: `GradingStrategy` (Absolute & Relative), `ScholarshipStrategy`
  - **Observer Pattern**: `AttendanceObserver`, `AttendanceAlertNotifier`
  - **Repository Pattern**: Generic `Repository<T, ID>`
  - **Singleton Pattern**: `AsyncAuditLogger`
- **Core Java Concepts**:
  - Encapsulation & Polymorphism (Abstract `User`, `Student`, `Faculty`, `Admin`)
  - Generics & Collections (`ConcurrentHashMap`, `List`, `Map`, `Optional`)
  - Functional Programming & Streams API (`filter`, `map`, `reduce`, `groupingBy`, `sorted`)
  - Concurrency (`ExecutorService`, `BlockingQueue`, Daemon Threads)
  - Custom Exception Hierarchy (`AppException` and specific subclasses)

---

## 4. Project Directory Layout

```
java project/
│
├── src/
│   └── com/vityarthi/edupulse/
│       ├── Main.java                          # Main Application Entry Point
│       ├── common/                            # Core Enums & Domain Exceptions
│       │   ├── Role.java
│       │   ├── Grade.java
│       │   ├── EnrollmentStatus.java
│       │   └── AppException.java
│       ├── model/                             # Domain Entities (OOP Models)
│       │   ├── User.java                      # Abstract Base Class
│       │   ├── Student.java                   # Concrete Model
│       │   ├── Faculty.java                   # Concrete Model
│       │   ├── Admin.java                     # Concrete Model
│       │   ├── Course.java                    # Course Catalog Entity
│       │   ├── Enrollment.java                # Student-Course Binding
│       │   ├── AttendanceRecord.java          # Session Record
│       │   └── AuditLog.java                  # Audit Entry
│       ├── pattern/                           # GoF Design Pattern Implementations
│       │   ├── factory/UserFactory.java
│       │   ├── strategy/GradingStrategy.java
│       │   ├── strategy/AbsoluteGradingStrategy.java
│       │   ├── strategy/RelativeGradingStrategy.java
│       │   ├── strategy/ScholarshipStrategy.java
│       │   ├── observer/AttendanceObserver.java
│       │   └── observer/AttendanceAlertNotifier.java
│       ├── repository/                        # Generic Data Access Layer
│       │   ├── Repository.java
│       │   ├── InMemoryRepository.java
│       │   └── JsonStorageManager.java
│       ├── service/                           # Business Services Layer
│       │   ├── AuthService.java
│       │   ├── CourseService.java
│       │   ├── AttendanceService.java
│       │   ├── AnalyticsService.java
│       │   └── FeeService.java
│       ├── cli/                               # Command-Line Presentation
│       │   ├── ConsoleView.java
│       │   └── MenuRouter.java
│       └── util/                              # Security, Logging & Formatting
│           ├── SecurityUtil.java
│           ├── AsyncAuditLogger.java
│           └── AnsiUtil.java
│
├── test/
│   └── com/vityarthi/edupulse/test/
│       └── EduPulseTestSuite.java             # 31 Automated Unit & Integration Tests
│
├── data/                                      # Persistent CSV/Text Database
│   ├── users.csv
│   ├── courses.csv
│   ├── enrollments.csv
│   ├── attendance.csv
│   └── audit.log
│
├── statement.md                               # Formal Problem Statement Document
├── README.md                                  # Complete Project Documentation
├── PROJECT_REPORT.md                          # 15-Section Comprehensive Project Report
├── Project_Report.pdf                         # Formatted PDF Submission Document
├── run.bat                                    # Windows One-Click Execution Script
└── run.sh                                     # Linux/macOS One-Click Execution Script
```

---

## 5. Steps to Install & Run the Project

### Prerequisites
- Java Development Kit (JDK 17 or higher, including JDK 21 or 26). Verify via:
  ```bash
  javac -version
  java -version
  ```

### Step 1: Clone the Repository
```bash
git clone https://github.com/vedant-parashar/edupulse.git
cd edupulse
```

### Step 2: Compile the Codebase
Using Windows PowerShell / Command Prompt:
```powershell
javac -d bin (Get-ChildItem -Path "src", "test" -Recurse -Filter "*.java").FullName
```
Or on Linux / macOS / Bash:
```bash
mkdir -p bin
javac -d bin $(find src test -name "*.java")
```

### Step 3: Run the Application

#### Option A: One-Click Quick Launch
- **Windows**: Simply run `run.bat`
- **Linux / macOS**: Run `./run.sh` (or `bash run.sh`)

#### Option B: Automated Evaluation Demo (`--demo`)
Evaluates the entire system end-to-end automatically without manual user input:
```bash
java -cp bin com.vityarthi.edupulse.Main --demo
```

#### Option C: Interactive Terminal Mode
```bash
java -cp bin com.vityarthi.edupulse.Main
```

### Pre-Seeded Demo Login Credentials

| Role | University Email | Password | Access Capabilities |
| :--- | :--- | :--- | :--- |
| **System Admin** | `admin@vityarthi.ac.in` | `admin123` | User registration, course creation, audit logs, system leaderboard |
| **Faculty** | `sengupta@vityarthi.ac.in` | `faculty123` | Mark daily attendance, grade flipped projects, grade distribution |
| **Student** | `vedant.parashar@vityarthi.ac.in` | `student123` | Course enrollment, attendance monitor, CGPA tracker, fee receipt |

---

## 6. Instructions for Testing

EduPulse includes a comprehensive, self-contained test runner executing **31 automated test assertions** across all layers.

### Run the Test Suite:
```bash
java -cp bin com.vityarthi.edupulse.Main --test
```
or directly:
```bash
java -cp bin com.vityarthi.edupulse.test.EduPulseTestSuite
```

### Test Coverage Highlights:
- ✅ **Security**: SHA-256 deterministic hashing, salt generation, and authentication verification.
- ✅ **OOP Factory**: `UserFactory` polymorphic instantiation of Student, Faculty, and Admin.
- ✅ **Business Rules**: Course capacity overflow rejection (`CourseCapacityException`) and duplicate enrollment prevention.
- ✅ **Observer Pattern**: Dynamic notification and debarment warning when attendance dips below 75%.
- ✅ **Grading Strategies**: Boundary validation for Absolute Grading and Bell-Curve Relative Grading with standard deviation.
- ✅ **Analytics**: Weighted credit CGPA computation formula and Java Stream leaderboard ranking.
- ✅ **Tuition & Fees**: Credit-based pricing and multi-tier merit scholarship discount formulas.
- ✅ **Persistence & Concurrency**: In-memory repository thread safety and non-blocking asynchronous audit logging.

---

## 7. Sample Terminal Execution Walkthrough

```text
===============================================================================
  ______ _____  _    _ _____  _    _ _       _____ ______ 
 |  ____|  __ \| |  | |  __ \| |  | | |     / ____|  ____|
 | |__  | |  | | |  | | |__) | |  | | |    | (___ | |__   
 |  __| | |  | | |  | |  ___/| |  | | |     \___ \|  __|  
 | |____| |__| | |__| | |    | |__| | |____ ____) | |____ 
 |______|_____/ \____/|_|     \____/|______|_____/|______|
       Smart Campus Academic & Performance Intelligence System
                 VITyarthi Flipped Course Platform
===============================================================================

>>> RUNNING AUTOMATED END-TO-END SYSTEM DEMO <<<
--------------------------------------------------------
[INFO] 1. Admin Module: Verifying Preloaded System Users & Course Offerings...
   Total Users: 6 | Total Courses: 4
[INFO] 2. Course Engine: Attempting Student Enrollment with Validation...
[SUCCESS] Enrollment Succeeded: Student U005 enrolled into CSE2002 (Enrollment ID: E0005)
[INFO] 3. Attendance Service & Observer Pattern: Recording Sessions for Student U005...
[WARNING] [DEBARMENT WARNING] Student U005 in Course CSE2001 has attendance 55.6% -> Below mandatory 75% cutoff!
[INFO] 4. Strategy Pattern: Evaluating Student Grade for Vedant Parashar (U004)...
   Marks: Assignment=28.5, Quiz=19.0, Project=48.5 | Total=96.0/100 -> Grade: S
[INFO] 5. Analytics Engine (Streams & Lambdas): Computing CGPA & Leaderboard...
   Vedant Parashar CGPA: 9.50 (Earned Credits: 8)
   --- Academic Merit Leaderboard Top 3 ---
   #1: Ananya Iyer          | RegNo: 25BAI11088   | CGPA: 9.65
   #2: Vedant Parashar      | RegNo: 25BAI11290   | CGPA: 9.50
   #3: Aarav Patel          | RegNo: 25BAI11015   | CGPA: 8.60
[INFO] 6. Fee Service & Scholarship Strategy: Generating Tuition Invoice...

================================================================
                  V I T y a r t h i   F E E   I N V O I C E       
================================================================
 Student Name   : Vedant Parashar           Registration No : 25BAI11290
 Department     : Computer Science          Current CGPA    : 9.50
 Invoice Date   : 2026-09-16 23:04          Invoice ID      : INV-U004-107
----------------------------------------------------------------
 COURSE     TITLE                            CREDITS  AMOUNT (INR)
----------------------------------------------------------------
 CSE2001    Object-Oriented Programming...   4        INR  26,000.00
 CSE2002    Data Structures and Algorithms   4        INR  26,000.00
----------------------------------------------------------------
 Total Registered Credits : 8               Tuition Total  : INR  52,000.00
 Base Campus Facilities & Lab Amenities Fee       : INR  12,000.00
 Gross Academic Assessment                       : INR  64,000.00
 Applied Scholarship: Academic Excellence Merit Scholarship (40.0%) : -INR  20,800.00
----------------------------------------------------------------
 NET PAYABLE AMOUNT (INCL. TAXES)                : INR  43,200.00
================================================================

[SUCCESS] 
>>> DEMO COMPLETED SUCCESSFULLY: ALL 3 MODULES & 4 DESIGN PATTERNS VERIFIED <<<
```
