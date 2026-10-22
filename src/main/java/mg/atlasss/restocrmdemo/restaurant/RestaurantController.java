package mg.atlasss.restocrmdemo.restaurant;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    @PreAuthorize("hasRole('GROUP_ADMIN')")
    public List<RestaurantResponseDto> findAllByGroupId(@RequestParam("groupId") Long groupId) {
        return restaurantService.findAllByGroupId(groupId);
    }
}
