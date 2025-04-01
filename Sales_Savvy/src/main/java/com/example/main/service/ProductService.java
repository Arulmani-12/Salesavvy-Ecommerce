package com.example.main.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.main.entity.Category;
import com.example.main.entity.Product;
import com.example.main.entity.ProductImage;
import com.example.main.repository.CategoryRepository;
import com.example.main.repository.ProductImageRepository;
import com.example.main.repository.ProductRepository;

@Service
public class ProductService {

	private ProductRepository productRepository;
	private ProductImageRepository imageRepository;
	private CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, ProductImageRepository imageRepository,
			CategoryRepository categoryRepository) {
		super();
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.categoryRepository = categoryRepository;
	}

	public List<Product> getProducts(String categoryName) {

		if (categoryName != null && !categoryName.isEmpty()) { // get Products Based on Category
			Optional<Category> optionalCategory = categoryRepository.findByCategoryName(categoryName);
			if (optionalCategory.isPresent()) {
				Category category = optionalCategory.get();
				return productRepository.findByCategory_CategoryId(category.getCategoryId());
			} else {
				throw new RuntimeException("CategoryNotFound");
			}
		} else {
			return productRepository.findAll();
		}
	}

	public List<String> getProductImages(Integer productId) { // Fetch ProductImages for the productId

		List<ProductImage> productImages = imageRepository.findByProduct_ProductId(productId);
		List<String> ImageUrl = new ArrayList<>();

		for (ProductImage image : productImages) {
			ImageUrl.add(image.getImageUrl());
		}
		return ImageUrl;
	}
}
