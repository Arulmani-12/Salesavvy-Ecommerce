package com.example.main.adminservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.main.entity.Category;
import com.example.main.entity.Product;
import com.example.main.entity.ProductImage;
import com.example.main.repository.CategoryRepository;
import com.example.main.repository.ProductImageRepository;
import com.example.main.repository.ProductRepository;

@Service
public class AdminProductService {

	private ProductRepository productRepository;
	private ProductImageRepository imageRepository;
	private CategoryRepository categoryRepository;

	public AdminProductService(ProductRepository productRepository, ProductImageRepository imageRepository,
			CategoryRepository categoryRepository) {
		super();
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.categoryRepository = categoryRepository;
	}

	public Product addProducts(String name, String description, double price, Integer stock, Integer categoryId,
			String imageUrl) {

		Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

		if (optionalCategory.isEmpty()) {
			throw new IllegalArgumentException("Invalid Category Id");
		}

		Product product = new Product(name, description, BigDecimal.valueOf(price), stock, optionalCategory.get(),
				LocalDateTime.now(), LocalDateTime.now());
		Product savedProduct = productRepository.save(product);

		if (!imageUrl.isEmpty() && imageUrl != null) {
			ProductImage image = new ProductImage(savedProduct, imageUrl);
			imageRepository.save(image);
		} else {
			throw new IllegalArgumentException("Product Image Url cannot be Empty");
		}

		return savedProduct;
	}

	public void deleteProduct(Integer productId) {
		Optional<Product> product = productRepository.findById(productId);
		if (product.isPresent()) {
			Product products = product.get();
			imageRepository.deleteByProductId(products.getProductId());
			productRepository.deleteById(products.getProductId());
		} else {
			throw new IllegalArgumentException("Invalid Product Id");
		}
	}
}
