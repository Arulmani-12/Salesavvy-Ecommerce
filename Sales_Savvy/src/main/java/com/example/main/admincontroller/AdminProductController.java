package com.example.main.admincontroller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.main.adminservice.AdminProductService;
import com.example.main.entity.Product;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/admin/products")
public class AdminProductController {
	private AdminProductService productService;

	public AdminProductController(AdminProductService productService) {
		super();
		this.productService = productService;
	}

	@PostMapping("/add")
	public ResponseEntity<?> addProducts(@RequestBody Map<String, Object> productsBody) {
		try {
			String name = (String) productsBody.get("name");
			String description = (String) productsBody.get("description");
			Double price = Double.valueOf(String.valueOf(productsBody.get("price")));
			Integer stock = (Integer) productsBody.get("stock");
			Integer categoryId = (Integer) productsBody.get("categoryId");
			String imageUrl = (String) productsBody.get("imageUrl");
			Product savedproduct = productService.addProducts(name, description, price, stock, categoryId, imageUrl);
			Map<String, Object> response = new HashMap<>();
			response.put("product", savedproduct);
			response.put("imageUrl", imageUrl);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went Wrong");
		}
	}

	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteProduct(@RequestBody Map<String, Object> requestBody) {
		try {
			Integer productId = (Integer) requestBody.get("productId");
			productService.deleteProduct(productId);
			return ResponseEntity.status(HttpStatus.OK).body("Product deleted Successfully");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something Went Wrong");
		}
	}
}
