package com.example.main.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.*;
import org.springframework.stereotype.Component;
import com.example.main.entity.Role;
import com.example.main.entity.Users;
import com.example.main.repository.UserRepository;
import com.example.main.service.LoginService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@WebFilter(urlPatterns = {"/api/*","/admin/*"})
//@Component
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
	private static final String authenticateUrl[] = { "/api/auth/login", "/api/users/register" };

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";
	LoginService loginService;
	UserRepository userRepository;

	public AuthenticationFilter(LoginService loginService, UserRepository userRepository) {
		super();
		this.loginService = loginService;
		this.userRepository = userRepository;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try {

			executeFilterLogin(request, response, chain);

		} catch (Exception e) {
			logger.error("Unexpected error in AuthenticationFilter", e);
			HttpServletResponse response2 = (HttpServletResponse) response;
			sendErrorResponse(response2, response2.SC_INTERNAL_SERVER_ERROR, "Internal_server_Error");
			
		}

	}

	public void executeFilterLogin(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String requestURI = httpRequest.getRequestURI();
		 logger.info("Request URI: {}", requestURI);

		if (Arrays.asList(authenticateUrl).contains(requestURI)) {
			chain.doFilter(request, response);
			return;
		}

		String token = getToken(httpRequest);
		System.out.println(token);
		if (token == null || !loginService.validateToken(token)) {
			sendErrorResponse(httpResponse, httpResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid Token");
			return;
		}

		String username = loginService.extractUsername(token);
		Optional<Users> existingUser = userRepository.findByUsername(username);
		if (existingUser.isEmpty()) {
			sendErrorResponse(httpResponse, httpResponse.SC_UNAUTHORIZED, "Unauthorized: User Not found");
			return;
		}

		Users authenticatedUser = existingUser.get();
		Role role = authenticatedUser.getRole();
		logger.info("Authenticated User: {}, Role: {}", authenticatedUser.getUsername(), role);
		if (requestURI.startsWith("/api/") && (role != Role.CUSTOMER && role != Role.ADMIN)) {
			sendErrorResponse(httpResponse, httpResponse.SC_FORBIDDEN, "Unauthorized: Not Allowed");
			return;
		}
		
		
		 if (requestURI.startsWith("/api/") && role != Role.CUSTOMER) {
	            sendErrorResponse(httpResponse, httpResponse.SC_FORBIDDEN, "Forbidden: Customer access required");
	            return;
	        }

		if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
			sendErrorResponse(httpResponse, httpResponse.SC_FORBIDDEN, "Unauthorized: Admin Access Required");
			return;
		}
		
		httpRequest.setAttribute("authenticatedUser", authenticatedUser);
		chain.doFilter(request, response);
	}

	public void sendErrorResponse(HttpServletResponse httpResponse, int statusCode, String message) throws IOException {

		httpResponse.setStatus(statusCode);
		httpResponse.getWriter().write(message);

	}

	public String getToken(HttpServletRequest request) {
		Cookie cookies[] = request.getCookies();
		if (cookies != null) {
			Cookie cookie = cookies[0];
			String token = cookie.getValue();
			return token;
		}
		return null;

	}

	  private void setCORSHeaders(HttpServletResponse response) {
	        response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
	        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
	        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
	        response.setHeader("Access-Control-Allow-Credentials", "true");
	        response.setStatus(HttpServletResponse.SC_OK);
	    }

}
