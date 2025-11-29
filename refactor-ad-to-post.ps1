# Refactor Ad to Post - Automated Script
# This script renames all Ad references to Post

$ErrorActionPreference = "Stop"

Write-Host "Starting Ad -> Post refactoring..." -ForegroundColor Cyan

# Step 1: Copy remaining service, controller, and mapper files with content replacement
Write-Host "`n[1/6] Creating Post service files..." -ForegroundColor Yellow

# Read AdService and transform to PostService
$adServiceContent = Get-Content "src\main\java\com\thegamersstation\marketplace\ad\AdService.java" -Raw
$postServiceContent = $adServiceContent `
    -replace 'package com\.thegamersstation\.marketplace\.ad;', 'package com.thegamersstation.marketplace.post;' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.dto\.', 'import com.thegamersstation.marketplace.post.dto.' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.', 'import com.thegamersstation.marketplace.post.' `
    -replace '\bAdDto\b', 'PostDto' `
    -replace '\bCreateAdRequest\b', 'CreatePostRequest' `
    -replace '\bUpdateAdRequest\b', 'UpdatePostRequest' `
    -replace '\bAdRepository\b', 'PostRepository' `
    -replace '\bAdMapper\b', 'PostMapper' `
    -replace '\bAdImage\b', 'PostImage' `
    -replace '\bAd\b', 'Post' `
    -replace 'class AdService', 'class PostService' `
    -replace 'createAd\(', 'createPost(' `
    -replace 'updateAd\(', 'updatePost(' `
    -replace 'getAdById\(', 'getPostById(' `
    -replace 'searchAds\(', 'searchPosts(' `
    -replace 'getMyAds\(', 'getMyPosts(' `
    -replace 'deleteAd\(', 'deletePost(' `
    -replace 'markAsSold\(', 'markAsSold(' `
    -replace '\"Ad not found\"', '"Post not found"' `
    -replace '\"ads\"', '"posts"'

