# JWT Authentication Integration Plan

**Project**: grace-manage-rwc (Angular 18) + grace-manage-service (Spring Boot 4)
**Date**: 2026-01-11
**Status**: Ready for Implementation
**Version**: 2.0 (Bearer Token Exchange with Refresh Tokens)

---

## Table of Contents
1. [Overview](#overview)
2. [User Preferences](#user-preferences)
3. [Authentication Flow Diagrams](#authentication-flow-diagrams)
4. [Backend Implementation](#backend-implementation)
5. [Frontend Implementation](#frontend-implementation)
6. [Testing Strategy](#testing-strategy)
7. [Verification Steps](#verification-steps)
8. [Security Considerations](#security-considerations)

---

## Overview

This plan integrates JWT-based authentication between the Angular frontend and Spring Boot backend using **Bearer token exchange** with **refresh token support** for secure and flexible token management.

### Current State

**Backend (grace-manage-service)**:
- Spring Security configured with OAuth2 dependencies
- All API endpoints currently permit anonymous access (`.permitAll()`)
- Passwords stored as plaintext (not hashed)
- No JWT implementation
- Clean architecture structure in place (domain, application, infrastructure, presentation layers)

**Frontend (grace-manage-rwc)**:
- Mock authentication with 3 hardcoded users (admin/admin123, user/user123, demo/demo123)
- User data and session expiry stored in localStorage
- Auth guards protect routes (authGuard, adminGuard)
- No HTTP interceptors configured
- API calls don't include authentication headers

### Goals

- Implement real JWT-based authentication with **Bearer token exchange**
- Store tokens in **localStorage** (with CSP mitigation for XSS protection)
- Implement **dual-token strategy**: 15-minute access token + 7-day refresh token
- Hash passwords with BCrypt
- Add role field to User entity (admin, user, viewer)
- Enable role-based access control
- Replace mock authentication with backend integration

---

## User Preferences

✓ **JWT Library**: jjwt (io.jsonwebtoken) v0.12.5
✓ **Token Storage**: localStorage + Bearer header (with CSP mitigation)
✓ **Token Strategy**: Dual-token (15-min access token + 7-day refresh token)
✓ **Refresh Tokens**: Yes, via `/api/v1/auth/refresh` endpoint
✓ **RBAC**: Simple role field in User entity (not separate Role table)
✓ **Logout**: Client-only (clear localStorage, no server-side blacklist)

### Token Strategy Details

| Token Type | Expiration | Storage | Purpose |
|------------|------------|---------|---------|
| **Access Token** | 15 minutes | localStorage | Authenticate API requests via Bearer header |
| **Refresh Token** | 7 days | localStorage | Obtain new access token when expired |

### Security Trade-offs

**Bearer Tokens in localStorage**:
- ⚠️ Vulnerable to XSS attacks (unlike HttpOnly cookies)
- ✅ Simpler CORS configuration (no credential handling)
- ✅ Works across different domains/subdomains easily
- ✅ Easier to implement token refresh logic

**Mitigation Strategy**:
- Implement Content Security Policy (CSP) headers
- Sanitize all user inputs
- Short access token lifetime (15 minutes) limits exposure
- Use refresh token rotation (optional future enhancement)

---

## Authentication Flow Diagrams

### Token Exchange & Refresh Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BEARER TOKEN AUTHENTICATION FLOW                         │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: INITIAL LOGIN                                                       │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────┐                                              ┌─────────────┐   │
│  │ Angular │                                              │ Spring Boot │   │
│  │Frontend │                                              │   Backend   │   │
│  └────┬────┘                                              └──────┬──────┘   │
│       │                                                          │          │
│       │  1. POST /api/v1/auth/login                              │          │
│       │     { username, password }                               │          │
│       │─────────────────────────────────────────────────────────>│          │
│       │                                                          │          │
│       │                      2. Validate credentials             │          │
│       │                         ├─ Find user by username         │          │
│       │                         ├─ Verify BCrypt password        │          │
│       │                         └─ Check if user is active       │          │
│       │                                                          │          │
│       │                      3. Generate tokens                  │          │
│       │                         ├─ Access Token (15 min)         │          │
│       │                         │   Claims: id, username,        │          │
│       │                         │   email, role, type:"access"   │          │
│       │                         └─ Refresh Token (7 days)        │          │
│       │                             Claims: id, username,        │          │
│       │                             type:"refresh"               │          │
│       │                                                          │          │
│       │  4. 200 OK                                               │          │
│       │     {                                                    │          │
│       │       accessToken: "eyJhbG...",                          │          │
│       │       refreshToken: "eyJhbG...",                         │          │
│       │       expiresIn: 900,                                    │          │
│       │       user: { id, username, email, role }                │          │
│       │     }                                                    │          │
│       │<─────────────────────────────────────────────────────────│          │
│       │                                                          │          │
│       │  5. Store in localStorage:                               │          │
│       │     ├─ accessToken                                       │          │
│       │     ├─ refreshToken                                      │          │
│       │     ├─ tokenExpiry (Date.now() + 900000)                 │          │
│       │     └─ currentUser                                       │          │
│       │                                                          │          │
│       │  6. Navigate to /home                                    │          │
│       ▼                                                          ▼          │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ PHASE 2: AUTHENTICATED API REQUEST                                           │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────┐                                              ┌─────────────┐   │
│  │ Angular │                                              │ Spring Boot │   │
│  │Frontend │                                              │   Backend   │   │
│  └────┬────┘                                              └──────┬──────┘   │
│       │                                                          │          │
│       │  1. GET /api/v1/users/123                                │          │
│       │     Headers:                                             │          │
│       │       Authorization: Bearer eyJhbG...                    │          │
│       │─────────────────────────────────────────────────────────>│          │
│       │                                                          │          │
│       │                      2. JwtAuthenticationFilter          │          │
│       │                         ├─ Extract token from header     │          │
│       │                         ├─ Validate signature            │          │
│       │                         ├─ Check expiration              │          │
│       │                         ├─ Verify type = "access"        │          │
│       │                         └─ Set SecurityContext           │          │
│       │                                                          │          │
│       │                      3. Process request                  │          │
│       │                         └─ Return user data              │          │
│       │                                                          │          │
│       │  4. 200 OK { user data }                                 │          │
│       │<─────────────────────────────────────────────────────────│          │
│       ▼                                                          ▼          │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ PHASE 3: TOKEN REFRESH (Access Token Expired)                                │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────┐                                              ┌─────────────┐   │
│  │ Angular │                                              │ Spring Boot │   │
│  │Frontend │                                              │   Backend   │   │
│  └────┬────┘                                              └──────┬──────┘   │
│       │                                                          │          │
│       │  1. Interceptor detects token expired                    │          │
│       │     (before making request or after 401)                 │          │
│       │                                                          │          │
│       │  2. POST /api/v1/auth/refresh                            │          │
│       │     Headers:                                             │          │
│       │       Authorization: Bearer <refreshToken>               │          │
│       │─────────────────────────────────────────────────────────>│          │
│       │                                                          │          │
│       │                      3. Validate refresh token           │          │
│       │                         ├─ Validate signature            │          │
│       │                         ├─ Check expiration              │          │
│       │                         ├─ Verify type = "refresh"       │          │
│       │                         └─ Load user from DB             │          │
│       │                                                          │          │
│       │                      4. Generate NEW access token        │          │
│       │                         └─ Access Token (15 min)         │          │
│       │                                                          │          │
│       │  5. 200 OK                                               │          │
│       │     {                                                    │          │
│       │       accessToken: "eyJhbG...(NEW)",                     │          │
│       │       expiresIn: 900                                     │          │
│       │     }                                                    │          │
│       │<─────────────────────────────────────────────────────────│          │
│       │                                                          │          │
│       │  6. Update localStorage:                                 │          │
│       │     ├─ accessToken (new)                                 │          │
│       │     └─ tokenExpiry (new)                                 │          │
│       │                                                          │          │
│       │  7. Retry original request with new token                │          │
│       │─────────────────────────────────────────────────────────>│          │
│       │                                                          │          │
│       │  8. 200 OK (original response)                           │          │
│       │<─────────────────────────────────────────────────────────│          │
│       ▼                                                          ▼          │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ PHASE 4: REFRESH TOKEN EXPIRED (Full Re-authentication Required)             │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────┐                                              ┌─────────────┐   │
│  │ Angular │                                              │ Spring Boot │   │
│  │Frontend │                                              │   Backend   │   │
│  └────┬────┘                                              └──────┬──────┘   │
│       │                                                          │          │
│       │  1. POST /api/v1/auth/refresh                            │          │
│       │     Headers:                                             │          │
│       │       Authorization: Bearer <expiredRefreshToken>        │          │
│       │─────────────────────────────────────────────────────────>│          │
│       │                                                          │          │
│       │                      2. Validate refresh token           │          │
│       │                         └─ EXPIRED! Reject               │          │
│       │                                                          │          │
│       │  3. 401 Unauthorized                                     │          │
│       │     { error: "Refresh token expired" }                   │          │
│       │<─────────────────────────────────────────────────────────│          │
│       │                                                          │          │
│       │  4. Clear localStorage:                                  │          │
│       │     ├─ Remove accessToken                                │          │
│       │     ├─ Remove refreshToken                               │          │
│       │     ├─ Remove tokenExpiry                                │          │
│       │     └─ Remove currentUser                                │          │
│       │                                                          │          │
│       │  5. Redirect to /login                                   │          │
│       ▼                                                          ▼          │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ PHASE 5: LOGOUT (Client-Only)                                                │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────┐                                                                 │
│  │ Angular │                                                                 │
│  │Frontend │                                                                 │
│  └────┬────┘                                                                 │
│       │                                                                      │
│       │  1. User clicks "Logout"                                             │
│       │                                                                      │
│       │  2. Clear localStorage:                                              │
│       │     ├─ Remove accessToken                                            │
│       │     ├─ Remove refreshToken                                           │
│       │     ├─ Remove tokenExpiry                                            │
│       │     └─ Remove currentUser                                            │
│       │                                                                      │
│       │  3. Clear BehaviorSubject state                                      │
│       │                                                                      │
│       │  4. Navigate to /login                                               │
│       │                                                                      │
│       │  Note: No server call needed (client-only logout)                    │
│       │  Tokens will naturally expire on their own                           │
│       ▼                                                                      │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Token Lifecycle Timeline

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         TOKEN LIFECYCLE TIMELINE                            │
└─────────────────────────────────────────────────────────────────────────────┘

TIME ──────────────────────────────────────────────────────────────────────────>

     LOGIN              15 min           15 min           15 min        7 days
       │                  │                │                │             │
       ▼                  ▼                ▼                ▼             ▼
┌──────┬──────────────────┬────────────────┬────────────────┬─────────────┬───┐
│LOGIN │  ACCESS TOKEN 1  │ ACCESS TOKEN 2 │ ACCESS TOKEN 3 │    ...      │END│
│      │    (15 min)      │   (15 min)     │   (15 min)     │             │   │
└──────┴────────┬─────────┴───────┬────────┴───────┬────────┴─────────────┴───┘
                │                 │                │                       │
                ▼                 ▼                ▼                       │
           REFRESH #1        REFRESH #2       REFRESH #3                   │
           (auto)            (auto)           (auto)                       │
                                                                           │
┌──────────────────────────────────────────────────────────────────────────┐
│                      REFRESH TOKEN (7 days)                              │
│                                                                          │
│  Same refresh token used for all access token renewals                   │
│  until it expires after 7 days                                           │
└──────────────────────────────────────────────────────────────────────────┘
                                                                           │
                                                                           ▼
                                                              REFRESH TOKEN EXPIRES
                                                              → User must re-login


INTERCEPTOR FLOW:
─────────────────

  ┌─────────────────┐
  │ HTTP Request    │
  └────────┬────────┘
           │
           ▼
  ┌─────────────────┐     YES    ┌─────────────────┐
  │ Access Token    │───────────>│ Add Bearer      │───> Make Request
  │ Valid?          │            │ Header          │
  └────────┬────────┘            └─────────────────┘
           │ NO
           ▼
  ┌─────────────────┐     YES    ┌─────────────────┐
  │ Refresh Token   │───────────>│ Call /refresh   │
  │ Exists?         │            │ endpoint        │
  └────────┬────────┘            └────────┬────────┘
           │ NO                           │
           ▼                              ▼
  ┌─────────────────┐            ┌─────────────────┐     YES
  │ Redirect to     │            │ Refresh         │────────────┐
  │ /login          │            │ Successful?     │            │
  └─────────────────┘            └────────┬────────┘            ▼
                                          │ NO         ┌─────────────────┐
                                          ▼            │ Update tokens   │
                                 ┌─────────────────┐   │ in localStorage │
                                 │ Clear tokens    │   └────────┬────────┘
                                 │ Redirect /login │            │
                                 └─────────────────┘            ▼
                                                       ┌─────────────────┐
                                                       │ Retry original  │
                                                       │ request         │
                                                       └─────────────────┘
```

### JWT Token Structure

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           JWT TOKEN STRUCTURE                               │
└─────────────────────────────────────────────────────────────────────────────┘

ACCESS TOKEN:
─────────────
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbi...
     │                                       │
     └──── Header (Base64) ──────────────────┘
           {
             "alg": "HS256",
             "typ": "JWT"
           }

                     eyJpZCI6MSwic3ViIjoiYWRtaW4iLC...
                          │
                          └──── Payload (Base64)
                                {
                                  "sub": "admin",           // username
                                  "id": 1,                  // user ID
                                  "email": "admin@test.com",
                                  "role": "admin",
                                  "type": "access",         // TOKEN TYPE
                                  "iat": 1736553600,        // issued at
                                  "exp": 1736554500         // expires (+15 min)
                                }

                                              .SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV...
                                                   │
                                                   └──── Signature (HMAC-SHA256)


REFRESH TOKEN:
──────────────
{
  "sub": "admin",           // username
  "id": 1,                  // user ID
  "type": "refresh",        // TOKEN TYPE (different!)
  "iat": 1736553600,        // issued at
  "exp": 1737158400         // expires (+7 days)
}

Note: Refresh token has FEWER claims (no email, role)
      - Minimizes data exposure
      - Role/email fetched fresh from DB on refresh
```

---

## Backend Implementation

### Phase 1: Add Dependencies

**File**: `grace-manage-service/pom.xml`

**Action**: Add after Lombok dependency (before Testing section):

```xml
<!-- JWT Authentication -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

**After adding**: Run `mvn clean install` to download dependencies.

---

### Phase 2: Database Migration

**File (NEW)**: `grace-manage-service/src/main/resources/db/migration/V2__Add_Role_To_User_Table.sql`

```sql
-- Add role column to T_USER table
ALTER TABLE T_USER ADD role VARCHAR(50) NOT NULL DEFAULT 'user';

-- Create index for role-based queries
CREATE INDEX idx_role ON T_USER(role);

-- Update existing users with appropriate roles (if any exist)
UPDATE T_USER SET role = 'user' WHERE role IS NULL;

-- Add check constraint to ensure only valid roles
ALTER TABLE T_USER ADD CONSTRAINT chk_role CHECK (role IN ('admin', 'user', 'viewer'));
```

**Note**: Flyway will automatically run this migration on application startup.

---

### Phase 3: Domain Layer Updates

#### 3.1 Update User Domain Entity

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/domain/entity/User.java`

**Changes**:
1. Add `private String role;` field
2. Add method:
```java
public boolean isAdmin() {
    return "admin".equalsIgnoreCase(role);
}
```

**Full Updated Class**:
```java
package com.grace.gracemanageservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;  // NEW FIELD
    private Boolean active;
    private Long createdAt;
    private Long updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return active != null && active;
    }

    // NEW METHOD
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
```

---

#### 3.2 Update CreateUserUseCase

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/domain/usecase/CreateUserUseCase.java`

**Changes**:
1. Add `PasswordEncoder` dependency (constructor injection)
2. Update `execute()` method signature to include `String role`
3. Hash password with `passwordEncoder.encode(password)`
4. Add role validation

**Full Updated Class**:
```java
package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ValidationException;
import com.grace.gracemanageservice.common.validator.EmailValidator;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // NEW DEPENDENCY

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;  // NEW
    }

    // UPDATED METHOD SIGNATURE (added role parameter)
    public User execute(String username, String email, String firstName, String lastName, String password, String role) {
        // Validation
        validateInput(username, email, firstName, lastName, password, role);
        validateEmailUniqueness(email);
        validateUsernameUniqueness(username);

        // Business logic
        User user = User.builder()
            .username(username)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .password(passwordEncoder.encode(password))  // HASH PASSWORD
            .role(role != null ? role : "user")  // DEFAULT TO 'user'
            .active(true)
            .createdAt(System.currentTimeMillis())
            .build();

        return userRepository.save(user);
    }

    // UPDATED VALIDATION METHOD (added role validation)
    private void validateInput(String username, String email, String firstName, String lastName, String password, String role) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("username", "Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new ValidationException("email", "Email is required");
        }
        if (!EmailValidator.isValid(email)) {
            throw new ValidationException("email", "Email format is invalid");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new ValidationException("firstName", "First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new ValidationException("lastName", "Last name is required");
        }
        if (password == null || password.length() < 8) {
            throw new ValidationException("password", "Password must be at least 8 characters");
        }
        // NEW ROLE VALIDATION
        if (role != null && !role.matches("^(admin|user|viewer)$")) {
            throw new ValidationException("role", "Role must be one of: admin, user, viewer");
        }
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("email", "Email already exists");
        }
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ValidationException("username", "Username already exists");
        }
    }
}
```

---

#### 3.3 Create LoginUserUseCase

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/domain/usecase/LoginUserUseCase.java`

```java
package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ValidationException;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Login user use case - validates credentials and returns authenticated user
 */
@Component
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(String username, String password) {
        // Validate input
        if (username == null || username.isBlank()) {
            throw new ValidationException("username", "Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("password", "Password is required");
        }

        // Find user by username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ValidationException("credentials", "Invalid username or password"));

        // Check if user is active
        if (!user.isActive()) {
            throw new ValidationException("account", "Account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("credentials", "Invalid username or password");
        }

        return user;
    }
}
```

---

### Phase 4: Infrastructure Layer

#### 4.1 Update UserEntity

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/infrastructure/persistence/UserEntity.java`

**Changes**:
1. Add `private String role;` field with `@Column(nullable = false)`
2. Update `fromDomain()` method to include role
3. Update `toDomain()` method to include role

**Key Sections to Update**:
```java
@Column(nullable = false)
private String role;  // ADD THIS FIELD

public static UserEntity fromDomain(User user) {
    return UserEntity.builder()
        // ... existing fields ...
        .role(user.getRole())  // ADD THIS
        // ... existing fields ...
        .build();
}

public User toDomain() {
    return User.builder()
        // ... existing fields ...
        .role(this.role)  // ADD THIS
        // ... existing fields ...
        .build();
}
```

---

#### 4.2 Create CustomUserDetails

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/infrastructure/security/CustomUserDetails.java`

```java
package com.grace.gracemanageservice.infrastructure.security;

import com.grace.gracemanageservice.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Adapter between domain User entity and Spring Security UserDetails
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public User getUser() {
        return user;
    }
}
```

---

#### 4.3 Create CustomUserDetailsService

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/infrastructure/security/CustomUserDetailsService.java`

```java
package com.grace.gracemanageservice.infrastructure.security;

import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation for Spring Security
 * Loads user from domain repository
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new CustomUserDetails(user);
    }
}
```

---

#### 4.4 Create JwtTokenProvider

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/infrastructure/security/JwtTokenProvider.java`

```java
package com.grace.gracemanageservice.infrastructure.security;

import com.grace.gracemanageservice.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider - handles token generation and validation
 * Supports dual-token strategy: access token (15 min) + refresh token (7 days)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        // Ensure secret is at least 256 bits (32 bytes) for HS256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate ACCESS token for authenticated user (short-lived: 15 minutes)
     * Contains full user claims: id, email, role
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("id", user.getId())
            .claim("email", user.getEmail())
            .claim("role", user.getRole())
            .claim("type", TOKEN_TYPE_ACCESS)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Generate REFRESH token for authenticated user (long-lived: 7 days)
     * Contains minimal claims: id, username only
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("id", user.getId())
            .claim("type", TOKEN_TYPE_REFRESH)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Get access token expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return ACCESS_TOKEN_EXPIRATION_MS / 1000;
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Extract token type from JWT token
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    /**
     * Check if token is an access token
     */
    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
    }

    /**
     * Check if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    /**
     * Parse token and return claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Validate access token specifically (must be valid AND be access type)
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token) && isAccessToken(token);
    }

    /**
     * Validate refresh token specifically (must be valid AND be refresh type)
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token) && isRefreshToken(token);
    }
}
```

---

#### 4.5 Create JwtAuthenticationFilter

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/infrastructure/security/JwtAuthenticationFilter.java`

```java
package com.grace.gracemanageservice.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter - validates JWT from Authorization Bearer header on every request
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromBearerHeader(request);

            // Only validate access tokens for API authentication (not refresh tokens)
            if (jwt != null && jwtTokenProvider.validateAccessToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", username);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization Bearer header
     */
    private String getJwtFromBearerHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
```

---

### Phase 5: Configuration Layer

#### 5.1 Add JWT Secret to application.properties

**File**: `grace-manage-service/src/main/resources/application.properties`

**Add at the end**:
```properties
# JWT Configuration
# IMPORTANT: In production, use a strong random secret (at least 256 bits / 32 characters)
# Generate with: openssl rand -base64 32
jwt.secret=YourVerySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong12345
```

---

#### 5.2 Update SecurityConfig

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/config/SecurityConfig.java`

**Replace entire file**:
```java
package com.grace.gracemanageservice.config;

import com.grace.gracemanageservice.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()  // Allow login/logout
                .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "USER")  // Require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

#### 5.3 Update WebConfig (CORS)

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/config/WebConfig.java`

**Replace entire file**:
```java
package com.grace.gracemanageservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200")  // Specify frontend origin
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")  // Allow Authorization header
            .exposedHeaders("Authorization")  // Expose Authorization header to frontend
            // NOTE: allowCredentials NOT needed for Bearer token auth (only for cookies)
            .maxAge(3600);
    }
}
```

**Note**: With Bearer token authentication, `allowCredentials(true)` is not required since tokens are sent via headers, not cookies. This simplifies CORS configuration and allows more flexible origin patterns if needed.

---

### Phase 6: Presentation Layer

#### 6.1 Create AuthController

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/presentation/controller/AuthController.java`

```java
package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.dto.LoginRequestDTO;
import com.grace.gracemanageservice.application.dto.LoginResponseDTO;
import com.grace.gracemanageservice.application.dto.RefreshTokenResponseDTO;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.usecase.LoginUserUseCase;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import com.grace.gracemanageservice.infrastructure.security.JwtTokenProvider;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

   private final LoginUserUseCase loginUserUseCase;
   private final JwtTokenProvider jwtTokenProvider;
   private final UserRepository userRepository;

   /**
    * Login endpoint - authenticates user and returns access + refresh tokens
    */
   @PostMapping("/login")
   public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
           @Valid @RequestBody LoginRequestDTO request) {

      log.info("Login attempt for user: {}", request.getUsername());

      // Authenticate user
      User user = loginUserUseCase.execute(request.getUsername(), request.getPassword());

      // Generate tokens
      String accessToken = jwtTokenProvider.generateAccessToken(user);
      String refreshToken = jwtTokenProvider.generateRefreshToken(user);

      // Prepare response with tokens in body (Bearer token approach)
      LoginResponseDTO loginResponse = LoginResponseDTO.builder()
              .accessToken(accessToken)
              .refreshToken(refreshToken)
              .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
              .tokenType("Bearer")
              .user(LoginResponseDTO.UserInfo.builder()
                      .id(user.getId())
                      .username(user.getUsername())
                      .email(user.getEmail())
                      .firstName(user.getFirstName())
                      .lastName(user.getLastName())
                      .role(user.getRole())
                      .active(user.getActive())
                      .build())
              .build();

      log.info("User {} logged in successfully", user.getUsername());
      return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
   }

   /**
    * Refresh endpoint - exchanges valid refresh token for new access token
    */
   @PostMapping("/refresh")
   public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refresh(
           @RequestHeader("Authorization") String authHeader) {

      log.info("Token refresh request received");

      // Extract refresh token from Bearer header
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                 .body(ApiResponse.error("Missing or invalid Authorization header"));
      }

      String refreshToken = authHeader.substring(7);

      // Validate refresh token
      if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
         log.warn("Invalid or expired refresh token");
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                 .body(ApiResponse.error("Invalid or expired refresh token"));
      }

      // Extract username and load fresh user data from DB
      String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
      User user = userRepository.findByUsername(username)
              .orElse(null);

      if (user == null || !user.isActive()) {
         log.warn("User not found or inactive: {}", username);
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                 .body(ApiResponse.error("User not found or inactive"));
      }

      // Generate new access token (refresh token stays the same)
      String newAccessToken = jwtTokenProvider.generateAccessToken(user);

      RefreshTokenResponseDTO response = RefreshTokenResponseDTO.builder()
              .accessToken(newAccessToken)
              .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
              .tokenType("Bearer")
              .build();

      log.info("Token refreshed for user: {}", username);
      return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
   }
}
```

**Note**: No `/logout` endpoint is needed since we use client-only logout (tokens cleared from localStorage). Tokens will naturally expire on their own.

---

#### 6.2 Create LoginRequest

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/presentation/request/LoginRequest.java`

