-- Strumica Parking Zones (Removed 'occupied' column)
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S11', 'бул. „Ленинова“', 42, 30.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S12', 'бул. „Маршал Тито“', 36, 30.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S21', 'КГС Партизан', 55, 20.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S24', 'ул. „Димитар Влахов“', 60, 20.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S23', 'ул. „Димитар Влахов“', 60, 20.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S25', 'ул. „Димитар Влахов“', 60, 20.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S26', 'ул. „Димитар Влахов“', 60, 20.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S30', 'Општа Болница', 70, 20.00);
INSERT INTO zones (zone_code, name, capacity, hourly_rate) VALUES ('S41', 'Општа Болница', 70, 20.00);

-- Vehicles
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SR-1234-AB', 'Volkswagen', 'Golf', 'Black');
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SR-5678-CD', 'Toyota', 'Yaris', 'Silver');
INSERT INTO vehicles (license_plate, make, model, color) VALUES ('SK-9999-ZZ', 'Audi', 'A4', 'Blue');

-- Active Parking Tickets
INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid)
VALUES ('SR-1234-AB', 'S11', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, CURRENT_TIMESTAMP), TRUE);

INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid)
VALUES ('SR-5678-CD', 'S12', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), TRUE);

INSERT INTO parking_tickets (license_plate, zone_code, start_time, end_time, is_paid)
VALUES ('SK-9999-ZZ', 'S11', DATEADD('MINUTE', -15, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, CURRENT_TIMESTAMP), TRUE);