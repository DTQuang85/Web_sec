package com.electronicshop.controller;

import com.electronicshop.entity.User;
import com.electronicshop.service.CartService;
import com.electronicshop.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;
    private final CartService cartService;

    public AuthController(UserService userService, CartService cartService) {
        this.userService = userService;
        this.cartService = cartService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "next", required = false) String next,
                            Model model,
                            HttpSession session) {
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        model.addAttribute("next", next);
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpServletRequest request,
                        HttpSession session,
                        Model model) {
        String loginId = request.getParameter("loginId");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String next = request.getParameter("next");

        String rawLoginId = (loginId != null && !loginId.isBlank()) ? loginId : username;
        String normalizedLoginId = rawLoginId != null ? rawLoginId.trim() : "";
        String safeNext = resolveNext(next);

        if (normalizedLoginId.isBlank()) {
            return renderLoginError(model, session, safeNext, "Vui lòng nhập tên đăng nhập hoặc email.", normalizedLoginId);
        }

        if (password == null || password.isBlank()) {
            return renderLoginError(model, session, safeNext, "Vui lòng nhập mật khẩu.", normalizedLoginId);
        }

        User user = userService.findByLoginId(normalizedLoginId).orElse(null);
        if (user == null) {
            return renderLoginError(model, session, safeNext, "Tài khoản không tồn tại. Vui lòng kiểm tra tên đăng nhập/email.", normalizedLoginId);
        }

        if (!userService.passwordMatches(user, password)) {
            return renderLoginError(model, session, safeNext, "Mật khẩu không đúng. Vui lòng thử lại.", normalizedLoginId);
        }

        session.setAttribute("LOGIN_USER", user);
        String role = normalizeRole(user.getRole());
        if ("SUPER_ADMIN".equals(role)) {
            if (!"/".equals(safeNext)) {
                return "redirect:" + safeNext;
            }
            return "redirect:/admin/root";
        }
        if ("ADMIN".equals(role)) {
            if (!"/".equals(safeNext)) {
                return "redirect:" + safeNext;
            }
            return "redirect:/admin/dashboard";
        }
        return "redirect:" + safeNext;
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(value = "username", required = false) String username,
                           @RequestParam(value = "email", required = false) String email,
                           @RequestParam(value = "fullName", required = false) String fullName,
                           @RequestParam(value = "password", required = false) String password,
                           @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                           Model model,
                           HttpSession session) {
        String normalizedUsername = username != null ? username.trim() : "";
        String normalizedEmail = email != null ? email.trim() : "";
        String normalizedFullName = fullName != null ? fullName.trim() : "";

        if (normalizedUsername.isBlank()) {
            return renderRegisterError(model, session, "Tên đăng nhập không được để trống.", normalizedUsername, normalizedEmail, normalizedFullName);
        }

        if (normalizedEmail.isBlank()) {
            return renderRegisterError(model, session, "Email không được để trống.", normalizedUsername, normalizedEmail, normalizedFullName);
        }

        if (!isValidEmail(normalizedEmail)) {
            return renderRegisterError(model, session, "Email không hợp lệ. Ví dụ: ten@domain.com", normalizedUsername, normalizedEmail, normalizedFullName);
        }

        if (password == null || password.length() < 6) {
            return renderRegisterError(model, session, "Mật khẩu phải có ít nhất 6 ký tự.", normalizedUsername, normalizedEmail, normalizedFullName);
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return renderRegisterError(model, session, "Xác nhận mật khẩu không khớp.", normalizedUsername, normalizedEmail, normalizedFullName);
        }

        if (userService.usernameExists(normalizedUsername)) {
            return renderRegisterError(model, session, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.", normalizedUsername, normalizedEmail, normalizedFullName);
        }

        if (userService.emailExists(normalizedEmail)) {
            return renderRegisterError(model, session, "Email đã được sử dụng, vui lòng dùng email khác.", normalizedUsername, normalizedEmail, normalizedFullName);
        }

        User created = userService.register(normalizedUsername, normalizedEmail, normalizedFullName, password);
        session.setAttribute("LOGIN_USER", created);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }

    private String resolveNext(String next) {
        if (next == null || next.isBlank()) {
            return "/";
        }
        String trimmed = next.trim();
        if (!trimmed.startsWith("/") || trimmed.startsWith("//") || trimmed.contains("\n") || trimmed.contains("\r")) {
            return "/";
        }
        return trimmed;
    }

    private String renderLoginError(Model model, HttpSession session, String next, String error, String loginId) {
        model.addAttribute("error", error);
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        model.addAttribute("next", next);
        model.addAttribute("loginId", loginId);
        return "login";
    }

    private String renderRegisterError(Model model, HttpSession session, String error,
                                       String username, String email, String fullName) {
        model.addAttribute("error", error);
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("fullName", fullName);
        return "register";
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