```java
package com.grace.gracemanageservice.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
```

---

#### 6.3 Update CreateUserRequest

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/presentation/request/CreateUserRequest.java`

**Add field**:
```java
@Pattern(regexp = "^(admin|user|viewer)$", message = "Role must be one of: admin, user, viewer")
private String role = "user";  // Default to 'user'
```

---

#### 6.4 Update UserResponse

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/presentation/response/UserResponse.java`

**Add field**:
```java
private String role;
```

---

#### 6.5 Update GlobalExceptionHandler

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/presentation/advice/GlobalExceptionHandler.java`

**Add methods**:
```java
@ExceptionHandler(UsernameNotFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
    log.error("Username not found: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("Authentication failed"));
}

@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
    log.error("Bad credentials: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("Invalid username or password"));
}
```

**Add imports**:
```java
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
```

---

### Phase 7: Application Layer

#### 7.1 Update UserDTO

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/application/dto/UserDTO.java`

**Add field**:
```java
private String role;
```

---

#### 7.2 Create LoginRequestDTO

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/application/dto/LoginRequestDTO.java`

```java
package com.grace.gracemanageservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String username;
    private String password;
}
```

---

#### 7.3 Create LoginResponseDTO

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/application/dto/LoginResponseDTO.java`

```java
package com.grace.gracemanageservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response containing access/refresh tokens and user info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;  // Access token expiration in seconds
    private String tokenType;  // "Bearer"
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private Boolean active;
    }
}
```

