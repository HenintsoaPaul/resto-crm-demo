package mg.atlasss.restocrmdemo.security;

import mg.atlasss.restocrmdemo.group.Group;
import mg.atlasss.restocrmdemo.restaurant.Restaurant;
import mg.atlasss.restocrmdemo.user.Role;
import mg.atlasss.restocrmdemo.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private JwtService jwtService;

    private Group group;
    private User owner;

    @BeforeEach
    void setup(){
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-only-secret-value-needs-32-bytes-minimum");
        properties.setAccessTokenTtlMinutes(15);
        properties.setRefreshTokenTtlDays(7);

        jwtService = new JwtService(properties);

        group = Group.builder().id(1L).name("Bistro Group").build();
        owner = User.builder().id(9L).email("owner@resto-demo.test").role(Role.GROUP_ADMIN).group(group).build();
    }

    @Test
    void accessTokenRoundTripsClaimsForRestaurantScopedUser() {
        Restaurant restaurant = Restaurant.builder().id(2L).group(group).name("Le Central").build();
        User staff = User.builder().id(3L).email("staff@resto-demo.test").role(Role.STAFF).group(group).restaurant(restaurant).build();

        String token = jwtService.generateAccessToken(staff);
        AuthenticatedPrincipal principal = jwtService.parseAccessToken(token);

        assertThat(principal.userId()).isEqualTo(3L);
        assertThat(principal.email()).isEqualTo("staff@resto-demo.test");
        assertThat(principal.role()).isEqualTo(Role.STAFF);
        assertThat(principal.groupId()).isEqualTo(1L);
        assertThat(principal.restaurantId()).isEqualTo(2L);
    }

    @Test
    void accessTokenHasNoRestaurantClaimForGroupAdmin() {
        AuthenticatedPrincipal principal = jwtService.parseAccessToken(jwtService.generateAccessToken(owner));

        assertThat(principal.restaurantId()).isNull();
        assertThat(principal.role()).isEqualTo(Role.GROUP_ADMIN);
    }

    @Test
    void refreshTokenIsRejectedByAccessTokenParser() {
        String refreshToken = jwtService.generateRefreshToken(owner);

        assertThatThrownBy(() -> jwtService.parseAccessToken(refreshToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void accessTokenIsRejectedByRefreshTokenParser() {
        String accessToken = jwtService.generateAccessToken(owner);

        assertThatThrownBy(() -> jwtService.parseRefreshToken(accessToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void twoTokensIssuedInTheSameSecondForTheSameUserAreStillDistinct() {
        String first = jwtService.generateRefreshToken(owner);
        String second = jwtService.generateRefreshToken(owner);

        assertThat(first).isNotEqualTo(second);
    }

}