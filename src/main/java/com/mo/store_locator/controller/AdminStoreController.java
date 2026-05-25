package com.mo.store_locator.controller;

import com.mo.store_locator.dto.admin.AdminStoreCreateRequest;
import com.mo.store_locator.dto.admin.AdminStorePatchRequest;
import com.mo.store_locator.dto.admin.AdminStoreResponse;
import com.mo.store_locator.service.AdminStoreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/stores")
public class AdminStoreController {
    private final AdminStoreService adminStoreService;

    public AdminStoreController(AdminStoreService adminStoreService) {
        this.adminStoreService = adminStoreService;
    }

    @PostMapping
    public ResponseEntity<AdminStoreResponse> createStore(@Valid @RequestBody AdminStoreCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminStoreService.createStore(request));
    }

    @GetMapping
    public Page<AdminStoreResponse> listStores(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return adminStoreService.listStores(page, size);
    }

    @GetMapping("/{store_id}")
    public AdminStoreResponse getStore(
            @PathVariable("store_id") @Pattern(regexp = "^S\\d+$", message = "store_id must match S followed by digits") String storeId
    ) {
        return adminStoreService.getStoreByStoreId(storeId);
    }

    @PatchMapping("/{store_id}")
    public AdminStoreResponse patchStore(
            @PathVariable("store_id") @Pattern(regexp = "^S\\d+$", message = "store_id must match S followed by digits") String storeId,
            @Valid @RequestBody AdminStorePatchRequest request
    ) {
        return adminStoreService.patchStore(storeId, request);
    }

    @DeleteMapping("/{store_id}")
    public AdminStoreResponse deactivateStore(
            @PathVariable("store_id") @Pattern(regexp = "^S\\d+$", message = "store_id must match S followed by digits") String storeId
    ) {
        return adminStoreService.deactivateStore(storeId);
    }
}
