# Bilingual Support (EN/AR) - Changes Summary

## ✅ What Was Changed

### 1. Database Schema Changes

#### Tables Updated:
- **`cities`** - Added `name_en` and `name_ar` columns (both NOT NULL, with unique constraints)
- **`categories`** - Added `name_en` and `name_ar` columns (both NOT NULL)
- **`stores`** - Added `name_en`, `name_ar`, `description_en`, `description_ar` columns

#### Migration Files:
- `V1__initial_schema.sql` - Updated table definitions
- `V2__seed_data.sql` - All seed data now includes both English and Arabic names

### 2. Configuration Files

#### New Files:
- `LocaleConfig.java` - Spring i18n configuration
  - Supports EN and AR locales
  - Default locale: Arabic (ar)
  - Accept-Language header detection
  - Query parameter support (`?lang=en` or `?lang=ar`)

#### Updated Files:
- `application.yaml` - Added i18n configuration section

### 3. Utility Classes

#### New Files:
- `LocalizedContent.java` - Utility class for handling bilingual content
  - Stores both `en` and `ar` values
  - Provides `get(Locale)` method for locale-aware retrieval
  - Fallback logic: requested language → default language

### 4. Documentation

#### New Files:
- `BILINGUAL_IMPLEMENTATION.md` - Complete implementation guide
  - Database schema patterns
  - DTO design patterns (3 approaches)
  - Service layer examples
  - Controller layer examples
  - MapStruct integration
  - Best practices and anti-patterns
  - Migration checklist

---

## 🗂️ Files Modified

```
src/main/resources/db/migration/
  ├── V1__initial_schema.sql          ✏️ MODIFIED
  └── V2__seed_data.sql                ✏️ MODIFIED

src/main/java/.../config/
  └── LocaleConfig.java                ➕ NEW

src/main/java/.../common/util/
  └── LocalizedContent.java            ➕ NEW

src/main/resources/
  └── application.yaml                 ✏️ MODIFIED

Documentation/
  ├── BILINGUAL_IMPLEMENTATION.md      ➕ NEW
  ├── BILINGUAL_CHANGES_SUMMARY.md     ➕ NEW (this file)
  └── IMPLEMENTATION_PROGRESS.md       ✏️ MODIFIED
```

---

## 📊 Seed Data Summary

### Cities (20 cities)
All Saudi Arabian cities now have both English and Arabic names:
- Example: `Riyadh` / `الرياض`
- Example: `Jeddah` / `جدة`

### Categories (60+ categories across 3 levels)
Complete gaming category hierarchy in both languages:
- Level 1: `Gaming Consoles` / `أجهزة الألعاب`
- Level 2: `PlayStation` / `بلايستيشن`
- Level 3: `PlayStation 5` / `بلايستيشن 5`

---

## 🔧 How to Use

### For API Consumers

#### 1. Request with Accept-Language Header:
```http
GET /api/v1/categories
Accept-Language: ar
```

#### 2. Request with Query Parameter:
```http
GET /api/v1/categories?lang=en
```

#### 3. Default Behavior:
If no language is specified, Arabic (ar) is used by default.

### For Developers

#### In Controllers:
```java
@GetMapping("/{id}")
public CategoryDto getCategory(
    @PathVariable Long id,
    @RequestHeader(name = "Accept-Language", required = false) Locale locale
) {
    return categoryService.getCategoryLocalized(id, locale);
}
```

#### In Services:
```java
public CategoryDto getCategoryLocalized(Long id, Locale locale) {
    Category category = categoryRepository.findById(id).orElseThrow();
    
    String name = "ar".equals(locale.getLanguage()) 
        ? category.getNameAr() 
        : category.getNameEn();
    
    return CategoryDto.builder()
        .id(category.getId())
        .name(name)
        .nameLocalized(LocalizedContent.of(category.getNameEn(), category.getNameAr()))
        .build();
}
```

---

## 📝 DTO Response Patterns

### Pattern 1: Always Return Both Languages
```json
{
  "id": 1,
  "nameEn": "Gaming Consoles",
  "nameAr": "أجهزة الألعاب",
  "slug": "gaming-consoles"
}
```
**Use Case:** Admin panels, data export, caching

### Pattern 2: Return Localized + Full Bilingual
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
**Use Case:** Mobile apps, SPAs (best of both worlds)

### Pattern 3: Return Only Localized
```json
{
  "id": 1,
  "name": "Gaming Consoles",
  "slug": "gaming-consoles"
}
```
**Use Case:** Lightweight responses, web apps with server-side rendering

---

## ⚠️ Breaking Changes

### Database Schema
❗ **Tables have changed** - you MUST run Flyway migrations:

**Before (OLD):**
```sql
CREATE TABLE cities (
    name VARCHAR(100) NOT NULL UNIQUE
);
```

**After (NEW):**
```sql
CREATE TABLE cities (
    name_en VARCHAR(100) NOT NULL,
    name_ar VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_name_en (name_en),
    UNIQUE KEY uk_name_ar (name_ar)
);
```

### Entity Classes
When you implement entities, they should now have:
```java
@Entity
@Table(name = "cities")
public class City {
    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;
    
    @Column(name = "name_ar", nullable = false, length = 100)
    private String nameAr;
}
```

---

## ✨ Benefits

1. **Better User Experience** - Saudi users see Arabic by default, international users can switch to English
2. **SEO Optimization** - Content available in both languages for search engines
3. **Accessibility** - Support for RTL (Right-to-Left) languages
4. **Market Expansion** - Easy to add more languages in the future
5. **Consistency** - System-generated content (cities, categories) always has proper translations

---

## 🚀 Next Steps

When implementing modules (User, Category, Ads, etc.):

1. **Entities**: Add `@Column` annotations for `name_en` and `name_ar`
2. **DTOs**: Include localized fields using `LocalizedContent`
3. **Services**: Inject `Locale` and return appropriate language
4. **Controllers**: Accept `Accept-Language` header
5. **Mappers**: Use `@Context Locale locale` in MapStruct
6. **Tests**: Test both EN and AR responses
7. **Swagger**: Document `Accept-Language` parameter

---

## 📚 Reference Documents

- **Complete Guide:** `BILINGUAL_IMPLEMENTATION.md`
- **Schema:** `V1__initial_schema.sql`
- **Seed Data:** `V2__seed_data.sql`
- **Config:** `LocaleConfig.java`
- **Utility:** `LocalizedContent.java`

---

## 🔍 Quick Reference

| **Aspect** | **Value** |
|------------|-----------|
| Supported Languages | English (en), Arabic (ar) |
| Default Language | Arabic (ar) |
| Detection Method | Accept-Language header or ?lang= param |
| Affected Tables | cities, categories, stores |
| Locale Config | `LocaleConfig.java` |
| Utility Class | `LocalizedContent.java` |
| Documentation | `BILINGUAL_IMPLEMENTATION.md` |

---

**Status:** ✅ Bilingual infrastructure is complete and ready for implementation!
