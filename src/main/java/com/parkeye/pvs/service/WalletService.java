package com.parkeye.pvs.service;

import com.parkeye.pvs.account.entity.UserEntity;
import com.parkeye.pvs.account.repository.UserRepository;
import com.parkeye.pvs.dto.ConsumerDtos.WalletDto;
import com.parkeye.pvs.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** ParkEye's in-house prepaid wallet. Concurrency is guarded by the @Version on UserEntity. */
@Service
public class WalletService {
    private static final String CURRENCY = "MKD";

    private final UserRepository users;

    public WalletService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public WalletDto get(long userId) {
        return new WalletDto(balanceOf(load(userId)), CURRENCY);
    }

    @Transactional
    public WalletDto credit(long userId, BigDecimal amount) {
        UserEntity u = load(userId);
        u.setWalletBalance(balanceOf(u).add(amount));
        users.save(u);
        return new WalletDto(u.getWalletBalance(), CURRENCY);
    }

    @Transactional
    public WalletDto debit(long userId, BigDecimal amount) {
        UserEntity u = load(userId);
        BigDecimal balance = balanceOf(u);
        if (balance.compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_FUNDS",
                    "Wallet balance is too low (" + balance + " MKD).");
        }
        u.setWalletBalance(balance.subtract(amount));
        users.save(u);
        return new WalletDto(u.getWalletBalance(), CURRENCY);
    }

    private UserEntity load(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));
    }

    private BigDecimal balanceOf(UserEntity u) {
        return u.getWalletBalance() != null ? u.getWalletBalance() : BigDecimal.ZERO;
    }
}
