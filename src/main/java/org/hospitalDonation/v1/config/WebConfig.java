package org.hospitalDonation.v1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        System.out.println(">>> Static resource path: " + uploadPath);

        java.io.File dir = new java.io.File(uploadPath);
        System.out.println(">>> Folder exists: " + dir.exists());
        System.out.println(">>> Files in folder: " + java.util.Arrays.toString(dir.listFiles()));

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);
    }
}