package com.electronicshop.service;

import com.electronicshop.entity.Product;
import com.electronicshop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    public List<Product> filterProducts(String keyword,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        List<String> brands,
                                        String category,
                                        String sort) {
        List<Product> products = new ArrayList<>(productRepository.findAll());

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> containsIgnoreCase(p.getName(), kw) || containsIgnoreCase(p.getDescription(), kw))
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() != null && p.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        if (brands != null && !brands.isEmpty()) {
            List<String> normalizedBrands = brands.stream()
                    .filter(v -> v != null && !v.isBlank())
                    .map(v -> v.trim().toLowerCase())
                    .collect(Collectors.toList());

            products = products.stream()
                    .filter(p -> {
                        String haystack = ((p.getName() == null ? "" : p.getName()) + " " +
                                (p.getDescription() == null ? "" : p.getDescription())).toLowerCase();
                        return normalizedBrands.stream().anyMatch(haystack::contains);
                    })
                    .collect(Collectors.toList());
        }

        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            String cat = category.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> containsIgnoreCase(p.getName(), cat) || containsIgnoreCase(p.getDescription(), cat))
                    .collect(Collectors.toList());
        }

        if (sort != null && !sort.isBlank()) {
            switch (sort) {
                case "priceAsc":
                    products.sort(Comparator.comparing(Product::getPrice, Comparator.nullsLast(BigDecimal::compareTo)));
                    break;
                case "priceDesc":
                    products.sort(Comparator.comparing(Product::getPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
                    break;
                case "nameAsc":
                    products.sort(Comparator.comparing(p -> safeLower(p.getName())));
                    break;
                case "nameDesc":
                    products.sort(Comparator.comparing((Product p) -> safeLower(p.getName())).reversed());
                    break;
                case "newest":
                    products.sort(Comparator.comparing(Product::getId, Comparator.nullsLast(Long::compareTo)).reversed());
                    break;
                default:
                    break;
            }
        }

        return products;
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public long countProducts() {
        return productRepository.count();
    }

    private boolean containsIgnoreCase(String source, String queryLower) {
        return source != null && source.toLowerCase().contains(queryLower);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
