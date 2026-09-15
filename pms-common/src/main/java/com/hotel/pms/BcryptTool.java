package com.hotel.pms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BcryptTool implements CommandLineRunner {
    
    public static void main(String[] args) {
        SpringApplication.run(BcryptTool.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("===========================================");
        System.out.println("BCrypt Password Generator");
        System.out.println("===========================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("Verification: " + encoder.matches(password, hash));
        System.out.println("===========================================");
        
        System.out.println("\nSQL to update password:");
        System.out.println("UPDATE sys_account SET password = '" + hash + "' WHERE username = 'admin';");
    }
}