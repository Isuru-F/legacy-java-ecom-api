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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory("Electronics");
        testProduct.setSku("TEST-001");
        testProduct.setImageUrl("http://example.com/image.jpg");
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    public void testCreateProduct_Success() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TEST-001"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    public void testGetProductById_Success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).getProductById(1L);
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(1L)).thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));

        verify(productService).getProductById(1L);
    }

    @Test
    public void testGetProductBySku_Success() throws Exception {
        when(productService.getProductBySku("TEST-001")).thenReturn(testProduct);

        mockMvc.perform(get("/products/sku/TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-001"));

        verify(productService).getProductBySku("TEST-001");
    }

    @Test
    public void testGetProductBySku_NotFound() throws Exception {
        when(productService.getProductBySku("TEST-001")).thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/products/sku/TEST-001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));

        verify(productService).getProductBySku("TEST-001");
    }

    @Test
    public void testGetAllProducts_Paginated() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        
        when(productService.getProductsPaginated(any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).getProductsPaginated(any(Pageable.class));
    }

    @Test
    public void testGetAllProducts_List() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).getAllProducts();
    }

    @Test
    public void testGetProductsByCategory_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByCategory("Electronics")).thenReturn(products);

        mockMvc.perform(get("/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(productService).getProductsByCategory("Electronics");
    }

    @Test
    public void testGetProductsByCategoryPaginated_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        
        when(productService.getProductsByCategoryPaginated(eq("Electronics"), any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/products/category/Electronics/paginated")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("Electronics"));

        verify(productService).getProductsByCategoryPaginated(eq("Electronics"), any(Pageable.class));
    }

    @Test
    public void testSearchProducts_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchProductsByName("Test")).thenReturn(products);

        mockMvc.perform(get("/products/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).searchProductsByName("Test");
    }

    @Test
    public void testGetProductsByPriceRange_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(products);

        mockMvc.perform(get("/products/price-range")
                .param("minPrice", "50.00")
                .param("maxPrice", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(99.99));

        verify(productService).getProductsByPriceRange(new BigDecimal("50.00"), new BigDecimal("150.00"));
    }

    @Test
    public void testGetAvailableProducts_Success() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAvailableProducts()).thenReturn(products);

        mockMvc.perform(get("/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockQuantity").value(100));

        verify(productService).getAvailableProducts();
    }

    @Test
    public void testGetAllCategories_Success() throws Exception {
        List<String> categories = Arrays.asList("Electronics", "Books");
        when(productService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Electronics"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService).getAllCategories();
    }

    @Test
    public void testUpdateProduct_Success() throws Exception {
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    public void testUpdateProduct_NotFound() throws Exception {
        when(productService.updateProduct(eq(1L), any(Product.class)))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    public void testUpdateStock_Success() throws Exception {
        when(productService.updateStock(1L, 50)).thenReturn(testProduct);

        mockMvc.perform(put("/products/1/stock")
                .param("stock", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(100));

        verify(productService).updateStock(1L, 50);
    }

    @Test
    public void testUpdateStock_NotFound() throws Exception {
        when(productService.updateStock(1L, 50))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(put("/products/1/stock")
                .param("stock", "50"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));

        verify(productService).updateStock(1L, 50);
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
        doThrow(new EntityNotFoundException("Product not found")).when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));

        verify(productService).deleteProduct(1L);
    }

    @Test
    public void testCheckAvailability_Available() throws Exception {
        when(productService.isProductAvailable(1L, 10)).thenReturn(true);

        mockMvc.perform(get("/products/1/availability/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(productService).isProductAvailable(1L, 10);
    }

    @Test
    public void testCheckAvailability_NotAvailable() throws Exception {
        when(productService.isProductAvailable(1L, 200)).thenReturn(false);

        mockMvc.perform(get("/products/1/availability/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(productService).isProductAvailable(1L, 200);
    }

    @Test
    public void testCheckAvailability_ProductNotFound() throws Exception {
        when(productService.isProductAvailable(1L, 10))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/products/1/availability/10"))
                .andExpect(status().isNotFound());

        verify(productService).isProductAvailable(1L, 10);
    }
}
