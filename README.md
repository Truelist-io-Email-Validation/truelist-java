# Truelist Java SDK

Official Java SDK for the [Truelist.io](https://truelist.io) email validation API.

[![CI](https://github.com/Truelist-io-Email-Validation/truelist-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Truelist-io-Email-Validation/truelist-java/actions/workflows/ci.yml)

## Requirements

- Java 11+
- Gradle 8+ (for building from source)

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.truelist:truelist-java:0.1.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.truelist:truelist-java:0.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.truelist</groupId>
    <artifactId>truelist-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```java
import io.truelist.Truelist;
import io.truelist.ValidationResult;

Truelist client = Truelist.builder("your-api-key").build();

ValidationResult result = client.validate("user@example.com");

if (result.isValid()) {
    System.out.println("Email is valid!");
}
```

## Usage

### Create a Client

```java
import io.truelist.Truelist;
import java.time.Duration;

Truelist client = Truelist.builder("your-api-key")
    .baseUrl("https://api.truelist.io")   // default
    .timeout(Duration.ofSeconds(10))       // default: 30s
    .maxRetries(2)                         // default: 2
    .build();
```

The client is thread-safe and should be reused across your application.

### Validate an Email

```java
ValidationResult result = client.validate("user@example.com");

result.getEmail();      // "user@example.com"
result.getDomain();     // "example.com"
result.getCanonical();  // "user"
result.getState();      // "ok", "email_invalid", "accept_all", or "unknown"
result.getSubState();   // "email_ok", "is_disposable", "is_role", etc.
result.getSuggestion(); // suggested correction, or null
result.getMxRecord();   // MX record for the domain, or null
result.getFirstName();  // first name, or null
result.getLastName();   // last name, or null
result.getVerifiedAt(); // verification timestamp
result.isValid();       // true if state is "ok"
result.isInvalid();     // true if state is "email_invalid"
result.isAcceptAll();   // true if state is "accept_all"
result.isUnknown();     // true if state is "unknown"
result.isDisposable();  // true if sub-state is "is_disposable"
result.isRole();        // true if sub-state is "is_role"
```

### Get Account Info

```java
import io.truelist.AccountInfo;

AccountInfo account = client.getAccount();

account.getEmail();                    // "you@company.com"
account.getName();                     // "Your Name"
account.getUuid();                     // "a3828d19-..."
account.getTimeZone();                 // "America/New_York"
account.isAdminRole();                 // true/false
account.getPlan();                     // "pro" (shortcut)
account.getAccount().getName();        // "Company Inc"
account.getAccount().getPaymentPlan(); // "pro"
```

## Response States

| State           | Description                          |
|-----------------|--------------------------------------|
| `ok`            | Email address is valid               |
| `email_invalid` | Email address is invalid             |
| `accept_all`    | Domain accepts all addresses         |
| `unknown`       | Could not determine validity         |

### Sub-states

| Sub-state           | Description                        |
|---------------------|------------------------------------|
| `email_ok`          | Valid, no issues                   |
| `is_disposable`     | Disposable/temporary email         |
| `is_role`           | Role-based address (admin@, etc.)  |
| `failed_smtp_check` | SMTP check failed                  |
| `unknown_error`     | Could not determine                |

## Error Handling

```java
import io.truelist.exceptions.*;

try {
    ValidationResult result = client.validate("user@example.com");
} catch (AuthenticationException e) {
    // Invalid API key (HTTP 401) - always thrown, never retried
    System.err.println("Bad API key: " + e.getMessage());
} catch (RateLimitException e) {
    // Too many requests (HTTP 429)
    System.err.println("Slow down: " + e.getMessage());
} catch (ApiException e) {
    // Server error (5xx) or unexpected status
    System.err.println("API error (" + e.getStatusCode() + "): " + e.getMessage());
} catch (TruelistException e) {
    // Network error or other failure
    System.err.println("Request failed: " + e.getMessage());
}
```

### Retry Behavior

The client automatically retries on transient errors with exponential backoff. Configure with `maxRetries()`:

- **Authentication errors (401)**: Never retried, always thrown immediately
- **Rate limit errors (429)**: Retried up to `maxRetries` times
- **Server errors (5xx)**: Retried up to `maxRetries` times
- **Network errors**: Retried up to `maxRetries` times

```java
// Disable retries
Truelist client = Truelist.builder("key").maxRetries(0).build();

// More aggressive retries
Truelist client = Truelist.builder("key").maxRetries(5).build();
```

## Development

### Build

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Requirements

- Java 11+
- Gradle 8+

## License

MIT - see [LICENSE](LICENSE) for details.
