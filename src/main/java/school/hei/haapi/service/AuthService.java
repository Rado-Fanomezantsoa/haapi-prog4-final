package school.hei.haapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.haapi.dto.login.LoginRequest;
import school.hei.haapi.dto.login.LoginResponse;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public LoginResponse login(LoginRequest request) {

    AppUser user =
        appUserRepository
            .findByEmail(request.email())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

    if (user.isDeleted()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
    }

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
    }

    String token = jwtService.generateToken(user);

    return new LoginResponse(token, user.getRole().name(), user.getId());
  }
}
