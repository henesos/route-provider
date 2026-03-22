package com.aviation.routeprovider.domain.model.valueobject;

public enum UserRole {
    
    ADMIN,
    AGENCY;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean canManageLocations() {
        return this == ADMIN;
    }

    public boolean canManageTransportations() {
        return this == ADMIN;
    }

    public boolean canSearchRoutes() {
        return true;
    }
}
