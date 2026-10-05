package auth.service;

import auth.dto.LoginRequestDTO;
import auth.dto.RegisterRequestDTO;
import auth.model.User;
import auth.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  private final Set<String> revokedTokens =
      ConcurrentHashMap.newKeySet();

  public AuthService(
      UserService userService,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil) {

    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  public void register(RegisterRequestDTO request) {

    if (userService.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("User already exists");
    }

    User user = new User();

    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(
        request.getRole() == null || request.getRole().isBlank()
            ? "USER"
            : request.getRole()
    );

    userService.save(user);
  }

  public String authenticate(LoginRequestDTO loginRequestDTO) {

    return userService.findByEmail(loginRequestDTO.getEmail())
        .filter(user ->
            passwordEncoder.matches(
                loginRequestDTO.getPassword(),
                user.getPassword()
            )
        )
        .map(user ->
            jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
            )
        )
        .orElse(null);
  }

  public boolean validateToken(String token) {

    if (revokedTokens.contains(token)) {
      return false;
    }

    try {
      jwtUtil.validateToken(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  public void logout(String token) {
    revokedTokens.add(token);
  }
}