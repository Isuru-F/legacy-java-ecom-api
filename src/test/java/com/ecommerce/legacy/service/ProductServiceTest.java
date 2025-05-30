package com.ecommerce.legacy.service;

import com.ecommerce.legacy.model.Product;
import com.ecommerce.legacy.repository.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @Before
    public void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("29.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory("Electronics");
        testProduct.setSku("TEST-001");
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    public void testCreateProduct_Success() {
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getSku(), result.getSku());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    public void testCreateProduct_SkuExists() {
        when(productRepository.existsBySku(anyString())).thenReturn(true);

        try {
            productService.createProduct(testProduct);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Product with SKU already exists"));
        }

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testGetProductById_Success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
    }

    @Test
    public void testGetProductById_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        try {
            productService.getProductById(1L);
            fail("Expected EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("Product not found with id"));
        }
    }

    @Test
    public void testGetProductBySku_Success() {
        when(productRepository.findBySku(anyString())).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductBySku("TEST-001");

        assertNotNull(result);
        assertEquals(testProduct.getSku(), result.getSku());
    }

    @Test
    public void testGetProductBySku_NotFound() {
        when(productRepository.findBySku(anyString())).thenReturn(Optional.empty());

        try {
            productService.getProductBySku("NONEXISTENT");
            fail("Expected EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("Product not found with SKU"));
        }
    }

    @Test
    public void testGetAllProducts() {
        List<Product> productList = Arrays.asList(testProduct, new Product());
        when(productRepository.findAll()).thenReturn(productList);

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testGetProductsByCategory() {
        List<Product> productList = Arrays.asList(testProduct);
        when(productRepository.findByCategory(anyString())).thenReturn(productList);

        List<Product> result = productService.getProductsByCategory("Electronics");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getCategory(), result.get(0).getCategory());
    }

    @Test
    public void testSearchProductsByName() {
        List<Product> productList = Arrays.asList(testProduct);
        when(productRepository.findByNameContaining(anyString())).thenReturn(productList);

        List<Product> result = productService.searchProductsByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByNameContaining("Test");
    }

    @Test
    public void testGetProductsByPriceRange() {
        List<Product> productList = Arrays.asList(testProduct);
        BigDecimal minPrice = new BigDecimal("20.00");
        BigDecimal maxPrice = new BigDecimal("50.00");
        
        when(productRepository.findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(productList);

        List<Product> result = productService.getProductsByPriceRange(minPrice, maxPrice);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByPriceBetween(minPrice, maxPrice);
    }

    @Test
    public void testUpdateProduct_Success() {
        Product updateDetails = new Product();
        updateDetails.setName("Updated Product");
        updateDetails.setPrice(new BigDecimal("39.99"));

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateProduct(1L, updateDetails);

        assertNotNull(result);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    public void testUpdateStock_Success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateStock(1L, 150);

        assertNotNull(result);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    public void testDeleteProduct_Success() {
        when(productRepository.existsById(anyLong())).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteProduct_NotFound() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        try {
            productService.deleteProduct(1L);
            fail("Expected EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("Product not found with id"));
        }

        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testIsProductAvailable_True() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        boolean result = productService.isProductAvailable(1L, 50);

        assertTrue(result);
    }

    @Test
    public void testIsProductAvailable_False() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        boolean result = productService.isProductAvailable(1L, 150);

        assertFalse(result);
    }
}
