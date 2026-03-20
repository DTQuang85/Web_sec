package com.electronicshop.controller;

import com.electronicshop.entity.User;
import com.electronicshop.service.CartService;  
import com.electronicshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CartService cartService;

    public HomeController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/products"})
    public String home(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "minPrice", required = false) String minPrice,
                       @RequestParam(value = "maxPrice", required = false) String maxPrice,
                       @RequestParam(value = "brands", required = false) List<String> brands,
                       @RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "sort", required = false) String sort,
                       Model model,
                       HttpSession session) {
        BigDecimal min = parseDecimal(minPrice);
        BigDecimal max = parseDecimal(maxPrice);

        model.addAttribute("products", productService.filterProducts(keyword, min, max, brands, category, sort));
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("minPrice", minPrice == null ? "" : minPrice);
        model.addAttribute("maxPrice", maxPrice == null ? "" : maxPrice);
        model.addAttribute("selectedBrands", brands == null ? List.of() : brands);
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("sort", sort == null ? "default" : sort);
        addSessionModel(model, session);
        return "index";
    }

    @GetMapping({"/about", "/gioi-thieu"})
    public String about(Model model, HttpSession session) {
        addSessionModel(model, session);
        return "about";
    }

    @GetMapping({"/contact", "/lien-he"})
    public String contact(Model model, HttpSession session) {
        addSessionModel(model, session);
        return "contact";
    }

    private void addSessionModel(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", cartService.itemCount(session));
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