---

#### 7.4 Create RefreshTokenResponseDTO

**File (NEW)**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/application/dto/RefreshTokenResponseDTO.java`

```java
package com.grace.gracemanageservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for token refresh endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDTO {
    private String accessToken;
    private long expiresIn;  // Access token expiration in seconds
    private String tokenType;  // "Bearer"
}
```

---

#### 7.4 Update UserApplicationService

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/application/service/UserApplicationService.java`

**Update createUser method**:
```java
public UserDTO createUser(String username, String email, String firstName, String lastName, String password, String role) {
    User user = createUserUseCase.execute(username, email, firstName, lastName, password, role);
    return userMapper.toDTO(user);
}
```

---

#### 7.5 Update UserController

**File**: `grace-manage-service/src/main/java/com/grace/gracemanageservice/presentation/controller/UserController.java`

**Update createUser method call**:
```java
UserDTO userDTO = userApplicationService.createUser(
    request.getUsername(),
    request.getEmail(),
    request.getFirstName(),
    request.getLastName(),
    request.getPassword(),
    request.getRole()  // ADD THIS
);
```

---

### Backend Testing

**Test with curl after starting backend** (`mvn spring-boot:run`):

```bash
# 1. Test login - get access and refresh tokens
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  -v

# Expected: 200 OK with response body:
# {
#   "status": "success",
#   "data": {
#     "accessToken": "eyJhbG...",
#     "refreshToken": "eyJhbG...",
#     "expiresIn": 900,
#     "tokenType": "Bearer",
#     "user": { "id": 1, "username": "admin", "role": "admin", ... }
#   }
# }

# 2. Test protected endpoint with Bearer token
# (Replace <ACCESS_TOKEN> with actual token from login response)
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -v

# Expected: 200 OK with user data

# 3. Test token refresh
# (Replace <REFRESH_TOKEN> with actual refresh token from login response)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Authorization: Bearer <REFRESH_TOKEN>" \
  -v

# Expected: 200 OK with new access token:
# {
#   "status": "success",
#   "data": {
#     "accessToken": "eyJhbG...(NEW)",
#     "expiresIn": 900,
#     "tokenType": "Bearer"
#   }
# }

# 4. Test protected endpoint without token
curl -X GET http://localhost:8080/api/v1/users/1 \
  -v

# Expected: 401 Unauthorized

# 5. Test with expired/invalid token
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer invalid_token_here" \
  -v

# Expected: 401 Unauthorized

# 6. Test refresh with expired refresh token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Authorization: Bearer <EXPIRED_REFRESH_TOKEN>" \
  -v

# Expected: 401 Unauthorized with "Invalid or expired refresh token"
```

