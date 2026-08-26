package com.relations.user_profile.controller;

import com.relations.user_profile.dto.VendorDTO;
import com.relations.user_profile.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<VendorDTO.Response> createVendor(@Valid @RequestBody VendorDTO.Request request) {
        VendorDTO.Response response = vendorService.createVendor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorDTO.Response> getVendorById(@PathVariable Long id) {
        VendorDTO.Response response = vendorService.getVendorById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VendorDTO.Response>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }
}
