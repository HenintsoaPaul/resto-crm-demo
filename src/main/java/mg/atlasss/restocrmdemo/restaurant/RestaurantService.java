package mg.atlasss.restocrmdemo.restaurant;

import jakarta.persistence.EntityNotFoundException;
import mg.atlasss.restocrmdemo.group.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final GroupRepository groupRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, GroupRepository groupRepository) {
        this.restaurantRepository = restaurantRepository;
        this.groupRepository = groupRepository;
    }

    public List<RestaurantResponseDto> findAllByGroupId(Long groupId) {
        if (!groupRepository.existsById(groupId)) throw new EntityNotFoundException("Group not found: " + groupId);
        return restaurantRepository.findAllByGroupId(groupId).stream().map(this::toDto).toList();
    }

    private RestaurantResponseDto toDto(Restaurant restaurant) {
        return new RestaurantResponseDto(restaurant.getName(), restaurant.getAddress(), restaurant.getPhone(), restaurant.getIsActive());
    }
}
