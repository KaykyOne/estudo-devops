package br.com.fatecads.fatecads.security;

/**
 * Papéis reconhecidos pela aplicação. Os valores mantêm o prefixo exigido
 * pelo Spring Security para uso futuro com hasRole/hasAuthority.
 */
public final class Roles {

    public static final String CLIENT = "ROLE_CLIENT";
    public static final String ADMIN = "ROLE_ADMIN";

    private Roles() {
    }
}
