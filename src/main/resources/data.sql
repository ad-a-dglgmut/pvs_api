-- =====================================================================
-- DEMO DATA (H2, in-memory). Re-created on every start.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Municipal side: zones  (occupancy is NOT stored - it is counted from active tickets)
-- lat/lng for S11, S12, S21 come from the HTML prototype.
-- !! The other coordinates are PLACEHOLDERS near the centre - replace with surveyed positions.
-- ---------------------------------------------------------------------
INSERT INTO zones (zone_code, name, address, category, working_hours, capacity, hourly_rate, lat, lng) VALUES
 ('S11', 'бул. „Ленинова“',       'Центар, покрај Градски Плоштад Слобода', 'RED',    '07:00 - 21:00', 42, 30.00, 41.4390, 22.6405),
 ('S12', 'бул. „Маршал Тито“',    'Центар, Струмица (спроти ТЦ Глобал)',   'RED',    '07:00 - 21:00', 36, 30.00, 41.4378, 22.6427),
 ('S21', 'КГС Партизан',          'Ул. Благој Јанков Мучето',              'YELLOW', '07:00 - 21:00', 55, 20.00, 41.4350, 22.6460),
 ('S24', 'ул. „Димитар Влахов“',  'Ул. Димитар Влахов, Струмица',          'YELLOW', '07:00 - 21:00', 60, 20.00, 41.4365, 22.6440),
 ('S23', 'ул. „Димитар Влахов“',  'Ул. Димитар Влахов, Струмица',          'YELLOW', '07:00 - 21:00', 60, 20.00, 41.4360, 22.6447),
 ('S25', 'ул. „Димитар Влахов“',  'Ул. Димитар Влахов, Струмица',          'YELLOW', '07:00 - 21:00', 60, 20.00, 41.4355, 22.6420),
 ('S26', 'ул. „Димитар Влахов“',  'Ул. Димитар Влахов, Струмица',          'YELLOW', '07:00 - 21:00', 60, 20.00, 41.4345, 22.6415),
 ('S30', 'Општа Болница',         'Општа Болница, Струмица',               'YELLOW', '07:00 - 21:00', 70, 20.00, 41.4330, 22.6390),
 ('S41', 'Општа Болница',         'Општа Болница, Струмица',               'YELLOW', '07:00 - 21:00', 70, 20.00, 41.4325, 22.6380);

-- Municipal vehicle registry (used by the scouts)
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SR-1234-AB', 'Volkswagen', 'Golf', 'Black');
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SR-5678-CD', 'Toyota', 'Yaris', 'Silver');
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SK-9999-ZZ', 'Audi', 'A4', 'Blue');

-- ---------------------------------------------------------------------
-- Municipal side: tickets
-- ---------------------------------------------------------------------
-- Active / expired tickets used by the scout demo (unchanged logic, plus cost + payment method)
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SR-1234-AB', 'S11', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, CURRENT_TIMESTAMP), TRUE, 90.00, 0.00, 'CARD');

INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
VALUES ('SR-5678-CD', 'S12', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), TRUE, 75.00, 0.00, 'CARD');

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
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-AA', 'S11', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), TRUE, 90.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 36);
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-BB', 'S24', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), TRUE, 60.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 50);
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid, cost, refunded, payment_method)
SELECT 'DM-' || LPAD(CAST(X AS VARCHAR), 4, '0') || '-CC', 'S41', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), TRUE, 60.00, 0.00, 'CARD' FROM SYSTEM_RANGE(1, 70);
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
VALUES (1, 'WARNING', 'Паркирање во Струмица', 'Вашата сесија на бул. „Ленинова“ истекува наскоро.', DATEADD('MINUTE', -3, CURRENT_TIMESTAMP), FALSE, NULL, NULL);
INSERT INTO notifications (user_id, type, title, body, created_at, seen, ticket_id, kind)
VALUES (1, 'SUCCESS', 'Успешна наплата', 'Наплатени се 30 ден. преку Mastercard **** 8821.', DATEADD('MINUTE', -45, CURRENT_TIMESTAMP), FALSE, NULL, NULL);
