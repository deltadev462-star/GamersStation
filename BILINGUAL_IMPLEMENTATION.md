# Bilingual Implementation Guide (EN/AR)

## Overview
The Gamers Station API supports both **English (EN)** and **Arabic (AR)** languages to serve the Saudi Arabian market. This document explains the implementation strategy for bilingual support.

---

## Database Schema

### Tables with Bilingual Support

#### 1. **Cities**
```sql
CREATE TABLE cities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_en VARCHAR(100) NOT NULL,
    name_ar VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    ...
);
```
- `name_en`: English name (e.g., "Riyadh")
- `name_ar`: Arabic name (e.g., "الرياض")
- Both fields are **required** and have **unique constraints**

#### 2. **Categories**
```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_en VARCHAR(100) NOT NULL,
    name_ar VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    ...
);
```
- Used for 3-level gaming category hierarchy
- All categories have both EN and AR names

#### 3. **Stores** (Phase 2)
```sql
CREATE TABLE stores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_en VARCHAR(100) NOT NULL,
    name_ar VARCHAR(100) NOT NULL,
    description_en TEXT,
    description_ar TEXT,
    slug VARCHAR(100) NOT NULL UNIQUE,
    ...
);
```
- Store names and descriptions in both languages
- Descriptions are **optional** (can be NULL)

---

## Language Detection

### 1. Accept-Language Header (Recommended)
```http
GET /api/v1/categories
Accept-Language: ar
```
or
```http
GET /api/v1/categories
Accept-Language: en
```

### 2. Query Parameter
```http
GET /api/v1/categories?lang=ar
GET /api/v1/categories?lang=en
```

### 3. Default Locale
- **Default:** Arabic (ar) - matches Saudi market
- Fallback order: requested locale → Arabic → English

---

## Backend Implementation

### Configuration

**LocaleConfig.java**
```java
@Configuration
public class LocaleConfig implements WebMvcConfigurer {
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(Arrays.asList(
            new Locale("en"),
            new Locale("ar")
        ));
        resolver.setDefaultLocale(new Locale("ar"));
        return resolver;
    }
}
```

**application.yaml**
```yaml
app:
  i18n:
    default-locale: ar
    supported-locales: en,ar
```

### Utility Class

**LocalizedContent.java**
```java
public class LocalizedContent {
    private String en;
    private String ar;
    
    public String get(Locale locale) {
        if ("ar".equals(locale.getLanguage())) {
            return ar != null ? ar : en;
        }
        return en != null ? en : ar;
    }
}
```

---

## DTO Design Patterns

### Pattern 1: Separate Fields (Simple)
For entities where both languages are always needed:

```java
@Data
public class CityDto {
    private Long id;
    private String nameEn;
    private String nameAr;
    private String slug;
}
```

**Response Example:**
```json
{
  "id": 1,
  "nameEn": "Riyadh",
  "nameAr": "الرياض",
  "slug": "riyadh"
}
```

### Pattern 2: Localized Field (Context-Aware)
For responses that should adapt to user's language:

```java
@Data
public class CategoryDto {
    private Long id;
    private String name;        // Localized based on Accept-Language
    private LocalizedContent nameLocalized; // Full bilingual data
    private String slug;
}
```

**Response Example (Accept-Language: ar):**
```json
{
  "id": 1,
  "name": "أجهزة الألعاب",
  "nameLocalized": {
    "en": "Gaming Consoles",
    "ar": "أجهزة الألعاب"
  },
  "slug": "gaming-consoles"
}
```

**Response Example (Accept-Language: en):**
```json
{
  "id": 1,
  "name": "Gaming Consoles",
  "nameLocalized": {
    "en": "Gaming Consoles",
    "ar": "أجهزة الألعاب"
  },
  "slug": "gaming-consoles"
}
```

### Pattern 3: Dual Response (Admin/Management)
For admin endpoints where both languages must be displayed:

```json
{
  "id": 1,
  "name": {
    "en": "Gaming Consoles",
    "ar": "أجهزة الألعاب"
  },
  "slug": "gaming-consoles"
}
```

---

## Service Layer

### Example: CategoryService
```java
@Service
public class CategoryService {
    
    public CategoryDto getCategoryLocalized(Long id, Locale locale) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setSlug(category.getSlug());
        
        // Set localized name based on user's language
        if ("ar".equals(locale.getLanguage())) {
            dto.setName(category.getNameAr());
        } else {
            dto.setName(category.getNameEn());
        }
        
        // Also provide full bilingual data
        dto.setNameLocalized(LocalizedContent.of(
            category.getNameEn(), 
            category.getNameAr()
        ));
        
        return dto;
    }
}
```

---

## Controller Layer

### Inject Locale from Request
```java
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(
        @PathVariable Long id,
        @RequestHeader(name = "Accept-Language", required = false) Locale locale
    ) {
        // If no Accept-Language header, defaults to Arabic (configured in LocaleConfig)
        CategoryDto category = categoryService.getCategoryLocalized(id, locale);
        return ResponseEntity.ok(category);
    }
}
```

