package com.parkeye.pvs.controller;

import com.parkeye.pvs.dto.ConsumerDtos.*;
import com.parkeye.pvs.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Driver-facing: profile, vehicles, wallet, saved cards, notifications. */
@RestController
@RequestMapping("/api/v1/consumer")
@CrossOrigin(origins = "*")
public class ConsumerAccountController {

    private final ProfileService profileService;
    private final VehicleService vehicleService;
    private final WalletService walletService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    public ConsumerAccountController(ProfileService profileService, VehicleService vehicleService,
                                     WalletService walletService, PaymentService paymentService,
                                     NotificationService notificationService, CurrentUser currentUser) {
        this.profileService = profileService;
        this.vehicleService = vehicleService;
        this.walletService = walletService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    // ---------------------------------------------------------------- profile

    @GetMapping("/me")
    public ResponseEntity<ProfileDto> me() {
        return ResponseEntity.ok(profileService.get(currentUser.id()));
    }

    @PatchMapping("/me/settings")
    public ResponseEntity<ProfileDto> updateSettings(@RequestBody UpdateSettingsRequest request) {
        return ResponseEntity.ok(profileService.updateSettings(currentUser.id(), request));
    }

    // --------------------------------------------------------------- vehicles

    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleDto>> getMyVehicles() {
        return ResponseEntity.ok(vehicleService.list(currentUser.id()));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<VehicleDto> addVehicle(@Valid @RequestBody AddVehicleRequest request) {
        return ResponseEntity.ok(vehicleService.add(currentUser.id(), request));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable long vehicleId) {
        vehicleService.delete(currentUser.id(), vehicleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/vehicles/{vehicleId}/default")
    public ResponseEntity<List<VehicleDto>> setDefaultVehicle(@PathVariable long vehicleId) {
        return ResponseEntity.ok(vehicleService.setDefault(currentUser.id(), vehicleId));
    }

    // ------------------------------------------------------- wallet & payments

    @GetMapping("/me/wallet")
    public ResponseEntity<WalletDto> getWallet() {
        return ResponseEntity.ok(walletService.get(currentUser.id()));
    }

    @PostMapping("/me/wallet/topup")
    public ResponseEntity<WalletDto> topUp(@Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(paymentService.topUpWallet(currentUser.id(), request.amount()));
    }

    @GetMapping("/me/payment-methods")
    public ResponseEntity<List<SavedCardDto>> getPaymentMethods() {
        return ResponseEntity.ok(profileService.savedCards(currentUser.id()));
    }

    // ---------------------------------------------------------- notifications

    @GetMapping("/notifications")
    public ResponseEntity<NotificationsResponse> getNotifications() {
        return ResponseEntity.ok(notificationService.list(currentUser.id()));
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllSeen(currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
