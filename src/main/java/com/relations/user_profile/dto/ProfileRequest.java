package com.relations.user_profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {
	
	private String city;
	private String state;
	private String country;
	private String pincode;
	
}
