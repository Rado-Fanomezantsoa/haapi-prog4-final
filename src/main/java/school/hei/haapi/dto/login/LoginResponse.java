package school.hei.haapi.dto.login;

import java.util.UUID;

public record LoginResponse(String token, String role, UUID userId) {}
