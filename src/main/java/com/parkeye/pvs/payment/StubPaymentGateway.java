package com.parkeye.pvs.payment;

import com.parkeye.pvs.model.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Development stand-in: approves everything. Replace with the real PSP integration. */
@Component
public class StubPaymentGateway implements PaymentGateway {
    private static final Logger log = LoggerFactory.getLogger(StubPaymentGateway.class);

    @Override
    public void charge(PaymentMethod method, BigDecimal amount, String reference) {
        log.info("[stub-payment] CHARGE {} {} MKD ref={}", method, amount, reference);
    }

    @Override
    public void refund(PaymentMethod method, BigDecimal amount, String reference) {
        log.info("[stub-payment] REFUND {} {} MKD ref={}", method, amount, reference);
    }
}
