package mg.atlasss.restocrmdemo.auth.dto;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds,
                            UserSummary user) {
}
