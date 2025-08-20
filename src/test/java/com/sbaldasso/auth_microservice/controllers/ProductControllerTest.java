package com.sbaldasso.auth_microservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbaldasso.auth_microservice.config.SecurityConfig;
import com.sbaldasso.auth_microservice.models.Product;
import com.sbaldasso.auth_microservice.services.AuthService;
import com.sbaldasso.auth_microservice.services.ProductService;
import com.sbaldasso.auth_microservice.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    public void getAllProducts_shouldReturnListOfProducts() throws Exception {
        Product product1 = new Product(1L, "Product 1", "Description 1", 10.0);
        Product product2 = new Product(2L, "Product 2", "Description 2", 20.0);

        when(productService.findAll()).thenReturn(Arrays.asList(product1, product2));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getProductById_shouldReturnProductWhenFound() throws Exception {
        Product product = new Product(1L, "Product 1", "Description 1", 10.0);

        when(productService.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getProductById_shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createProduct_shouldReturnCreatedProduct() throws Exception {
        Product productToCreate = new Product(null, "New Product", "New Description", 30.0);
        Product createdProduct = new Product(1L, "New Product", "New Description", 30.0);

        when(productService.save(any(Product.class))).thenReturn(createdProduct);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productToCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateProduct_shouldUpdateAndReturnProduct() throws Exception {
        Product existingProduct = new Product(1L, "Old Name", "Old Desc", 10.0);
        Product updatedDetails = new Product(null, "New Name", "New Desc", 20.0);
        Product updatedProduct = new Product(1L, "New Name", "New Desc", 20.0);

        when(productService.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productService.save(any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("New Desc"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateProduct_shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        Product updatedDetails = new Product(null, "New Name", "New Desc", 20.0);

        when(productService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteProduct_shouldDeleteProductAndReturnOk() throws Exception {
        Product existingProduct = new Product(1L, "Product to delete", "Desc", 50.0);
        when(productService.findById(1L)).thenReturn(Optional.of(existingProduct));
        doNothing().when(productService).deleteById(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteProduct_shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNotFound());
    }
}
