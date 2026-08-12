package org.hospitalDonation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = "org.hospitalDonation.v1.entity")
public class DonationApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DonationApiApplication.class, args);
        System.out.println("ระบบ Donation API เริ่มทำงานแล้ว");
    }

}