package mg.atlasss.restocrmdemo.restaurant;

import mg.atlasss.restocrmdemo.security.SecurityConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
@Import(SecurityConfig.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    @Test
    @WithMockUser(roles = {"GROUP_ADMIN"})
    void findAllByGroupIdShouldReturnOkWhenGroupIdIsProvided() throws Exception {
        // --- ARRANGE ---
        Long groupId = 1L;
        RestaurantResponseDto dto = new RestaurantResponseDto("Sushi Bar", "address123", "123", true);
        when(restaurantService.findAllByGroupId(groupId)).thenReturn(List.of(dto));

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/api/restaurants")
                        .param("groupId", "1")
                        .accept(MediaType.APPLICATION_JSON))

                // --- ASSERT ---
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Sushi Bar"))
                .andExpect(jsonPath("$[0].address").value("address123"))
                .andExpect(jsonPath("$[0].phone").value("123"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "RESTAURANT_ADMIN"})
    void findAllByGroupIdShouldReturnForbiddenForUnauthorizedRoles(String role) throws Exception {
        var user = SecurityMockMvcRequestPostProcessors.user("testUser").roles(role);
        mockMvc.perform(get("/restaurants")
                        .param("groupId", "1")
                        .with(user))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void findAllByGroupIdShouldReturnUnauthorizedForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isUnauthorized());
    }
}