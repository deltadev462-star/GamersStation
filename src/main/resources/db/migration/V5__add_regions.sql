-- Create regions table
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_en VARCHAR(100) NOT NULL UNIQUE,
    name_ar VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert Saudi regions (13 administrative regions)
INSERT INTO regions (name_en, name_ar, slug) VALUES
('Riyadh', 'الرياض', 'riyadh'),
('Makkah', 'مكة المكرمة', 'makkah'),
('Madinah', 'المدينة المنورة', 'madinah'),
('Eastern Province', 'المنطقة الشرقية', 'eastern-province'),
('Asir', 'عسير', 'asir'),
('Tabuk', 'تبوك', 'tabuk'),
('Qassim', 'القصيم', 'qassim'),
('Ha\'il', 'حائل', 'hail'),
('Northern Borders', 'الحدود الشمالية', 'northern-borders'),
('Jazan', 'جازان', 'jazan'),
('Najran', 'نجران', 'najran'),
('Al-Bahah', 'الباحة', 'al-bahah'),
('Al-Jawf', 'الجوف', 'al-jawf');

-- Add region_id to cities table (nullable first)
ALTER TABLE cities ADD COLUMN region_id BIGINT NULL AFTER slug;

-- Update existing cities with their regions (example mapping)
-- You'll need to update this based on your actual city data
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'riyadh') WHERE slug = 'riyadh';
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'makkah') WHERE slug IN ('jeddah', 'makkah', 'taif');
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'eastern-province') WHERE slug IN ('dammam', 'khobar', 'dhahran');
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'madinah') WHERE slug = 'madinah';

-- Set default region for any remaining cities without region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'riyadh' LIMIT 1) WHERE region_id IS NULL;

-- Make region_id NOT NULL and add foreign key
ALTER TABLE cities MODIFY COLUMN region_id BIGINT NOT NULL;
ALTER TABLE cities ADD CONSTRAINT fk_city_region FOREIGN KEY (region_id) REFERENCES regions(id);
ALTER TABLE cities ADD INDEX idx_region (region_id);
