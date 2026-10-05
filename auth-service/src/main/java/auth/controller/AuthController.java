package auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import auth.dto.LoginRequestDTO;
import auth.dto.LoginResponseDTO;
import auth.dto.RegisterRequestDTO;
import auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Register a new user")
  @PostMapping("/register")
  public ResponseEntity<Void> register(
      @RequestBody RegisterRequestDTO request) {

    try {
      authService.register(request);
      return ResponseEntity.status(HttpStatus.CREATED).build();

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @Operation(summary = "Generate token on user login")
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> login(
      @RequestBody LoginRequestDTO loginRequestDTO) {

    String token = authService.authenticate(loginRequestDTO);

    if (token == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity.ok(new LoginResponseDTO(token));
  }

  @Operation(summary = "Logout")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @RequestHeader("Authorization") String authHeader) {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String token = authHeader.substring(7);

    authService.logout(token);

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Validate Token")
  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(
      @RequestHeader("Authorization") String authHeader) {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String token = authHeader.substring(7);

    return authService.validateToken(token)
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
  
  @PostMapping("/admin/create")
  public ResponseEntity<Void> createAdmin(@RequestBody RegisterRequestDTO request) {
      // Force the role to ADMIN (ignore whatever the client sends)
      request.setRole("ADMIN");
      authService.register(request);
      return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}