package mg.atlasss.restocrmdemo.restaurant;

public record RestaurantResponseDto(
        String name,
        String address,
        String phone,
        boolean isActive
) {
}
