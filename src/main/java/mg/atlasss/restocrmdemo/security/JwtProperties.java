package mg.atlasss.restocrmdemo.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret cannot be blank")
    private String secret;

    @Min(15)
    private long accessTokenTtlMinutes = 15;

    @Min(7)
    private long refreshTokenTtlDays = 7;
}