---

## Frontend Implementation

### Phase 1: Create HTTP Interceptor

**File (NEW)**: `grace-manage-rwc/src/app/interceptors/auth.interceptor.ts`

```typescript
import {HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {catchError, switchMap, throwError, BehaviorSubject, filter, take} from 'rxjs';
import {AuthService} from '../auth.service';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  // Skip auth header for login and refresh endpoints
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  // Add Bearer token to request
  const accessToken = authService.getAccessToken();
  let authReq = req;
  
  if (accessToken) {
    authReq = addTokenToRequest(req, accessToken);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        return handleUnauthorizedError(req, next, authService, router);
      }
      return throwError(() => error);
    })
  );
};

function addTokenToRequest(request: HttpRequest<any>, token: string): HttpRequest<any> {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

function handleUnauthorizedError(
  request: HttpRequest<any>,
  next: HttpHandlerFn,
  authService: AuthService,
  router: Router
) {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    const refreshToken = authService.getRefreshToken();
    
    if (refreshToken) {
      return authService.refreshToken().pipe(
        switchMap((response: any) => {
          isRefreshing = false;
          const newAccessToken = response.data.accessToken;
          refreshTokenSubject.next(newAccessToken);
          
          // Retry the original request with new token
          return next(addTokenToRequest(request, newAccessToken));
        }),
        catchError((err) => {
          isRefreshing = false;
          // Refresh failed - logout and redirect to login
          authService.logout();
          router.navigate(['/login']);
          return throwError(() => err);
        })
      );
    } else {
      // No refresh token - redirect to login
      isRefreshing = false;
      authService.logout();
      router.navigate(['/login']);
      return throwError(() => new Error('No refresh token available'));
    }
  } else {
    // Wait for the refresh to complete, then retry
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => next(addTokenToRequest(request, token!)))
    );
  }
}
```

