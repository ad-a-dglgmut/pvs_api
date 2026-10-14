package com.parkeye.pvs.service;

import com.parkeye.pvs.account.entity.SavedCardEntity;
import com.parkeye.pvs.account.repository.SavedCardRepository;
import com.parkeye.pvs.dto.ConsumerDtos.WalletDto;
import com.parkeye.pvs.exception.ApiException;
import com.parkeye.pvs.model.PaymentMethod;
import com.parkeye.pvs.payment.PaymentGateway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Routes a payment to the wallet (in-house) or to the external gateway (card / SMS). */
@Service
public class PaymentService {

    private final WalletService wallet;
    private final PaymentGateway gateway;
    private final SavedCardRepository cards;

    public PaymentService(WalletService wallet, PaymentGateway gateway, SavedCardRepository cards) {
        this.wallet = wallet;
        this.gateway = gateway;
        this.cards = cards;
    }

    @Transactional
    public void charge(long userId, PaymentMethod method, BigDecimal amount, String reference) {
        switch (method) {
            case WALLET -> wallet.debit(userId, amount);
            case CARD -> {
                requireCard(userId);
                gateway.charge(method, amount, reference);
            }
            case SMS -> gateway.charge(method, amount, reference);
        }
    }

    @Transactional
    public void refund(long userId, PaymentMethod method, BigDecimal amount, String reference) {
        if (method == PaymentMethod.WALLET) {
            wallet.credit(userId, amount);
        } else {
            gateway.refund(method, amount, reference);
        }
    }

    /** Tops up the wallet from the user's saved card. */
    @Transactional
    public WalletDto topUpWallet(long userId, BigDecimal amount) {
        requireCard(userId);
        gateway.charge(PaymentMethod.CARD, amount, "wallet-topup:" + userId);
        return wallet.credit(userId, amount);
    }

    /** Human readable label used in notifications, e.g. "Mastercard **** 8821". */
    public String describe(long userId, PaymentMethod method) {
        return switch (method) {
            case WALLET -> "ParkEye Паричник";
            case SMS -> "СМС";
            case CARD -> primaryCard(userId)
                    .map(c -> c.getBrand() + " **** " + c.getLast4())
                    .orElse("картичка");
        };
    }

    private void requireCard(long userId) {
        if (cards.findByUserIdOrderByIdAsc(userId).isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "NO_CARD", "No saved card on this account.");
        }
    }

    private java.util.Optional<SavedCardEntity> primaryCard(long userId) {
        List<SavedCardEntity> all = cards.findByUserIdOrderByIdAsc(userId);
        return all.stream().filter(c -> Boolean.TRUE.equals(c.getPrimaryCard())).findFirst()
                .or(() -> all.stream().findFirst());
    }
}
