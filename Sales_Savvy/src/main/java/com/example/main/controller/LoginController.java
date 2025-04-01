package com.example.main.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.main.dto.LoginData;
import com.example.main.entity.Users;
import com.example.main.service.LoginService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("api/auth")
public class LoginController {

	private final LoginService loginService;

	public LoginController(LoginService loginService) {
		super();
		this.loginService = loginService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> UserLogin(@RequestBody LoginData loginData, HttpServletResponse response) {
		try {
			Users user = loginService.ValidateUser(loginData.getUsername(), loginData.getPassword());
			String token = loginService.generateToken(user);
			Cookie cookie = new Cookie("authToken", token);
           
			 cookie.setHttpOnly(true);  // Prevents JavaScript access
		        cookie.setSecure(false);   // Should be true in production (HTTPS)
		        cookie.setPath("/");       // Available for all routes
		        cookie.setMaxAge(3600);    // 1 hour expiration
		        cookie.setAttribute("SameSite", "Lax");
//			
//			cookie.setHttpOnly(true);
//            cookie.setSecure(false); // Set to true if using HTTPS
//            cookie.setPath("/");
//            cookie.setMaxAge(3600); // 1 hour
//        //    cookie.setDomain("localhost");
//            cookie.setAttribute("SameSite", "Lax"); 
            
            
            response.addCookie(cookie);
			
			
			 response.addHeader("Set-Cookie",
	                    String.format("authToken=%s; HttpOnly; Path=/; Max-Age=3600; SameSite=None", token));
//			response.addHeader("Set-Cookie",
//					String.format("authToken=%s HttpOnly; path=/; MaxAge=3600 ; SameSite=None", token));
			System.out.println(token);

			Map<String, String> map = new HashMap<>();
			map.put("message", "Login Successfull");
			map.put("token",token);
			map.put("username", user.getUsername());
			map.put("role", user.getRole().name());

			return ResponseEntity.ok(map);

		} catch (Exception e) {
//			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Error", e.getMessage()));
		}

	}
}
