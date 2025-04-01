package com.example.main.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jwt_tokens")
public class JWTTokens {

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int token_id;

	@Column
	private String token;

	@Column
	private LocalDateTime created_at;

	@Column
	private LocalDateTime expires_at;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private Users user;

	public JWTTokens() {
		super();
		// TODO Auto-generated constructor stub
	}

	public JWTTokens(int token_id, String token, LocalDateTime created_at, LocalDateTime expires_at, Users user) {
		super();
		this.token_id = token_id;
		this.token = token;
		this.created_at = created_at;
		this.expires_at = expires_at;
		this.user = user;
	}

	public JWTTokens(String token, LocalDateTime created_at, LocalDateTime expires_at, Users user) {
		super();
		this.token = token;
		this.created_at = created_at;
		this.expires_at = expires_at;
		this.user = user;
	}

	public int getToken_id() {
		return token_id;
	}

	public void setToken_id(int token_id) {
		this.token_id = token_id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}

	public LocalDateTime getExpires_at() {
		return expires_at;
	}

	public void setExpires_at(LocalDateTime expires_at) {
		this.expires_at = expires_at;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

}
