package com.example.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.main.entity.Users;
import com.example.main.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api")
public class OrderController {

	OrderService orderService;

	public OrderController(OrderService orderService) {
		super();
		this.orderService = orderService;
	}

	@GetMapping("/orders")
	public ResponseEntity<Map<String, Object>> getOrders(HttpServletRequest request) {
		try {
			Users user = (Users) request.getAttribute("authenticatedUser");
			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Error", "User not Authenticated"));
			}
			Map<String, Object> response = orderService.getOrderItems(user);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e1) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Error", e1.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("Error", "An Unexpected Error Occured"));
		}
	}
}
