# Hostel Management System

A full-stack college project: **Spring Boot (Java 17) + MySQL 8 + HTML/CSS/JavaScript (Bootstrap 5)**
with **two role-based dashboards** – one for **Admin / Warden** and one for **Students**.

---

## 1. Project Introduction
The Hostel Management System digitises everyday hostel work: student records, room allocation, fee tracking,
complaints, attendance and notices. The warden manages everything from one dashboard; every student
logs in to a separate dashboard where they see only their own room, fees, attendance and complaints.

## 2. Features
**Admin / Warden dashboard**
- Overview cards: total students, total rooms, occupied rooms, vacant rooms, pending complaints, pending fees
- Students: add, edit, delete, search, view details
- Rooms: add, edit, delete, filter (available / occupied / vacant)
- Room allocation, change room, vacate room
- Process room-change requests (approve with a chosen room / reject)
- Fees: add fee, mark paid (with payment mode), view paid / pending
- Complaints: view and update status (Pending → In progress → Resolved) with remarks
- Attendance: mark Present / Absent / Leave for any date
- Notices: add, edit, delete
- Reports: fee collection, occupancy by block, complaints by status, students by course (printable)

**Student dashboard**
- Summary cards: Room Number, Fee Status, Attendance %, Pending Complaints
- Profile, room details, block details, roommates
- Fee status and payment history
- Attendance record
- Submit complaints and track their status
- Request a room change and track the request
- Hostel notices

**Security**
- Session-based login, BCrypt-hashed passwords
- Role check on every API call (`ADMIN`, `WARDEN`, `STUDENT`)
- Dashboard pages themselves are protected (a student opening `/admin` is redirected to `/student`)
- Only `ADMIN` can delete records; `WARDEN` has all other admin powers

## 3. Technologies Used
| Layer | Technology |
|-------|-----------|
| Frontend | HTML, CSS, JavaScript (Fetch API), Bootstrap 5, Bootstrap Icons |
| Backend | Java 17, Spring Boot 3.2, Spring MVC (REST), Spring Data JPA, Hibernate |
| Database | MySQL 8 |
| Build tool | Maven |
| Password hashing | BCrypt (spring-security-crypto) |

> Bootstrap and the icons are loaded from a CDN, so the **browser needs internet access** to look styled.

## 4. Project Structure
```
hostel-management-system/
├── pom.xml                         Maven configuration + dependencies
├── README.md
├── database/hostel_management.sql  Optional: create DB + tables manually
├── scripts/                        Windows helper scripts (run.bat, build.bat ...)
└── src/main/
    ├── java/com/hostelmanagement/
    │   ├── HostelManagementApplication.java   Starts the app
    │   ├── controller/   REST controllers (+ PageController for the 2 dashboards)
    │   ├── service/      Business logic (allocation rules, fee payment ...)
    │   ├── repository/   Spring Data JPA interfaces (database access)
    │   ├── entity/       JPA entities = database tables
    │   ├── dto/          Small request/response objects
    │   ├── config/       AuthInterceptor (role check), WebConfig, DataLoader (demo data)
    │   ├── exception/    Error handling -> JSON { "message": "..." }
    │   └── util/         Validation helpers
    └── resources/
        ├── application.properties
        ├── static/       login.html, css/, js/ (common.js, login.js, admin.js, student.js), images/
        └── protected/    admin.html, student.html  (only served after login, by PageController)
```

## 5. Software Requirements
- Windows 10/11
- JDK 17 (or newer, e.g. 21)
- Apache Maven 3.8+
- MySQL Server 8
- VS Code with the *Extension Pack for Java* (optional but recommended)
- A web browser (Chrome / Edge) with internet access (for Bootstrap CDN)

## 6. How to Install Java
1. Download **JDK 17** from https://adoptium.net (Temurin 17, Windows x64 `.msi`).
2. Run the installer and tick **"Set JAVA_HOME variable"** and **"Add to PATH"**.
3. Open a **new** Command Prompt and check: `java -version` (must show 17 or higher) and `javac -version`.

## 7. How to Install Maven
1. Download the *Binary zip archive* from https://maven.apache.org/download.cgi and extract it, e.g. to `C:\apache-maven-3.9.6`.
2. Add `C:\apache-maven-3.9.6\bin` to the **Path** environment variable (Start → "Edit the system environment variables" → Environment Variables → Path → New).
3. Open a **new** Command Prompt and check: `mvn -version`.

