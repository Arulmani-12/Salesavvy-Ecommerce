package com.example.main.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.main.entity.Product;
import com.example.main.entity.Users;
import com.example.main.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api")
public class ProductController {

	private ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/products")
	public ResponseEntity<Map<String, Object>> getProducts(@RequestParam String category, HttpServletRequest request) {

		try {
			Users authUsers = (Users) request.getAttribute("authenticatedUser");

			if (authUsers == null) {
				return ResponseEntity.status(401).body(Map.of("Error", "Unauthorized User"));
			}

			List<Product> productsList = productService.getProducts(category);

			// Send as Response
			Map<String, Object> response = new HashMap<>();

			// User Info
			Map<String, String> userInfo = new HashMap<>();
			userInfo.put("name", authUsers.getUsername());
			userInfo.put("role", authUsers.getRole().name());
			response.put("user", userInfo);
			List<Map<String, Object>> plist = new ArrayList<>();

			for (Product product : productsList) {

				Map<String, Object> productDetails = new HashMap<>();

				productDetails.put("product_id", product.getProductId());
				productDetails.put("name", product.getName());
				productDetails.put("description", product.getDescription());
				productDetails.put("price", product.getPrice());
				productDetails.put("stock", product.getStock());

				List<String> images = productService.getProductImages(product.getProductId());
				productDetails.put("images", images);
				plist.add(productDetails);
			}
			response.put("products", plist);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
		}
	}
}
