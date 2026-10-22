package mg.atlasss.restocrmdemo.security;

import mg.atlasss.restocrmdemo.user.Role;

/**
 * Identity extracted from a validated JWT's claims. Carried as the
 * Authentication principal for the lifetime of a request.
 */
public record AuthenticatedPrincipal(Long userId, String email, Role role, Long groupId, Long restaurantId) {
}
