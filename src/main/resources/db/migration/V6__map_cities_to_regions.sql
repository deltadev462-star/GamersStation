-- Correct city-to-region mappings for Saudi Arabia

-- Riyadh Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'riyadh') 
WHERE slug IN ('riyadh', 'al-kharj');

-- Makkah Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'makkah') 
WHERE slug IN ('jeddah', 'mecca', 'taif');

-- Madinah Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'madinah') 
WHERE slug IN ('medina', 'yanbu');

-- Eastern Province
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'eastern-province') 
WHERE slug IN ('dammam', 'khobar', 'dhahran', 'jubail', 'qatif', 'al-ahsa', 'hafar-al-batin');

-- Asir Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'asir') 
WHERE slug IN ('abha', 'khamis-mushait');

-- Tabuk Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'tabuk') 
WHERE slug = 'tabuk';

-- Qassim Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'qassim') 
WHERE slug = 'buraidah';

-- Ha'il Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'hail') 
WHERE slug = 'hail';

-- Najran Region
UPDATE cities SET region_id = (SELECT id FROM regions WHERE slug = 'najran') 
WHERE slug = 'najran';
