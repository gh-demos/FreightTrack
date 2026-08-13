CREATE TABLE IF NOT EXISTS customers (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  address VARCHAR(255),
  city VARCHAR(50),
  state VARCHAR(50),
  postal_code VARCHAR(10),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS drivers (
  id BIGSERIAL PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  license_number VARCHAR(20) NOT NULL UNIQUE,
  license_expiry_date DATE NOT NULL,
  email VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  license_class VARCHAR(50),
  address VARCHAR(255),
  city VARCHAR(50),
  state VARCHAR(50),
  postal_code VARCHAR(10),
  status VARCHAR(20) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipments (
  id BIGSERIAL PRIMARY KEY,
  tracking_number VARCHAR(50) NOT NULL UNIQUE,
  customer_id BIGINT NOT NULL REFERENCES customers(id),
  origin VARCHAR(255) NOT NULL,
  destination VARCHAR(255) NOT NULL,
  weight NUMERIC(12,2) NOT NULL,
  weight_unit VARCHAR(50),
  value NUMERIC(14,2) NOT NULL,
  currency VARCHAR(50) NOT NULL,
  description VARCHAR(500),
  status VARCHAR(30) NOT NULL,
  pickup_date DATE NOT NULL,
  expected_delivery_date DATE NOT NULL,
  actual_delivery_date DATE,
  driver_id BIGINT REFERENCES drivers(id),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tracking_events (
  id BIGSERIAL PRIMARY KEY,
  shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
  event_type VARCHAR(50) NOT NULL,
  location VARCHAR(255) NOT NULL,
  latitude VARCHAR(50),
  longitude VARCHAR(50),
  description VARCHAR(500),
  event_time TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS delivery_routes (
  id BIGSERIAL PRIMARY KEY,
  route_code VARCHAR(50) NOT NULL UNIQUE,
  route_name VARCHAR(100) NOT NULL,
  driver_id BIGINT NOT NULL REFERENCES drivers(id),
  route_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  start_location VARCHAR(255) NOT NULL,
  end_location VARCHAR(255) NOT NULL,
  planned_stops INTEGER NOT NULL,
  actual_stops INTEGER,
  estimated_distance DOUBLE PRECISION,
  actual_distance DOUBLE PRECISION,
  status VARCHAR(20) NOT NULL,
  notes VARCHAR(500),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipping_exceptions (
  id BIGSERIAL PRIMARY KEY,
  shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
  exception_type VARCHAR(40) NOT NULL,
  description VARCHAR(500) NOT NULL,
  location VARCHAR(255),
  status VARCHAR(20) NOT NULL,
  reported_at TIMESTAMP NOT NULL,
  resolved_at TIMESTAMP,
  resolution_notes VARCHAR(500),
  reported_by VARCHAR(100),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
  id BIGSERIAL PRIMARY KEY,
  recipient VARCHAR(100) NOT NULL,
  type VARCHAR(50) NOT NULL,
  message VARCHAR(500) NOT NULL,
  subject VARCHAR(500),
  shipment_id BIGINT REFERENCES shipments(id) ON DELETE SET NULL,
  status VARCHAR(20) NOT NULL,
  sent_at TIMESTAMP,
  read_at TIMESTAMP,
  delivery_channel VARCHAR(500),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

-- Demo seed data to ensure frontend sections are populated.
INSERT INTO customers (name, email, phone, address, city, state, postal_code, active, created_at, updated_at)
VALUES
  ('Contoso Retail', 'ops@contoso.example', '+1-425-555-0101', '1 Microsoft Way', 'Redmond', 'WA', '98052', TRUE, NOW(), NOW()),
  ('Northwind Traders', 'logistics@northwind.example', '+1-206-555-0142', '500 Pine St', 'Seattle', 'WA', '98101', TRUE, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO drivers (
  first_name,
  last_name,
  license_number,
  license_expiry_date,
  email,
  phone,
  license_class,
  address,
  city,
  state,
  postal_code,
  status,
  active,
  created_at,
  updated_at
)
VALUES
  ('Alex', 'Johnson', 'WA-TRK-1001', CURRENT_DATE + INTERVAL '365 days', 'alex.johnson@freighttrack.example', '+1-425-555-0191', 'Class A', '44 Harbor Ave', 'Tacoma', 'WA', '98402', 'AVAILABLE', TRUE, NOW(), NOW()),
  ('Priya', 'Sharma', 'WA-TRK-1002', CURRENT_DATE + INTERVAL '400 days', 'priya.sharma@freighttrack.example', '+1-206-555-0177', 'Class A', '88 Lake View', 'Bellevue', 'WA', '98004', 'AVAILABLE', TRUE, NOW(), NOW())
ON CONFLICT (license_number) DO NOTHING;

INSERT INTO shipments (
  tracking_number,
  customer_id,
  origin,
  destination,
  weight,
  weight_unit,
  value,
  currency,
  description,
  status,
  pickup_date,
  expected_delivery_date,
  actual_delivery_date,
  driver_id,
  created_at,
  updated_at
)
SELECT
  'FT-2026-0001',
  c.id,
  'Seattle, WA',
  'Portland, OR',
  1200.50,
  'KG',
  48500.00,
  'USD',
  'Consumer electronics pallet',
  'IN_TRANSIT',
  CURRENT_DATE - 1,
  CURRENT_DATE + 1,
  NULL,
  d.id,
  NOW(),
  NOW()
FROM customers c, drivers d
WHERE c.email = 'ops@contoso.example'
  AND d.license_number = 'WA-TRK-1001'
  AND NOT EXISTS (SELECT 1 FROM shipments s WHERE s.tracking_number = 'FT-2026-0001');

INSERT INTO shipments (
  tracking_number,
  customer_id,
  origin,
  destination,
  weight,
  weight_unit,
  value,
  currency,
  description,
  status,
  pickup_date,
  expected_delivery_date,
  actual_delivery_date,
  driver_id,
  created_at,
  updated_at
)
SELECT
  'FT-2026-0002',
  c.id,
  'Redmond, WA',
  'San Jose, CA',
  850.00,
  'KG',
  31200.00,
  'USD',
  'Networking equipment',
  'DELIVERED',
  CURRENT_DATE - 4,
  CURRENT_DATE - 1,
  CURRENT_DATE - 1,
  d.id,
  NOW(),
  NOW()
FROM customers c, drivers d
WHERE c.email = 'logistics@northwind.example'
  AND d.license_number = 'WA-TRK-1002'
  AND NOT EXISTS (SELECT 1 FROM shipments s WHERE s.tracking_number = 'FT-2026-0002');

INSERT INTO tracking_events (
  shipment_id,
  event_type,
  location,
  latitude,
  longitude,
  description,
  event_time,
  created_at
)
SELECT
  s.id,
  'PICKED_UP',
  'Seattle Distribution Center',
  '47.6062',
  '-122.3321',
  'Shipment picked up from origin warehouse',
  NOW() - INTERVAL '20 hours',
  NOW()
FROM shipments s
WHERE s.tracking_number = 'FT-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM tracking_events te
    WHERE te.shipment_id = s.id AND te.event_type = 'PICKED_UP'
  );

INSERT INTO tracking_events (
  shipment_id,
  event_type,
  location,
  latitude,
  longitude,
  description,
  event_time,
  created_at
)
SELECT
  s.id,
  'IN_TRANSIT',
  'I-5 Corridor, WA',
  '47.2529',
  '-122.4443',
  'Shipment is in transit toward destination',
  NOW() - INTERVAL '6 hours',
  NOW()
FROM shipments s
WHERE s.tracking_number = 'FT-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM tracking_events te
    WHERE te.shipment_id = s.id AND te.event_type = 'IN_TRANSIT'
  );

INSERT INTO delivery_routes (
  route_code,
  route_name,
  driver_id,
  route_date,
  start_time,
  end_time,
  start_location,
  end_location,
  planned_stops,
  actual_stops,
  estimated_distance,
  actual_distance,
  status,
  notes,
  created_at,
  updated_at
)
SELECT
  'RT-WA-001',
  'Seattle to Portland Mainline',
  d.id,
  CURRENT_DATE,
  TIME '08:00:00',
  TIME '14:30:00',
  'Seattle, WA',
  'Portland, OR',
  4,
  2,
  280.5,
  140.2,
  'ACTIVE',
  'Primary route for Northwest corridor deliveries',
  NOW(),
  NOW()
FROM drivers d
WHERE d.license_number = 'WA-TRK-1001'
  AND NOT EXISTS (SELECT 1 FROM delivery_routes r WHERE r.route_code = 'RT-WA-001');

INSERT INTO shipping_exceptions (
  shipment_id,
  exception_type,
  description,
  location,
  status,
  reported_at,
  resolved_at,
  resolution_notes,
  reported_by,
  created_at,
  updated_at
)
SELECT
  s.id,
  'WEATHER_DELAY',
  'Severe weather slowed transit by approximately 2 hours.',
  'Olympia, WA',
  'OPEN',
  NOW() - INTERVAL '5 hours',
  NULL,
  NULL,
  'System Monitor',
  NOW(),
  NOW()
FROM shipments s
WHERE s.tracking_number = 'FT-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM shipping_exceptions se
    WHERE se.shipment_id = s.id AND se.exception_type = 'WEATHER_DELAY'
  );

INSERT INTO notifications (
  recipient,
  type,
  message,
  subject,
  shipment_id,
  status,
  sent_at,
  read_at,
  delivery_channel,
  created_at,
  updated_at
)
SELECT
  'ops@contoso.example',
  'SHIPMENT_IN_TRANSIT',
  'Shipment FT-2026-0001 is currently in transit.',
  'FreightTrack Shipment Update',
  s.id,
  'SENT',
  NOW() - INTERVAL '4 hours',
  NULL,
  'EMAIL',
  NOW(),
  NOW()
FROM shipments s
WHERE s.tracking_number = 'FT-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM notifications n
    WHERE n.shipment_id = s.id AND n.recipient = 'ops@contoso.example' AND n.type = 'EMAIL'
  );
