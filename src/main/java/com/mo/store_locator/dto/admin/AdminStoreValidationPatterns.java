package com.mo.store_locator.dto.admin;

public final class AdminStoreValidationPatterns {
    public static final String STORE_TYPE = "^(Flagship|Urban|Neighborhood)$";
    public static final String STATUS = "^(?i)(active|inactive)$";
    public static final String HOURS = "^(Closed|([01]\\d|2[0-3]):[0-5]\\d-([01]\\d|2[0-3]):[0-5]\\d)$";

    private AdminStoreValidationPatterns() {
    }
}
