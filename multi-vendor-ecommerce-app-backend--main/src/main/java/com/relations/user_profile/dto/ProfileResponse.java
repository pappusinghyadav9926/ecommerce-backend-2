package com.relations.user_profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
	
	private Long id;
	private String city;
	private String state;
	private String country;
	private String pincode;
	private Long  user_id;
}
