-- =====================================================================
-- DEMO DATA (H2, in-memory). Re-created on every start.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Municipal side: zones  (occupancy is NOT stored - it is counted from active tickets)
-- Source of the codes/prices/hours: ЈП "Паркиралишта Струмица" (strumicaparking.mk).
-- Zones have NO names - they are identified by code only. There is no S25.
--
--   RED    S11, S12                   30 ден/ч, max 2 ч
--   YELLOW S21-S24, S26-S30           20 ден/ч, no time limit
--   GARAGE S41 (underpass, Goce Delchev square)   50 ден per started hour, 07:00-00:00 daily
--   Street zones: Mon-Fri 07:00-21:00, Sat 07:00-17:00, free on Sundays and public holidays.
--
-- TODO(tariff): the operator published a new price list effective 2026-09-01 (zones A/B/C/D at
--   50/30/blank/50 ден/ч). It does not say which S-code belongs to which zone, so the 30/20 prices
--   above are kept until that mapping is known.
-- TODO(capacity): the capacities below are PLACEHOLDERS (the operator's counts are not known yet).
-- TODO(coordinates): lat/lng are NULL where the position is not known - fill in surveyed positions.
--   S11, S12, S21 come from the HTML prototype; S24 and S30 are earlier rough placeholders.
-- ---------------------------------------------------------------------
INSERT INTO zones (zone_code, address, category, working_hours, capacity, hourly_rate, max_duration_hours, lat, lng) VALUES
                                                                                                                         ('S11', NULL, 'RED',    'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 42, 30.00, 2,    41.4390, 22.6405),
                                                                                                                         ('S12', NULL, 'RED',    'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 36, 30.00, 2,    41.4378, 22.6427),
                                                                                                                         ('S21', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 55, 20.00, NULL, 41.4350, 22.6460),
                                                                                                                         ('S22', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 40, 20.00, NULL, NULL,    NULL),
                                                                                                                         ('S23', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 60, 20.00, NULL, NULL,    NULL),
                                                                                                                         ('S24', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 60, 20.00, NULL, 41.4365, 22.6440),
                                                                                                                         ('S26', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 60, 20.00, NULL, NULL,    NULL),
                                                                                                                         ('S27', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 40, 20.00, NULL, NULL,    NULL),
                                                                                                                         ('S28', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 40, 20.00, NULL, NULL,    NULL),
                                                                                                                         ('S29', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 40, 20.00, NULL, NULL,    NULL),
                                                                                                                         ('S30', NULL, 'YELLOW', 'Пон-Пет 07:00-21:00, Саб 07:00-17:00, Нед и празници бесплатно', 70, 20.00, NULL, 41.4330, 22.6390),
                                                                                                                         ('S41', NULL, 'GARAGE', 'Секој ден 07:00-00:00',                                          70, 50.00, NULL, NULL,    NULL);

-- Municipal vehicle registry (used by the scouts)
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SR-1234-AB', 'Volkswagen', 'Golf', 'Black');
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SR-5678-CD', 'Toyota', 'Yaris', 'Silver');
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SK-9999-ZZ', 'Audi', 'A4', 'Blue');

-- ---------------------------------------------------------------------
-- Municipal side: tickets  (red zones S11/S12 allow max 2 h, so the demo tickets there respect that)
-- ---------------------------------------------------------------------
-- Active / expired tickets used by the scout demo
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SR-1234-AB', 'S11', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, CURRENT_TIMESTAMP), TRUE, 60.00, 0.00, 'CARD');

INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SR-5678-CD', 'S12', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), TRUE, 45.00, 0.00, 'CARD');

INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SK-9999-ZZ', 'S11', DATEADD('MINUTE', -15, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, CURRENT_TIMESTAMP), TRUE, 37.50, 0.00, 'SMS');

-- Past sessions of the demo driver (feed the History tab). Third one was stopped early => refund.
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SR-1234-AB', 'S12', DATEADD('HOUR', -50, CURRENT_TIMESTAMP), DATEADD('HOUR', -49, CURRENT_TIMESTAMP), TRUE, 30.00, 0.00, 'CARD');
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SR-1234-AB', 'S21', DATEADD('HOUR', -120, CURRENT_TIMESTAMP), DATEADD('HOUR', -118, CURRENT_TIMESTAMP), TRUE, 40.00, 0.00, 'WALLET');
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SR-1234-AB', 'S11', DATEADD('HOUR', -75, CURRENT_TIMESTAMP), DATEADD('MINUTE', -(75*60 - 90), CURRENT_TIMESTAMP), TRUE, 45.00, 45.00, 'WALLET');

-- DEMO FILLER: synthetic parked cars so zones show different availability (remove in production).
--   S11: 36 + 2 real above = 38/42 (limited)   S24: 50/60 (limited)   S41: 70/70 (full)
--   S21: 20/55, S30: 25/70 (available)
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-AA', 'S11', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('MINUTE', 100, CURRENT_TIMESTAMP), TRUE, 60.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 36);
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-BB', 'S24', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), TRUE, 60.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 50);
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-CC', 'S41', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), TRUE, 150.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 70);
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-DD', 'S21', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), TRUE, 60.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 20);
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-EE', 'S30', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), TRUE, 60.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 25);

-- ---------------------------------------------------------------------
-- ParkEye side (own database): demo driver "Гоце Делчев" = user id 1
-- ---------------------------------------------------------------------
INSERT INTO app_users (full_name, email, verified, municipality, language, sms_notifications, wallet_balance, version)
VALUES ('Гоце Делчев', 'goce.d@strumica.gov.mk', TRUE, 'Општина Струмица', 'Македонски (MK)', TRUE, 380.00, 0);

INSERT INTO user_vehicles (user_id, license_plate, make, model, nickname, default_vehicle)
VALUES (1, 'SR-1234-AB', 'Volkswagen', 'Golf', 'Мојата Голф', TRUE);
INSERT INTO user_vehicles (user_id, license_plate, make, model, nickname, default_vehicle)
VALUES (1, 'SR-8844-CD', 'Renault', 'Clio', 'Службена', FALSE);

INSERT INTO saved_cards (user_id, brand, last4, expiry_month, expiry_year, primary_card)
VALUES (1, 'Mastercard', '8821', 11, 2027, TRUE);

INSERT INTO notifications (user_id, type, title, body, created_at, seen, ticket_id, kind)
VALUES (1, 'WARNING', 'Паркирање во Струмица', 'Вашата сесија на S11 истекува наскоро.', DATEADD('MINUTE', -3, CURRENT_TIMESTAMP), FALSE, NULL, NULL);
INSERT INTO notifications (user_id, type, title, body, created_at, seen, ticket_id, kind)
VALUES (1, 'SUCCESS', 'Успешна наплата', 'Наплатени се 30 ден. преку Mastercard **** 8821.', DATEADD('MINUTE', -45, CURRENT_TIMESTAMP), FALSE, NULL, NULL);