package com.mo.store_locator.service;

import com.mo.store_locator.dto.admin.AdminStoreAddressResponse;
import com.mo.store_locator.dto.admin.AdminStoreCreateRequest;
import com.mo.store_locator.dto.admin.AdminStoreHoursPatchRequest;
import com.mo.store_locator.dto.admin.AdminStoreHoursResponse;
import com.mo.store_locator.dto.admin.AdminStorePatchRequest;
import com.mo.store_locator.dto.admin.AdminStoreResponse;
import com.mo.store_locator.model.Store;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminStoreMapper {
    public Store toEntity(AdminStoreCreateRequest request, GeoCoordinates coordinates) {
        Store store = new Store();
        store.setStoreId(request.getStoreId().trim());
        store.setName(request.getName().trim());
        store.setStoreType(request.getStoreType().trim());
        store.setStatus(normalizeStatus(request.getStatus()));
        store.setLatitude(coordinates.latitude());
        store.setLongitude(coordinates.longitude());
        store.setAddressStreet(request.getAddress().getStreet().trim());
        store.setAddressCity(request.getAddress().getCity().trim());
        store.setAddressState(request.getAddress().getState().trim());
        store.setAddressPostalCode(request.getAddress().getPostalCode().trim());
        store.setAddressCountry(request.getAddress().getCountry().trim());
        store.setPhone(request.getPhone().trim());
        store.setServices(request.getServices() == null ? null : request.getServices().trim());
        store.setHoursMon(request.getHours().getMon().trim());
        store.setHoursTue(request.getHours().getTue().trim());
        store.setHoursWed(request.getHours().getWed().trim());
        store.setHoursThu(request.getHours().getThu().trim());
        store.setHoursFri(request.getHours().getFri().trim());
        store.setHoursSat(request.getHours().getSat().trim());
        store.setHoursSun(request.getHours().getSun().trim());
        return store;
    }

    public void applyPatch(Store store, AdminStorePatchRequest request) {
        if (request.getName() != null) {
            store.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            store.setPhone(request.getPhone().trim());
        }
        if (request.getServices() != null) {
            store.setServices(request.getServices().trim());
        }
        if (request.getStatus() != null) {
            store.setStatus(normalizeStatus(request.getStatus()));
        }
        if (request.getHours() != null) {
            applyHoursPatch(store, request.getHours());
        }
    }

    public AdminStoreResponse toResponse(Store store) {
        return new AdminStoreResponse(
                store.getStoreId(),
                store.getName(),
                store.getStoreType(),
                store.getStatus(),
                store.getLatitude(),
                store.getLongitude(),
                store.getPhone(),
                store.getServices(),
                new AdminStoreAddressResponse(
                        store.getAddressStreet(),
                        store.getAddressCity(),
                        store.getAddressState(),
                        store.getAddressPostalCode(),
                        store.getAddressCountry()
                ),
                new AdminStoreHoursResponse(
                        store.getHoursMon(),
                        store.getHoursTue(),
                        store.getHoursWed(),
                        store.getHoursThu(),
                        store.getHoursFri(),
                        store.getHoursSat(),
                        store.getHoursSun()
                )
        );
    }

    private void applyHoursPatch(Store store, AdminStoreHoursPatchRequest hours) {
        if (hours.getMon() != null) {
            store.setHoursMon(hours.getMon().trim());
        }
        if (hours.getTue() != null) {
            store.setHoursTue(hours.getTue().trim());
        }
        if (hours.getWed() != null) {
            store.setHoursWed(hours.getWed().trim());
        }
        if (hours.getThu() != null) {
            store.setHoursThu(hours.getThu().trim());
        }
        if (hours.getFri() != null) {
            store.setHoursFri(hours.getFri().trim());
        }
        if (hours.getSat() != null) {
            store.setHoursSat(hours.getSat().trim());
        }
        if (hours.getSun() != null) {
            store.setHoursSun(hours.getSun().trim());
        }
    }

    private String normalizeStatus(String status) {
        return status.trim().toLowerCase(Locale.ROOT);
    }
}
