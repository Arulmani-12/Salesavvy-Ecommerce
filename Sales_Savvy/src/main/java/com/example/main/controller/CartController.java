package com.example.main.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.main.entity.Users;
import com.example.main.repository.UserRepository;
import com.example.main.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("api/cart")
public class CartController {

	private UserRepository userRepository;
	private CartService cartService;

	public CartController(CartService cartService, UserRepository userRepository) {
		super();
		this.cartService = cartService;
		this.userRepository = userRepository;
	}

	@GetMapping("items/count")
	public ResponseEntity<Integer> getCartCount(@RequestParam String username) {

		Users existingUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not Found with username " + username));
		int count = cartService.getCartItemCount(existingUser.getUserId());
		return ResponseEntity.ok(count);
	}

	@PostMapping("/add")
	public ResponseEntity<Void> addItemstoCart(@RequestBody Map<String, Object> request) {

		String username = (String) request.get("username");
		int productId = (int) request.get("productId");
		int quantity = request.containsKey("quantity") ? (int) request.get("quantity") : 1;
		Users user = userRepository.findByUsername(username).get();
		cartService.addItemsTocart(user.getUserId(), productId, quantity);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/items")
	public ResponseEntity<Map<String, Object>> getCartItems(HttpServletRequest request) {

		Users user = (Users) request.getAttribute("authenticatedUser");
		Map<String, Object> response = cartService.getProductsFromCart(user.getUserId());
		return ResponseEntity.ok(response);
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateCartQuantity(@RequestBody Map<String, Object> request) {
		try {
			String username = (String) request.get("username");
			int productId = (int) request.get("productId");
			int quantity = (int) request.get("quantity");
			Users user = userRepository.findByUsername(username)
					.orElseThrow(() -> new IllegalArgumentException("No UserFound with username: " + username));
			cartService.UpdateProductQuantity(user.getUserId(), productId, quantity);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@DeleteMapping("/delete")
	public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request) {
		String username = (String) request.get("username");
		int productId = (int) request.get("productId");
		Users user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("Username not Found"));
		cartService.deleteCartItem(user.getUserId(), productId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
