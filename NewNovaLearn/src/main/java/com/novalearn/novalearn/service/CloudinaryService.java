package com.novalearn.novalearn.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                             @Value("${cloudinary.api-key}") String apiKey,
                             @Value("${cloudinary.api-secret}") String apiSecret) {
        if (cloudName != null && !cloudName.isBlank() && 
            apiKey != null && !apiKey.isBlank() && 
            apiSecret != null && !apiSecret.isBlank()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret));
        } else {
            this.cloudinary = null;
        }
    }

    public String uploadFile(File file, String contentType) throws IOException {
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary credentials are not configured. Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET in your .env file.");
        }
        
        String resourceType = "auto";
        if (contentType != null && contentType.startsWith("video/")) {
            resourceType = "video";
        }
        
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("resource_type", resourceType));
        return uploadResult.get("secure_url").toString();
    }
}
