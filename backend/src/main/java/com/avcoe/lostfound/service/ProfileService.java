package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.ProfileUpdateRequest;
import com.avcoe.lostfound.entity.User;
import com.avcoe.lostfound.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public Map<String, Object> getProfile() {
        User user = getCurrentUser();
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("department", user.getDepartment());
        profile.put("rollNumber", user.getRollNumber());
        profile.put("role", user.getRole().name());
        profile.put("profileImageUrl", user.getProfileImageUrl());
        profile.put("createdAt", user.getCreatedAt());
        return profile;
    }

    public Map<String, Object> updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentUser();

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getRollNumber() != null) user.setRollNumber(request.getRollNumber());

        user = userRepository.save(user);

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("department", user.getDepartment());
        profile.put("rollNumber", user.getRollNumber());
        profile.put("role", user.getRole().name());
        profile.put("profileImageUrl", user.getProfileImageUrl());
        profile.put("createdAt", user.getCreatedAt());
        return profile;
    }
}
