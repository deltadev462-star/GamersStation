-- Seed Cities (Major Saudi Arabian Cities)
INSERT INTO cities (name_en, name_ar, slug) VALUES
('Riyadh', 'الرياض', 'riyadh'),
('Jeddah', 'جدة', 'jeddah'),
('Mecca', 'مكة المكرمة', 'mecca'),
('Medina', 'المدينة المنورة', 'medina'),
('Dammam', 'الدمام', 'dammam'),
('Khobar', 'الخبر', 'khobar'),
('Dhahran', 'الظهران', 'dhahran'),
('Taif', 'الطائف', 'taif'),
('Tabuk', 'تبوك', 'tabuk'),
('Buraidah', 'بريدة', 'buraidah'),
('Khamis Mushait', 'خميس مشيط', 'khamis-mushait'),
('Hail', 'حائل', 'hail'),
('Najran', 'نجران', 'najran'),
('Hafar Al-Batin', 'حفر الباطن', 'hafar-al-batin'),
('Jubail', 'الجبيل', 'jubail'),
('Abha', 'أبها', 'abha'),
('Yanbu', 'ينبع', 'yanbu'),
('Al-Kharj', 'الخرج', 'al-kharj'),
('Qatif', 'القطيف', 'qatif'),
('Al-Ahsa', 'الأحساء', 'al-ahsa');

-- Seed Categories (3-level hierarchy for gaming)

-- Level 1: Main Categories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Gaming Consoles', 'أجهزة الألعاب', 'gaming-consoles', NULL, 1, 1),
('PC Gaming', 'ألعاب الكمبيوتر', 'pc-gaming', NULL, 1, 2),
('Gaming Accessories', 'إكسسوارات الألعاب', 'gaming-accessories', NULL, 1, 3),
('Gaming Accounts', 'حسابات الألعاب', 'gaming-accounts', NULL, 1, 4),
('Collectibles & Merchandise', 'المقتنيات والبضائع', 'collectibles-merchandise', NULL, 1, 5),
('Mobile Gaming', 'ألعاب الجوال', 'mobile-gaming', NULL, 1, 6);

-- Level 2: Gaming Consoles Subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('PlayStation', 'بلايستيشن', 'playstation', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-consoles') AS temp), 2, 1),
('Xbox', 'إكس بوكس', 'xbox', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-consoles') AS temp), 2, 2),
('Nintendo', 'نينتندو', 'nintendo', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-consoles') AS temp), 2, 3),
('Retro Consoles', 'أجهزة كلاسيكية', 'retro-consoles', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-consoles') AS temp), 2, 4);

-- Level 3: PlayStation Sub-subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('PlayStation 5', 'بلايستيشن 5', 'playstation-5', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'playstation') AS temp), 3, 1),
('PlayStation 4', 'بلايستيشن 4', 'playstation-4', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'playstation') AS temp), 3, 2),
('PlayStation 3', 'بلايستيشن 3', 'playstation-3', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'playstation') AS temp), 3, 3),
('PS VR', 'بلايستيشن في آر', 'ps-vr', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'playstation') AS temp), 3, 4);

-- Level 3: Xbox Sub-subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Xbox Series X|S', 'إكس بوكس سيريس إكس|إس', 'xbox-series-xs', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'xbox') AS temp), 3, 1),
('Xbox One', 'إكس بوكس ون', 'xbox-one', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'xbox') AS temp), 3, 2),
('Xbox 360', 'إكس بوكس 360', 'xbox-360', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'xbox') AS temp), 3, 3);

-- Level 3: Nintendo Sub-subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Nintendo Switch', 'نينتندو سويتش', 'nintendo-switch', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'nintendo') AS temp), 3, 1),
('Nintendo 3DS', 'نينتندو 3دي إس', 'nintendo-3ds', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'nintendo') AS temp), 3, 2),
('Wii/Wii U', 'وي/وي يو', 'wii-wii-u', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'nintendo') AS temp), 3, 3);

