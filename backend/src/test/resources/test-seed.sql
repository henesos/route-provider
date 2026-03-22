-- Seeds required test data before each test.
-- Users (admin/agency) are NOT seeded here — they are created once
-- by DataInitializer on context startup and are never deleted.

-- Locations
INSERT INTO locations (name, country, city, location_code, created_at, updated_at) VALUES
('Istanbul Airport',        'Turkey',         'Istanbul', 'IST',   NOW(), NOW()),
('Sabiha Gokcen Airport',   'Turkey',         'Istanbul', 'SAW',   NOW(), NOW()),
('London Heathrow Airport', 'United Kingdom', 'London',   'LHR',   NOW(), NOW()),
('Taksim Square',           'Turkey',         'Istanbul', 'CCTAK', NOW(), NOW()),
('Wembley Stadium',         'United Kingdom', 'London',   'CCWEM', NOW(), NOW());

-- Transportations (mirrors DataInitializer.initTransportations)
-- Taksim -> IST (UBER, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'UBER', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'CCTAK' AND d.location_code = 'IST';

-- Taksim -> SAW (BUS, weekdays)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'BUS', '{1,2,3,4,5}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'CCTAK' AND d.location_code = 'SAW';

-- IST -> Taksim (UBER, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'UBER', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'IST' AND d.location_code = 'CCTAK';

-- SAW -> Taksim (BUS, weekdays)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'BUS', '{1,2,3,4,5}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'SAW' AND d.location_code = 'CCTAK';

-- IST -> LHR (FLIGHT, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'FLIGHT', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'IST' AND d.location_code = 'LHR';

-- LHR -> IST (FLIGHT, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'FLIGHT', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'LHR' AND d.location_code = 'IST';

-- SAW -> LHR (FLIGHT, Tue/Thu/Sat)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'FLIGHT', '{2,4,6}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'SAW' AND d.location_code = 'LHR';

-- LHR -> Wembley (SUBWAY, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'SUBWAY', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'LHR' AND d.location_code = 'CCWEM';

-- Wembley -> LHR (SUBWAY, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'SUBWAY', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'CCWEM' AND d.location_code = 'LHR';

-- LHR -> Wembley (UBER, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'UBER', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'LHR' AND d.location_code = 'CCWEM';

-- Wembley -> LHR (UBER, daily)
INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type, operating_days, created_at, updated_at)
SELECT o.id, d.id, 'UBER', '{1,2,3,4,5,6,7}', NOW(), NOW()
FROM locations o, locations d WHERE o.location_code = 'CCWEM' AND d.location_code = 'LHR';
