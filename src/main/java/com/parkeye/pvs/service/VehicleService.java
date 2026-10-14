package com.parkeye.pvs.service;

import com.parkeye.pvs.account.entity.UserVehicleEntity;
import com.parkeye.pvs.account.repository.UserVehicleRepository;
import com.parkeye.pvs.adapter.MunicipalParkingPort;
import com.parkeye.pvs.dto.ConsumerDtos.AddVehicleRequest;
import com.parkeye.pvs.dto.ConsumerDtos.VehicleDto;
import com.parkeye.pvs.exception.ApiException;
import com.parkeye.pvs.util.PlateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/** A driver's saved cars. These are ParkEye data; the municipal system only sees plates on tickets. */
@Service
public class VehicleService {

    private final UserVehicleRepository repo;
    private final MunicipalParkingPort port;
    private final Clock clock;

    public VehicleService(UserVehicleRepository repo, MunicipalParkingPort port, Clock clock) {
        this.repo = repo;
        this.port = port;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> list(long userId) {
        return repo.findByUserIdOrderByIdAsc(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public VehicleDto add(long userId, AddVehicleRequest req) {
        String plate = PlateUtil.normalize(req.licensePlate());
        if (repo.existsByUserIdAndLicensePlateIgnoreCase(userId, plate)) {
            throw new ApiException(HttpStatus.CONFLICT, "VEHICLE_EXISTS", "This plate is already on your account.");
        }
        boolean first = repo.findByUserIdOrderByIdAsc(userId).isEmpty();

        UserVehicleEntity v = new UserVehicleEntity();
        v.setUserId(userId);
        v.setLicensePlate(plate);
        v.setMake(req.make().trim());
        v.setModel(req.model().trim());
        v.setNickname(req.nickname() == null || req.nickname().isBlank() ? null : req.nickname().trim());
        v.setDefaultVehicle(first);
        return toDto(repo.save(v));
    }

    @Transactional
    public void delete(long userId, long vehicleId) {
        UserVehicleEntity v = owned(userId, vehicleId);
        List<UserVehicleEntity> all = repo.findByUserIdOrderByIdAsc(userId);
        if (all.size() <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "LAST_VEHICLE", "You must keep at least one registered vehicle.");
        }
        Instant now = clock.instant();
        boolean hasActive = port.findSessionsByPlates(List.of(v.getLicensePlate())).stream()
                .anyMatch(s -> s.isActiveAt(now));
        if (hasActive) {
            throw new ApiException(HttpStatus.CONFLICT, "VEHICLE_HAS_ACTIVE_SESSION",
                    "End the active parking session for this vehicle first.");
        }
        repo.delete(v);
        if (Boolean.TRUE.equals(v.getDefaultVehicle())) {
            all.stream().filter(o -> !o.getId().equals(v.getId())).findFirst().ifPresent(o -> {
                o.setDefaultVehicle(true);
                repo.save(o);
            });
        }
    }

    @Transactional
    public List<VehicleDto> setDefault(long userId, long vehicleId) {
        owned(userId, vehicleId);
        List<UserVehicleEntity> all = repo.findByUserIdOrderByIdAsc(userId);
        for (UserVehicleEntity v : all) {
            v.setDefaultVehicle(v.getId().equals(vehicleId));
        }
        repo.saveAll(all);
        return all.stream().map(this::toDto).toList();
    }

    private UserVehicleEntity owned(long userId, long vehicleId) {
        return repo.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Vehicle not found."));
    }

    private VehicleDto toDto(UserVehicleEntity v) {
        return new VehicleDto(v.getId(), v.getLicensePlate(), v.getMake(), v.getModel(), v.getNickname(),
                Boolean.TRUE.equals(v.getDefaultVehicle()));
    }
}
