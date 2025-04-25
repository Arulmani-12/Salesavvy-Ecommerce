package com.example.main.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.example.main.entity.OrderItem;
import com.example.main.entity.Product;
import com.example.main.entity.ProductImage;
import com.example.main.entity.Users;
import com.example.main.repository.OrderItemRepository;
import com.example.main.repository.ProductImageRepository;
import com.example.main.repository.ProductRepository;

@Service
public class OrderService {

	private OrderItemRepository orderItemRepository;
	private ProductRepository productRepository;
	private ProductImageRepository imageRepository;

	public OrderService(OrderItemRepository orderItemRepository, ProductRepository productRepository,
			ProductImageRepository imageRepository) {
		super();
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
	}

	public Map<String, Object> getOrderItems(Users user) {

		List<OrderItem> orderItems = orderItemRepository.findSuccessfullOrdersBYOrderId(user.getUserId());
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> productsList = new ArrayList<>();

		for (OrderItem item : orderItems) {
			Map<String, Object> productDetails = new HashMap<>();
			Product product = productRepository.findById(item.getProductId()).orElse(null);
			
			if (product == null) {
				continue;
			}
			List<ProductImage> images = imageRepository.findByProduct_ProductId(product.getProductId());
			String imageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();
			
			productDetails.put("order_id", item.getOrder().getOrderId());
			productDetails.put("quantity", item.getQuantity());
			productDetails.put("total_price", item.getTotalPrice());
			productDetails.put("image_url", imageUrl);
			productDetails.put("product_id", product.getProductId());
			productDetails.put("name", product.getName());
			productDetails.put("description", product.getDescription());
			productDetails.put("price_per_unit", item.getPricePerUnit());

			productsList.add(productDetails);
		}
		response.put("username", user.getUsername());
		response.put("role", user.getRole().name());
		response.put("products", productsList);
		return response;
	}
}
