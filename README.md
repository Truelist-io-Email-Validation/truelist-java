# Truelist Java SDK

Official Java SDK for the [Truelist.io](https://truelist.io) email validation API.

[![CI](https://github.com/truelist/truelist-java/actions/workflows/ci.yml/badge.svg)](https://github.com/truelist/truelist-java/actions/workflows/ci.yml)

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
    .formApiKey("your-form-key")           // optional, for formValidate()
    .build();
```

The client is thread-safe and should be reused across your application.

### Validate an Email (Server-side)

Use `validate()` with your server API key for backend validation. Rate limit: 10 requests/second.

```java
ValidationResult result = client.validate("user@example.com");

result.getState();      // "valid", "invalid", "risky", or "unknown"
result.getSubState();   // "ok", "disposable_address", "role_address", etc.
result.getSuggestion(); // suggested correction, or null
result.isValid();       // true if state is "valid"
result.isValid(true);   // true if state is "valid" or "risky"
result.isInvalid();     // true if state is "invalid"
result.isRisky();       // true if state is "risky"
result.isUnknown();     // true if state is "unknown"
result.isFreeEmail();   // true if free email provider (Gmail, Yahoo, etc.)
result.isRole();        // true if role address (admin@, support@, etc.)
result.isDisposable();  // true if disposable/temporary address
```

### Validate an Email (Frontend/Form)

Use `formValidate()` with your form API key for frontend validation. Rate limit: 60 requests/minute.

```java
Truelist client = Truelist.builder("your-server-key")
    .formApiKey("your-form-key")
    .build();

ValidationResult result = client.formValidate("user@example.com");
```

### Get Account Info

```java
import io.truelist.AccountInfo;

AccountInfo account = client.getAccount();

account.getEmail();   // "you@company.com"
account.getPlan();    // "pro"
account.getCredits(); // 9542
```

## Response States

| State     | Description                          |
|-----------|--------------------------------------|
| `valid`   | Email address is valid               |
| `invalid` | Email address is invalid             |
| `risky`   | Email exists but may have issues     |
| `unknown` | Could not determine validity         |

### Sub-states

| Sub-state              | Description                        |
|------------------------|------------------------------------|
| `ok`                   | Valid, no issues                   |
| `accept_all`           | Domain accepts all addresses       |
| `disposable_address`   | Disposable/temporary email         |
| `role_address`         | Role-based address (admin@, etc.)  |
| `failed_mx_check`      | Domain has no MX records           |
| `failed_spam_trap`     | Known spam trap address            |
| `failed_no_mailbox`    | Mailbox does not exist             |
| `failed_greylisted`    | Server temporarily rejected        |
| `failed_syntax_check`  | Invalid email syntax               |
| `unknown`              | Could not determine                |

## Error Handling

```java
import io.truelist.exceptions.*;

try {
    ValidationResult result = client.validate("user@example.com");
} catch (AuthenticationException e) {
    // Invalid API key (HTTP 401) - always thrown, never retried
    System.err.println("Bad API key: " + e.getMessage());
} catch (RateLimitException e) {
    // Too many requests (HTTP 429) - never retried
    System.err.println("Slow down: " + e.getMessage());
} catch (ApiException e) {
    // Server error (5xx) or unexpected status - retried per maxRetries config
    System.err.println("API error (" + e.getStatusCode() + "): " + e.getMessage());
} catch (TruelistException e) {
    // Network error or other failure - retried per maxRetries config
    System.err.println("Request failed: " + e.getMessage());
}
```

### Retry Behavior

The client automatically retries on transient errors (5xx server errors, network failures) with exponential backoff. Configure with `maxRetries()`:

- **Authentication errors (401)**: Never retried, always thrown immediately
- **Rate limit errors (429)**: Never retried, always thrown immediately
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
