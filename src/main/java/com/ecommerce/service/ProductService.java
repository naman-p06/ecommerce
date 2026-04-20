package com.ecommerce.service;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CustomException;
import com.ecommerce.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

        ProductResponse response = new ProductResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());
        response.setPrice(saved.getPrice());

        return response;
    }

    public Page<ProductResponse> getAllProducts(int page,int size){
        Page<Product> products=productRepository.findAll(PageRequest.of(page,size));

        return products.map(product -> {
            ProductResponse response=new ProductResponse();
            response.setId(product.getId());
            response.setName(product.getName());
            response.setDescription(product.getDescription());
            response.setPrice(product.getPrice());
            return response;
        });
    }

    public ProductResponse findById(Long id){
        Optional<Product> product=productRepository.findById(id);
        if(product.isPresent()){
            ProductResponse response=new ProductResponse();
            response.setId(product.get().getId());
            response.setName(product.get().getName());
            response.setDescription(product.get().getDescription());
            response.setPrice(product.get().getPrice());
            return  response;
        }
        else{
            throw new CustomException("Product not found");
        }
    }

    public ProductResponse updateProduct(ProductRequest request,Long id){
        Product product=productRepository.findById(id).orElseThrow(()->new CustomException("Product not found"));
        product.setDescription(request.getDescription());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        Product updated=productRepository.save(product);
        ProductResponse res=new ProductResponse();
        res.setId(updated.getId());
        res.setName(updated.getName());
        res.setDescription(updated.getDescription());
        res.setPrice(updated.getPrice());
        return res;
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException("Product not found"));

        productRepository.delete(product);
    }
    public List<ProductResponse> searchProducts(String keyword) {

        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);

        return products.stream().map(product -> {
            ProductResponse res = new ProductResponse();
            res.setId(product.getId());
            res.setName(product.getName());
            res.setDescription(product.getDescription());
            res.setPrice(product.getPrice());
            return res;
        }).toList();
    }
}
