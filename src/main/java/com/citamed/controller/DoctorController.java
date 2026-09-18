package com.citamed.controller;

import com.citamed.domain.Doctor;
import com.citamed.domain.HorarioBloqueado;
import com.citamed.service.CitaService;
import com.citamed.service.DoctorService;
import com.citamed.service.PacienteService;
import com.citamed.service.UsuarioService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.context.i18n.LocaleContextHolder;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    // Atiende la agenda, los pacientes y los bloqueos del doctor.
    private final DoctorService doctorService;
    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public DoctorController(DoctorService doctorService, CitaService citaService,
            PacienteService pacienteService, UsuarioService usuarioService, MessageSource messageSource) {
        this.doctorService = doctorService;
        this.citaService = citaService;
        this.pacienteService = pacienteService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false) String vista,
            @RequestParam(required = false) String fecha, Model model, HttpSession session) {
        LocalDate fechaSeleccionada = LocalDate.now();
        if (fecha != null && !fecha.isBlank()) {
            fechaSeleccionada = LocalDate.parse(fecha);
        }
        String vistaSeleccionada = "dia";
        if (vista != null) {
            vistaSeleccionada = vista;
        }
        LocalDate fechaAgenda = fechaSeleccionada;
        String vistaAgenda = vistaSeleccionada;
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario != null) {
            // La agenda muestra solamente las citas del doctor para el día actual.
            doctorService.getDoctorPorUsuario(idUsuario).ifPresent(doctor -> {
                model.addAttribute("doctorActual", doctor);
                if ("semana".equals(vistaAgenda)) {
                    model.addAttribute("citas", citaService.getAgendaDoctor(
                            doctor, fechaAgenda, fechaAgenda.plusDays(6)));
                } else {
                    model.addAttribute("citas", citaService.getAgendaDoctor(doctor, fechaAgenda));
                }
            });
        }
        model.addAttribute("fecha", fechaSeleccionada);
        model.addAttribute("vista", vistaSeleccionada);
        return "/doctor/agenda";
    }

    @GetMapping("/pacientes")
    public String pacientes(@RequestParam(required = false) Integer idPaciente,
            @RequestParam(required = false) String busqueda,
            Model model) {
        var pacientes = (busqueda == null || busqueda.isBlank())
                ? pacienteService.getPacientes(true) : pacienteService.buscarPacientes(busqueda);
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("busqueda", busqueda);
        // El historial se carga solamente cuando se selecciona un paciente.
        if (idPaciente != null) {
            pacienteService.getPaciente(idPaciente).ifPresent(paciente -> {
                model.addAttribute("pacienteActual", paciente);
                model.addAttribute("citas", citaService.getCitasPorPaciente(paciente));
            });
        }
        return "/doctor/pacientes";
    }

    @GetMapping("/bloquear")
    public String bloquear(Model model, HttpSession session) {
        getDoctorActual(session).ifPresent(doctor -> {
            model.addAttribute("doctorActual", doctor);
            model.addAttribute("bloqueos", doctorService.getBloqueos(doctor));
        });
        return "/doctor/bloquear";
    }

    @PostMapping("/bloquear/guardar")
    public String guardarBloqueo(@RequestParam String fechaInicio,
            @RequestParam String fechaFin, @RequestParam String motivo,
            HttpSession session, RedirectAttributes redirectAttributes) {
        var doctorOpt = getDoctorActual(session);
        if (doctorOpt.isPresent()) {
            // Las fechas del formulario se convierten antes de guardar el bloqueo.
            var bloqueo = new HorarioBloqueado();
            bloqueo.setDoctor(doctorOpt.get());
            bloqueo.setFechaInicio(LocalDateTime.parse(fechaInicio));
            bloqueo.setFechaFin(LocalDateTime.parse(fechaFin));
            bloqueo.setMotivo(motivo);
            doctorService.guardarBloqueo(bloqueo);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("bloqueo.guardado", null, LocaleContextHolder.getLocale()));
        } else {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("mensaje.doctorNoEncontrado", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/doctor/bloquear";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario != null) {
            doctorService.getDoctorPorUsuario(idUsuario).ifPresent(doctor -> model.addAttribute("doctor", doctor));
        }
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        model.addAttribute("rol", session.getAttribute("rol"));
        return "/doctor/perfil";
    }

    @PostMapping("/perfil/contrasena")
    public String cambiarContrasena(@RequestParam String actual, @RequestParam String nueva,
            @RequestParam String confirmar, HttpSession session, RedirectAttributes redirectAttributes) {
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario != null && usuarioService.cambiarContrasena(idUsuario, actual, nueva, confirmar)) {
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("perfil.contrasenaActualizada", null, LocaleContextHolder.getLocale()));
        } else {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("perfil.contrasenaError", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/doctor/perfil";
    }

    @PostMapping("/citas/completar")
    public String completar(@RequestParam Integer idCita, @RequestParam String notas,
            HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            var doctor = getDoctorActual(session)
                    .orElseThrow(() -> new IllegalStateException("No se encontró el doctor en sesión."));
            citaService.completar(idCita, doctor, notas);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("cita.completada", null, LocaleContextHolder.getLocale()));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/agenda";
    }

    private java.util.Optional<Doctor> getDoctorActual(HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario == null) {
            return java.util.Optional.empty();
        }
        return doctorService.getDoctorPorUsuario(idUsuario);
    }
}
