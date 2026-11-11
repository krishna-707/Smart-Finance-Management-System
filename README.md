# FINORA – Smart Finance Management System

A Java Swing desktop application using JDBC and MySQL. This project intentionally uses no Spring Boot, Maven, web server, or HTML frontend.

## Requirements
- JDK 17 or later
- MySQL Server 8.x (MySQL 8.4 is suitable)
- MySQL Connector/J JAR

## Setup
1. Open MySQL Workbench and run `finora_db.sql`.
2. Open `src/database/DBConnection.java`.
3. Set `DB_USER` and `DB_PASSWORD` to your MySQL credentials.
4. Download MySQL Connector/J and copy the JAR into `lib/`.
5. In IntelliJ/Eclipse, add the JAR from `lib/` to the project libraries and mark `src` as the source root.
6. Run `src/Main.java`.

## Command-line run (Windows PowerShell)
From the FINORA folder, after placing the Connector/J JAR in `lib`:

```powershell
$jar = (Get-ChildItem .\lib\*.jar | Select-Object -First 1).FullName
New-Item -ItemType Directory -Force out | Out-Null
javac -cp "$jar" -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp "out;$jar" Main
```

If no Connector/J JAR is present, compilation will fail because JDBC driver classes are required at runtime. The JDK itself includes the JDBC API, but not the MySQL driver.

## First use
Choose **Create account**, register, then log in. A Main Wallet and starter categories are created automatically.

## Notes
- Amounts are stored using MySQL DECIMAL.
- Passwords are stored as salted SHA-256 hashes with repeated hashing. For a production financial application, use a modern password-hashing algorithm such as Argon2id or bcrypt.
- This is a local academic project. It does not connect to banks or provide real financial advice.
