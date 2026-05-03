package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.AddressRequest;
import com.ecommerce.api_geek_store.api.dto.AddressResponse;
import com.ecommerce.api_geek_store.api.dto.AddressUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AddressService {
    Page<AddressResponse> getMyAddresses(String email, Pageable pageable);
    AddressResponse createAddress(String email, AddressRequest request);
    void deleteAddress(String email, Long addressId);
    AddressResponse updateAddress(Long addressId, String email, AddressUpdateRequest request);
}

