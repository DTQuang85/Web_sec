package com.electronicshop.config;

import com.electronicshop.entity.Comment;
import com.electronicshop.entity.Product;
import com.electronicshop.entity.User;
import com.electronicshop.repository.CommentRepository;
import com.electronicshop.repository.ProductRepository;
import com.electronicshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CommentRepository commentRepository;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) {
        ensureAdminAccount();
        ensureSuperAdminAccount();

        if (userRepository.count() == 0) {
            userRepository.saveAll(List.of(
                    buildUser("user1", "password", "USER"),
                    buildUser("user2", "pass123", "USER")
            ));
        }

        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                    buildProduct("Laptop Dell XPS 15", "Laptop cao cap cho dan van phong va do hoa", "dell.jpg", new BigDecimal("1500.00")),
                    buildProduct("Mouse Logitech MX Master 3", "Chuot khong day cao cap", "logitech.jpg", new BigDecimal("80.00")),
                    buildProduct("Ban phim co Keychron K2", "Ban phim co RGB khong day", "keychron.jpg", new BigDecimal("90.00")),
                    buildProduct("Tai nghe Sony WH-1000XM4", "Tai nghe chong on tot nhat", "sony.jpg", new BigDecimal("350.00")),
                    buildProduct("Man hinh Samsung 27\"", "Man hinh 4K cho game thu", "samsung.jpg", new BigDecimal("400.00")),
                    buildProduct("O cung SSD Samsung 1TB", "O cung the ran toc do cao", "ssd.jpg", new BigDecimal("120.00"))
            ));
        }

        if (commentRepository.count() == 0) {
            commentRepository.saveAll(List.of(
                    buildComment(2L, 1L, "San pham rat tot, dang tien!"),
                    buildComment(3L, 1L, "Dung muot, pin lau"),
                    buildComment(2L, 2L, "Chuot nhay, cam ung tot")
            ));
        }
    }

    private User buildUser(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    private void ensureAdminAccount() {
        User admin = userRepository.findByUsername("admin").orElseGet(() -> buildUser("admin", "123456", "ADMIN"));
        admin.setRole("ADMIN");
        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            admin.setPassword("123456");
        }
        userRepository.save(admin);
    }

    private void ensureSuperAdminAccount() {
        User superAdmin = userRepository.findByUsername("root").orElseGet(() -> buildUser("root", "root123", "SUPER_ADMIN"));
        superAdmin.setRole("SUPER_ADMIN");
        if (superAdmin.getPassword() == null || superAdmin.getPassword().isBlank()) {
            superAdmin.setPassword("root123");
        }
        userRepository.save(superAdmin);
    }

    private Product buildProduct(String name, String description, String image, BigDecimal price) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setImage(image);
        product.setPrice(price);
        return product;
    }

    private Comment buildComment(Long userId, Long productId, String content) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setProductId(productId);
        comment.setContent(content);
        return comment;
    }
}
