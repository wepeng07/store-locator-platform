package com.mo.store_locator.service;

import com.mo.store_locator.dto.admin.AdminStoreCreateRequest;
import com.mo.store_locator.dto.admin.AdminStoreResponse;
import com.mo.store_locator.dto.admin.AdminStorePatchRequest;
import com.mo.store_locator.model.Store;
import com.mo.store_locator.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminStoreService {
    private final StoreRepository storeRepository;
    private final GeocodingService geocodingService;
    private final AdminStoreMapper adminStoreMapper;

    public AdminStoreService(
            StoreRepository storeRepository,
            GeocodingService geocodingService,
            AdminStoreMapper adminStoreMapper
    ) {
        this.storeRepository = storeRepository;
        this.geocodingService = geocodingService;
        this.adminStoreMapper = adminStoreMapper;
    }

    public AdminStoreResponse createStore(AdminStoreCreateRequest request) {
        String storeId = request.getStoreId().trim();
        if (storeRepository.existsByStoreId(storeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Store with storeId " + storeId + " already exists");
        }

        GeoCoordinates coordinates = resolveCoordinates(request);
        Store store = adminStoreMapper.toEntity(request, coordinates);
        return adminStoreMapper.toResponse(storeRepository.save(store));
    }

    public Page<AdminStoreResponse> listStores(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("storeId").ascending());
        return storeRepository.findAllByOrderByStoreIdAsc(pageRequest).map(adminStoreMapper::toResponse);
    }

    public AdminStoreResponse getStoreByStoreId(String storeId) {
        return adminStoreMapper.toResponse(findStoreByStoreId(storeId));
    }

    public AdminStoreResponse patchStore(String storeId, AdminStorePatchRequest request) {
        Store store = findStoreByStoreId(storeId);
        adminStoreMapper.applyPatch(store, request);
        return adminStoreMapper.toResponse(storeRepository.save(store));
    }

    public AdminStoreResponse deactivateStore(String storeId) {
        Store store = findStoreByStoreId(storeId);
        store.setStatus("inactive");
        return adminStoreMapper.toResponse(storeRepository.save(store));
    }

    private Store findStoreByStoreId(String storeId) {
        return storeRepository.findByStoreId(storeId.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store not found with storeId " + storeId
                ));
    }

    private GeoCoordinates resolveCoordinates(AdminStoreCreateRequest request) {
        if (request.getLatitude() != null && request.getLongitude() != null) {
            return new GeoCoordinates(request.getLatitude(), request.getLongitude());
        }

        return geocodingService.geocode(request.getAddress())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unable to geocode store address"
                ));
    }
}
