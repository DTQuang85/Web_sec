package com.electronicshop.controller;

import com.electronicshop.entity.Order;
import com.electronicshop.entity.User;
import com.electronicshop.service.CartService;
import com.electronicshop.service.OrderService;
import com.electronicshop.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class UserController {

    private static final String PROFILE_EDIT_ALLOWED = "PROFILE_EDIT_ALLOWED";

    private final UserService userService;
    private final OrderService orderService;
    private final CartService cartService;

    public UserController(UserService userService, OrderService orderService, CartService cartService) {
        this.userService = userService;
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping
    public String profile(@RequestParam(defaultValue = "false") boolean edit, HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login?next=/profile";
        }

        User user = userService.findById(loginUser.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login?next=/profile";
        }

        // Fetch user's recent orders
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> recentOrders = orderService.findByUserId(user.getId(), pageable);

        // Calculate statistics
        long totalOrders = orderService.countByUserId(user.getId());
        double totalSpent = recentOrders.getContent().stream()
            .map(Order::getTotal)
            .filter(java.util.Objects::nonNull)
            .mapToDouble(total -> total.doubleValue())
                .sum();

        model.addAttribute("user", user);
        model.addAttribute("recentOrders", recentOrders != null ? recentOrders.getContent() : Collections.emptyList());
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSpent", String.format("%.2f", totalSpent));
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", loginUser);
        model.addAttribute("editMode", edit);

        if (edit) {
            session.setAttribute(PROFILE_EDIT_ALLOWED, true);
        } else {
            session.removeAttribute(PROFILE_EDIT_ALLOWED);
        }

        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String fullname,
            @RequestParam String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        User user = userService.findById(loginUser.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (!Boolean.TRUE.equals(session.getAttribute(PROFILE_EDIT_ALLOWED))) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng bấm Chỉnh sửa trước khi cập nhật hồ sơ");
            return "redirect:/profile";
        }

        // Validate email format
        if (!isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ");
            return "redirect:/profile?edit=true";
        }

        user.setFullname(fullname);
        user.setEmail(email);

        // Handle avatar upload
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String avatarPath = saveAvatar(avatar);
                user.setAvatar(avatarPath);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh");
                return "redirect:/profile?edit=true";
            }
        }

        userService.save(user);
        session.setAttribute("LOGIN_USER", user);
        session.removeAttribute(PROFILE_EDIT_ALLOWED);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        User user = userService.findById(loginUser.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Validate current password
        if (!user.getPassword().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu hiện tại không đúng");
            return "redirect:/profile";
        }

        // Validate new password
        if (!isValidPassword(newPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", 
                    "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, và số");
            return "redirect:/profile";
        }

        // Validate confirm password
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Xác nhận mật khẩu không khớp");
            return "redirect:/profile";
        }

        user.setPassword(newPassword);
        userService.save(user);
        
        redirectAttributes.addFlashAttribute("passwordSuccess", "Đổi mật khẩu thành công");
        return "redirect:/profile";
    }

    @PostMapping("/upload-avatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(
            @RequestParam MultipartFile file,
            HttpSession session) {

        Map<String, Object> response = new java.util.HashMap<>();
        
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }

        try {
            // Validate file
            if (!isValidImageFile(file)) {
                response.put("success", false);
                response.put("message", "Chỉ chấp nhận ảnh (jpg, png, gif) nhỏ hơn 5MB");
                return response;
            }

            String avatarPath = saveAvatar(file);
            response.put("success", true);
            response.put("avatarPath", avatarPath);
            response.put("message", "Upload ảnh thành công");

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi upload ảnh");
        }

        return response;
    }

    @GetMapping("/edit")
    public String editProfile(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        User user = userService.findById(loginUser.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", loginUser);

        return "profile-edit";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // Helper methods
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }

    private boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) return false;
        if (file.getSize() > 5 * 1024 * 1024) return false; // 5MB

        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif")
        );
    }

    private String saveAvatar(MultipartFile file) throws IOException {
        String uploadDir = "src/main/resources/static/uploads/avatars/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        Files.write(filePath, file.getBytes());

        return "/uploads/avatars/" + filename;
    }
}
