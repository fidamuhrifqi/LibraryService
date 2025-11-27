**Project Overview**
A backend service built with Spring Boot, providing JWT authentication, RBAC authorization, article management, audit logging, rate limiting, and Redis caching.
Includes full test coverage (>80%).
Developed using Java 21 and Spring Boot 3.5.7.

**Tech Stack**

Backend
- Java 21
- Spring Boot 3.5.7
- Spring Security + JWT
- Spring Validation
- Spring Data JPA (Hibernate)
- Spring Cache + Redis
- PostgreSQL
- Docker & Docker Compose
- Lombok
- JUnit & Mockito

Other Tools
- Swagger OpenAPI 3
- Beaver
- Postman
- docker desktop
- intellij IDEA
- Another Redis desktop Manager



**Architecture**
This project uses a layered architecture:
Controller → service → DAO → entity, plus DTO.
for repository I used pattern DAO + implementasi



**Features**
1. User Authentication
	- User registration with validation
	- Login using username or email
	- Password encrypted using BCrypt
	- MFA (Multi-Factor Authentication) using OTP via Email (It pointed to the real email in the database even though the Mailtrap configuration was already present but commented out)
	- JWT Access Token generation
	- Custom Authentication & AccessDenied handlers

2. RBAC (Role-Based Access Control)
| Role                 | Permissions                                             |
| -------------------- | ------------------------------------------------------- |
| Super Admin          | Full CRUD Users, CRUD all articles, view all audit logs |
| Editor               | CRUD only own articles, view all articles               |
| Contributor          | Create & update own articles, no delete                 |
| Viewer (Default      | View only **public articles**                           |

3. RBAC implemented using:
	- GrantedAuthority
	- Extracted role from JWT
	- Authorization checks inside Service layer

4. Article Management
	- Create / Read / Update / Delete Articles
	- Ownership validation (Editor & Contributor can only modify their own content)
	- Public vs Private article filtering
	- Sorting support using bubble sort algorithm (ASC / DESC)

5. User Management
	- CRUD User (Super Admin only)
	- Soft validation (duplicate email, username, password rules)
	- Sorting support using bubble sort algorithm (ASC / DESC)

6. Audit Logging
	- Login
	- User CRUD
	- Article CRUD
	- Includes:
	   - timestamp
	   - userId
	   - resourceId
	   - IP
	   - device/browser
	   - action type

7. Rate Limiting (Redis)
	Implemented using Redis INCR, EXPIRE:
	- 60 requests / 60 seconds per user/IP
	- Returns:
	  - X-RateLimit-Remaining
	  - Retry-After
	  - 429 Too Many Requests on limit exceeded
  
8. Payload Validation
	- Request DTO validation using:
	  - @NotNull
	  - @NotBlank
	  - @Email
	- global exception handler:
	  - Validation errors
	  - Path variable errors
	  - Runtime exceptions
	  - Custom 404, 400, 401, 403 handling
  
9. Unit Testing (covered atleast 80% of Class, Method, Line, and Branch)
	Coverage includes:
	- Service logic tests
	- Validation tests
	- Utility tests (OTP, JWT)
	- Using:
	  - JUnit 5
	  - Mockito
	  - IntelliJ IDEA is used to view unit test coverage


**Database table and field**

	Users Table
	 id varchar(255)
	 username varchar(255)
	 email varchar(255)
	 password varchar(255)
	 full_name varchar(255)
	 role varchar(255)
	 created_at timestamp
	 updated_at timestamp
	 last_login timestamp
	 account_locked timestamp(6)
	 failed_login int4
	 last_failed_login timestamp(6)
	 otp_code varchar(255)
	 otp_expired timestamp(6)

	Articles Table
	 id varchar(255)
	 title varchar(255)
	 content varchar(255)
	 author_id varchar(255)
	 created_at timestamp
	 updated_at timestamp
	 is_public bool

	AuditLog Table
	 id varchar(255)
	 username varchar(255)
	 action varchar(255)
	 resource_ty varchar(255)
	 resource_id varchar(255)
	 method varchar(255)
	 path varchar(255)
	 user_agent varchar(255)
	 ip_address varchar(255)
	 timestamp timestamp



**Docker Setup**
docker-compose.yml service:
 -app
 -postgres:
 -redis:
with volume for postgres and redis


**Swagger / API Documentation (for access API)**
http://localhost:8080/swagger-ui/index.html
for the first login, login using SUPER_ADMIN existing to get token:
username 		: fida
password 		: P@ssw0rd

These credentials are used only for accessing the email inbox to retrieve the OTP sent during Multi-Factor Authentication:
Email 	 		: contact.fidamuhrifqi@gmail.com
Email Password 	: Test123123!


**How to Run**
* Make sure Docker is installed and running on your device or you can download on https://www.docker.com/products/docker-desktop
* Make sure WSL is installed on your device.
	
	1. Run using run.bat (only os window device)
	   Double click on run.bat
	2. Manual build & run (optional)
	   - BUILD jar Spring Boot *already builded you can skip it
	     .\mvnw clean package -DskipTests
		- run docker (Spring + Postgres + Redis)
		  docker compose up --build
		  
*port app run at 8080
*for get method please fill the path variable with asc/desc

**Testing**
Run unit tests:
.\mvnw test





Fida Muhamad Rifqi
Backend Engineer – Java Spring Boot

Indonesia