---

### Phase 2: Register Interceptor

**File**: `grace-manage-rwc/src/app/app.config.ts`

**Update to**:
```typescript
import {ApplicationConfig} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {provideAnimations} from '@angular/platform-browser/animations';
import {provideZoneChangeDetection} from '@angular/core';
import {authInterceptor} from './interceptors/auth.interceptor';
import {routes} from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideAnimations(),
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])  // ADD THIS
    )
  ]
};
```

---

### Phase 3: Update AuthService

**File**: `grace-manage-rwc/src/app/auth.service.ts`

**Replace entire file**:
```typescript
import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, BehaviorSubject, tap, catchError, throwError, map} from 'rxjs';

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  active: boolean;
}

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  user: User;
}

interface RefreshResponse {
  accessToken: string;
  expiresIn: number;
  tokenType: string;
}

interface ApiResponse<T> {
  status: string;
  data: T;
  message: string;
}

// localStorage keys
const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const TOKEN_EXPIRY_KEY = 'tokenExpiry';
const CURRENT_USER_KEY = 'currentUser';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/v1/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    // Check if user data exists in localStorage on init
    this.loadStoredUser();
  }

  /**
   * Load stored user from localStorage
   */
  private loadStoredUser(): void {
    const storedUser = localStorage.getItem(CURRENT_USER_KEY);
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    
    if (storedUser && accessToken) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  /**
   * Login user via backend API
   * Returns access token and refresh token in response body
   */
  login(username: string, password: string): Observable<LoginResponse> {
    const loginRequest: LoginRequest = {username, password};

    return this.http.post<ApiResponse<LoginResponse>>(`${this.baseUrl}/login`, loginRequest).pipe(
      map(response => response.data),
      tap(data => {
        // Store tokens in localStorage
        localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
        
        // Calculate and store token expiry time
        const expiryTime = Date.now() + (data.expiresIn * 1000);
        localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTime.toString());
        
        // Store user data
        localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(data.user));
        this.currentUserSubject.next(data.user);
      }),
      catchError(error => {
        console.error('Login error:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Refresh access token using refresh token
   */
  refreshToken(): Observable<ApiResponse<RefreshResponse>> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${refreshToken}`);

    return this.http.post<ApiResponse<RefreshResponse>>(`${this.baseUrl}/refresh`, {}, { headers }).pipe(
      tap(response => {
        if (response.status === 'success' && response.data) {
          // Update access token and expiry
          localStorage.setItem(ACCESS_TOKEN_KEY, response.data.accessToken);
          const expiryTime = Date.now() + (response.data.expiresIn * 1000);
          localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTime.toString());
        }
      }),
      catchError(error => {
        console.error('Token refresh error:', error);
        // If refresh fails, clear all tokens
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Logout user - client-only (clear localStorage)
   * No server call needed since tokens will naturally expire
   */
  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    localStorage.removeItem(CURRENT_USER_KEY);
    this.currentUserSubject.next(null);
  }

  /**
   * Get access token from localStorage
   */
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  /**
   * Get refresh token from localStorage
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /**
   * Check if access token is expired
   */
  isTokenExpired(): boolean {
    const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY);
    if (!expiry) return true;
    
    // Add 30 second buffer before actual expiry
    return Date.now() >= (parseInt(expiry, 10) - 30000);
  }

  /**
   * Check if user is authenticated (has valid tokens)
   */
  isAuthenticated(): boolean {
    const accessToken = this.getAccessToken();
    const refreshToken = this.getRefreshToken();
    
    // User is authenticated if they have at least a refresh token
    // (access token can be refreshed if expired)
    return !!(accessToken || refreshToken) && this.currentUserSubject.value !== null;
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if current user has a specific role
   */
  hasRole(role: string): boolean {
    return this.currentUserSubject.value?.role === role;
  }

  /**
   * Check if current user is admin
   */
  isAdmin(): boolean {
    return this.hasRole('admin');
  }
}
```

---

### Phase 4: Update ApiService

**File**: `grace-manage-rwc/src/app/shared/services/api.service.ts`

**Note**: With Bearer token authentication, `withCredentials: true` is **NOT needed**. The `authInterceptor` automatically adds the `Authorization: Bearer <token>` header to all requests.

**Keep existing HTTP methods as-is** (no changes needed):

```typescript
// No changes needed - interceptor handles Bearer token automatically

