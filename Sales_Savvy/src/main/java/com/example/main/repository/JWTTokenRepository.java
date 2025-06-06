package com.example.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.main.entity.JWTTokens;

import jakarta.transaction.Transactional;

@Repository
public interface JWTTokenRepository extends JpaRepository<JWTTokens, Integer> {

	@Query("SELECT t FROM JWTTokens t where t.user.userId =:userId")
	JWTTokens findByuser_id(int userId); // Find token for specific userId

	Optional<JWTTokens> findBytoken(String token);

	@Modifying
	@Transactional
	@Query("DELETE FROM JWTTokens jt WHERE jt.user.userId = :userId")
	 void deleteByUserId(int userId);
}
