package com.example.main.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.main.entity.Users;
import com.example.main.repository.UserRepository;

@Service
public class UserService {

	private BCryptPasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		super();
		this.passwordEncoder = new BCryptPasswordEncoder();
		this.userRepository = userRepository;
	}

	public Users registerUser(Users user) throws RuntimeException {

		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new RuntimeException("Username Already Taken");
		}
		if (userRepository.findByEmail(user.getEmail()).isPresent()) {
			throw new RuntimeException("Email Already Registered");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);

	}
}
