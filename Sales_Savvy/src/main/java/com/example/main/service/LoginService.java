package com.example.main.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.main.entity.JWTTokens;
import com.example.main.entity.Users;
import com.example.main.repository.JWTTokenRepository;
import com.example.main.repository.UserRepository;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class LoginService {

	private static SecretKey SIGN_KEY; // SIGN_KEY to Generate JWT Token
	private JWTTokenRepository tokenRepository;
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;

	public LoginService(JWTTokenRepository tokenRepository, UserRepository userRepository,
			@Value("${jwt.secret}") String jwtsecret) {
		super();
		this.SIGN_KEY = Keys.hmacShaKeyFor(jwtsecret.getBytes(StandardCharsets.UTF_8));
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	// User&password Validation
	public Users ValidateUser(String username, String password) {

		Optional<Users> existingUser = userRepository.findByUsername(username); // Get Username from Database
		if (existingUser.isPresent()) {
			Users user = existingUser.get();
			if (!passwordEncoder.matches(password, existingUser.get().getPassword())) {
				throw new RuntimeException("Invalid Password"); // Throw Error if Password Wrong
			}
			return user;
		} else {
			throw new RuntimeException("Invalid Username");
		}
	}

	public String generateToken(Users user) {
		String token;
		LocalDateTime currentTime = LocalDateTime.now();
		JWTTokens existingTokens = tokenRepository.findByuser_id(user.getUserId());
		if (existingTokens != null && currentTime.isBefore(existingTokens.getExpires_at())) {
			token = existingTokens.getToken();
		} else {
			token = generateNewToken(user);
			if (existingTokens != null) {
				tokenRepository.delete(existingTokens);
			}
			saveToken(user, token);
		}
		return token;
	}

	public String generateNewToken(Users user) {

		JwtBuilder builder = Jwts.builder(); // Generate JWT Token using SIGN_KEY
		builder.setSubject(user.getUsername());
		builder.claim("role", user.getRole().name());
		builder.setIssuedAt(new Date());
		builder.setExpiration(new Date(System.currentTimeMillis() + 3600000)); // set Expiration
		builder.signWith(SIGN_KEY);
		String token = builder.compact(); // Build token
		return token;
	}

	public boolean validateToken(String token) {
		System.out.println("Validating token");
		try {
			Jwts.parserBuilder().setSigningKey(SIGN_KEY).build().parseClaimsJws(token); // Token Parsing happens
			Optional<JWTTokens> existingTokens = tokenRepository.findBytoken(token);
			if (existingTokens.isPresent()) {
				return existingTokens.get().getExpires_at().isAfter(LocalDateTime.now()); // Check token is Expired or
																							// not
			}
			return false;
		} catch (Exception e) {
			System.out.println("Token validation Fail.." + e.getMessage());
			return false;
		}
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder() // Extract UserName from JWT Token
				.setSigningKey(SIGN_KEY).build().parseClaimsJws(token).getBody().getSubject();
	}

	public void saveToken(Users user, String tokens) {
		JWTTokens jwt = new JWTTokens(tokens, LocalDateTime.now(), LocalDateTime.now().plusHours(1), user);
		tokenRepository.save(jwt);
	}
}
