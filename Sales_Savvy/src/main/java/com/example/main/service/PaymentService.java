package com.example.main.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.main.entity.CartItem;
import com.example.main.entity.OrderItem;
import com.example.main.entity.OrderStatus;
import com.example.main.entity.Orders;
import com.example.main.repository.CartRepository;
import com.example.main.repository.OrderItemRepository;
import com.example.main.repository.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;

@Service
public class PaymentService {

	@Value("${razorpay.key_id}")
	private String razorpayKeyId;
	@Value("${razorpay.key_secret}")
	private String razorpaySecretKey;
	private OrderRepository orderRepository;
	private OrderItemRepository itemRepository;
	private CartRepository cartRepository;

	public PaymentService(OrderRepository orderRepository, OrderItemRepository itemRepository,
			CartRepository cartRepository) {
		super();
		this.orderRepository = orderRepository;
		this.itemRepository = itemRepository;
		this.cartRepository = cartRepository;
	}

	@Transactional
	public String createOrder(int userId, BigDecimal totalAmount, List<OrderItem> cartItems) throws RazorpayException {
		// Create RazorPay Client
		RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpaySecretKey);

		// RazorPay Order Request
		var orderRequest = new JSONObject();
		orderRequest.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue());
		orderRequest.put("currency", "INR");
		orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
		// Create Razorpay Order
		com.razorpay.Order razorpayOrder = client.orders.create(orderRequest);

		// Save order Details
		Orders orders = new Orders();
		orders.setOrderId(razorpayOrder.get("id"));
		orders.setUserId(userId);
		orders.setTotalAmount(totalAmount);
		orders.setStatus(OrderStatus.PENDING);
		orders.setCreatedAt(LocalDateTime.now());
		orderRepository.save(orders);

		return razorpayOrder.get("id"); // Return orderId
	}

	@Transactional
	public boolean verifyPayment(String razorpayOrderId, String razorpaymentId, String razorpaySignature, int userId) {
		try {
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", razorpayOrderId);
			attributes.put("razorpay_payment_id", razorpaymentId);
			attributes.put("razorpay_signature", razorpaySignature);

			// Verify Razorpay SecretKey
			boolean signatureValid = com.razorpay.Utils.verifyPaymentSignature(attributes, razorpaySecretKey);
			if (signatureValid) {
				// change Order Status
				Orders orders = orderRepository.findById(razorpayOrderId)
						.orElseThrow(() -> new RuntimeException("Order Not Found"));
				orders.setUpdatedAt(LocalDateTime.now());
				orders.setStatus(OrderStatus.SUCCESS);
				orderRepository.save(orders);
				// Fetch Cart Item From user
				List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);
				for (CartItem cartItem : cartItems) {
					OrderItem orderItem = new OrderItem();
					orderItem.setOrder(orders);
					orderItem.setProductId(cartItem.getProduct().getProductId());
					orderItem.setQuantity(cartItem.getQuantity());
					orderItem.setPricePerUnit(cartItem.getProduct().getPrice());
					orderItem.setTotalPrice(
							cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
					itemRepository.save(orderItem);
				}
				// Clear User's Cart
				cartRepository.deleteCart(userId);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
