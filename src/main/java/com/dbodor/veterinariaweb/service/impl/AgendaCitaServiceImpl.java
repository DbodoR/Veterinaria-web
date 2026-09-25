package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.dto.CitaForm;
import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.model.Veterinario;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.MascotaRepository;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.repository.VeterinarioRepository;
import com.dbodor.veterinariaweb.service.AgendaCitaService;
import com.dbodor.veterinariaweb.service.CitaService;
import com.dbodor.veterinariaweb.service.ServicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementacion del agendamiento (HU-25).
 *
 * El control de solapamiento no se repite aqui: se delega en
 * CitaService.agendarCita, que ya lo resuelve (HU-20 CA3).
 */
@Service
public class AgendaCitaServiceImpl implements AgendaCitaService {

    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ServicioRepository servicioRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final CitaRepository citaRepository;
    private final ServicioService servicioService;
    private final CitaService citaService;

    public AgendaCitaServiceImpl(UsuarioRepository usuarioRepository,
                                 MascotaRepository mascotaRepository,
                                 ServicioRepository servicioRepository,
                                 VeterinarioRepository veterinarioRepository,
                                 CitaRepository citaRepository,
                                 ServicioService servicioService,
                                 CitaService citaService) {
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.servicioRepository = servicioRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.citaRepository = citaRepository;
        this.servicioService = servicioService;
        this.citaService = citaService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> clientesActivos() {
        return usuarioRepository.buscarPorRol(RolUsuario.CLIENTE, "").stream()
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> mascotasActivas(Long idCliente) {
        if (idCliente == null) {
            return List.of();
        }
        return mascotaRepository.porDuenoYEstado(idCliente, MASCOTA_ACTIVA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> serviciosActivos() {
        return servicioService.listarServiciosParaCliente();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Veterinario> veterinariosActivos() {
        return veterinarioRepository.porEstado(EstadoUsuario.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listar() {
        return citaRepository.listarConDetalle();
    }

    @Override
    @Transactional(readOnly = true)
    public Long duenoDeMascota(Long idMascota) {
        if (idMascota == null) {
            return null;
        }
        return mascotaRepository.findById(idMascota)
                .map(m -> m.getUsuario().getIdUsuario())
                .orElse(null);
    }

    @Override
    @Transactional
    public Cita agendar(CitaForm form) {
        Mascota mascota = mascotaRepository.findById(form.getIdMascota())
                .orElseThrow(() -> new IllegalArgumentException("La mascota seleccionada no existe"));
        Servicio servicio = servicioRepository.findById(form.getIdServicio())
                .orElseThrow(() -> new IllegalArgumentException("El servicio seleccionado no existe"));
        Veterinario veterinario = veterinarioRepository.findById(form.getIdVeterinario())
                .orElseThrow(() -> new IllegalArgumentException("El veterinario seleccionado no existe"));

        // CA1 y CA2: aunque el formulario solo ofrece opciones validas, los
        // identificadores pueden llegar manipulados, asi que se revalidan.
        if (servicio.getEstado() != EstadoServicio.ACTIVO) {
            throw new IllegalArgumentException("El servicio seleccionado no esta disponible");
        }
        if (!veterinario.estaActivo()) {
            throw new IllegalArgumentException("El veterinario seleccionado no esta disponible");
        }
        if (!MASCOTA_ACTIVA.equals(mascota.getEstado())) {
            throw new IllegalArgumentException("La mascota seleccionada no esta activa");
        }

        // CA5: no se agenda en el pasado.
        if (LocalDateTime.of(form.getFecha(), form.getHora()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha y hora de la cita ya pasaron");
        }

        // CA6: la cita completa debe caber en la jornada del veterinario.
        LocalTime inicio = form.getHora();
        LocalTime fin = inicio.plusMinutes(servicio.getDuracionMinutos());
        boolean cruzaMedianoche = fin.isBefore(inicio);
        if (inicio.isBefore(veterinario.getHoraInicio())
                || fin.isAfter(veterinario.getHoraFin())
                || cruzaMedianoche) {
            throw new IllegalArgumentException("La cita queda fuera del horario del veterinario");
        }

        // CA3: el valor se congela al agendar, para que un cambio posterior
        // de precio o tarifa no altere las citas ya registradas (HU-17).
        double valor = servicio.getPrecioBase()
                + (veterinario.getTarifa() == null ? 0.0 : veterinario.getTarifa().doubleValue());

        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setServicio(servicio);
        cita.setVeterinario(veterinario);
        cita.setFechaCita(form.getFecha());
        cita.setHoraCita(inicio);
        cita.setCostoTotal(valor);
        cita.setEstado(ESTADO_PROGRAMADA);
        cita.setCreatedAt(LocalDateTime.now());

        // CA4: el solapamiento lo controla CitaService y lanza IllegalStateException.
        return citaService.agendarCita(cita);
    }
}