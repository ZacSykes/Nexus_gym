package com.proyecto.nexus.usuario.controller;

import java.util.Comparator;
import java.util.Optional;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.nexus.clase.repository.ClaseRepository;
import com.proyecto.nexus.clase.model.Clase;
import com.proyecto.nexus.pago.repository.PagoRepository;
import com.proyecto.nexus.pago.model.Pago;
import com.proyecto.nexus.plan.repository.PlanRepository;
import com.proyecto.nexus.reserva.model.Reserva;
import com.proyecto.nexus.reserva.repository.ReservaRepository;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;
import com.proyecto.nexus.usuario.repository.ProfesorRepository;

@Controller
@RequestMapping("/admin")
public class AdminPanelController {

    private final ClaseRepository claseRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final PlanRepository planRepository;
    private final DatosUsuarioRepository datosUsuarioRepository;
    private final ProfesorRepository profesorRepository;

    public AdminPanelController(ClaseRepository claseRepository,
                                ReservaRepository reservaRepository,
                                PagoRepository pagoRepository,
                                PlanRepository planRepository,
                                DatosUsuarioRepository datosUsuarioRepository,
                                ProfesorRepository profesorRepository) {
        this.claseRepository = claseRepository;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.planRepository = planRepository;
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.profesorRepository = profesorRepository;
    }

