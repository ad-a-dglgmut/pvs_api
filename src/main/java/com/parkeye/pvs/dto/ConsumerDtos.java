package com.parkeye.pvs.dto;

import com.parkeye.pvs.account.entity.NotificationType;
import com.parkeye.pvs.model.PaymentMethod;
import com.parkeye.pvs.model.ZoneCategory;
import com.parkeye.pvs.model.ZoneStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Request/response shapes of the driver-facing API (/api/v1/consumer). */
public final class ConsumerDtos {

    private ConsumerDtos() {}

    // ---------------------------------------------------------------- zones

    /** distanceMeters is null unless the caller passed lat/lng. */
    public record ZoneDto(
            Long id,
            String zoneCode,
            String name,
            String address,
            ZoneCategory category,
            String workingHours,
            BigDecimal hourlyRate,
            int capacity,
            int occupied,
            int freeSpots,
            ZoneStatus status,
            Double lat,
            Double lng,
            Integer distanceMeters
    ) {}

    // ------------------------------------------------------------- vehicles

    public record VehicleDto(
            Long id,
            String licensePlate,
            String make,
            String model,
            String nickname,
            boolean defaultVehicle
    ) {}

    public record AddVehicleRequest(
            @NotBlank String licensePlate,
            @NotBlank String make,
            @NotBlank String model,
            String nickname
    ) {}

    // ------------------------------------------------------------- sessions

    public record StartParkingRequest(
            @NotBlank String licensePlate,
            @NotBlank String zoneCode,
            @NotNull @Min(1) @Max(8) Integer hours,
            String paymentMethod
    ) {}

    /** hours defaults to 1; paymentMethod defaults to the one used for the session. */
    public record ExtendRequest(
            @Min(1) @Max(8) Integer hours,
            String paymentMethod
    ) {}

    /**
     * remainingSeconds/totalSeconds are computed server-side; serverTime lets the app correct for
     * a wrong phone clock when it runs its countdown.
     */
    public record SessionDto(
            Long ticketId,
            String licensePlate,
            String zoneCode,
            String zoneName,
            Instant startTime,
            Instant endTime,
            BigDecimal totalCost,
            PaymentMethod paymentMethod,
            String receiptNo,
            long totalSeconds,
            long remainingSeconds,
            Instant serverTime
    ) {}

    // -------------------------------------------------------------- history

    public record HistoryItemDto(
            Long ticketId,
            String licensePlate,
            String zoneCode,
            String zoneName,
            Instant startTime,
            Instant endTime,
            long durationMinutes,
            BigDecimal cost,
            BigDecimal refunded,
            PaymentMethod paymentMethod,
            String receiptNo
    ) {}

    public record HistoryStatsDto(
            int totalSessions,
            BigDecimal thisMonthSpent,
            BigDecimal totalSaved
    ) {}

    public record HistoryResponse(HistoryStatsDto stats, List<HistoryItemDto> items) {}

    public record ReceiptDto(
            String receiptNo,
            String issuer,
            Instant issuedAt,
            String licensePlate,
            String zoneName,
            Instant startTime,
            Instant endTime,
            long durationMinutes,
            PaymentMethod paymentMethod,
            BigDecimal total
    ) {}

    // -------------------------------------------------------------- account

    public record ProfileDto(
            Long id,
            String fullName,
            String initials,
            String email,
            boolean verified,
            String municipality,
            String language,
            boolean smsNotifications
    ) {}

    public record UpdateSettingsRequest(Boolean smsNotifications) {}

    public record WalletDto(BigDecimal balance, String currency) {}

    public record TopUpRequest(
            @NotNull @DecimalMin("10") @DecimalMax("5000") BigDecimal amount
    ) {}

    public record SavedCardDto(
            Long id,
            String brand,
            String last4,
            int expiryMonth,
            int expiryYear,
            boolean primary
    ) {}

    public record NotificationDto(
            Long id,
            NotificationType type,
            String title,
            String body,
            Instant createdAt,
            boolean seen
    ) {}

    public record NotificationsResponse(long unreadCount, List<NotificationDto> items) {}
}
