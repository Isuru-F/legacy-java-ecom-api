package com.ecommerce.legacy.service;

import com.ecommerce.legacy.model.Product;
import com.ecommerce.legacy.repository.ProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        validateProduct(product);
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("Product with SKU already exists: " + product.getSku());
        }
        return productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with SKU: " + sku));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProductsPaginated(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Page<Product> getProductsByCategoryPaginated(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = getProductById(id);

        if (StringUtils.isNotBlank(productDetails.getName())) {
            existingProduct.setName(productDetails.getName());
        }

        if (StringUtils.isNotBlank(productDetails.getDescription())) {
            existingProduct.setDescription(productDetails.getDescription());
        }

        if (productDetails.getPrice() != null) {
            existingProduct.setPrice(productDetails.getPrice());
        }

        if (productDetails.getStockQuantity() != null) {
            existingProduct.setStockQuantity(productDetails.getStockQuantity());
        }

        if (StringUtils.isNotBlank(productDetails.getCategory())) {
            existingProduct.setCategory(productDetails.getCategory());
        }

        if (StringUtils.isNotBlank(productDetails.getSku()) && 
            !existingProduct.getSku().equals(productDetails.getSku())) {
            if (productRepository.existsBySku(productDetails.getSku())) {
                throw new IllegalArgumentException("Product with SKU already exists: " + productDetails.getSku());
            }
            existingProduct.setSku(productDetails.getSku());
        }

        if (StringUtils.isNotBlank(productDetails.getImageUrl())) {
            existingProduct.setImageUrl(productDetails.getImageUrl());
        }

        return productRepository.save(existingProduct);
    }

    public Product updateStock(Long id, Integer newStock) {
        Product product = getProductById(id);
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public boolean isProductAvailable(Long id, Integer requestedQuantity) {
        Product product = getProductById(id);
        return product.getStockQuantity() >= requestedQuantity;
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (StringUtils.isBlank(product.getName())) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (StringUtils.isBlank(product.getSku())) {
            throw new IllegalArgumentException("Product SKU cannot be blank");
        }
    }
}
