package com.example.main.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.main.entity.CartItem;
import com.example.main.entity.Product;
import com.example.main.entity.ProductImage;
import com.example.main.entity.Users;
import com.example.main.repository.CartRepository;
import com.example.main.repository.ProductImageRepository;
import com.example.main.repository.ProductRepository;
import com.example.main.repository.UserRepository;

@Service
public class CartService {

	private CartRepository cartRepository;
	private UserRepository userRepository;
	private ProductRepository productRepository;
	private ProductImageRepository imageRepository;

	public CartService(CartRepository cartRepository, UserRepository userRepository,
			ProductRepository productRepository, ProductImageRepository imageRepository) {
		super();
		this.cartRepository = cartRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
	}

	public void UpdateProductQuantity(int userId, int productId, int quantity) {
		// Update CartItems's Quantity
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User Not Found with id " + userId));
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Product Not Found"));

		Optional<CartItem> cartItems = cartRepository.findByUserandProduct(user.getUserId(), product.getProductId());

		if (cartItems.isPresent()) {
			CartItem cartitem = cartItems.get();
			if (quantity == 0) {
				deleteCartItem(userId, productId);
			} else {
				cartitem.setQuantity(quantity);
				cartRepository.save(cartitem);
			}
		} else {
			throw new RuntimeException("Cart Item not Found"); // throw Exception if cartItem is Not found
		}
	}

	public void deleteCartItem(int userId, int productId) {
		// Deleting Cart Items By userId
		Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not Found."));
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Product Not Found"));
		cartRepository.deleteCartProduct(user.getUserId(), product.getProductId()); // deleted
	}

	public Map<String, Object> getProductsFromCart(int userId) {
		// Get All CartItems for the specific UserId
		List<CartItem> existingCartItem = cartRepository.findCartItemsWithProductDetails(userId);

		Map<String, Object> response = new HashMap<>(); // HashMap to Store CartDetails
		Users user = userRepository.findById(userId).get();
		response.put("username", user.getUsername());
		response.put("role", user.getRole().name());

		List<Map<String, Object>> productList = new ArrayList<>(); // List to Store Products
		int overall_price = 0;

		for (CartItem item : existingCartItem) {
			Map<String, Object> productDetails = new HashMap<>(); // HashMap to store Product Details
			Product product = item.getProduct();
			List<ProductImage> pimage = imageRepository.findByProduct_ProductId(product.getProductId());
			String imageUrl = (pimage != null & !pimage.isEmpty()) ? pimage.get(0).getImageUrl() : "default-image-url";

			// Store ProductDetails into HashMap
			productDetails.put("product_id", product.getProductId());
			productDetails.put("image_url", imageUrl);
			productDetails.put("name", product.getName());
			productDetails.put("description", product.getDescription());
			productDetails.put("price_per_unit", product.getPrice());
			productDetails.put("quantity", item.getQuantity());
			productDetails.put("total_price", item.getQuantity() * product.getPrice().doubleValue());

			productList.add(productDetails);
			overall_price += item.getQuantity() * product.getPrice().doubleValue();
		}

		Map<String, Object> cart = new HashMap<>();
		cart.put("products", productList);
		cart.put("overall_total_price", overall_price);
		response.put("cart", cart); // CartItems Stored into HashMap
		return response;
	}

	// Add products to Cart
	public void addItemsTocart(int userId, int productId, int quantity) {

		Optional<CartItem> existingCart = cartRepository.findByUserandProduct(userId, productId);
		if (existingCart.isPresent()) {
			CartItem cart = existingCart.get();
			cart.setQuantity(cart.getQuantity() + 1);
			cartRepository.save(cart);
		} else {
			Users user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("User Not Found"));
			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new IllegalArgumentException("Product Not Found"));
			CartItem items = new CartItem(user, product, quantity);
			cartRepository.save(items);
		}
	}

	// Get CartCount
	public int getCartItemCount(int userId) {

		return cartRepository.countTotalItems(userId);

	}
}
