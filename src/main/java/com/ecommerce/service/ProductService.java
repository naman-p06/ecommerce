package com.ecommerce.service;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CustomException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public ProductResponse addProduct(ProductRequest request){
        Product product=new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public Page<ProductResponse> getAllProducts(int page,int size){
        Page<Product> products=productRepository.findAll(PageRequest.of(page,size, Sort.by("id").ascending()));

        return products.map(this::toResponse);
    }

    public ProductResponse findById(Long id){
        Product product=productRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("Product not found"));
        return toResponse(product);
    }

    public ProductResponse updateProduct(ProductRequest request,Long id){
        Product product=productRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Product not found"));
        product.setDescription(request.getDescription());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        Product updated=productRepository.save(product);

        return toResponse(updated);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productRepository.delete(product);
    }
    public List<ProductResponse> searchProducts(String keyword) {

        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword.trim());
        return products.stream().map(this::toResponse).toList();
    }

    private ProductResponse toResponse(Product product){
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        return response;
    }
}
