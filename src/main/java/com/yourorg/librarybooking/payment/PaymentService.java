package com.yourorg.librarybooking.payment;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.yourorg.librarybooking.booking.Booking;
import com.yourorg.librarybooking.booking.BookingRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentService(RazorpayClient razorpayClient, PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Payment createPaymentOrder(Long bookingId) throws RazorpayException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.HELD) {
            throw new IllegalStateException("Booking is not in HELD state");
        }

        // Idempotency Check: If they already clicked pay and generated a pending order, reuse it!
        java.util.Optional<Payment> existingPending = paymentRepository.findFirstByBookingIdAndStatusOrderByIdDesc(bookingId, Payment.PaymentStatus.PENDING);
        if (existingPending.isPresent()) {
            return existingPending.get();
        }

        BigDecimal amountInRupees = booking.getFareSnapshot();
        // Razorpay expects amount in paise (multiply by 100)
        int amountInPaise = amountInRupees.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_booking_" + bookingId);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String orderId = razorpayOrder.get("id");

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaidAmount(amountInRupees);
        payment.setGatewayReference(orderId);
        payment.setIdempotencyKey(UUID.randomUUID().toString());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    @Transactional
    public void processWebhook(String payload, String signature) {
        try {
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                throw new SecurityException("Invalid Razorpay Webhook Signature");
            }

            JSONObject jsonPayload = new JSONObject(payload);
            String event = jsonPayload.getString("event");

            if ("payment.captured".equals(event)) {
                JSONObject paymentPayload = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String orderId = paymentPayload.getString("order_id");

                Payment payment = paymentRepository.findByGatewayReference(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

                if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                    payment.setStatus(Payment.PaymentStatus.SUCCESS);
                    payment.setCompletedAt(ZonedDateTime.now());
                    paymentRepository.save(payment);

                    Booking booking = payment.getBooking();
                    booking.setStatus(Booking.BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                }
            }
        } catch (RazorpayException e) {
            throw new RuntimeException("Error verifying webhook", e);
        }
    }
}
