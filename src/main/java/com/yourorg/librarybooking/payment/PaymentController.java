package com.yourorg.librarybooking.payment;

import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Long> payload) {
        Long bookingId = payload.get("bookingId");
        if (bookingId == null) {
            return ResponseEntity.badRequest().body("bookingId is required");
        }

        try {
            Payment payment = paymentService.createPaymentOrder(bookingId);
            return ResponseEntity.ok(Map.of(
                    "paymentId", payment.getId(),
                    "razorpayOrderId", payment.getGatewayReference(),
                    "amount", payment.getPaidAmount()
            ));
        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().body("Error from Razorpay: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-razorpay-signature") String signature) {
        try {
            paymentService.processWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            // We return 200 even on error so Razorpay doesn't keep retrying infinitely 
            // if it's a signature mismatch, but standard practice is 400 for bad signatures.
            return ResponseEntity.badRequest().body("Webhook failed: " + e.getMessage());
        }
    }
}
