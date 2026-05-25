package com.mo.store_locator.service;

import com.mo.store_locator.model.Store;
import com.mo.store_locator.repository.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<Store> getAllStores() {
        return storeRepository.findAllByOrderByIdAsc();
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store not found with id " + id
                ));
    }

    public List<Store> searchStoresByCity(String city) {
        List<Store> stores = storeRepository.findByAddressCityIgnoreCaseOrderByIdAsc(city.trim());

        if (stores.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No stores found in city " + city
            );
        }

        return stores;
    }
}
