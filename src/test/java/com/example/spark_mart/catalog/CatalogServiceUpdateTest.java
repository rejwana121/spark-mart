package com.example.spark_mart.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogServiceUpdateTest {

    private ProductRepository productRepository;
    private TagRepository tagRepository;
    private CategoryRepository categoryRepository;
    private CatalogService catalogService;

    private Product existingProduct;
    private Tag tag1;
    private Tag tag2;
    private Category grocery;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        tagRepository = mock(TagRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        catalogService = new CatalogService(categoryRepository, productRepository, tagRepository);

        grocery = new Category("grocery", "Grocery", "Daily essentials");
        ArrayList<Category> allCategories = new ArrayList<>(List.of(grocery));

        when(categoryRepository.findAll()).thenReturn(allCategories);
        when(categoryRepository.findById("grocery")).thenAnswer(inv -> Optional.of(grocery));

        tag1 = mock(Tag.class);
        when(tag1.getId()).thenReturn(1L);
        when(tag1.getName()).thenReturn("Bestseller");

        tag2 = mock(Tag.class);
        when(tag2.getId()).thenReturn(2L);
        when(tag2.getName()).thenReturn("New Arrival");

        existingProduct = new Product(900L, "Test Product", "test-product", "grocery", "Grocery",
                new BigDecimal("99.99"), 10, "Original description",
                "https://images.unsplash.com/photo-original?auto=format&fit=crop&w=900&q=80",
                4.5, false, LocalDateTime.now());
        existingProduct.addTag(tag1);
    }

    @Test
    void updateAppliesTagsAndSaves() {
        when(productRepository.findById(900L)).thenReturn(Optional.of(existingProduct));
        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(tag1, tag2));

        ProductForm form = ProductForm.from(existingProduct);
        form.setTagIds(List.of(1L, 2L));

        catalogService.update(900L, form);

        verify(productRepository).save(existingProduct);
        assertEquals(2, existingProduct.getTags().size());
    }

    @Test
    void updateWithEmptyTagIdsClearsTags() {
        when(productRepository.findById(900L)).thenReturn(Optional.of(existingProduct));

        ProductForm form = ProductForm.from(existingProduct);
        form.setTagIds(List.of());

        catalogService.update(900L, form);

        verify(productRepository).save(existingProduct);
        assertTrue(existingProduct.getTags().isEmpty());
    }

    @Test
    void updateWithNullTagIdsClearsTags() {
        when(productRepository.findById(900L)).thenReturn(Optional.of(existingProduct));

        ProductForm form = ProductForm.from(existingProduct);
        form.setTagIds(null);

        catalogService.update(900L, form);

        verify(productRepository).save(existingProduct);
        assertTrue(existingProduct.getTags().isEmpty());
    }

    @Test
    void updateNonExistentProductDoesNothing() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ProductForm form = new ProductForm();
        form.setName("Ghost Product");
        form.setCategorySlug("grocery");

        catalogService.update(999L, form);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateScalarFieldsAreApplied() {
        when(productRepository.findById(900L)).thenReturn(Optional.of(existingProduct));

        ProductForm form = ProductForm.from(existingProduct);
        form.setName("Updated Name");
        form.setPrice(new BigDecimal("149.99"));
        form.setStock(25);
        form.setDescription("Updated description");
        form.setImageUrl("https://example.com/new.jpg");

        catalogService.update(900L, form);

        assertEquals("Updated Name", existingProduct.getName());
        assertEquals(new BigDecimal("149.99"), existingProduct.getPrice());
        assertEquals(25, existingProduct.getStock());
        assertEquals("Updated description", existingProduct.getDescription());
        assertEquals("https://example.com/new.jpg", existingProduct.getImageUrl());
        verify(productRepository).save(existingProduct);
    }
}
