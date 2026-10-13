# <img src="https://github.com/TheDudeThatCode/TheDudeThatCode/blob/master/Assets/Hi.gif" width="35" /> HELLO, WORLD!




<p align="center">
  <img width="2245" height="1587" alt="objection" src="https://github.com/user-attachments/assets/93d95ddc-4908-45f2-ac36-22a35c6aacda" />
</p>

<h2 align="center"> <img src="https://github.com/TheDudeThatCode/TheDudeThatCode/blob/master/Assets/Developer.gif" width="45" /> About Us </h2>

OBJECTION is an Object-Oriented Programming initiative by IT students from Mapúa Malayan Digital College, focused on redesigning the MotorPH Payroll System into a scalable, modular architecture. Through abstraction, encapsulation, and inheritance, the project transforms procedural logic into a maintainable, enterprise-ready software design. 

---

## ✨ Key Features

- 🧩 Modular class-based payroll architecture
- 🔐 Role-based user hierarchy (Admin, HR, IT, Logistics, etc.)
- 📦 Separation of concerns
- 🔄 Inheritance-driven design
- 📊 Payroll computation engine
- 🧪 Academic architecture documentation (CRC Cards, UML, etc.)


Designed not just as a requirement — but as a developer growth milestone

---
<!-- OOP CONCEPTS -->
<div align="center">

<h2>🧠 OOP Concepts Applied</h2>

<table>
<tr>
<th>🧩 Concept</th>
<th>🚀 Implementation</th>
</tr>
<tr>
<td><b>Encapsulation</b></td>
<td>Sensitive payroll data encapsulated within domain classes</td>
</tr>
<tr>
<td><b>Inheritance</b></td>
<td>Role-based hierarchy extending a shared SystemUser base class</td>
</tr>
<tr>
<td><b>Polymorphism</b></td>
<td>Department-specific behaviors overriding base methods</td>
</tr>
<tr>
<td><b>Abstraction</b></td>
<td>Abstract parent classes defining shared system contracts</td>
</tr>
</table>

</div>

---

## 📈 Current Project Progress

🔧 **Finish**

---

## Milestone 2 – Control 1: Password Hashing

Implemented bcrypt password hashing using jBCrypt 0.4 with cost factor 12 to replace plaintext password storage and comparison.

### Key Changes
- All passwords in `MotorPH_EmployeeLogin.csv` are now stored as bcrypt hashes
- Authentication uses `PasswordHasher.verify()` instead of plaintext `.equals()`
- `LoginDAO` guards against non-bcrypt password writes
- Forced first-login password change no longer allows users to reset other users' passwords
- Hardcoded `"1234"` literals replaced with `LoginService.TEMP_PASSWORD` constant

### Test Command
```bash
cd MotorPH_OOP
javac -encoding UTF-8 -d build/classes -cp "lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" $(find src test -name "*.java")
java -cp "build/classes;lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" motorph_oop.PasswordHashingTest
```

### Evidence
See [docs/M2_Control1_Evidence.md](docs/M2_Control1_Evidence.md) for detailed evidence, test results, and before/after comparisons.

---

## 🚀 Get Started: Usage Instructions

Get started with Payroll Hub in just a few steps:

1️⃣ **Clone this repository**

- git clone https://github.com/Jriv7929/Group-6_OOP_-A2101.git
  
2️⃣ **Open in NetBeans**

- Open NetBeans and select `File > Open Project`.
- Navigate to the cloned repository directory and open the project.
- Ensure the `opencsv.jar` library is added to the project’s libraries (if not already included).

3️⃣ **Library Setup (IMPORTANT!)**

Before running the project, make sure the following libraries are added:

✅ **a. JFileChooser (Built-in)**
- No installation required.
- JFileChooser is part of Java Swing (javax.swing package).
- Just import it in your code: **Run Payroll Hub**

✅ **b. LGoodDatePicker (Date Picker UI) and iTextPDF (Generation)**

Step 1: Download the library

- You can see it on the github lib folder and download it.

Step 2: Add to NetBeans

- Right-click your project → Properties
- Go to Libraries
- Click Add JAR/Folder
- Select the downloaded LGoodDatePicker.jar and itextpdf.jar

4️⃣ Run Payroll Hub
- Ensure `Employee.csv` is in the `data` directory with the required employee data.
- Right-click `MotorPH.java` in the `Projects` tab and select `Run File`.
- Follow the prompts to enter the employee ID, pay period, hours worked, overtime, tardiness, days late, and holiday work details.
- View the payroll statement in the console and check `MotorPHPayslip.csv` for saved data.
- **Note**: Ensure `MotorPHPayslip.csv` is not open in another application (e.g., Excel) while running the program, as this may cause a "file in use" error when saving the payroll data.

5️⃣ **Log in credentials**

**Employee**
- User ID: BSantos
- Password: MMDC1234!

**Admin**
- User ID: MGarcia
- Password: MMDC1234!

**HR**
- User ID: Cmata
- Password: MMDC1234!

**IT**
- User ID: Ehernandez
- Password: MMDC1234!

**Finance**
- User ID: Jlopez
- Password: MMDC1234!

---

### <img align ='center' src='https://media2.giphy.com/media/UQDSBzfyiBKvgFcSTw/giphy.gif?cid=ecf05e47p3cd513axbek3f56ti3jzizq8hincw20jauyyfyw&rid=giphy.gif' width ='29' /> Here's some humor for you:
<img src="https://readme-jokes.vercel.app/api" alt="Error fetching resource, Refresh again to view Jokes Card" width = '11000' />
---

We’d love to hear from you! Connect with the MO-IT103 Daemonteam:

<p align="center">
  <a href="https://github.com/Kristov-Judilla"><img src="https://img.shields.io/badge/GitHub-Kristov--Judilla-181717?style=social&logo=github" alt="Kristov Judilla GitHub"></a>
  <!-- Add more team members' GitHub links here -->
</p>

<p align="center">
  Made with ❤️ by MO-IT103 Team Daemon
</p>