## 8. How to Install MySQL
1. Download **MySQL Installer** from https://dev.mysql.com/downloads/installer/ and choose *MySQL Server 8.x* (Server only is enough).
2. During setup set the **root password to `root`** (this matches the default in `application.properties`; any password works if you change the config).
3. Keep the default port **3306**. Make sure the *MySQL80* Windows service is running.
4. Check in Command Prompt: `mysql --version` (add `C:\Program Files\MySQL\MySQL Server 8.0\bin` to Path if not found).

## 9. How to Create the Database
**Option A – automatic (easiest):** do nothing. The connection URL contains `createDatabaseIfNotExist=true`, so the app creates `hostel_management` and all tables on first start.

**Option B – manual:**
```
mysql -u root -p
```
then inside MySQL:
```sql
CREATE DATABASE IF NOT EXISTS hostel_management CHARACTER SET utf8mb4;
SHOW DATABASES;
EXIT;
```
or run the full script: `mysql -u root -p < database\hostel_management.sql`  (or double-click `scripts\setup-mysql.bat`).

The demo data (users, rooms, students, fees, complaints, notices) is inserted **automatically on the first run** when the database is empty.

## 10. How to Configure `application.properties`
File: `src/main/resources/application.properties`

| Setting | Default | Environment variable |
|---------|---------|----------------------|
| Database URL | `jdbc:mysql://localhost:3306/hostel_management?...` | `DB_URL` |
| MySQL username | `root` | `DB_USERNAME` |
| MySQL password | `root` | `DB_PASSWORD` |
| Server port | `8080` | `PORT` |
| Hibernate DDL mode | `update` | `DDL_AUTO` |

**Important:** if your MySQL root password is *not* `root`, change `spring.datasource.password` (or set `DB_PASSWORD`) before running.

