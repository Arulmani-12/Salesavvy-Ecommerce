package com.example.main.filter;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@WebFilter(urlPatterns = { "/api/*", "/admin/*" })
@Component // Bean Management
public class AuthenticationFilters implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilters.class);
	private final LoginService loginService;
	private final UserRepository userRepository;

	private static final String ALLOWED_ORIGIN = "http://localhost:5173"; // set Origin
	private static final String[] UNAUTHENTICATED_PATHS = { "/api/users/register", "/api/auth/login" }; // Paths no need
																										// validation

	public AuthenticationFilters(LoginService loginService, UserRepository userRepository) {
		super();
		System.out.println("filter working");
		this.loginService = loginService;
		this.userRepository = userRepository;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			executeFilterLogic(request, response, chain);
		} catch (Exception e) {
			logger.error("Unexpected error in AuthenticationFilter", e);
			sendErrorResponse((HttpServletResponse) response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Internal server error");
		}
	}

	private void executeFilterLogic(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Add CORS headers to all responses
		setCORSHeaders(httpResponse, httpRequest);

		String requestURI = httpRequest.getRequestURI();
		logger.info("Request URI: {}", requestURI);

		// Allow unauthenticated paths
		if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
			chain.doFilter(request, response);
			return;
		}

		if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
			setCORSHeaders(httpResponse, httpRequest);
			chain.doFilter(request, response);
			return; // Return immediately after setting CORS headers
		}

		// Extract and validate the token
		String token = getAuthTokenFromCookies(httpRequest);
		System.out.println(token);
		if (token == null || !loginService.validateToken(token)) {

			sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
					"Unauthorized: Invalid or missing token");
			return;
		}

		// Extract username and verify user
		String username = loginService.extractUsername(token);
		Optional<Users> userOptional = userRepository.findByUsername(username);
		if (userOptional.isEmpty()) {
			sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User not found");
			return;
		}

		// Get authenticated user and role
		Users authenticatedUser = userOptional.get();
		Role role = authenticatedUser.getRole();
		logger.info("Authenticated User: {}, Role: {}", authenticatedUser.getUsername(), role);

		// Role-based access control
		if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
			sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Admin access required");
			return;
		}

		if (requestURI.startsWith("/api/") && role != Role.CUSTOMER) {
			sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Customer access required");
			return;
		}

		// Attach user details to request
		httpRequest.setAttribute("authenticatedUser", authenticatedUser);
		chain.doFilter(request, response);
	}

	private void setCORSHeaders(HttpServletResponse response, HttpServletRequest request) {
		response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
		}

	}

	private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
		response.setStatus(statusCode);
		response.getWriter().write(message);
	}

	private String getAuthTokenFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies(); // get cookies from client
		if (cookies != null) {
			return Arrays.stream(cookies).filter(cookie -> "authToken".equals(cookie.getName())).map(Cookie::getValue)
					.findFirst().orElse(null);
		}
		return null;
	}
}