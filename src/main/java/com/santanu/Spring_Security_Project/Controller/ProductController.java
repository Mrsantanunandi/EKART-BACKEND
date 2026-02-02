package com.santanu.Spring_Security_Project.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.santanu.Spring_Security_Project.Model.Product;
import com.santanu.Spring_Security_Project.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ---------------- ADD PRODUCT ----------------
    @PostMapping(
            value = "/addProduct",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestPart("data") String data,
            @RequestPart("images") List<MultipartFile> images,
            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Product product = mapper.readValue(data, Product.class);

            Product savedProduct =
                    productService.addProduct(product, images, authentication);

            response.put("success", true);
            response.put("message", "Product Added Successfully");
            response.put("data", savedProduct);

            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            response.put("success", false);
            response.put("message", "Only Admin Can Add a Product");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ---------------- GET ALL PRODUCTS ----------------
    @GetMapping("/allProducts")
    public ResponseEntity<Map<String, Object>> getAllProduct() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productService.getAllProduct();
            response.put("success", true);
            response.put("message", "Fetch Product successfully");
            response.put("data", products);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    //-------------------------------------------------------------------------------
    //Delete A Product
    @DeleteMapping("/removeProduct/{productId}")
    public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable Long productId,Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.removeProduct(productId,authentication);
            response.put("success", true);
            response.put("message", "Delete Product successfully");
            return ResponseEntity.ok(response);
        }
        catch(AccessDeniedException e)
        {
            response.put("success", false);
            response.put("message", "Only Admin Can Delete a Product");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    //---------------------------------------------------------------------------------
    //Update A Product
    @PutMapping(value = "/updateProduct/{productId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateProductById(
            @PathVariable Long productId,
            @RequestPart("data") String data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // ✅ Use ObjectMapper with JavaTimeModule
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // optional: ISO format

            // Deserialize product JSON
            Product product = mapper.readValue(data, Product.class);

            // Update product in service
            Product savedProduct = productService.updateProductById(productId, product, images, authentication);

            response.put("success", true);
            response.put("message", "Product updated Successfully");
            response.put("data", savedProduct);

            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            response.put("success", false);
            response.put("message", "Only Admin Can update a Product");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    //Get ProductById For Single Page product
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductById(
            @PathVariable Long productId
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.getProductById(productId);
            response.put("success", true);
            response.put("message", "Fetch Product successfully");
            response.put("data", product);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}

