package com.mo.store_locator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.mo.store_locator.model.Store;
import com.mo.store_locator.service.StoreService;

@RestController
public class StoreController {
    private final StoreService storeService;
//    表示 Controller 里面需要一个 StoreService。

    public  StoreController(StoreService storeService){
//        表示通过构造函数接收 Spring Boot 自动注入的 StoreService。
        this.storeService = storeService;
//        表示把传进来的 Service 保存到当前 Controller 里面，后面方法可以调用它。
    }

    @GetMapping ("/stores")
    public List<Store> getStores(){
        return storeService.getAllStores();
    }

    @GetMapping("/stores/{id}")
    public Store getStoreById(@PathVariable Long id) {
        return storeService.getStoreById(id);
    }

    @GetMapping("/stores/search")
    public List<Store> searchStores(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusMiles,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) List<String> storeTypes,
            @RequestParam(required = false) List<String> services
    ) {
        if (address != null) {
            return storeService.searchStoresByAddress(address, radiusMiles, limit, storeTypes, services);
        }

        if (postalCode != null) {
            return storeService.searchStoresByPostalCode(postalCode, storeTypes, services);
        }

        if (city != null) {
            return storeService.searchStoresByCity(city);
        }

        return storeService.searchStoresByCoordinates(latitude, longitude, radiusMiles, limit, storeTypes, services);
    }
}