## 11. How to Run the Project (Windows + VS Code)
1. Extract the ZIP and open the folder in VS Code (**File → Open Folder → hostel-management-system**).
2. Make sure MySQL is running.
3. Open the VS Code terminal (**Ctrl + `**) and run:
   ```
   mvn spring-boot:run
   ```
   (the first run downloads dependencies – needs internet and takes 1–3 minutes)
4. Wait for: `Started HostelManagementApplication`.
5. Open **http://localhost:8080** in your browser.

Alternatively double-click `scripts\run.bat`. To stop the app press **Ctrl + C** in the terminal.

## 12. Login Credentials
| Role | Username | Password | Opens |
|------|----------|----------|-------|
| Admin | `admin` | `admin123` | Admin dashboard (`/admin`) |
| Warden | `warden` | `warden123` | Admin/Warden dashboard (`/admin`) |
| Student | `student` | `student123` | Student dashboard (`/student`) – this is Rahul Sharma |

Other demo students (password `student123` for all): `priya`, `arjun`, `sneha`, `vikram`, `ananya`, `rohit`, `kavya`.
`rohit` has no room yet – handy for demonstrating **Allocate Room**. `arjun` has a pending room change request.

**How the dashboards are reached:** you always start at `http://localhost:8080` → login page. After login the server returns the
role and the browser goes to `/admin` (ADMIN/WARDEN) or `/student` (STUDENT). Typing `/admin` while logged in as a student redirects back to `/student`.

## 13. Admin Dashboard Features (sidebar)
Dashboard · Students · Rooms · Room Allocation · Room Change Requests · Fees · Complaints · Attendance · Notices · Reports · Logout

## 14. Student Dashboard Features (sidebar)
Dashboard · My Profile · My Room (room, block, roommates) · Fees & Payments · Attendance · Complaints · Room Change · Notices · Logout

## 15. Database Structure
| Table | Purpose | Key relationships |
|-------|---------|-------------------|
| `users` | Login accounts, role | – |
| `students` | Student profile | 1-to-1 with `users` |
| `rooms` | Rooms, block, capacity, rent | – |
| `room_allocations` | Who lives in which room (`active` flag keeps history) | many-to-one → `students`, `rooms` |
| `fees` | Fee records, PENDING / PAID | many-to-one → `students` |
| `complaints` | Complaints and status | many-to-one → `students` |
| `attendance` | One row per student per day (unique) | many-to-one → `students` |
| `notices` | Notice board | – |
| `room_change_requests` | Student requests | many-to-one → `students`, `rooms` (current + requested) |

Relationships in JPA are `@ManyToOne` / `@OneToOne` with `@JoinColumn`. Tables are generated by Hibernate from the entity classes.

## 16. API Overview
All responses are JSON. Errors look like `{ "message": "..." }`.

**Auth (public):** `POST /api/auth/login` · `POST /api/auth/logout` · `GET /api/auth/me`

**Admin/Warden** (`/api/admin/**`, ADMIN or WARDEN; `DELETE` = ADMIN only)
| Resource | Endpoints |
|----------|-----------|
| Dashboard | `GET /dashboard`, `GET /reports` |
| Students | `GET /students?q=`, `GET /students/{id}`, `POST /students`, `PUT /students/{id}`, `DELETE /students/{id}` |
| Rooms | `GET /rooms?filter=all\|available\|occupied\|vacant`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Allocations | `GET /allocations`, `POST /allocations`, `POST /allocations/change`, `PUT /allocations/{id}/vacate` |
| Room requests | `GET /room-requests`, `PUT /room-requests/{id}/approve`, `PUT /room-requests/{id}/reject` |
| Fees | `GET /fees?status=`, `POST /fees`, `PUT /fees/{id}/pay`, `DELETE /fees/{id}` |
| Complaints | `GET /complaints`, `PUT /complaints/{id}` |
| Attendance | `GET /attendance?date=yyyy-mm-dd`, `POST /attendance` |
| Notices | `GET /notices`, `POST`, `PUT /{id}`, `DELETE /{id}` |

**Student** (`/api/student/**`, STUDENT only – always the logged-in student's own data)
`GET /dashboard` · `/profile` · `/room` · `/fees` · `/attendance` · `/complaints` · `POST /complaints` · `/notices` · `/available-rooms` · `/room-requests` · `POST /room-requests`

## 17. How to Build the Project
```
mvn clean package -DskipTests
```
(or `scripts\build.bat`)

## 18. How to Create the JAR
The command above creates **`target\hostel-management.jar`**. Run it with:
```
java -jar target\hostel-management.jar
```

## 19. Basic Deployment Instructions
The app reads its configuration from environment variables, so it can be deployed to any cloud platform
(Railway, Render, AWS, Azure ...):
1. Create a managed **MySQL 8** database on the platform.
2. Build the JAR (`mvn clean package -DskipTests`).
3. Set environment variables on the platform:
   ```
   DB_URL=jdbc:mysql://<host>:3306/<database>?useSSL=true&serverTimezone=UTC
   DB_USERNAME=<user>
   DB_PASSWORD=<password>
   PORT=8080            (many platforms set this automatically)
   ```
4. Start command: `java -jar hostel-management.jar`
5. **Change the demo passwords** (or delete the demo users) before using the app for real.

On a plain Windows/Linux server: install Java 17 + MySQL, copy the JAR, set the variables and run the command in step 4.

---

## Viva Cheat-Sheet (short explanations)
- **Frontend**: HTML pages styled with Bootstrap; JavaScript uses `fetch()` to call the REST APIs and builds tables/forms dynamically.
- **Backend / Spring Boot**: Java application with embedded Tomcat; `mvn spring-boot:run` starts everything on port 8080.
- **MVC / layers**: *Controller* (receives HTTP requests) → *Service* (business rules) → *Repository* (database) → *Entity* (table). DTO records carry request data.
- **REST API**: URLs + HTTP methods (GET read, POST create, PUT update, DELETE remove) returning JSON.
- **JPA / Hibernate**: annotations such as `@Entity`, `@Id`, `@ManyToOne` map Java classes to MySQL tables; Hibernate generates the SQL. `JpaRepository` gives CRUD methods for free.
- **Authentication**: login checks the BCrypt password hash, then stores `USER_ID` and `ROLE` in the HTTP session.
- **Role-based access**: `AuthInterceptor` checks the session role for every `/api/admin/**` and `/api/student/**` request; `PageController` protects the two dashboard pages.
- **Room allocation**: `AllocationService` checks the room has a free bed and the student has no active room; changing a room closes the old allocation and opens a new one.
- **Fee management**: each fee row is PENDING or PAID; "Mark Paid" sets the paid date and payment mode. Pending fees past the due date show as OVERDUE.
- **Complaint management**: students create complaints (PENDING); the warden moves them to IN_PROGRESS / RESOLVED and adds remarks.

## Troubleshooting
| Problem | Fix |
|---------|-----|
| `Access denied for user 'root'` | Wrong MySQL password. Update `spring.datasource.password` or set `DB_PASSWORD`. |
| `Communications link failure` | MySQL service is not running (start *MySQL80* in Windows Services). |
| `Port 8080 was already in use` | Set `PORT=8081` (e.g. `set PORT=8081` then `mvn spring-boot:run`) and open `http://localhost:8081`. |
| `mvn` is not recognized | Maven is not in PATH – reopen the terminal after editing PATH. |
| Page has no styling | The browser has no internet (Bootstrap CDN). |
| Want fresh demo data | In MySQL run `DROP DATABASE hostel_management;` and start the app again. |