    @GetMapping("/clases")
    public String clases(Model model, Authentication auth) {
        agregarUsuarioActual(model, auth);
        model.addAttribute("clases", claseRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(c -> c.getNombre() == null ? "" : c.getNombre()))
                .toList());
        model.addAttribute("profesores", profesorRepository.findAllByOrderByNombreAscApellidoAsc());
        return "admin/clases";
    }

    @PostMapping("/clases/crear")
    public String crearClase(@RequestParam String nombre,
                             @RequestParam String disciplina,
                             @RequestParam String nivel,
                             @RequestParam String diaSemana,
                             @RequestParam String horaInicio,
                             @RequestParam String horaFin,
                             @RequestParam Integer cupoMaximo,
                             @RequestParam(required = false) String cupoDisponible,
                             @RequestParam(required = false) String salon,
                             @RequestParam(defaultValue = "false") Boolean disponible,
                             @RequestParam(required = false) String idProfesor,
                             RedirectAttributes redirectAttributes) {

        try {
            Clase clase = new Clase();
            String errorValidacion = aplicarDatosClase(
                    clase,
                    nombre,
                    disciplina,
                    nivel,
                    diaSemana,
                    horaInicio,
                    horaFin,
                    cupoMaximo,
                    cupoDisponible,
                    salon,
                    disponible,
                    idProfesor
            );

            if (errorValidacion != null) {
                redirectAttributes.addFlashAttribute("error", errorValidacion);
                return "redirect:/admin/clases";
            }

            claseRepository.save(clase);
            redirectAttributes.addFlashAttribute("mensaje", "Clase creada correctamente.");
            return "redirect:/admin/clases";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo crear la clase.");
            return "redirect:/admin/clases";
        }
    }

    @PostMapping("/clases/editar")
    public String editarClase(@RequestParam Integer idClase,
                              @RequestParam String nombre,
                              @RequestParam String disciplina,
                              @RequestParam String nivel,
                              @RequestParam String diaSemana,
                              @RequestParam String horaInicio,
                              @RequestParam String horaFin,
                              @RequestParam Integer cupoMaximo,
                              @RequestParam(required = false) String cupoDisponible,
                              @RequestParam(required = false) String salon,
                              @RequestParam(defaultValue = "false") Boolean disponible,
                              @RequestParam(required = false) String idProfesor,
                              RedirectAttributes redirectAttributes) {
        try {
            var claseOpt = claseRepository.findById(idClase);
            if (claseOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La clase no existe.");
                return "redirect:/admin/clases";
            }

            Clase clase = claseOpt.get();

            String errorValidacion = aplicarDatosClase(
                    clase,
                    nombre,
                    disciplina,
                    nivel,
                    diaSemana,
                    horaInicio,
                    horaFin,
                    cupoMaximo,
                    cupoDisponible,
                    salon,
                    disponible,
                    idProfesor
            );

            if (errorValidacion != null) {
                redirectAttributes.addFlashAttribute("error", errorValidacion);
                return "redirect:/admin/clases";
            }

            claseRepository.save(clase);
            redirectAttributes.addFlashAttribute("mensaje", "Clase actualizada correctamente.");
            return "redirect:/admin/clases";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la clase.");
            return "redirect:/admin/clases";
        }
    }

    @PostMapping("/clases/eliminar/{idClase}")
    public String eliminarClase(@PathVariable Integer idClase,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!claseRepository.existsById(idClase)) {
                redirectAttributes.addFlashAttribute("error", "La clase no existe.");
                return "redirect:/admin/clases";
            }

            claseRepository.deleteById(idClase);
            redirectAttributes.addFlashAttribute("mensaje", "Clase eliminada correctamente.");
            return "redirect:/admin/clases";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la clase porque tiene datos relacionados.");
            return "redirect:/admin/clases";
        }
    }

    @GetMapping("/reservas")
    public String reservas(Model model, Authentication auth) {
        agregarUsuarioActual(model, auth);
        model.addAttribute("reservas", reservaRepository.findAll()
                .stream()
            .sorted(Comparator.comparing(
                (Reserva r) -> r.getFechaReserva() == null ? java.time.LocalDateTime.MIN : r.getFechaReserva())
                .reversed())
                .toList());
        return "admin/reservas";
    }

    @GetMapping("/planes")
    public String planes(Model model, Authentication auth) {
        agregarUsuarioActual(model, auth);
        model.addAttribute("planes", planRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(p -> p.getNombre() == null ? "" : p.getNombre()))
                .toList());
        return "admin/planes";
    }

    @GetMapping("/gimnasios")
    public String gimnasios(Model model, Authentication auth) {
        agregarUsuarioActual(model, auth);
        model.addAttribute("mensajeModulo", "Modulo de gimnasios en construccion.");
        return "admin/gimnasios";
    }

    @GetMapping("/config")
    public String config(Model model, Authentication auth) {
        agregarUsuarioActual(model, auth);
        model.addAttribute("mensajeModulo", "Modulo de configuracion en construccion.");
        return "admin/config";
    }

    private void agregarUsuarioActual(Model model, Authentication auth) {
        if (auth == null) {
            return;
        }
        try {
            Long cedula = Long.parseLong(auth.getName());
            Optional<DatosUsuario> usuario = datosUsuarioRepository.findByCedula(cedula);
            usuario.ifPresent(value -> model.addAttribute("usuario", value));
        } catch (NumberFormatException ignored) {
            // Si el username no es cedula, dejamos el fallback en la vista.
        }
    }

    private String aplicarDatosClase(Clase clase,
                                     String nombre,
                                     String disciplina,
                                     String nivel,
                                     String diaSemana,
                                     String horaInicio,
                                     String horaFin,
                                     Integer cupoMaximo,
                                     String cupoDisponible,
                                     String salon,
                                     Boolean disponible,
                                     String idProfesor) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre de la clase es obligatorio.";
        }

        if (cupoMaximo == null || cupoMaximo <= 0) {
            return "El cupo maximo debe ser mayor a 0.";
        }

        LocalTime inicio;
        LocalTime fin;

        try {
            inicio = LocalTime.parse(horaInicio);
            fin = LocalTime.parse(horaFin);
        } catch (DateTimeParseException e) {
            return "Formato de hora invalido.";
        }

        if (!fin.isAfter(inicio)) {
            return "La hora fin debe ser mayor a la hora inicio.";
        }

        Integer cupoDisponibleParsed = (cupoDisponible == null || cupoDisponible.isBlank())
                ? null
                : Integer.parseInt(cupoDisponible.trim());

        int cupoDisponibleFinal = (cupoDisponibleParsed == null) ? cupoMaximo : cupoDisponibleParsed;
        if (cupoDisponibleFinal < 0 || cupoDisponibleFinal > cupoMaximo) {
            return "El cupo disponible debe estar entre 0 y el cupo maximo.";
        }

        clase.setNombre(nombre.trim());
        clase.setDisciplina(disciplina);
        clase.setNivel(nivel);
        clase.setDiaSemana(diaSemana);
        clase.setHoraInicio(inicio);
        clase.setHoraFin(fin);
        clase.setCupoMaximo(cupoMaximo);
        clase.setCupoDisponible(cupoDisponibleFinal);
        clase.setSalon(salon == null ? null : salon.trim());
        clase.setDisponible(disponible);
        clase.setProfesor(null);

        if (idProfesor != null && !idProfesor.isBlank()) {
            Integer idProfesorParsed = Integer.parseInt(idProfesor.trim());
            var profesorOpt = profesorRepository.findById(idProfesorParsed);
            if (profesorOpt.isEmpty()) {
                return "Profesor no encontrado.";
            }
            clase.setProfesor(profesorOpt.get());
        }

        return null;
    }
}
