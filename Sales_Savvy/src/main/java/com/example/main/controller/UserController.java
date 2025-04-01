package com.example.main.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.main.entity.Users;
import com.example.main.service.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/users")
public class UserController {

	private UserService userService;

	public UserController(UserService userService) {
		super();
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> userRegister(@RequestBody Users user) {
		try {
			Users registuser = userService.registerUser(user);
			return ResponseEntity
					.ok(Map.of("message", "User Registered Successfully", "username", registuser.getUsername()));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
		}
	}
}
