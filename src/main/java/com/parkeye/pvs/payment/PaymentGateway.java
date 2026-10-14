package com.parkeye.pvs.payment;

import com.parkeye.pvs.model.PaymentMethod;

import java.math.BigDecimal;

/**
 * Outbound port to the real payment provider (card PSP / SMS-billing operator).
 * Wallet payments never reach it - they are handled in-house by WalletService.
 * Implementations throw ApiException(PAYMENT_DECLINED) when the charge is refused.
 */
public interface PaymentGateway {
    void charge(PaymentMethod method, BigDecimal amount, String reference);
    void refund(PaymentMethod method, BigDecimal amount, String reference);
}
