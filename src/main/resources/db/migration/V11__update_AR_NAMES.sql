ALTER TABLE categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. تصحيح أسماء المنصات الرئيسية (Level 1)
UPDATE categories SET name_ar = 'بلايستيشن' WHERE id = 1;
UPDATE categories SET name_ar = 'إكس بوكس' WHERE id = 2;
UPDATE categories SET name_ar = 'نينتندو' WHERE id = 3;
UPDATE categories SET name_ar = 'بي سي' WHERE id = 4;

-- 3. تصحيح أسماء التصنيفات الفرعية (Level 2) بناءً على الـ Slug
-- استخدمت الـ Slug هنا لأنه أدق من الـ ID في حال كانت البيانات متكررة
UPDATE categories SET name_ar = 'أجهزة' WHERE slug LIKE '%-devices';
UPDATE categories SET name_ar = 'ألعاب' WHERE slug LIKE '%-games';
UPDATE categories SET name_ar = 'إكسسوارات' WHERE slug LIKE '%-accessories';