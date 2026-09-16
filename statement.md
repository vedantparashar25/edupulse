# EduPulse: Smart Campus Academic & Student Analytics System
## Official Course Project - Problem Statement & Project Scope Document
**Student Name**: Vedant Parashar  
**Registration Number**: 25BAI11290  
**Course Title**: Object-Oriented Programming with Java  
**Institution**: VIT Bhopal University / VITyarthi Learning Destination  
**Evaluation Mode**: Flipped Course Project Evaluation  

---

## 1. Problem Statement
In higher educational environments and flipped-classroom learning frameworks (such as VITyarthi), academic operations are often fragmented across disparate tools. Universities face key operational bottlenecks:
1. **Manual Attendance & Debarment Risk**: Real-time tracking of mandatory attendance thresholds (e.g., the 75% minimum attendance rule) is difficult, leading to surprise debarments for students before examinations.
2. **Disconnected Assessment & Grading**: Managing continuous assessments (quizzes, flipped lab projects, theory tests) with support for both absolute cutoff grading and relative standard-deviation bell curves requires complex manual adjustments by faculty.
3. **Tuition Fee & Merit Recognition Gaps**: Calculating tuition fees tied dynamically to credit workloads and applying multi-tier academic scholarships is error-prone.
4. **Auditability & Operational Integrity**: In academic administration, unlogged modifications to grades, enrollments, and attendance can compromise institutional integrity.

EduPulse addresses these challenges by delivering a high-performance, modular, secure, and fully terminal-executable Java console application designed to handle end-to-end campus academic administration, course enrollment, real-time attendance surveillance, analytics, and billing.

---

## 2. Scope of the Project
EduPulse is engineered as a robust Command-Line Interface (CLI) platform built on pure Java SE. Its scope spans:
- **Identity & Role-Based Access**: Multi-tier authentication for Administrators, Academic Faculty, and Registered Students using salted SHA-256 password hashing.
- **Curriculum & Enrollment Engine**: Course registration with credit weighting, prerequisite validation, and automated capacity/seat capping.
- **Real-Time Attendance Monitoring**: Session logging with automated event notifications (Observer pattern) triggering debarment alerts when attendance dips below 75%.
- **Dual Grading Strategy Engine**: Strategy pattern implementation supporting both fixed-threshold (Absolute) and cohort bell-curve (Relative) grading.
- **Academic Performance & Merit Analytics**: Java 8+ Stream-driven CGPA calculation, top performers ranking, and at-risk student detection.
- **Tuition Assessment & Fee Billing**: Dynamic credit fee computation and automatic merit scholarship deductions.
- **Asynchronous Audit Trail**: Multithreaded background worker logging all critical transactions to an append-only audit file.

---

## 3. Target Users & Stakeholders

| User Role | Primary Needs & Responsibilities |
| :--- | :--- |
| **System Administrator** | Oversees student/faculty directory, creates new course offerings, monitors system-wide attendance compliance, inspects security audit logs, and reviews campus-wide merit leaderboards. |
| **Academic Faculty** | Manages enrolled student cohorts, logs daily lecture attendance, records flipped project & quiz marks, evaluates final student grades using chosen grading strategies, and reviews score distribution charts. |
| **Enrolled Student** | Browses course catalog, registers for elective/core courses, monitors personal attendance percentages to avoid debarment, tracks grades/CGPA, and generates verified tuition fee invoices. |
| **Course Evaluator / Auditor** | Reviews compliance, audits grade distributions, checks data integrity, and validates automated simulation benchmarks via the `--demo` and `--test` CLI switches. |

---

## 4. High-Level Features & Capabilities

1. **Role-Based Access Control (RBAC)**
   - Secure login with salt-hashed passwords.
   - Distinct, context-sensitive menus for Admin, Faculty, and Student.
   - Encapsulated user entities with polymorphic summaries.

2. **Course Catalog & Capacity Guard**
   - Dynamic course catalog with title, credits, department, and maximum seating capacity.
   - Concurrency-safe seat allocation preventing over-enrollment.
   - Course dropping mechanism with automatic capacity restoration.

3. **Continuous Attendance & Debarment Alerting (Observer Pattern)**
   - Real-time percentage tracking across all courses.
   - Debarment warnings automatically broadcast when attendance falls below 75%.
   - Instant visual alerts in terminal and persistent audit logging.

4. **Multi-Algorithm Grading Engine (Strategy Pattern)**
   - Absolute Cutoff Grading (10-point scale: S, A, B, C, D, E, F).
   - Relative Bell-Curve Grading using cohort mean and standard deviation.
   - Cumulative GPA (CGPA) recomputed on-the-fly weighted by credit hours.

5. **Tuition Fee Assessment & Merit Scholarship (Strategy Pattern)**
   - Per-credit tuition assessment combined with base campus amenities fee.
   - Merit scholarship engine providing up to 40% tuition waiver for high CGPA achievers (CGPA ≥ 9.5).
   - Formatted, professional fee receipt and invoice generation.

6. **Asynchronous System Audit Logging (Multithreading)**
   - Dedicated background daemon worker thread consuming from a thread-safe `BlockingQueue`.
   - File-backed persistent audit trail (`data/audit.log`) without blocking user interactions.

7. **Zero-Dependency CLI & Automated Verification**
   - 100% executable from command line on Windows, Linux, and macOS.
   - Interactive CLI with ANSI color formatting and menu navigation.
   - Automated `--demo` flag for 30-second unattended end-to-end evaluation.
   - Built-in `--test` suite executing 31 automated test assertions with 100% pass rate.
