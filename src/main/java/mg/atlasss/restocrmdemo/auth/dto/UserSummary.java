package mg.atlasss.restocrmdemo.auth.dto;

import mg.atlasss.restocrmdemo.user.Role;
import mg.atlasss.restocrmdemo.user.User;

public record UserSummary(Long id, String email, String fullName, Role role, Long groupId, Long restaurantId) {

    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getGroup().getId(),
                user.getRestaurant() != null ? user.getRestaurant().getId() : null);
    }
}
