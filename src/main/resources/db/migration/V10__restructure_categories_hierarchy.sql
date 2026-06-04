-- Restructure categories: add parent platform categories and make existing ones subcategories

-- Step 1: Insert parent platform categories (level 1)
INSERT INTO categories (id, parent_id, name_en, name_ar, slug, level, sort_order, is_active, created_at, updated_at)
VALUES
(1, NULL, 'PlayStation', 'بلايستيشن', 'playstation', 1, 1, true, NOW(), NOW()),
(2, NULL, 'Xbox', 'إكس بوكس', 'xbox', 1, 2, true, NOW(), NOW()),
(3, NULL, 'Nintendo', 'نينتندو', 'nintendo', 1, 3, true, NOW(), NOW()),
(4, NULL, 'PC', 'الكمبيوتر', 'pc', 1, 4, true, NOW(), NOW());

-- Step 2: Update existing categories to be subcategories (level 2) under their platform parent
-- Also simplify names since platform context comes from parent

-- PlayStation subcategories (parent_id = 1)
UPDATE categories SET parent_id = 1, level = 2, name_en = 'Devices', name_ar = 'أجهزة', slug = 'ps-devices', sort_order = 1 WHERE id = 100;
UPDATE categories SET parent_id = 1, level = 2, name_en = 'Games', name_ar = 'ألعاب', slug = 'ps-games', sort_order = 2 WHERE id = 101;
UPDATE categories SET parent_id = 1, level = 2, name_en = 'Accessories', name_ar = 'إكسسوارات', slug = 'ps-accessories', sort_order = 3 WHERE id = 102;

-- Xbox subcategories (parent_id = 2)
UPDATE categories SET parent_id = 2, level = 2, name_en = 'Devices', name_ar = 'أجهزة', slug = 'xbox-devices', sort_order = 1 WHERE id = 200;
UPDATE categories SET parent_id = 2, level = 2, name_en = 'Games', name_ar = 'ألعاب', slug = 'xbox-games', sort_order = 2 WHERE id = 201;
UPDATE categories SET parent_id = 2, level = 2, name_en = 'Accessories', name_ar = 'إكسسوارات', slug = 'xbox-accessories', sort_order = 3 WHERE id = 202;

-- Nintendo subcategories (parent_id = 3)
UPDATE categories SET parent_id = 3, level = 2, name_en = 'Devices', name_ar = 'أجهزة', slug = 'nintendo-devices', sort_order = 1 WHERE id = 300;
UPDATE categories SET parent_id = 3, level = 2, name_en = 'Games', name_ar = 'ألعاب', slug = 'nintendo-games', sort_order = 2 WHERE id = 301;
UPDATE categories SET parent_id = 3, level = 2, name_en = 'Accessories', name_ar = 'إكسسوارات', slug = 'nintendo-accessories', sort_order = 3 WHERE id = 302;

-- PC subcategories (parent_id = 4)
UPDATE categories SET parent_id = 4, level = 2, name_en = 'Devices', name_ar = 'أجهزة', slug = 'pc-devices', sort_order = 1 WHERE id = 400;
UPDATE categories SET parent_id = 4, level = 2, name_en = 'Games', name_ar = 'ألعاب', slug = 'pc-games', sort_order = 2 WHERE id = 401;
UPDATE categories SET parent_id = 4, level = 2, name_en = 'Accessories', name_ar = 'إكسسوارات', slug = 'pc-accessories', sort_order = 3 WHERE id = 402;
