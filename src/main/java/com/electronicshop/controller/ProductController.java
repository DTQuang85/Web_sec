package com.electronicshop.controller;

import com.electronicshop.entity.Comment;
import com.electronicshop.entity.Product;
import com.electronicshop.entity.User;
import com.electronicshop.repository.CommentRepository;
import com.electronicshop.repository.UserRepository;
import com.electronicshop.service.CartService;
import com.electronicshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public ProductController(ProductService productService,
                             CommentRepository commentRepository,
                             UserRepository userRepository,
                             CartService cartService) {
        this.productService = productService;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @GetMapping("/product/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/";
        }

        List<Comment> comments = commentRepository.findByProductIdOrderByIdDesc(id);
        Map<Long, String> usernames = new HashMap<>();
        for (Comment comment : comments) {
            String username = userRepository.findById(comment.getUserId())
                    .map(User::getUsername)
                    .orElse("Unknown");
            usernames.put(comment.getId(), username);
        }

        addSessionModel(model, session);
        model.addAttribute("product", productOpt.get());
        model.addAttribute("comments", comments);
        model.addAttribute("commentUsernames", usernames);
        return "product";
    }

    @PostMapping("/product/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Comment comment = new Comment();
        comment.setUserId(currentUser.getId());
        comment.setProductId(id);
        comment.setContent(content);
        commentRepository.save(comment);

        return "redirect:/product/" + id;
    }

    private void addSessionModel(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", cartService.itemCount(session));
    }
}
