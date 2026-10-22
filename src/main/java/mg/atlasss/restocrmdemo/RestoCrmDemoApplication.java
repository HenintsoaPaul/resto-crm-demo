package mg.atlasss.restocrmdemo;

import mg.atlasss.restocrmdemo.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class RestoCrmDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestoCrmDemoApplication.class, args);
    }

}
