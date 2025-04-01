package com.example.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.main.entity.CartItem;

import jakarta.transaction.Transactional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Integer> {

	@Query("select c from CartItem c where c.user.userId = :userId and c.product.productId = :productId")
	Optional<CartItem> findByUserandProduct(int userId, int productId); // verfiying cart is present for the Userid

	@Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c where c.user.userId = :userId")
	public int countTotalItems(int userId); // Sum of total Items in the Cart for the userID

	@Query("SELECT c FROM CartItem c JOIN FETCH c.product p LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId where c.user.userId=:userId")
	List<CartItem> findCartItemsWithProductDetails(int userId); // Getting All cartItems

	@Query("Update CartItem c set c.quantity =: quantity where c.id =: id")
	public void updateCartItemQuantity(int id, int quantity); // Update cartItem's Quantity

	@Modifying
	@Transactional
	@Query("delete from CartItem c where c.user.userId = :userId and c.product.productId = :productId")
	public void deleteCartProduct(int userId, int productId); // Delete CartItems's Quantity

//	@Modifying
//	@Transactional
//	@Query("delete from CartItem c where c.user.userId = :userId")
//	public void deleteCart(int userId); // Delete Cart

}