### Alternative: Use LocaleContextHolder
```java
public CategoryDto getCategory(Long id) {
    Locale locale = LocaleContextHolder.getLocale();
    return categoryService.getCategoryLocalized(id, locale);
}
```

---

## MapStruct Integration

### Mapping with Locale Context
```java
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    @Mapping(target = "name", expression = "java(getLocalizedName(category, locale))")
    @Mapping(target = "nameLocalized", expression = "java(getLocalizedContent(category))")
    CategoryDto toDto(Category category, @Context Locale locale);
    
    default String getLocalizedName(Category category, Locale locale) {
        if (locale != null && "ar".equals(locale.getLanguage())) {
            return category.getNameAr();
        }
        return category.getNameEn();
    }
    
    default LocalizedContent getLocalizedContent(Category category) {
        return LocalizedContent.of(category.getNameEn(), category.getNameAr());
    }
}
```

---

## API Documentation (Swagger)

### Add Language Parameter to Swagger
```java
@Operation(
    summary = "Get category by ID",
    description = "Returns category with name localized based on Accept-Language header"
)
@Parameter(
    name = "Accept-Language",
    description = "Language preference (en or ar)",
    example = "ar",
    in = ParameterIn.HEADER
)
@GetMapping("/{id}")
public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
    // implementation
}
```

---

## Validation

### Creating Bilingual Content
When creating or updating entities:

```java
@Data
@Validated
public class CreateCategoryRequest {
    @NotBlank(message = "English name is required")
    @Size(max = 100)
    private String nameEn;
    
    @NotBlank(message = "Arabic name is required")
    @Size(max = 100)
    private String nameAr;
}
```

---

## Search & Filtering

### Searching Across Both Languages
For ads search (title/description are single-language, user-created):
```sql
-- User ads remain in user's preferred language
-- But categories and cities need bilingual search
SELECT * FROM ads a
JOIN categories c ON a.category_id = c.id
WHERE (c.name_en LIKE ? OR c.name_ar LIKE ?)
```

---

## Best Practices

### ✅ DO:
1. **Always provide both EN and AR** for system-generated content (cities, categories, stores)
2. **Use Accept-Language header** for API clients to request preferred language
3. **Default to Arabic** for Saudi market
4. **Return full bilingual data** in DTOs when needed (e.g., admin panels, mobile apps caching)
5. **Use UTF-8 encoding** everywhere to support Arabic characters
6. **Validate both fields** when creating/updating bilingual content

### ❌ DON'T:
1. **Don't translate user-generated content** (ad titles, descriptions, comments) - keep them as-is
2. **Don't assume language** based on user location alone - let users choose
3. **Don't hardcode language logic** in multiple places - centralize in service layer
4. **Don't forget RTL considerations** for frontend (out of scope for backend)

---

## Frontend Integration

### Request Headers
```javascript
// React/Next.js example
const response = await fetch('/api/v1/categories', {
  headers: {
    'Accept-Language': localStorage.getItem('preferredLanguage') || 'ar',
    'Content-Type': 'application/json'
  }
});
```

### Language Switcher
```javascript
function setLanguage(lang) {
  localStorage.setItem('preferredLanguage', lang);
  // Refresh data or update state
}
```

---

## Testing

### Unit Test Example
```java
@Test
void shouldReturnArabicNameWhenLocaleIsArabic() {
    Category category = new Category();
    category.setNameEn("Gaming Consoles");
    category.setNameAr("أجهزة الألعاب");
    
    Locale arabicLocale = new Locale("ar");
    CategoryDto dto = categoryMapper.toDto(category, arabicLocale);
    
    assertEquals("أجهزة الألعاب", dto.getName());
}
```

---

## Migration Checklist

When adding bilingual support to a new entity:

- [ ] Add `name_en` and `name_ar` columns to database table
- [ ] Update Flyway migration script
- [ ] Add seed data with both languages
- [ ] Update Entity class with both fields
- [ ] Update DTOs with localization logic
- [ ] Update Service methods to handle locale
- [ ] Update MapStruct mapper with locale context
- [ ] Update Controller to inject locale
- [ ] Add Swagger documentation for Accept-Language
- [ ] Write unit tests for both languages
- [ ] Update API documentation

---

## Future Enhancements

1. **More Languages**: Add support for additional languages by adding columns (`name_fr`, `name_es`, etc.)
2. **Translation API**: Integrate with Google Translate or DeepL for auto-translation suggestions
3. **User Preferences**: Store user's preferred language in profile
4. **Content Negotiation**: Advanced locale negotiation with quality values (e.g., `Accept-Language: ar;q=0.9, en;q=0.8`)
5. **Admin Translation UI**: Build admin interface to manage translations easily

---

## Summary

| **Aspect** | **Implementation** |
|------------|-------------------|
| **Supported Languages** | English (en), Arabic (ar) |
| **Default Language** | Arabic (ar) |
| **Detection Method** | Accept-Language header or `?lang=` param |
| **Database** | Separate columns: `name_en`, `name_ar` |
| **DTOs** | Localized field + full bilingual object |
| **Entities with i18n** | Cities, Categories, Stores |
| **User Content** | Not translated (kept as-is) |

**All bilingual infrastructure is ready for use across the application!** 🌍
