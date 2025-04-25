package com.example.main.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.main.entity.OrderItem;
import com.example.main.entity.Users;
import com.example.main.repository.UserRepository;
import com.example.main.service.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173/" ,allowCredentials = "true")
@RequestMapping("api/payment")
public class PaymentController {

	private PaymentService paymentService;
	private UserRepository userRepository;

	public PaymentController(PaymentService paymentService, UserRepository userRepository) {
		super();
		this.paymentService = paymentService;
		this.userRepository = userRepository;
	}

	@PostMapping("/create")
	public ResponseEntity<String> createOrders(@RequestBody Map<String, Object> requestBody,
			HttpServletRequest request) {

		try {
			Users user = (Users) request.getAttribute("authenticatedUser");
			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
			}
			BigDecimal totalAmount = new BigDecimal(requestBody.get("totalAmount").toString());
			List<Map<String, Object>> cartItems = (List<Map<String, Object>>) requestBody.get("cartItems");
			// Convert cartItem into orderItems
			List<OrderItem> orderItem = cartItems.stream().map(item -> {
				OrderItem orderItems = new OrderItem();
				orderItems.setProductId((Integer) item.get("productId"));
				orderItems.setQuantity((Integer) item.get("quantity"));
				BigDecimal pricePerUnit = new BigDecimal(item.get("price").toString());
				orderItems.setPricePerUnit(pricePerUnit);
				orderItems.setTotalPrice(pricePerUnit.multiply(BigDecimal.valueOf((Integer) item.get("quantity"))));
				return orderItems;
			}).collect(Collectors.toList());
			// Call the Payment Service to Create Order
			String razorPayOrderId = paymentService.createOrder(user.getUserId(), totalAmount, orderItem);
			System.out.println(razorPayOrderId);
			return ResponseEntity.ok(razorPayOrderId);
		} catch (RazorpayException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error creating RazorPay Order: " + e.getMessage());
		} catch (Exception e1) {
			e1.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request Data " + e1.getMessage());
		}
	}

	@PostMapping("/verify")
	public ResponseEntity<String> verifypayment(@RequestBody Map<String, Object> requestBody,
			HttpServletRequest request) {
		try {
			System.out.println("payment verifying");
			Users user = (Users) request.getAttribute("authenticatedUser");
			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
			}
			int userId = user.getUserId();
			String razorpayPaymentId = (String) requestBody.get("razorpayPaymentId");
			String razorpayOrderId = (String) requestBody.get("razorpayOrderId");
			String razorpaySignature = (String) requestBody.get("razorpaySignature");

			// call the payment service to verify the payment
			boolean isValid = paymentService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature,
					userId);
			if (isValid) {
				return ResponseEntity.ok("Payment Verified Successfully");
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error Verifying payment: " + e.getMessage());
		}
	}
}
