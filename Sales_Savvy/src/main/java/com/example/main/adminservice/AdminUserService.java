package com.example.main.adminservice;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.main.entity.Role;
import com.example.main.entity.Users;
import com.example.main.repository.JWTTokenRepository;
import com.example.main.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminUserService {

	private UserRepository userRepository;
	private JWTTokenRepository tokenRepository;

	public AdminUserService(UserRepository userRepository, JWTTokenRepository tokenRepository) {
		super();
		this.userRepository = userRepository;
		this.tokenRepository = tokenRepository;
	}

	@Transactional
	public Users modifyUserDetails(Integer userId, String username, String email, String role) {

		Optional<Users> optionalUser = userRepository.findById(userId);

		if (optionalUser.isEmpty()) {
			throw new IllegalArgumentException("User Not Found");
		}

		Users existingUser = optionalUser.get();

		if (!username.isEmpty() && username != null) {
			existingUser.setUsername(username);
		}

		if (email != null && !email.isEmpty()) {
			existingUser.setEmail(email);
		}

		if (role != null && !role.isEmpty()) {
			existingUser.setRole(Role.valueOf(role));
		}
		existingUser.setUpdated_at(LocalDateTime.now());
		tokenRepository.deleteByUserId(existingUser.getUserId());
		return userRepository.save(existingUser);
	}

	public Users getUsersById(Integer userId) {
		return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not Found"));

	}

	public void deleteUsersById(Integer userId) {
		Optional<Users> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			Users user = optionalUser.get();
			userRepository.deleteById(user.getUserId());
		} else {
			throw new IllegalArgumentException("Invalid user id");
		}
	}

}