-- Level 2: PC Gaming Subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Gaming PCs', 'أجهزة كمبيوتر الألعاب', 'gaming-pcs', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-gaming') AS temp), 2, 1),
('Gaming Laptops', 'لابتوبات الألعاب', 'gaming-laptops', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-gaming') AS temp), 2, 2),
('PC Components', 'قطع الكمبيوتر', 'pc-components', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-gaming') AS temp), 2, 3),
('VR Headsets', 'نظارات الواقع الافتراضي', 'vr-headsets', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-gaming') AS temp), 2, 4);

-- Level 3: PC Components Sub-subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Graphics Cards', 'كروت الشاشة', 'graphics-cards', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-components') AS temp), 3, 1),
('Processors', 'المعالجات', 'processors', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-components') AS temp), 3, 2),
('RAM', 'الذاكرة العشوائية', 'ram', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-components') AS temp), 3, 3),
('Motherboards', 'اللوحات الأم', 'motherboards', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-components') AS temp), 3, 4),
('Storage', 'وحدات التخزين', 'storage', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-components') AS temp), 3, 5),
('Power Supply', 'مزود الطاقة', 'power-supply', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'pc-components') AS temp), 3, 6);

-- Level 2: Gaming Accessories Subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Controllers', 'يدات التحكم', 'controllers', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accessories') AS temp), 2, 1),
('Headsets', 'سماعات الرأس', 'headsets', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accessories') AS temp), 2, 2),
('Keyboards & Mice', 'لوحات المفاتيح والفأرة', 'keyboards-mice', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accessories') AS temp), 2, 3),
('Gaming Chairs', 'كراسي الألعاب', 'gaming-chairs', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accessories') AS temp), 2, 4),
('Monitors', 'الشاشات', 'monitors', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accessories') AS temp), 2, 5),
('Streaming Equipment', 'معدات البث المباشر', 'streaming-equipment', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accessories') AS temp), 2, 6);

-- Level 3: Controllers Sub-subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('PS Controllers', 'يدات بلايستيشن', 'ps-controllers', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'controllers') AS temp), 3, 1),
('Xbox Controllers', 'يدات إكس بوكس', 'xbox-controllers', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'controllers') AS temp), 3, 2),
('Pro Controllers', 'يدات احترافية', 'pro-controllers', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'controllers') AS temp), 3, 3),
('Racing Wheels', 'عجلات السباق', 'racing-wheels', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'controllers') AS temp), 3, 4),
('Flight Sticks', 'عصي الطيران', 'flight-sticks', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'controllers') AS temp), 3, 5);

-- Level 2: Gaming Accounts Subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('PSN Accounts', 'حسابات بلايستيشن نتورك', 'psn-accounts', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accounts') AS temp), 2, 1),
('Xbox Live Accounts', 'حسابات إكس بوكس لايف', 'xbox-live-accounts', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accounts') AS temp), 2, 2),
('Steam Accounts', 'حسابات ستيم', 'steam-accounts', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accounts') AS temp), 2, 3),
('Epic Games Accounts', 'حسابات إيبك قيمز', 'epic-games-accounts', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accounts') AS temp), 2, 4),
('In-Game Currencies', 'العملات داخل اللعبة', 'in-game-currencies', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'gaming-accounts') AS temp), 2, 5);

-- Level 2: Collectibles Subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Figures & Statues', 'التماثيل والمجسمات', 'figures-statues', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'collectibles-merchandise') AS temp), 2, 1),
('Posters & Art', 'الملصقات والفن', 'posters-art', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'collectibles-merchandise') AS temp), 2, 2),
('Clothing & Apparel', 'الملابس', 'clothing-apparel', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'collectibles-merchandise') AS temp), 2, 3),
('Limited Editions', 'إصدارات محدودة', 'limited-editions', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'collectibles-merchandise') AS temp), 2, 4);

-- Level 2: Mobile Gaming Subcategories
INSERT INTO categories (name_en, name_ar, slug, parent_id, level, sort_order) VALUES
('Gaming Phones', 'هواتف الألعاب', 'gaming-phones', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'mobile-gaming') AS temp), 2, 1),
('Mobile Accessories', 'إكسسوارات الجوال', 'mobile-accessories', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'mobile-gaming') AS temp), 2, 2),
('Mobile Game Accounts', 'حسابات ألعاب الجوال', 'mobile-game-accounts', (SELECT id FROM (SELECT id FROM categories WHERE slug = 'mobile-gaming') AS temp), 2, 3);

-- Create an admin user (password should be set via API)
-- Default phone: +966500000000, password will need to be set on first login
INSERT INTO users (phone_number, username, role, is_active, profile_completed) VALUES
('+966500000000', 'admin', 'ADMIN', TRUE, TRUE);
