package com.example.demoapp.controller;

import com.example.demoapp.model.Product;
import com.example.demoapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void testCreateAndGetProduct() throws Exception {
        // Create product
        String productJson = "{\"name\":\"Laptop\",\"description\":\"Gaming Laptop\",\"price\":1500.00}";
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Laptop")));

        // Get all products
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Laptop")));
    }
    
    @Test
    void testGetNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/999"))
               .andExpect(status().isNotFound());
    }
    
    @Test
    void testDeleteProduct() throws Exception {
        // First create a product
        Product product = new Product("Mouse", "Wireless Mouse", new java.math.BigDecimal("25.99"));
        productRepository.save(product);
        
        // Delete it
        mockMvc.perform(delete("/api/products/" + product.getId()))
               .andExpect(status().isOk());
               
        // Verify it's gone
        mockMvc.perform(get("/api/products/" + product.getId()))
               .andExpect(status().isNotFound());
    }
}
