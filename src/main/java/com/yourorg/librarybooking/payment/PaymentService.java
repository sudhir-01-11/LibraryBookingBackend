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
    private final com.yourorg.librarybooking.waitlist.WaitingListRepository waitingListRepository;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentService(RazorpayClient razorpayClient, PaymentRepository paymentRepository, BookingRepository bookingRepository, com.yourorg.librarybooking.waitlist.WaitingListRepository waitingListRepository) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.waitingListRepository = waitingListRepository;
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
    public Payment createWaitlistPaymentOrder(Long waitlistId) throws RazorpayException {
        com.yourorg.librarybooking.waitlist.WaitingList waitlist = waitingListRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

        if (waitlist.getStatus() != com.yourorg.librarybooking.waitlist.WaitingList.Status.PENDING_PAYMENT) {
            throw new IllegalStateException("Waitlist is not in PENDING_PAYMENT state");
        }

        long hours = java.time.Duration.between(waitlist.getRequestedTimeRange().lower(), waitlist.getRequestedTimeRange().upper()).toHours();
        if (hours == 0) hours = 1;
        BigDecimal amountInRupees = BigDecimal.valueOf(hours * 10);
        int amountInPaise = amountInRupees.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_waitlist_" + waitlistId);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String orderId = razorpayOrder.get("id");

        Payment payment = new Payment();
        payment.setBooking(null); // No booking yet
        payment.setPaidAmount(amountInRupees);
        payment.setGatewayReference(orderId);
        payment.setIdempotencyKey(UUID.randomUUID().toString());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);
        waitlist.setPaymentId(savedPayment.getId());
        waitingListRepository.save(waitlist);

        return savedPayment;
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

                    if (payment.getBooking() != null) {
                        Booking booking = payment.getBooking();
                        booking.setStatus(Booking.BookingStatus.CONFIRMED);
                        bookingRepository.save(booking);
                    } else {
                        // Check if it's a waitlist
                        waitingListRepository.findByPaymentId(payment.getId()).ifPresent(waitlist -> {
                            waitlist.setStatus(com.yourorg.librarybooking.waitlist.WaitingList.Status.ACTIVE);
                            waitingListRepository.save(waitlist);
                        });
                    }
                }
            }
        } catch (RazorpayException e) {
            throw new RuntimeException("Error verifying webhook", e);
        }
    }

    @Transactional
    public void processRefund(Long bookingId) throws RazorpayException {
        java.util.Optional<Payment> existingPaymentOpt = paymentRepository.findFirstByBookingIdAndStatusOrderByIdDesc(bookingId, Payment.PaymentStatus.SUCCESS);
        if (existingPaymentOpt.isEmpty()) {
            return; // No successful payment to refund
        }

        Payment payment = existingPaymentOpt.get();
        if (payment.getRefundReference() != null) {
            return; // Already refunded
        }

        // Fetch payments for the order to get the pay_XXX ID
        java.util.List<com.razorpay.Payment> rzpPayments = razorpayClient.orders.fetchPayments(payment.getGatewayReference());
        if (rzpPayments == null || rzpPayments.isEmpty()) {
            throw new IllegalStateException("No captured payments found for this order in Razorpay.");
        }

        String payId = rzpPayments.get(0).get("id");
        
        // Issue refund
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", payment.getPaidAmount().multiply(BigDecimal.valueOf(100)).intValue());
        
        com.razorpay.Refund refund = razorpayClient.payments.refund(payId, refundRequest);
        
        payment.setRefundReference(refund.get("id"));
        payment.setRefundAmount(payment.getPaidAmount());
        payment.setRefundedAt(ZonedDateTime.now());
        paymentRepository.save(payment);
    }

    @Transactional
    public void processWaitlistRefund(com.yourorg.librarybooking.waitlist.WaitingList waitlist) throws RazorpayException {
        if (waitlist.getPaymentId() == null) return;
        
        Payment payment = paymentRepository.findById(waitlist.getPaymentId()).orElse(null);
        if (payment == null || payment.getRefundReference() != null) return;
        
        java.util.List<com.razorpay.Payment> rzpPayments = razorpayClient.orders.fetchPayments(payment.getGatewayReference());
        if (rzpPayments == null || rzpPayments.isEmpty()) return;
        
        String payId = rzpPayments.get(0).get("id");
        
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", payment.getPaidAmount().multiply(BigDecimal.valueOf(100)).intValue());
        
        com.razorpay.Refund refund = razorpayClient.payments.refund(payId, refundRequest);
        
        payment.setRefundReference(refund.get("id"));
        payment.setRefundAmount(payment.getPaidAmount());
        payment.setRefundedAt(ZonedDateTime.now());
        paymentRepository.save(payment);
    }
}
