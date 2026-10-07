package com.parkeye.pvs.service;

import com.parkeye.pvs.adapter.entity.ParkingTicketEntity;
import com.parkeye.pvs.adapter.entity.ZoneEntity;
import com.parkeye.pvs.adapter.repository.ParkingTicketRepository;
import com.parkeye.pvs.adapter.repository.ZoneRepository;
import com.parkeye.pvs.dto.ConsumerDtos.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsumerService {

    private final ZoneRepository zoneRepository;
    private final ParkingTicketRepository ticketRepository;

    public ConsumerService(ZoneRepository zoneRepository, ParkingTicketRepository ticketRepository) {
        this.zoneRepository = zoneRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<ConsumerVehicle> getMyVehicles() {
        // Mocking saved vehicles for the consumer
        return List.of(
                new ConsumerVehicle("SR-1234-AB", "Volkswagen", "Golf"),
                new ConsumerVehicle("SK-9999-ZZ", "Toyota", "Yaris")
        );
    }

    public ActiveSessionResponse startParking(StartParkingRequest req) {
        ZoneEntity zone = zoneRepository.findAll().stream()
                .filter(z -> z.getZoneCode().equalsIgnoreCase(req.zoneCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Zone not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(req.hours());
        BigDecimal cost = zone.getHourlyRate().multiply(BigDecimal.valueOf(req.hours()));

        // Save the ticket to the database
        ParkingTicketEntity ticket = new ParkingTicketEntity();
        ticket.setLicensePlate(req.licensePlate());
        ticket.setZoneCode(zone.getZoneCode());
        ticket.setStartTime(now);
        ticket.setEndTime(endTime);
        ticket.setIsPaid(true); // Assuming payment succeeds automatically for now

        ticket = ticketRepository.save(ticket);

        // Update zone occupancy
        zone.setOccupied(Math.min(zone.getCapacity(), zone.getOccupied() + 1));
        zoneRepository.save(zone);

        return new ActiveSessionResponse(
                ticket.getId(), req.licensePlate(), zone.getZoneCode(), zone.getName(), now, endTime, cost
        );
    }
}