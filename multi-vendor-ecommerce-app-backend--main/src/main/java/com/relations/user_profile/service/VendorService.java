package com.relations.user_profile.service;

import com.relations.user_profile.dto.VendorDTO;
import com.relations.user_profile.entity.Vendor;
import com.relations.user_profile.exception.ResourceNotFoundException;
import com.relations.user_profile.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;

    @Transactional
    public VendorDTO.Response createVendor(VendorDTO.Request request) {
        Vendor vendor = Vendor.builder()
                .storeName(request.getStoreName())
                .sellerCode(request.getSellerCode())
                .contactEmail(request.getContactEmail())
                .phoneNumber(request.getPhoneNumber())
                .rating(5.0)
                .build();
        Vendor saved = vendorRepository.save(vendor);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public VendorDTO.Response getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return mapToResponse(vendor);
    }

    @Transactional(readOnly = true)
    public List<VendorDTO.Response> getAllVendors() {
        return vendorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VendorDTO.Response mapToResponse(Vendor vendor) {
        return VendorDTO.Response.builder()
                .id(vendor.getId())
                .storeName(vendor.getStoreName())
                .sellerCode(vendor.getSellerCode())
                .contactEmail(vendor.getContactEmail())
                .phoneNumber(vendor.getPhoneNumber())
                .rating(vendor.getRating())
                .totalProducts(vendor.getProducts() != null ? vendor.getProducts().size() : 0)
                .build();
    }
}
