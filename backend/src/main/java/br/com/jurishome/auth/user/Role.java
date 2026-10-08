package br.com.jurishome.auth.user;

public enum Role {
    CLIENTE("Cliente"),
    ADVOGADO("Advogado"),
    ADMIN("Administrador");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
