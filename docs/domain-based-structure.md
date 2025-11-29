# Domain-Based Package Structure

## Overview
Domain-Driven Design (DDD) organizes code by business domains rather than technical layers.

## Proposed Structure

```
src/main/java/com/thegamersstation/marketplace/
│
├── domain/                                    # Core business domains
│   │
│   ├── post/                                  # Post domain (renamed from ad)
│   │   ├── core/                              # Core domain logic
│   │   │   ├── Post.java                      # Aggregate root
│   │   │   ├── PostImage.java                 # Value object
│   │   │   ├── PostType.java                  # Enum (SELL, ASK)
│   │   │   ├── PostCondition.java             # Enum
│   │   │   └── PostStatus.java                # Enum
│   │   ├── repository/                        # Data access
│   │   │   └── PostRepository.java
│   │   ├── service/                           # Domain services
│   │   │   ├── PostService.java               # User-facing service
│   │   │   └── PostModerationService.java     # Admin service
│   │   ├── dto/                               # Data transfer objects
│   │   │   ├── request/
│   │   │   │   ├── CreatePostRequest.java
│   │   │   │   └── UpdatePostRequest.java
│   │   │   └── response/
│   │   │       ├── PostDto.java
│   │   │       └── PostImageDto.java
│   │   ├── mapper/                            # Entity-DTO mappers
│   │   │   └── PostMapper.java
│   │   └── api/                               # REST controllers
│   │       ├── PostController.java
│   │       └── PostModerationController.java
│   │
│   ├── user/                                  # User domain
│   │   ├── core/
│   │   │   ├── User.java
│   │   │   └── UserRole.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   └── UserModerationService.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── mapper/
│   │   │   └── UserMapper.java
│   │   └── api/
│   │       ├── UserController.java
│   │       └── UserModerationController.java
│   │
│   ├── category/                              # Category domain
│   │   ├── core/
│   │   │   └── Category.java
│   │   ├── repository/
│   │   │   └── CategoryRepository.java
│   │   ├── service/
│   │   │   └── CategoryService.java
│   │   ├── dto/
│   │   ├── mapper/
│   │   └── api/
│   │       ├── CategoryController.java
│   │       └── CategoryModerationController.java
│   │
│   ├── location/                              # Location domain (was city)
│   │   ├── core/
│   │   │   └── City.java
│   │   ├── repository/
│   │   │   └── CityRepository.java
│   │   └── api/
│   │       └── CityController.java
│   │
│   ├── messaging/                             # Messaging domain
│   │   ├── core/
│   │   │   ├── Conversation.java
│   │   │   └── Message.java
│   │   ├── repository/
│   │   ├── service/
│   │   ├── dto/
│   │   └── api/
│   │
│   └── comment/                               # Comment domain
│       ├── core/
│       │   └── Comment.java
│       ├── repository/
│       ├── service/
│       ├── dto/
│       └── api/
│
├── infrastructure/                            # Infrastructure concerns
│   ├── security/                              # Security infrastructure
│   │   ├── jwt/
│   │   │   ├── JwtUtil.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── oauth/                             # Future OAuth support
│   │   ├── UserPrincipal.java
│   │   ├── SecurityConfig.java
│   │   └── SecurityUtil.java
│   │
│   ├── persistence/                           # Database infrastructure
│   │   ├── config/
│   │   │   └── JpaAuditingConfig.java
│   │   └── migration/                         # Flyway migrations location
│   │
│   ├── media/                                 # Media storage infrastructure
│   │   ├── MediaService.java
│   │   ├── s3/
│   │   │   └── S3StorageService.java          # Future: extract S3 logic
│   │   └── local/
│   │       └── LocalStorageService.java       # Future: extract local logic
│   │
│   ├── messaging/                             # External messaging (Ably, etc.)
│   │   └── AblyService.java
│   │
│   ├── notification/                          # Notification infrastructure
│   │   ├── otp/
│   │   │   ├── OtpService.java
│   │   │   ├── SimulatedOtpService.java
│   │   │   ├── OtpLog.java
│   │   │   └── OtpLogRepository.java
│   │   ├── email/                             # Future
│   │   └── push/                              # Future
│   │
│   └── config/                                # Infrastructure configuration
│       ├── OpenApiConfig.java
│       ├── LocaleConfig.java
│       └── WebConfig.java
│
├── application/                               # Application layer
│   ├── auth/                                  # Authentication application service
│   │   ├── AuthService.java
│   │   ├── dto/
│   │   │   ├── OtpRequestDto.java
│   │   │   ├── OtpVerifyDto.java
│   │   │   └── AuthResponseDto.java
│   │   └── api/
│   │       └── AuthController.java
│   │
│   └── common/                                # Shared application concerns
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   ├── ResourceNotFoundException.java
│       │   ├── BusinessRuleException.java
│       │   └── RateLimitExceededException.java
│       └── dto/
│           ├── PageResponseDto.java
│           └── PageRequestDto.java
│
└── shared/                                    # Shared kernel (used across domains)
    ├── util/
    │   ├── LocalizationService.java
    │   ├── ContentSanitizer.java
    │   ├── ProfanityFilter.java
    │   ├── SlugUtil.java
    │   └── LocalizedContent.java
    ├── validation/
    │   └── PhoneValidator.java
    └── constants/
        └── AppConstants.java
```

