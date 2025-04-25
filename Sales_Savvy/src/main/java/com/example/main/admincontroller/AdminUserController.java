package com.example.main.admincontroller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.main.adminservice.AdminUserService;
import com.example.main.entity.Users;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/admin/user")
public class AdminUserController {

	AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		super();
		this.adminUserService = adminUserService;
	}
	
	
	
	@PutMapping("/modify")
	public ResponseEntity<?> modifyUser(@RequestBody Map<String, Object> request) {
		try {
			
			 Integer userId = (Integer) request.get("userId");
			 String username = (String) request.get("username");
			 String email = (String) request.get("email");
			 String role = (String) request.get("role");
			
			Users savedUser = adminUserService.modifyUserDetails(userId, username, email, role);
			Map<String, Object> response = new HashMap<>();
			response.put("userId", savedUser.getUserId());
			response.put("username", savedUser.getUsername());
			response.put("email", savedUser.getEmail());
			response.put("role", savedUser.getRole().name());
			response.put("Created_at", savedUser.getCreated_at());
			response.put("updated_at", savedUser.getUpdated_at());
			
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
			
		}catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
		catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something Is Wrong");
		}
	}
	
	
	
	@GetMapping("/getByid")
	public ResponseEntity<?> getUserById(@RequestBody Map<String, Object> request) {
		
		try {
			  Integer userId = (Integer) request.get("userId");
			    Users user = adminUserService.getUsersById(userId);
			    return ResponseEntity.status(HttpStatus.OK).body(user);
			
		}catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
		catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something Is wrong");
			// TODO: handle exception
		}
	}
	
	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteUsersByid(@RequestBody Map<String, Object> request){
		
		try {
			 Integer userId = (Integer) request.get("userId");
			    adminUserService.deleteUsersById(userId);  
			    return ResponseEntity.status(HttpStatus.OK).body("User Deleted Successfully");
			    
		} catch (IllegalArgumentException e) {
			  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something Went Wrong");
		}
	}
	
}
