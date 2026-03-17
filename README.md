# SQL Injection Login Demo (Spring Boot + MySQL)

Demo nay co 2 API dang nhap:

- `POST /api/auth/login`: co tinh viet khong an toan (SQL Injection)
- `POST /api/auth/login-safe`: ban sua an toan dung `PreparedStatement`

## 1) Cau hinh database

Mac dinh app dung:

- DB: `websec`
- User: `root`
- Password: `123456`
- Port: `3306`

Neu khac, sua trong file `src/main/resources/application.properties`.

Import schema du lieu:

```sql
SOURCE sql_demo.sql;
```

## 2) Chay ung dung

```powershell
mvn spring-boot:run
```

## 3) Test API

### 3.1 Login dung (vulnerable)

```powershell
curl -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

### 3.2 SQL Injection vao endpoint vulnerable

Payload mau:

- username: `admin' OR '1'='1' -- `
- password: bat ky

```powershell
curl -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"admin' OR '1'='1' -- \",\"password\":\"abc\"}"
```

Ky vong: endpoint vulnerable co the tra ve user du password sai.

### 3.3 Thu payload do voi endpoint safe

```powershell
curl -X POST "http://localhost:8080/api/auth/login-safe" -H "Content-Type: application/json" -d "{\"username\":\"admin' OR '1'='1' -- \",\"password\":\"abc\"}"
```

Ky vong: endpoint safe tra `401`.

## 4) Vi sao 2 cach khac nhau?

- Vulnerable: ghep chuoi SQL truc tiep => input cua user tro thanh mot phan cau lenh SQL.
- Safe: dung `PreparedStatement` + placeholder `?` => input duoc binding nhu data, khong bi parser nhu code SQL.

## 5) File chinh

- `src/main/java/com/example/demo/auth/AuthRepository.java`
  - `loginVulnerable(...)`: co tinh SQL Injection
  - `loginSafe(...)`: ban fix an toan

