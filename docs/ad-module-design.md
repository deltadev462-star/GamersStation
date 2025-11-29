# Ad Module Design Documentation

## Overview
The Ad module manages marketplace advertisements where users can post items for sale or requests to buy.

## Ad-User Relationship

### Direct User Ownership
- **Every ad is directly owned by a User** (stored in `owner_id` column)
- The user can be of any role: `USER`, `STORE_MANAGER`, or `ADMIN`
- **No direct store relationship**: Ads are NOT linked to stores, only to users
- This design allows:
  - Normal users to post ads without owning a store
  - Store managers to post personal ads separate from their store
  - Flexibility in user types who can create ads

### Why not link to stores?
In the future, if store features are added, the relationship would be:
- `User` → owns → `Store` (one-to-many)
- `User` → creates → `Ad` (one-to-many)
- Store displays are filtered by querying ads where `owner.store_id = X`

This keeps ads flexible and not tightly coupled to the store concept.

## Price Fields Design

### Optional Pricing
All three price fields are **optional** (nullable):

1. **`price`** (BigDecimal, optional)
   - Fixed selling price
   - Used for: "PlayStation 5 - 2000 SAR"
   - Can be null for "make an offer" scenarios

2. **`priceMin`** (BigDecimal, optional)
   - Minimum acceptable price
   - Used for: "Selling Xbox, minimum 1500 SAR"
   - Or for ASK ads: "Looking for GPU, budget starts at 500 SAR"

3. **`priceMax`** (BigDecimal, optional)
   - Maximum price/budget
   - Used for ASK ads: "Looking for gaming chair, max 1000 SAR"
   - Or price range: "Accepting offers between 1000-1500 SAR"

### Use Cases
- **Fixed price**: Only `price` is set
- **Price range**: Both `priceMin` and `priceMax` are set
- **Make an offer**: All price fields are null
- **Minimum only**: Only `priceMin` is set
- **Budget cap**: For ASK ads, only `priceMax` is set

## DTO Pattern Explanation

### Why Two DTOs? (CreateAdRequest vs UpdateAdRequest)

#### CreateAdRequest
**Purpose**: Used for `POST /api/ads` (creating new ads)

**Characteristics**:
- Contains **all required fields** with strict validation
- `@NotNull`, `@NotBlank`, `@NotEmpty` annotations for mandatory fields
- Ensures data completeness at creation time
- Fields include:
  - `type` (required) - SELL or ASK
  - `title` (required, 5-200 chars)
  - `description` (required, 20-5000 chars)
  - `categoryId` (required)
  - `cityId` (required)
  - `imageUrls` (required, 1-10 images)
  - `price`, `priceMin`, `priceMax` (optional)
  - `condition` (optional)

**Example**:
```json
{
  "type": "SELL",
  "title": "PlayStation 5 Console",
  "description": "Brand new PS5 with controller...",
  "categoryId": 10,
  "cityId": 5,
  "price": 2000.00,
  "condition": "NEW",
  "imageUrls": ["https://...img1.jpg", "https://...img2.jpg"]
}
```

#### UpdateAdRequest
**Purpose**: Used for `PUT /api/ads/{id}` (updating existing ads)

**Characteristics**:
- **All fields are optional** - allows partial updates
- No `@NotNull` validations (except size constraints)
- Only provided fields are updated
- Missing fields are ignored (not set to null)
- Cannot change: `categoryId`, `type` (immutable after creation)

**Benefits**:
- Update just price: `{"price": 1800.00}`
- Update just description: `{"description": "New description..."}`
- Update multiple fields: `{"price": 1800.00, "title": "PS5 - Price Drop!"}`

**Example**:
```json
{
  "price": 1800.00,
  "title": "PlayStation 5 - Price Reduced!"
}
```

### Design Benefits

1. **Clear Intent**
   - `POST` endpoint expects complete ad data
   - `PUT` endpoint allows flexible modifications

2. **Validation Separation**
   - Create: Strict validation ensures data quality
   - Update: Flexible validation allows partial changes

3. **Prevents Errors**
   - Can't accidentally null out required fields during updates
   - Can't change immutable fields (category, type)

4. **Better API Documentation**
   - Clear distinction in Swagger/OpenAPI docs
   - Frontend knows exactly what's required vs optional

5. **Maintainability**
   - Easy to add create-only or update-only fields
   - Can enforce different business rules per operation

### Alternative Approaches (Why We Didn't Use Them)

❌ **Single DTO**: Would need complex conditional validation logic
❌ **PATCH with JSON Patch**: Overly complex for simple updates
❌ **Separate DTOs per field**: Too many classes, maintenance burden

## Ad Types

### SELL
- User wants to sell an item
- Usually has a fixed `price` or price range
- Example: "PS5 for sale - 2000 SAR"

### ASK
- User wants to buy something
- May specify budget range (`priceMin`, `priceMax`)
- Example: "Looking for RTX 4080, budget 3000-4000 SAR"

## Status Flow

```
WAITING_APPROVAL → ACTIVE → SOLD
                  ↓
                BLOCKED
                  ↓
                DELETED
```

- **WAITING_APPROVAL**: New ads pending admin review
- **ACTIVE**: Published and visible to all users
- **SOLD**: Marked as sold by owner
- **BLOCKED**: Blocked by admin
- **DELETED**: Soft-deleted by owner or admin
