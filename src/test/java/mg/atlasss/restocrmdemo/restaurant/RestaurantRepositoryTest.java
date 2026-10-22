package mg.atlasss.restocrmdemo.restaurant;

import mg.atlasss.restocrmdemo.group.Group;
import mg.atlasss.restocrmdemo.group.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    void findAllByGroupId() {
        // --- ARRANGE ---
        Group group1 = Group.builder().name("group1").build(),
                group2 = Group.builder().name("group2").build();
        groupRepository.saveAll(List.of(group1,group2));

        Restaurant restaurant1 = Restaurant.builder()
                .name("Gourmet Burger")
                .group(group1)
                .build();
        Restaurant restaurant2 = Restaurant.builder()
                .name("Sushi Bar")
                .group(group2)
                .build();
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2));

        // --- ACT ---
        List<Restaurant> results = restaurantRepository.findAllByGroupId(group1.getId());

        // --- ASSERT ---
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Gourmet Burger");
    }
}