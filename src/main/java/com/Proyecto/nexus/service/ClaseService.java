package com.Proyecto.nexus.service;

import com.Proyecto.nexus.model.*;
import com.Proyecto.nexus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClaseService {

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PaqueteClaseRepository paqueteClaseRepository;

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @Autowired
    private PlanRepository planRepository;

    // Mapa para obtener nombre del día de la semana en español
    private static final Map<DayOfWeek, String> DIAS_ESPANOL = new HashMap<>();
    static {
        DIAS_ESPANOL.put(DayOfWeek.MONDAY, "Lunes");
        DIAS_ESPANOL.put(DayOfWeek.TUESDAY, "Martes");
        DIAS_ESPANOL.put(DayOfWeek.WEDNESDAY, "Miércoles");
        DIAS_ESPANOL.put(DayOfWeek.THURSDAY, "Jueves");
        DIAS_ESPANOL.put(DayOfWeek.FRIDAY, "Viernes");
        DIAS_ESPANOL.put(DayOfWeek.SATURDAY, "Sábado");
        DIAS_ESPANOL.put(DayOfWeek.SUNDAY, "Domingo");
    }

    private String obtenerDiaSemanaEnEspanol(LocalDate fecha) {
        return DIAS_ESPANOL.get(fecha.getDayOfWeek());
    }

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
        return paqueteClaseRepository.findFirstByUsuarioAndEstadoOrderByFechaVencimientoAsc(usuario, "ACTIVO");
    }
    
    public PaqueteClase guardarPaquete(PaqueteClase paquete) {
    return paqueteClaseRepository.save(paquete);
}

    public List<Plan> obtenerPlanesActivos() {
        return planRepository.findByEstado("ACTIVO");
    }

    @Transactional
    public String comprarPlan(Integer idPlan, String cedulaUsuario) {
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(Long.parseLong(cedulaUsuario));
        if (usuarioOpt.isEmpty()) {
            return "error: Usuario no encontrado";
        }
        DatosUsuario usuario = usuarioOpt.get();

        Optional<Plan> planOpt = planRepository.findById(idPlan);
        if (planOpt.isEmpty()) {
            return "error: Plan no encontrado";
        }
        Plan plan = planOpt.get();

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
        // 1. Validar usuario
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(Long.parseLong(cedulaUsuario));
        if (usuarioOpt.isEmpty()) {
            return "error: Usuario no encontrado";
        }
        DatosUsuario usuario = usuarioOpt.get();

        // 2. Validar paquete activo
        Optional<PaqueteClase> paqueteOpt = obtenerPaqueteActivo(usuario);
        if (paqueteOpt.isEmpty()) {
            return "error: No tienes un paquete activo";
        }
        PaqueteClase paquete = paqueteOpt.get();
        if (paquete.getClasesRestantes() <= 0) {
            return "error: No tienes clases restantes en tu paquete";
        }

        // 3. Validar clase
        Optional<Clase> claseOpt = claseRepository.findById(idClase);
        if (claseOpt.isEmpty()) {
            return "error: Clase no encontrada";
        }
        Clase clase = claseOpt.get();

        // 4. Validar que el día de la semana de la clase coincide con la fecha seleccionada
        String diaSemanaClase = clase.getDiaSemana();
        String diaSemanaFecha = obtenerDiaSemanaEnEspanol(fechaClase);
        if (!diaSemanaClase.equalsIgnoreCase(diaSemanaFecha)) {
            return "error: La clase solo se imparte los " + diaSemanaClase;
        }

        // 5. Validar cupo disponible para esa fecha (reservas activas)
        long reservasActivas = reservaRepository.countReservasActivasByClaseAndFecha(clase, fechaClase);
        if (reservasActivas >= clase.getCupoMaximo()) {
            return "error: No hay cupo disponible en esta clase para la fecha seleccionada";
        }

        // 6. Crear la reserva
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

        // 7. Descontar clase del paquete
        paquete.setClasesRestantes(paquete.getClasesRestantes() - 1);
        if (paquete.getClasesRestantes() == 0) {
            paquete.setEstado("AGOTADO");
        }
        paqueteClaseRepository.save(paquete);

        // 8. Ya no se actualiza cupoDisponible en clase porque se usa countReservasActivas
        // (Opcional: si se mantiene, podríamos no actualizarlo)

        return "ok";
    }

    public List<Reserva> obtenerReservasUsuario(DatosUsuario usuario) {
        return reservaRepository.findByUsuarioOrderByFechaReservaDesc(usuario);
    }

    @Transactional
    public String cancelarReserva(Integer idReserva, String cedulaUsuario) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            return "error: Reserva no encontrada";
        }
        Reserva reserva = reservaOpt.get();

        if (!reserva.getUsuario().getCedula().toString().equals(cedulaUsuario)) {
            return "error: No tienes permiso para cancelar esta reserva";
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime claseDateTime = reserva.getFechaClase().atTime(reserva.getHoraInicio());
        long horasAnticipacion = java.time.Duration.between(ahora, claseDateTime).toHours();

        reserva.setEstado(horasAnticipacion >= 2 ? "Cancelada" : "Cancelada_Tarde");
        reserva.setCanceladaEn(ahora);

        if (horasAnticipacion >= 2) {
            PaqueteClase paquete = reserva.getPaquete();
            paquete.setClasesRestantes(paquete.getClasesRestantes() + 1);
            paqueteClaseRepository.save(paquete);
            reserva.setClaseDevuelta(true);
            // Ya no se modifica cupo de clase porque el cupo se basa en reservas activas
        }

        reservaRepository.save(reserva);
        return "ok";
    }
}