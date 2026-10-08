package br.com.jurishome.auth.web;

import br.com.jurishome.auth.user.AdminUserView;
import br.com.jurishome.auth.user.Role;
import br.com.jurishome.auth.user.UserRepository;
import br.com.jurishome.auth.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository users;
    private final UserService userService;

    public AdminUserController(UserRepository users, UserService userService) {
        this.users = users;
        this.userService = userService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int pagina, Model model) {
        Pageable pageable = PageRequest.of(Math.max(0, pagina), 20,
            Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminUserView> page = users.findAll(pageable).map(AdminUserView::from);
        model.addAttribute("users", page);
        model.addAttribute("roles", Role.values());
        return "admin/usuarios";
    }

    @PostMapping("/{id}/perfil")
    public String changeRole(@PathVariable String id, @RequestParam Role role,
                             Authentication authentication, RedirectAttributes flash) {
        try {
            userService.changeRole(id, role, authentication.getName());
            flash.addFlashAttribute("notice", "Perfil atualizado e sessões anteriores revogadas.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable String id, @RequestParam boolean enabled,
                               Authentication authentication, RedirectAttributes flash) {
        try {
            userService.setEnabled(id, enabled, authentication.getName());
            flash.addFlashAttribute("notice", enabled
                ? "Conta reativada." : "Conta bloqueada e sessões revogadas.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable String id, Authentication authentication,
                         RedirectAttributes flash) {
        try {
            userService.deleteUser(id, authentication.getName());
            flash.addFlashAttribute("notice", "Conta removida e sessões revogadas.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
