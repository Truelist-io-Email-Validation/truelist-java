# Changelog

All notable changes to this project will be documented in this file.

## [0.1.0] - 2026-02-20

### Added

- Initial release
- Email validation via `validate()` (server-side, POST /api/v1/verify)
- Form validation via `formValidate()` (frontend, POST /api/v1/form_verify)
- Account info via `getAccount()` (GET /api/v1/account)
- Builder pattern for client configuration
- Configurable base URL, timeout, and max retries
- Automatic retries with exponential backoff for transient errors
- Typed exceptions: `AuthenticationException`, `RateLimitException`, `ApiException`
- Result predicates: `isValid()`, `isInvalid()`, `isRisky()`, `isUnknown()`, `isFreeEmail()`, `isRole()`, `isDisposable()`
- Thread-safe client using `java.net.http.HttpClient`
