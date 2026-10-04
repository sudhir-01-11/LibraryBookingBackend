package com.yourorg.librarybooking.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByGatewayReference(String gatewayReference);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findFirstByBookingIdAndStatusOrderByIdDesc(Long bookingId, Payment.PaymentStatus status);
}
