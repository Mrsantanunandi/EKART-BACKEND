package com.santanu.Spring_Security_Project.Service;

import com.santanu.Spring_Security_Project.Model.*;
import com.santanu.Spring_Security_Project.dao.ProductRepo;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ADD PRODUCT
    @Transactional
    public Product addProduct(
            Product product,
            List<MultipartFile> images,
            Authentication authentication
    ) {
        User loggedInUser = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can add product");
        }

        // one image is must
        if (images == null || images.isEmpty()) {
            throw new RuntimeException("At least one image is required");
        }

        if (images.size() > 5) {
            throw new RuntimeException("Maximum 5 images allowed");
        }

        if (product.getProductName() == null ||
                product.getProductDesc() == null ||
                product.getProductPrice() == null ||
                product.getBrand() == null ||
                product.getCategory() == null ||
                product.getQuantity() == null
        ) {
            throw new RuntimeException("All product fields are required");
        }
        product.setUser(loggedInUser);

        List<ProductImage> imageList = new ArrayList<>();
        List<String> uploadedPublicIds = new ArrayList<>();

        try {
            for (MultipartFile image : images) {

                if (!image.getContentType().startsWith("image/")) {
                    throw new RuntimeException("Only image files are allowed");
                }

                Map<String, Object> uploadResult =
                        cloudinaryService.uploadImage(image);
                String imageUrl = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");

                if (imageUrl == null || publicId == null) {
                    throw new RuntimeException("Image upload failed");
                }

                ProductImage productImage = new ProductImage();
                productImage.setUrl(imageUrl);
                productImage.setPublicId(publicId);

                imageList.add(productImage);
                uploadedPublicIds.add(publicId);
            }
            product.setProductImg(imageList);
            return productRepo.save(product);

        } catch (Exception e) {
            for (String publicId : uploadedPublicIds) {
                cloudinaryService.deleteImage(publicId);
            }
            throw e;
        }
    }

    //-------------------------------------------------------------------------------
    // GET ALL PRODUCTS
    public List<Product> getAllProduct() {
        return productRepo.findAll();
    }
    //--------------------------------------------------------------------------------

    public void removeProduct(Long productId, Authentication authentication) {
        //Checked By ProductId Product is in db or not
        Product product = productRepo.findById(productId).orElseThrow(() ->
                new RuntimeException("Product not found"));
        //Checked loggedIn User Is admin or not
        User loggedInUser = userRepo.findByUsername(authentication.getName()).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("You are not an Admin");
        }
        //For Delete product first delete Images from Cloudinary
        List<ProductImage> images = product.getProductImg();

        for (ProductImage img : images) {
            try {
                cloudinaryService.deleteImage(img.getPublicId());
            } catch (Exception e) {

                System.out.println("Failed to delete image: " + img.getPublicId());
                e.printStackTrace();
            }
        }
        //Now Delete all the data of images
        productRepo.delete(product);
    }

    //-------------------------------------------------------------------------------------------------
    public Product updateProductById(Long productId,
                                 Product product,
                                 List<MultipartFile> images,
                                 Authentication authentication)
    {
        //Checked By ProductId Product is in db or not
        Product currentProduct = productRepo.findById(productId).orElseThrow(() ->
                new RuntimeException("Product not found"));
        //Checked loggedIn User Is admin or not
        User loggedInUser = userRepo.findByUsername(authentication.getName()).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("You are not an Admin");
        }
        //Update Product data partially else keep same
        if (product.getProductName() != null) currentProduct.setProductName(product.getProductName());
        if (product.getProductDesc() != null) currentProduct.setProductDesc(product.getProductDesc());
        if (product.getProductPrice() != null) currentProduct.setProductPrice(product.getProductPrice());
        if (product.getBrand() != null) currentProduct.setBrand(product.getBrand());
        if (product.getCategory() != null) currentProduct.setCategory(product.getCategory());
        if (product.getQuantity() != null) currentProduct.setQuantity(product.getQuantity());
        //Optional for images
        if (images != null && !images.isEmpty())
        {
            if (images.size() + currentProduct.getProductImg().size() > 5) {
                throw new RuntimeException("Maximum 5 images allowed");
            }
            List<String> uploadedPublicIds = new ArrayList<>();
            try
            {
                for (MultipartFile image : images)
                {
                    if (!image.getContentType().startsWith("image/"))
                    {
                        throw new RuntimeException("Only image files are allowed");
                    }

                    Map<String, Object> uploadResult =
                            cloudinaryService.uploadImage(image);
                    String imageUrl = (String) uploadResult.get("secure_url");
                    String publicId = (String) uploadResult.get("public_id");
                    if (imageUrl == null || publicId == null)
                    {
                        throw new RuntimeException("Image upload failed");
                    }

                    ProductImage productImage = new ProductImage();
                    productImage.setUrl(imageUrl);
                    productImage.setPublicId(publicId);

                    currentProduct.getProductImg().add(productImage);
                    uploadedPublicIds.add(publicId);
                }
            }
            catch (Exception e)
            {
                // If something fails, delete newly uploaded images
                for (String publicId : uploadedPublicIds)
                {
                    cloudinaryService.deleteImage(publicId);
                }
                throw e;
            }
        }
        return productRepo.save(currentProduct);
    }


    public Product getProductById(Long productId) {
        return productRepo.findById(productId).orElseThrow(()->
                new RuntimeException("Product Not Found"));
    }
}