[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\post\PostService.java", $postServiceContent)
Write-Host "  ✓ PostService.java created"

# Step 2: Create PostMapper
Write-Host "`n[2/6] Creating PostMapper..." -ForegroundColor Yellow

$adMapperContent = Get-Content "src\main\java\com\thegamersstation\marketplace\ad\AdMapper.java" -Raw
$postMapperContent = $adMapperContent `
    -replace 'package com\.thegamersstation\.marketplace\.ad;', 'package com.thegamersstation.marketplace.post;' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.dto\.', 'import com.thegamersstation.marketplace.post.dto.' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.', 'import com.thegamersstation.marketplace.post.' `
    -replace '\bAdDto\b', 'PostDto' `
    -replace '\bAdImageDto\b', 'PostImageDto' `
    -replace '\bAdImage\b', 'PostImage' `
    -replace '\bAd\b', 'Post' `
    -replace 'class AdMapper', 'class PostMapper' `
    -replace '\bad\b', 'post' `
    -replace 'getCategoryName\(Ad ad\)', 'getCategoryName(Post post)' `
    -replace 'getCityName\(Ad ad\)', 'getCityName(Post post)' `
    -replace 'ad\.getCategory', 'post.getCategory' `
    -replace 'ad\.getCity', 'post.getCity' `
    -replace 'ad\.getImages', 'post.getImages'

[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\post\PostMapper.java", $postMapperContent)
Write-Host "  ✓ PostMapper.java created"

# Step 3: Create PostController
Write-Host "`n[3/6] Creating PostController..." -ForegroundColor Yellow

$adControllerContent = Get-Content "src\main\java\com\thegamersstation\marketplace\ad\AdController.java" -Raw
$postControllerContent = $adControllerContent `
    -replace 'package com\.thegamersstation\.marketplace\.ad;', 'package com.thegamersstation.marketplace.post;' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.dto\.', 'import com.thegamersstation.marketplace.post.dto.' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.', 'import com.thegamersstation.marketplace.post.' `
    -replace '\bAdDto\b', 'PostDto' `
    -replace '\bCreateAdRequest\b', 'CreatePostRequest' `
    -replace '\bUpdateAdRequest\b', 'UpdatePostRequest' `
    -replace '\bAdService\b', 'PostService' `
    -replace '\bAd\b', 'Post' `
    -replace 'class AdController', 'class PostController' `
    -replace '@RequestMapping\("/api/ads"\)', '@RequestMapping("/api/posts")' `
    -replace '@Tag\(name = "Ads"', '@Tag(name = "Posts"' `
    -replace '"Ad management"', '"Post management"' `
    -replace 'createAd\(', 'createPost(' `
    -replace 'updateAd\(', 'updatePost(' `
    -replace 'getAdById\(', 'getPostById(' `
    -replace 'searchAds\(', 'searchPosts(' `
    -replace 'getMyAds\(', 'getMyPosts(' `
    -replace 'deleteAd\(', 'deletePost(' `
    -replace 'markAsSold\(', 'markAsSold(' `
    -replace '"Create a new ad"', '"Create a new post"' `
    -replace '"Update an ad"', '"Update a post"' `
    -replace '"Get ad by ID"', '"Get post by ID"' `
    -replace '"Search ads"', '"Search posts"' `
    -replace '"Get my ads"', '"Get my posts"' `
    -replace '"Delete an ad"', '"Delete a post"' `
    -replace '"Mark ad as sold"', '"Mark post as sold"'

[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\post\PostController.java", $postControllerContent)
Write-Host "  ✓ PostController.java created"

# Step 4: Move Admin files to admin package
Write-Host "`n[4/6] Creating admin package structure..." -ForegroundColor Yellow

# Create PostModerationService
$adminAdServiceContent = Get-Content "src\main\java\com\thegamersstation\marketplace\admin\AdminAdService.java" -Raw
$postModerationServiceContent = $adminAdServiceContent `
    -replace 'package com\.thegamersstation\.marketplace\.admin;', 'package com.thegamersstation.marketplace.admin.post;' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.dto\.', 'import com.thegamersstation.marketplace.post.dto.' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.', 'import com.thegamersstation.marketplace.post.' `
    -replace '\bAdDto\b', 'PostDto' `
    -replace '\bAdRepository\b', 'PostRepository' `
    -replace '\bAdMapper\b', 'PostMapper' `
    -replace '\bAd\b', 'Post' `
    -replace 'class AdminAdService', 'class PostModerationService' `
    -replace 'getPendingAds\(', 'getPendingPosts(' `
    -replace 'getAllAds\(', 'getAllPosts(' `
    -replace 'approveAd\(', 'approvePost(' `
    -replace 'blockAd\(', 'blockPost(' `
    -replace 'deleteAd\(', 'deletePost(' `
    -replace '\"Ad not found\"', '"Post not found"' `
    -replace 'pending ads', 'pending posts' `
    -replace 'Only pending ads', 'Only pending posts'

[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\admin\post\PostModerationService.java", $postModerationServiceContent)
Write-Host "  ✓ PostModerationService.java created"

# Create PostModerationController
$adminAdControllerContent = Get-Content "src\main\java\com\thegamersstation\marketplace\admin\AdminAdController.java" -Raw
$postModerationControllerContent = $adminAdControllerContent `
    -replace 'package com\.thegamersstation\.marketplace\.admin;', 'package com.thegamersstation.marketplace.admin.post;' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.dto\.', 'import com.thegamersstation.marketplace.post.dto.' `
    -replace 'import com\.thegamersstation\.marketplace\.ad\.', 'import com.thegamersstation.marketplace.post.' `
    -replace '\bAdDto\b', 'PostDto' `
    -replace '\bAd\b', 'Post' `
    -replace '\bAdminAdService\b', 'PostModerationService' `
    -replace 'class AdminAdController', 'class PostModerationController' `
    -replace '@RequestMapping\("/api/admin/ads"\)', '@RequestMapping("/api/admin/posts")' `
    -replace '@Tag\(name = "Admin - Ads"', '@Tag(name = "Admin - Posts"' `
    -replace '"Admin ad moderation"', '"Admin post moderation"' `
    -replace 'getPendingAds\(', 'getPendingPosts(' `
    -replace 'getAllAds\(', 'getAllPosts(' `
    -replace 'approveAd\(', 'approvePost(' `
    -replace 'blockAd\(', 'blockPost(' `
    -replace 'deleteAd\(', 'deletePost(' `
    -replace '"Get pending ads"', '"Get pending posts"' `
    -replace '"Get all ads"', '"Get all posts"' `
    -replace '"Approve a pending ad"', '"Approve a pending post"' `
    -replace '"Block an ad"', '"Block a post"' `
    -replace '"Permanently delete an ad"', '"Permanently delete a post"'

[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\admin\post\PostModerationController.java", $postModerationControllerContent)
Write-Host "  ✓ PostModerationController.java created"

# Step 5: Move other admin files to proper packages
Write-Host "`n[5/6] Moving other admin files..." -ForegroundColor Yellow

# Copy AdminUserController and AdminUserService to admin/user
$adminUserControllerContent = Get-Content "src\main\java\com\thegamersstation\marketplace\admin\AdminUserController.java" -Raw
$adminUserControllerNew = $adminUserControllerContent -replace 'package com\.thegamersstation\.marketplace\.admin;', 'package com.thegamersstation.marketplace.admin.user;'
[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\admin\user\UserModerationController.java", $adminUserControllerNew)
Write-Host "  ✓ UserModerationController.java created"

$adminUserServiceContent = Get-Content "src\main\java\com\thegamersstation\marketplace\admin\AdminUserService.java" -Raw
$adminUserServiceNew = $adminUserServiceContent -replace 'package com\.thegamersstation\.marketplace\.admin;', 'package com.thegamersstation.marketplace.admin.user;'
[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\admin\user\UserModerationService.java", $adminUserServiceNew)
Write-Host "  ✓ UserModerationService.java created"

# Copy AdminCategoryController to admin/category
$adminCategoryControllerContent = Get-Content "src\main\java\com\thegamersstation\marketplace\admin\AdminCategoryController.java" -Raw
$adminCategoryControllerNew = $adminCategoryControllerContent -replace 'package com\.thegamersstation\.marketplace\.admin;', 'package com.thegamersstation.marketplace.admin.category;'
[System.IO.File]::WriteAllText("src\main\java\com\thegamersstation\marketplace\admin\category\CategoryModerationController.java", $adminCategoryControllerNew)
Write-Host "  ✓ CategoryModerationController.java created"

# Step 6: Delete old ad and admin directories
Write-Host "`n[6/6] Cleaning up old files..." -ForegroundColor Yellow
Remove-Item -Path "src\main\java\com\thegamersstation\marketplace\ad" -Recurse -Force
Write-Host "  ✓ Deleted old ad/ directory"

Remove-Item -Path "src\main\java\com\thegamersstation\marketplace\admin\Admin*.java" -Force
Write-Host "  ✓ Deleted old admin files"

Write-Host "`n✅ Refactoring complete!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Update database migration files (ads -> posts, ad_images -> post_images)"
Write-Host "2. Run: mvn clean compile -DskipTests"
Write-Host "3. Fix any remaining compilation errors"
Write-Host "4. Update documentation"
