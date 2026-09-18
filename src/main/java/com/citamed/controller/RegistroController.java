package com.citamed.controller;

import com.citamed.domain.Paciente;
import com.citamed.domain.Usuario;
import com.citamed.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    // Atiende el registro público de nuevas cuentas de paciente.
    private final PacienteService pacienteService;
    private final MessageSource messageSource;

    public RegistroController(PacienteService pacienteService, MessageSource messageSource) {
        this.pacienteService = pacienteService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String nuevo(Model model) {
        var paciente = new Paciente();
        paciente.setUsuario(new Usuario());
        model.addAttribute("paciente", paciente);
        return "/auth/registro";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Paciente paciente, @RequestParam String confirmar,
            RedirectAttributes redirectAttributes) {
        // La confirmación se revisa antes de enviar los datos al servicio.
        if (!paciente.getUsuario().getContrasena().equals(confirmar)) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("registro.errorContrasenas", null, LocaleContextHolder.getLocale()));
            return "redirect:/registro";
        }
        try {
            pacienteService.registrarPaciente(paciente);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("registro.ok", null, LocaleContextHolder.getLocale()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro";
        }
        return "redirect:/login";
    }
}
