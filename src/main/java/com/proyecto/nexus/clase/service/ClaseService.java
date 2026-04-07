package com.proyecto.nexus.clase.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.nexus.clase.model.Clase;
import com.proyecto.nexus.clase.model.PaqueteClase;
import com.proyecto.nexus.clase.repository.ClaseRepository;
import com.proyecto.nexus.clase.repository.PaqueteClaseRepository;
import com.proyecto.nexus.plan.model.Plan;
import com.proyecto.nexus.plan.repository.PlanRepository;
import com.proyecto.nexus.reserva.model.Reserva;
import com.proyecto.nexus.reserva.repository.ReservaRepository;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;

import java.time.*;
import java.util.*;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final ReservaRepository reservaRepository;
    private final PaqueteClaseRepository paqueteClaseRepository;
    private final DatosUsuarioRepository datosUsuarioRepository;
    private final PlanRepository planRepository;

    public ClaseService(ClaseRepository claseRepository,
                        ReservaRepository reservaRepository,
                        PaqueteClaseRepository paqueteClaseRepository,
                        DatosUsuarioRepository datosUsuarioRepository,
                        PlanRepository planRepository) {

        this.claseRepository = claseRepository;
        this.reservaRepository = reservaRepository;
        this.paqueteClaseRepository = paqueteClaseRepository;
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.planRepository = planRepository;
    }

    // ==================== MAPA DIAS ====================

    private static final Map<DayOfWeek, String> DIAS_ESPANOL = Map.of(
        DayOfWeek.MONDAY, "Lunes",
        DayOfWeek.TUESDAY, "Martes",
        DayOfWeek.WEDNESDAY, "Miércoles",
        DayOfWeek.THURSDAY, "Jueves",
        DayOfWeek.FRIDAY, "Viernes",
        DayOfWeek.SATURDAY, "Sábado",
        DayOfWeek.SUNDAY, "Domingo"
    );

    // ==================== CLASES ====================

    public List<Clase> obtenerClasesDisponibles() {
        return claseRepository.findByDisponibleTrue();
    }

    public List<Clase> filtrarPorDisciplina(String disciplina) {
        return claseRepository.findByDisciplinaAndDisponibleTrue(disciplina);
    }

    public List<Clase> filtrarPorNivel(String nivel) {
        return claseRepository.findByNivelAndDisponibleTrue(nivel);
    }

    // ==================== PAQUETES ====================

    public Optional<PaqueteClase> obtenerPaqueteActivo(DatosUsuario usuario) {
        return paqueteClaseRepository
                .findFirstByUsuarioAndEstadoOrderByFechaVencimientoAsc(usuario, "ACTIVO");
    }

    public PaqueteClase guardarPaquete(PaqueteClase paquete) {
        return paqueteClaseRepository.save(paquete);
    }

    public List<Plan> obtenerPlanesActivos() {
        return planRepository.findByEstado("ACTIVO");
    }

    @Transactional
    public String comprarPlan(Integer idPlan, String cedulaUsuario) {

        DatosUsuario usuario = obtenerUsuarioPorCedula(cedulaUsuario);

        Plan plan = planRepository.findById(idPlan)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        PaqueteClase paquete = new PaqueteClase();
        paquete.setUsuario(usuario);
        paquete.setTotalClases(plan.getTotalClases());
        paquete.setClasesRestantes(plan.getTotalClases());
        paquete.setIdPlanes(plan.getIdPlanes());
        paquete.setFechaInicio(LocalDateTime.now());
        paquete.setFechaVencimiento(LocalDateTime.now().plusDays(plan.getDuracionDias()));
        paquete.setEstado("ACTIVO");
        paquete.setCreatedAt(LocalDateTime.now());

        paqueteClaseRepository.save(paquete);

        return "ok";
    }

    // ==================== RESERVAS ====================

    @Transactional
    public String reservarClase(Integer idClase, String cedulaUsuario, LocalDate fechaClase) {

        DatosUsuario usuario = obtenerUsuarioPorCedula(cedulaUsuario);

        PaqueteClase paquete = obtenerPaqueteActivo(usuario)
                .orElseThrow(() -> new RuntimeException("No tienes un paquete activo"));

        if (paquete.getClasesRestantes() <= 0) {
            return "error: No tienes clases disponibles";
        }

        Clase clase = claseRepository.findById(idClase)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));

        validarDiaClase(clase, fechaClase);
        validarCupo(clase, fechaClase);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setClase(clase);
        reserva.setPaquete(paquete);
        reserva.setFechaClase(fechaClase);
        reserva.setHoraInicio(clase.getHoraInicio());
        reserva.setEstado("Confirmada");
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setClaseDevuelta(false);

        reservaRepository.save(reserva);

        paquete.setClasesRestantes(paquete.getClasesRestantes() - 1);

        if (paquete.getClasesRestantes() == 0) {
            paquete.setEstado("AGOTADO");
        }

        paqueteClaseRepository.save(paquete);

        return "ok";
    }

    public List<Reserva> obtenerReservasUsuario(DatosUsuario usuario) {
        return reservaRepository.findByUsuarioOrderByFechaReservaDesc(usuario);
    }

    public List<Reserva> obtenerReservasProximas(DatosUsuario usuario) {
        return obtenerReservasUsuario(usuario).stream()
                .filter(r -> ("Confirmada".equals(r.getEstado()) || "Pendiente".equals(r.getEstado()))
                        && r.getFechaClase().isAfter(LocalDate.now().minusDays(1)))
                .toList();
    }

    @Transactional
    public String cancelarReserva(Integer idReserva, String cedulaUsuario) {

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!reserva.getUsuario().getCedula().toString().equals(cedulaUsuario)) {
            return "error: No autorizado";
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime claseDateTime = reserva.getFechaClase().atTime(reserva.getHoraInicio());

        long horas = Duration.between(ahora, claseDateTime).toHours();

        reserva.setEstado(horas >= 2 ? "Cancelada" : "Cancelada_Tarde");
        reserva.setCanceladaEn(ahora);

        if (horas >= 2) {
            PaqueteClase paquete = reserva.getPaquete();
            paquete.setClasesRestantes(paquete.getClasesRestantes() + 1);
            paqueteClaseRepository.save(paquete);
            reserva.setClaseDevuelta(true);
        }

        reservaRepository.save(reserva);

        return "ok";
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private DatosUsuario obtenerUsuarioPorCedula(String cedula) {
        try {
            Long cedulaLong = Long.parseLong(cedula);
            return datosUsuarioRepository.findByCedula(cedulaLong)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        } catch (Exception e) {
            throw new RuntimeException("Cédula inválida");
        }
    }

    private void validarDiaClase(Clase clase, LocalDate fecha) {

        String diaClase = clase.getDiaSemana();
        String diaFecha = DIAS_ESPANOL.get(fecha.getDayOfWeek());

        if (!diaClase.equalsIgnoreCase(diaFecha)) {
            throw new RuntimeException("La clase solo se imparte los " + diaClase);
        }
    }

    private void validarCupo(Clase clase, LocalDate fecha) {

        long reservas = reservaRepository.countReservasActivasByClaseAndFecha(clase, fecha);

        if (reservas >= clase.getCupoMaximo()) {
            throw new RuntimeException("Clase llena");
        }
    }

public List<Map<String, Object>> obtenerClasesSemana(String disciplina, String nivel) {

    List<Clase> clases;

    if (disciplina != null && !disciplina.equals("todas")) {
        clases = claseRepository.findByDisciplinaAndDisponibleTrue(disciplina);
    } else if (nivel != null && !nivel.equals("todos")) {
        clases = claseRepository.findByNivelAndDisponibleTrue(nivel);
    } else {
        clases = claseRepository.findByDisponibleTrue();
    }

    List<Map<String, Object>> resultado = new java.util.ArrayList<>();

    LocalDate hoy = LocalDate.now();

    for (int i = 0; i < 7; i++) {
        LocalDate fecha = hoy.plusDays(i);
        String diaSemana = DIAS_ESPANOL.get(fecha.getDayOfWeek());

        for (Clase clase : clases) {

            if (clase.getDiaSemana().equalsIgnoreCase(diaSemana)) {

                if (!claseYaPaso(clase, fecha)) {

                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("clase", clase);
                    item.put("fecha", fecha);

                    resultado.add(item);
                }
            }
        }
    }

    return resultado;
}

private boolean claseYaPaso(Clase clase, LocalDate fecha) {

    // Si no es hoy → no ha pasado
    if (!fecha.equals(LocalDate.now())) {
        return false;
    }

    // Si es hoy → comparar hora
    return clase.getHoraInicio().isBefore(java.time.LocalTime.now());
}
}