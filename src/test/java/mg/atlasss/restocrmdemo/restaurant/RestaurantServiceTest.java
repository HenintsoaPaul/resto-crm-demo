package mg.atlasss.restocrmdemo.restaurant;

import jakarta.persistence.EntityNotFoundException;
import mg.atlasss.restocrmdemo.group.Group;
import mg.atlasss.restocrmdemo.group.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private GroupRepository groupRepository;

    @Test
    void shouldReturnRestaurantResponseDtosWhenGroupFound() {
        // --- ARRANGE ---
        Long groupId = 1L;
        Group group = Group.builder().id(groupId).name("Test Group").build();
        Restaurant restaurant = Restaurant.builder().id(10L)
                .name("Pizza Place")
                .address("address123")
                .phone("123")
                .group(group).build();

        // Mock the repository behavior
        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(restaurantRepository.findAllByGroupId(groupId)).thenReturn(List.of(restaurant));

        // --- ACT ---
        List<RestaurantResponseDto> response = restaurantService.findAllByGroupId(groupId);

        // --- ASSERT ---
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        var resto = response.getFirst();
        assertThat(resto.name()).isEqualTo("Pizza Place");
        assertThat(resto.address()).isEqualTo("address123");
        assertThat(resto.phone()).isEqualTo("123");

        // Verify interaction
        verify(groupRepository).existsById(groupId);
        verify(restaurantRepository).findAllByGroupId(groupId);
    }

    @Test
    void shouldThrowExceptionWhenGroupNotFound() {
        // --- ARRANGE ---
        Long groupId = 1L;

        // Mock the repository behavior
        when(groupRepository.existsById(groupId)).thenReturn(false);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> restaurantService.findAllByGroupId(groupId))
                .isInstanceOf(EntityNotFoundException.class);

        // Verify interaction
        verify(groupRepository).existsById(groupId);
        verify(restaurantRepository, never()).findAllByGroupId(groupId);
    }
}