createUser(userData: CreateUserRequest): Observable<ApiResponse<User>> {
  return this.http.post<ApiResponse<User>>(`${this.baseUrl}/users`, userData)
    .pipe(catchError(this.handleError));
}

getUserById(id: number): Observable<ApiResponse<User>> {
  return this.http.get<ApiResponse<User>>(`${this.baseUrl}/users/${id}`)
    .pipe(catchError(this.handleError));
}

getUserByEmail(email: string): Observable<ApiResponse<User>> {
  return this.http.get<ApiResponse<User>>(`${this.baseUrl}/users/email/${email}`)
    .pipe(catchError(this.handleError));
}

deleteUser(id: number): Observable<ApiResponse<null>> {
  return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/users/${id}`)
    .pipe(catchError(this.handleError));
}
```

---

### Phase 5: Update LoginComponent

**File**: `grace-manage-rwc/src/app/login/login.component.ts`

**Update error handling in `onSubmit()` method**:

```typescript
onSubmit(): void {
  if (this.loginForm.valid) {
    this.loading = true;
    this.errorMessage = '';

    const {username, password} = this.loginForm.value;

    this.authService.login(username, password).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('Login successful:', response);
        this.router.navigate(['/home']);
      },
      error: (error) => {
        this.loading = false;
        // Handle backend error response
        if (error.error?.message) {
          this.errorMessage = error.error.message;
        } else if (error.status === 401) {
          this.errorMessage = 'Invalid username or password';
        } else if (error.status === 0) {
          this.errorMessage = 'Unable to connect to server. Please check if the backend is running.';
        } else {
          this.errorMessage = 'Login failed. Please try again.';
        }
        console.error('Login error:', error);
      }
    });
  }
}
```

**Update `ngOnInit()` to not auto-logout**:
```typescript
ngOnInit(): void {
  // Don't auto-logout on page load - check if already authenticated
  if (this.authService.isAuthenticated()) {
    this.router.navigate(['/home']);
  }
}
```

---

### Phase 6: Update User Models

**File**: `grace-manage-rwc/src/app/shared/models/api-response.model.ts`

**Ensure User interface includes**:
```typescript
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;  // ENSURE THIS EXISTS
  active: boolean;
  createdAt?: number;
  updatedAt?: number;
}
```

---

### Frontend Testing

**Test after starting frontend** (`ng serve`):

1. **Login Flow**:
   - Open http://localhost:4200
   - Enter credentials (admin/admin123)
   - Check browser DevTools → Application → Local Storage
   - Verify `accessToken`, `refreshToken`, `tokenExpiry`, `currentUser` exist
   - Should redirect to /home

2. **Authentication State**:
   - Check localStorage for `currentUser`
   - Verify user object contains role field
   - Refresh page - should remain authenticated
   - Check Network tab - requests should have `Authorization: Bearer <token>` header

3. **Protected Routes**:
   - Try accessing /home/employee-management as admin - should work
   - Logout and try again - should redirect to /login

4. **API Calls**:
   - Open DevTools → Network
   - Make API call (e.g., create user)
   - Verify Authorization header includes `Bearer <token>`

5. **Token Refresh** (optional manual test):
   - Modify `tokenExpiry` in localStorage to a past timestamp
   - Make an API call
   - Interceptor should automatically refresh token
   - New `accessToken` should appear in localStorage

---

## Testing Strategy

### Backend Testing Order

1. **Database Migration**: Verify role column exists
   ```bash
   docker exec -it grace-sqlserver-local /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "YourStrong@Password123" -Q "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='T_USER' AND COLUMN_NAME='role'"
   ```

2. **Unit Tests**: Test individual components (JwtTokenProvider, LoginUserUseCase)

3. **Integration Tests**: Test AuthController endpoints

4. **Manual Testing**: Use curl commands (see above)

### Frontend Testing Order

1. **Unit Tests**: Test AuthService, guards

2. **Manual Browser Testing**: Login, check localStorage for tokens, test routes

3. **E2E Tests**: Full authentication flow

### Integration Testing

**Test the full flow**:
1. Start backend: `mvn spring-boot:run`
2. Start frontend: `ng serve`
3. Login with admin/admin123
4. Verify tokens in browser DevTools → Application → Local Storage
5. Test protected endpoints (check Network tab for Authorization header)
6. Test logout (verify localStorage cleared)
7. Test token refresh (wait 15 min or manually expire token)
8. Test expired refresh token handling (should redirect to login)

---

## Verification Steps

### Backend Verification Checklist

- [ ] Dependencies added to pom.xml
- [ ] Database migration V2 created
- [ ] User.java has role field
- [ ] CreateUserUseCase hashes passwords
- [ ] LoginUserUseCase validates credentials
- [ ] UserEntity updated with role
- [ ] CustomUserDetails created
- [ ] CustomUserDetailsService created
- [ ] JwtTokenProvider created (with access + refresh token methods)
- [ ] JwtAuthenticationFilter created (extracts Bearer token from header)
- [ ] SecurityConfig updated
- [ ] WebConfig CORS updated (no credentials needed)
- [ ] application.properties has jwt.secret
- [ ] AuthController created with /login and /refresh endpoints
- [ ] LoginRequest created
- [ ] LoginResponseDTO includes accessToken, refreshToken, expiresIn
- [ ] RefreshTokenResponseDTO created
- [ ] UserDTO updated
- [ ] Backend starts without errors
- [ ] Login endpoint returns tokens in response body
- [ ] Protected endpoints require Authorization Bearer header
- [ ] Refresh endpoint returns new access token

### Frontend Verification Checklist

- [ ] auth.interceptor.ts created (adds Bearer header, handles 401 with refresh)
- [ ] app.config.ts registers interceptor
- [ ] auth.service.ts updated with token storage and refresh methods
- [ ] api.service.ts unchanged (interceptor handles auth)
- [ ] login.component.ts has better error handling
- [ ] api-response.model.ts has role field
- [ ] Frontend starts without errors
- [ ] Login UI works
- [ ] Tokens stored in localStorage (accessToken, refreshToken, tokenExpiry)
- [ ] Network requests include Authorization Bearer header
- [ ] Protected routes work
- [ ] Logout clears localStorage
- [ ] Token refresh works automatically on 401
- [ ] Expired refresh token redirects to login

---

## Security Considerations

### Development Environment

- Token storage: localStorage (acceptable for development)
- JWT secret: Development secret in application.properties
- CORS: Allow http://localhost:4200
- Access token expiry: 15 minutes
- Refresh token expiry: 7 days

### Production Environment

- JWT secret: Environment variable `${JWT_SECRET}` (at least 256 bits)
- CORS: Specific production frontend URL only
- Enable HTTPS (required for security)
- Implement Content Security Policy (CSP) headers to mitigate XSS
- Consider token rotation for refresh tokens (optional enhancement)

### XSS Mitigation (Critical for Bearer Tokens in localStorage)

Since tokens are stored in localStorage, XSS protection is essential:

1. **Content Security Policy (CSP)** - Add CSP headers to prevent inline scripts:
```java
// In SecurityConfig.java or via filter
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';")
            )
        )
        // ... rest of config
}
```

2. **Angular's built-in XSS protection** - Angular automatically sanitizes interpolated values

3. **Avoid innerHTML** - Never use `innerHTML` with user-provided content

4. **Short access token lifetime** - 15 minutes limits exposure if token is stolen

**Production Configuration Example**:

`application-prod.properties`:
```properties
jwt.secret=${JWT_SECRET}
spring.security.require-ssl=true
frontend.url=${FRONTEND_URL}
```


---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| **CORS Error** | Ensure specific origin in WebConfig; Authorization header in allowedHeaders |
| **Token Not Sent** | Check interceptor is registered in app.config.ts; verify localStorage has token |
| **401 on First Request** | Check if JWT filter is before authentication filter in SecurityConfig |
| **Token Validation Fails** | Verify JWT secret is same as used for generation (at least 256 bits) |
| **Role Not Working** | Ensure JWT claims include role and SecurityContext has authorities |
| **Password Hash Error** | Ensure PasswordEncoder bean is defined in SecurityConfig |
| **Filter Not Applied** | Ensure JwtAuthenticationFilter is annotated with @Component |
| **Flyway Error** | Check database connection and migration file naming (V2__*.sql) |
| **Refresh Token Fails** | Ensure refresh endpoint validates token type is "refresh" not "access" |
| **Token Not Refreshing** | Check interceptor catches 401 and calls refresh before redirecting |

---

## Rollback Plan

If issues arise during implementation:

1. **Backend**: Temporarily permit all requests in SecurityConfig:
   ```java
   .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
   ```

2. **Frontend**: Auth guards will still work with mock authentication

3. **Database**: Flyway rollback (if needed):
   ```bash
   mvn flyway:undo
   ```

---

## Implementation Checklist

### Step 1: Backend Database & Domain
- [ ] Add jjwt dependencies to pom.xml
- [ ] Run `mvn clean install`
- [ ] Create V2 migration SQL file
- [ ] Update User.java (add role field and isAdmin method)
- [ ] Update CreateUserUseCase.java (inject PasswordEncoder, hash password, add role param)
- [ ] Create LoginUserUseCase.java

### Step 2: Backend Infrastructure
- [ ] Update UserEntity.java (add role field)
- [ ] Create CustomUserDetails.java
- [ ] Create CustomUserDetailsService.java
- [ ] Create JwtTokenProvider.java (with generateAccessToken, generateRefreshToken, validateAccessToken, validateRefreshToken)
- [ ] Create JwtAuthenticationFilter.java (extract token from Authorization Bearer header)

### Step 3: Backend Configuration
- [ ] Add jwt.secret to application.properties
- [ ] Update SecurityConfig.java (add PasswordEncoder bean, configure filter chain)
- [ ] Update WebConfig.java (specify frontend origin, expose Authorization header)

### Step 4: Backend Presentation
- [ ] Create AuthController.java (with /login and /refresh endpoints)
- [ ] Create LoginRequest.java
- [ ] Update CreateUserRequest.java (add role field)
- [ ] Update UserResponse.java (add role field)
- [ ] Update GlobalExceptionHandler.java (add auth exception handlers)

### Step 5: Backend Application Layer
- [ ] Update UserDTO.java (add role field)
- [ ] Create LoginRequestDTO.java
- [ ] Create LoginResponseDTO.java (with accessToken, refreshToken, expiresIn, user)
- [ ] Create RefreshTokenResponseDTO.java
- [ ] Update UserApplicationService.java (add role parameter)
- [ ] Update UserController.java (pass role to service)

### Step 6: Test Backend
- [ ] Start backend: `mvn spring-boot:run`
- [ ] Test login with curl - verify tokens in response body
- [ ] Test protected endpoint with Authorization Bearer header
- [ ] Test refresh endpoint with refresh token
- [ ] Test with invalid/expired tokens

### Step 7: Frontend Integration
- [ ] Create auth.interceptor.ts (with Bearer header and refresh logic)
- [ ] Update app.config.ts (register interceptor)
- [ ] Update auth.service.ts (token storage in localStorage, refresh method)
- [ ] Update login.component.ts (better error handling)
- [ ] Update api-response.model.ts (ensure role field)

### Step 8: Test End-to-End
- [ ] Start both backend and frontend
- [ ] Test login flow
- [ ] Verify tokens in browser DevTools → Application → Local Storage
- [ ] Test protected routes with Authorization header
- [ ] Test logout (localStorage cleared)
- [ ] Test token refresh (wait 15 min or manually expire access token)
- [ ] Test expired refresh token handling (should redirect to login)

---

## Next Steps After Implementation

1. **Create Admin User**: Use the createUser endpoint to create an admin user
2. **Test Role-Based Access**: Verify admin vs user permissions
3. **Add Unit Tests**: Write tests for JWT components
4. **Performance Testing**: Test with multiple concurrent users
5. **Security Audit**: Review for vulnerabilities
6. **Documentation**: Update API documentation with authentication flow
7. **Production Deployment**: Configure environment variables and HTTPS

---

## File Summary

### Backend Files (24 files)

**Modified**:
1. `pom.xml`
2. `src/main/java/.../domain/entity/User.java`
3. `src/main/java/.../domain/usecase/CreateUserUseCase.java`
4. `src/main/java/.../infrastructure/persistence/UserEntity.java`
5. `src/main/java/.../config/SecurityConfig.java`
6. `src/main/java/.../config/WebConfig.java`
7. `src/main/resources/application.properties`
8. `src/main/java/.../presentation/request/CreateUserRequest.java`
9. `src/main/java/.../presentation/response/UserResponse.java`
10. `src/main/java/.../presentation/controller/UserController.java`
11. `src/main/java/.../presentation/advice/GlobalExceptionHandler.java`
12. `src/main/java/.../application/dto/UserDTO.java`
13. `src/main/java/.../application/service/UserApplicationService.java`

**Created**:
14. `src/main/resources/db/migration/V2__Add_Role_To_User_Table.sql`
15. `src/main/java/.../domain/usecase/LoginUserUseCase.java`
16. `src/main/java/.../infrastructure/security/CustomUserDetails.java`
17. `src/main/java/.../infrastructure/security/CustomUserDetailsService.java`
18. `src/main/java/.../infrastructure/security/JwtTokenProvider.java`
19. `src/main/java/.../infrastructure/security/JwtAuthenticationFilter.java`
20. `src/main/java/.../presentation/controller/AuthController.java`
21. `src/main/java/.../presentation/request/LoginRequest.java`
22. `src/main/java/.../application/dto/LoginRequestDTO.java`
23. `src/main/java/.../application/dto/LoginResponseDTO.java`
24. `src/main/java/.../application/dto/RefreshTokenResponseDTO.java`

### Frontend Files (5 files)

**Modified**:
1. `src/app/auth.service.ts`
2. `src/app/app.config.ts`
3. `src/app/login/login.component.ts`
4. `src/app/shared/models/api-response.model.ts`

**Created**:
5. `src/app/interceptors/auth.interceptor.ts`

**Note**: `api.service.ts` no longer needs changes - the interceptor handles Bearer token automatically.

---

## Conclusion

This plan provides a complete, production-ready JWT authentication system with:

✅ **Bearer Token Exchange**: Access tokens (15 min) + Refresh tokens (7 days)
✅ **Security Best Practices**: BCrypt password hashing, CSP headers for XSS mitigation
✅ **Automatic Token Refresh**: Interceptor handles 401 and refreshes tokens seamlessly
✅ **Clean Architecture**: Proper layer separation in backend
✅ **Role-Based Access Control**: Simple admin/user/viewer roles
✅ **Modern Stack**: Spring Boot 4 + Angular 18
✅ **Developer Experience**: Comprehensive testing strategy and clear documentation

**Estimated Implementation Time**: 5-7 hours

**Priority**: High (currently no real authentication in place)

---

*Document Version*: 2.0
*Last Updated*: 2026-01-11
*Status*: Ready for Implementation
