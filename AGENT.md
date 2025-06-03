# AGENT.md - Legacy Java E-commerce API

## Build Commands
- Build: `mvn clean compile`
- Test: `mvn test`
- Run app: `mvn spring-boot:run`
- Single test: `mvn test -Dtest=UserServiceTest`
- Single test method: `mvn test -Dtest=UserServiceTest#testCreateUser_Success`

## Tech Stack
- Java 22, Spring Boot 3.2.12, Spring Data JPA, H2 database, Maven, JUnit 4

## Code Style
- Traditional layered architecture: Controller → Service → Repository → Model
- Package structure: `com.ecommerce.legacy.{controller,service,repository,model}`
- Constructor injection with `@Autowired`
- Use HashMap responses instead of DTOs: `createErrorResponse("message")`
- Manual exception handling in controllers with ResponseEntity
- JUnit 4 with `@RunWith(MockitoJUnitRunner.class)`, `@Mock`, `@InjectMocks`
- Test setup in `@Before` methods, assertions with JUnit 4 style
- Use `javax.persistence` imports, not `jakarta`
- Apache Commons utilities (commons-lang3, commons-collections)
- Verbose getter/setter patterns, manual validation in services

## Database Access
- H2 console: http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`, Username: `sa`, Password: (blank)