## Key Differences: Feature-based vs Domain-based

| Aspect | Feature-based (Current) | Domain-based (Proposed) |
|--------|-------------------------|-------------------------|
| **Organization** | By technical feature | By business domain |
| **Package depth** | Shallow (2-3 levels) | Deeper (4-5 levels) |
| **Coupling** | Feature-level coupling | Domain-level isolation |
| **Scalability** | Good for small apps | Better for large systems |
| **Team structure** | Feature teams | Domain teams |
| **Learning curve** | Easier | Steeper |
| **Bounded contexts** | Implicit | Explicit |

## Domain Organization Principles

### 1. **Core** (domain/*/core/)
- Pure domain entities
- Business logic
- Domain events (future)
- No external dependencies

### 2. **Repository** (domain/*/repository/)
- Data access interfaces
- Spring Data JPA repositories
- Custom query methods

### 3. **Service** (domain/*/service/)
- Domain services
- Business use cases
- Orchestration logic
- Transaction boundaries

### 4. **DTO** (domain/*/dto/)
- Request DTOs (input validation)
- Response DTOs (API contracts)
- Organized by request/response folders

### 5. **Mapper** (domain/*/mapper/)
- Entity ↔ DTO conversions
- MapStruct interfaces

### 6. **API** (domain/*/api/)
- REST controllers
- API endpoints
- Request/response handling

## Benefits of Domain-Based Structure

### ✅ Advantages

1. **Clear Boundaries**
   - Each domain is self-contained
   - Easy to understand responsibilities
   - Explicit dependencies between domains

2. **Scalability**
   - Can split domains into microservices later
   - Each domain can evolve independently
   - Easy to add new domains

3. **Team Organization**
   - Teams can own entire domains
   - Reduces merge conflicts
   - Clear ownership

4. **Testing**
   - Domain logic isolated and testable
   - Mock external dependencies easily
   - Unit test domain core separately

5. **Maintainability**
   - Changes localized to domains
   - Easier to refactor
   - Clear impact analysis

### ⚠️ Disadvantages

1. **More complex** - Deeper folder structure
2. **Steeper learning curve** - DDD concepts needed
3. **Potential over-engineering** - For small apps
4. **More files to navigate** - IDE navigation required

## When to Use Which?

### Feature-based (Current) ✅
- **Small to medium projects** (< 50K LOC)
- **Small teams** (1-5 developers)
- **Simple domain logic**
- **Fast prototyping**
- **Startups/MVPs**

### Domain-based (Proposed) ✅
- **Large projects** (> 50K LOC)
- **Multiple teams** (5+ developers)
- **Complex business rules**
- **Long-term maintenance**
- **Enterprise applications**
- **Microservices future**

## Migration Strategy

If you want to adopt domain-based structure, here's the migration path:

### Phase 1: Rename Ad → Post (Keep current structure)
```
ad/ → post/
├── Post.java (was Ad.java)
├── PostController.java
├── PostService.java
etc.
```

### Phase 2: Introduce domain layers (gradual)
```
post/
├── core/
│   └── Post.java (move entity)
├── repository/
│   └── PostRepository.java
├── service/
│   └── PostService.java
└── api/
    └── PostController.java
```

### Phase 3: Full domain structure
- Add all subpackages
- Organize DTOs by request/response
- Separate moderation concerns
- Extract infrastructure

## Recommendation for Your Project

**I recommend Phase 1 only (Rename Ad → Post)**

**Reasons:**
1. Your project is still in early stages (MVP)
2. Current feature-based structure works well for your size
3. Domain-based adds complexity without immediate benefit
4. You can always migrate later when you scale
5. Faster development velocity now

**Keep domain-based in mind for:**
- When you reach 10+ features
- When you have 5+ developers
- When you consider microservices
- When business logic gets complex

## Proposed Change: Ad → Post

Let's rename "Ad" to "Post" while keeping the current feature-based structure:

```
src/main/java/com/thegamersstation/marketplace/
├── post/                          # Renamed from ad/
│   ├── Post.java                  # Renamed from Ad.java
│   ├── PostImage.java             # Renamed from AdImage.java
│   ├── PostController.java
│   ├── PostModerationController.java  # Renamed from AdminAdController
│   ├── PostService.java
│   ├── PostModerationService.java     # Renamed from AdminAdService
│   ├── PostRepository.java
│   ├── PostMapper.java
│   └── dto/
│       ├── PostDto.java
│       ├── PostImageDto.java
│       ├── CreatePostRequest.java
│       └── UpdatePostRequest.java
```

This approach:
- ✅ Better terminology ("Post" is more universal than "Ad")
- ✅ Keeps current working structure
- ✅ Minimal disruption
- ✅ Easy to understand
- ✅ Maintains development velocity
