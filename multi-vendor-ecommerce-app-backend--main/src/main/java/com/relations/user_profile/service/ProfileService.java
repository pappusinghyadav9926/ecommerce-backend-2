package com.relations.user_profile.service;

import com.relations.user_profile.entity.CustomerProfile;
import com.relations.user_profile.entity.User;
import com.relations.user_profile.repository.CustomerProfileRepository;
import com.relations.user_profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private final CustomerProfileRepository profileRepo;
	private final UserRepository userRepo;

	@Transactional
	public CustomerProfile createProfile(Long id, CustomerProfile profile) {
		User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		profile.setUser(u);
		u.setCustomerProfile(profile);
		userRepo.save(u);
		return profileRepo.save(profile);
	}

	public List<CustomerProfile> getAll() {
		return profileRepo.findAll();
	}
}
