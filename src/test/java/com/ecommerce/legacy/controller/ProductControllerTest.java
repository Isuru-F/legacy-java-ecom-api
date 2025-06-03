package com.ecommerce.legacy.controller;

import com.ecommerce.legacy.model.Product;
import com.ecommerce.legacy.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Product testProduct;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setSku("TEST-001");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStockQuantity(10);
        testProduct.setCategory("Electronics");
    }

    @Test
    public void testCreateProduct_Success() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        String productJson = objectMapper.writeValueAsString(testProduct);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TEST-001"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    public void testCreateProduct_InvalidInput() throws Exception {
        when(productService.createProduct(any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product name is required"));

        Product invalidProduct = new Product();
        invalidProduct.setName("");  // Invalid name
        String productJson = objectMapper.writeValueAsString(invalidProduct);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product name is required"));
    }

    @Test
    public void testGetProductById_Success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).getProductById(1L);
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    @Test
    public void testGetProductBySku_Success() throws Exception {
        when(productService.getProductBySku("TEST-001")).thenReturn(testProduct);

        mockMvc.perform(get("/products/sku/TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).getProductBySku("TEST-001");
    }

    @Test
    public void testGetProductBySku_NotFound() throws Exception {
        when(productService.getProductBySku("INVALID-SKU"))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/products/sku/INVALID-SKU"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    @Test
    public void testGetAllProducts_WithPagination() throws Exception {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), PageRequest.of(0, 10), 1);
        when(productService.getProductsPaginated(any())).thenReturn(productPage);

        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));

        verify(productService).getProductsPaginated(any());
    }

    @Test
    public void testGetAllProducts_WithDescendingSort() throws Exception {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), PageRequest.of(0, 10), 1);
        when(productService.getProductsPaginated(any())).thenReturn(productPage);

        mockMvc.perform(get("/products")
                .param("sortBy", "price")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        verify(productService).getProductsPaginated(any());
    }

    @Test
    public void testGetAllProductsList_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).getAllProducts();
    }

    @Test
    public void testGetProductsByCategory_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByCategory("Electronics")).thenReturn(products);

        mockMvc.perform(get("/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(productService).getProductsByCategory("Electronics");
    }

    @Test
    public void testGetProductsByCategoryPaginated_Success() throws Exception {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
        when(productService.getProductsByCategoryPaginated(eq("Electronics"), any())).thenReturn(productPage);

        mockMvc.perform(get("/products/category/Electronics/paginated")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(productService).getProductsByCategoryPaginated(eq("Electronics"), any());
    }

    @Test
    public void testSearchProductsByName_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchProductsByName("Test")).thenReturn(products);

        mockMvc.perform(get("/products/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).searchProductsByName("Test");
    }

    @Test
    public void testGetProductsByPriceRange_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByPriceRange(
                any(BigDecimal.class), 
                any(BigDecimal.class))).thenReturn(products);

        mockMvc.perform(get("/products/price-range")
                .param("minPrice", "50.00")
                .param("maxPrice", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).getProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    public void testGetAvailableProducts_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAvailableProducts()).thenReturn(products);

        mockMvc.perform(get("/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).getAvailableProducts();
    }

    @Test
    public void testGetAllCategories_Success() throws Exception {
        List<String> categories = Arrays.asList("Electronics", "Books", "Clothing");
        when(productService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Electronics"));

        verify(productService).getAllCategories();
    }

    @Test
    public void testUpdateProduct_Success() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        String productJson = objectMapper.writeValueAsString(testProduct);

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    public void testUpdateProduct_NotFound() throws Exception {
        when(productService.updateProduct(eq(999L), any(Product.class)))
                .thenThrow(new EntityNotFoundException("Product not found"));

        String productJson = objectMapper.writeValueAsString(testProduct);

        mockMvc.perform(put("/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    @Test
    public void testUpdateProduct_InvalidInput() throws Exception {
        when(productService.updateProduct(eq(1L), any(Product.class)))
                .thenThrow(new IllegalArgumentException("Invalid product data"));

        String productJson = objectMapper.writeValueAsString(testProduct);

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid product data"));
    }

    @Test
    public void testUpdateProductStock_Success() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setStockQuantity(15);
        
        when(productService.updateStock(1L, 15)).thenReturn(updatedProduct);

        mockMvc.perform(put("/products/1/stock")
                .param("stock", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(15));

        verify(productService).updateStock(1L, 15);
    }

    @Test
    public void testUpdateProductStock_NotFound() throws Exception {
        when(productService.updateStock(999L, 15))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(put("/products/999/stock")
                .param("stock", "15"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    @Test
    public void testDeleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));

        verify(productService).deleteProduct(1L);
    }

    @Test
    public void testDeleteProduct_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Product not found"))
                .when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    @Test
    public void testCheckProductAvailability_Available() throws Exception {
        when(productService.isProductAvailable(1L, 5)).thenReturn(true);

        mockMvc.perform(get("/products/1/availability/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(productService).isProductAvailable(1L, 5);
    }

    @Test
    public void testCheckProductAvailability_NotAvailable() throws Exception {
        when(productService.isProductAvailable(1L, 20)).thenReturn(false);

        mockMvc.perform(get("/products/1/availability/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(productService).isProductAvailable(1L, 20);
    }

    @Test
    public void testCheckProductAvailability_ProductNotFound() throws Exception {
        when(productService.isProductAvailable(999L, 5))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/products/999/availability/5"))
                .andExpect(status().isNotFound());
    }
}
