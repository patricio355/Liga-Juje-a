package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.*;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.EquipoZonaMapper;
import com.patricio.springboot.app.mapper.JugadorMapper;
import com.patricio.springboot.app.repository.*;
import org.springframework.stereotype.Service;
import com.patricio.springboot.app.mapper.EquipoMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class EquipoService {
    private EquipoRepository equipoRepository;
    private ZonaRepository zonaRepository;
    private CanchaRepository canchaRepository;
    private JugadorRepository jugadorRepository;
    private EquipoZonaRepository equipoZonaRepository;
    private UsuarioRepository usuarioRepository;

    public EquipoService(EquipoRepository equipoRepository, UsuarioRepository usuarioRepository , ZonaRepository zonaRepository, CanchaRepository canchaRepository , JugadorRepository jugadorRepository, EquipoZonaRepository equipoZonaRepository) {
        this.equipoRepository = equipoRepository;
        this.zonaRepository = zonaRepository;
        this.canchaRepository = canchaRepository;
        this.jugadorRepository = jugadorRepository;
        this.equipoZonaRepository = equipoZonaRepository;
        this.usuarioRepository = usuarioRepository;
    }


    public Equipo getEquipoById(Long idEquipo) {
        return equipoRepository.findById(idEquipo).orElseThrow( () -> new RuntimeException("equipo no encontrado"));
    }

    public List<EquipoDTO> listarEquipos() {

        return equipoRepository.findAll()
                .stream()
                .map(equipo -> {

                    // 1️⃣ Equipo básico
                    EquipoDTO dto = EquipoMapper.toDTO(equipo);

                    // 2️⃣ Buscar inscripciones (equipo_zona)
                    List<EquipoZonaDTO> inscripciones =
                            equipoZonaRepository
                                    .findByEquipoId(equipo.getId())
                                    .stream()
                                    .map(EquipoZonaMapper::toDTO)
                                    .toList();

                    // 3️⃣ Setearlas en el DTO
                    dto.setInscripciones(inscripciones);

                    return dto;
                })
                .toList();
    }

    public List<EquipoDTO> listarEquiposActivos() {

        return equipoRepository.findAllByEstado(true)
                .stream()
                .map(equipo -> {

                    EquipoDTO dto = EquipoMapper.toDTO(equipo);

                    List<EquipoZonaDTO> inscripciones =
                            equipoZonaRepository
                                    .findByEquipoId(equipo.getId())
                                    .stream()
                                    .map(EquipoZonaMapper::toDTO)
                                    .toList();

                    dto.setInscripciones(inscripciones);

                    return dto;
                })
                .toList();
    }



    public List<JugadorDTO> listarJugadores(Long idEquipo) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        return jugadorRepository.findByEquipoId(idEquipo)
                .stream()
                .map(JugadorMapper::toDTO)
                .collect(Collectors.toList());
    }


    public EquipoDTO crearEquipo(EquipoDTO dto) {

        Equipo equipo = EquipoMapper.toEntity(dto);
        equipo.setFechaCreacion(LocalDate.now());
        // ZONA


        // CANCHA
        if (dto.getCanchaId() != null) {
            Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                    .orElseThrow(() -> new RuntimeException("Cancha inexistente"));
            equipo.setLocalia(cancha);
        }



        if (dto.getEncargadoEmail()!=null) {


            Usuario encargado = usuarioRepository
                    .findByEmail(dto.getEncargadoEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no existe"));




            if (!encargado.getRol().equals("ENCARGADOEQUIPO")) {
                throw new RuntimeException("Este usuario no es un encargado");
            }

            if (equipoRepository.existsByEncargado(encargado)) {
                throw new RuntimeException("Este usuario ya es encargado de un equipo");
            }

            equipo.setEncargado(encargado);
        }








        Equipo guardado = equipoRepository.save(equipo);

        return EquipoMapper.toDTO(guardado);
    }

    public EquipoDTO eliminarEquipo(Long id) {
        Equipo eq = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no existe"));

        eq.setEstado(false);
        equipoRepository.save(eq);
        return EquipoMapper.toDTO(eq);
    }

    public EquipoDTO editarEquipo(Long id, EquipoDTO dto) {

        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // ============================
        // VALIDAR NOMBRE POR TORNEO
        // ============================
        List<Long> torneoIds =
                equipoZonaRepository.findTorneoIdsByEquipoId(id);

        for (Long torneoId : torneoIds) {
            if (equipoZonaRepository
                    .existsByNombreEquipoIgnoreCaseAndZona_Torneo_IdAndEquipo_IdNot(
                            dto.getNombre(),
                            torneoId,
                            id
                    )) {
                throw new RuntimeException(
                        "Ya existe un equipo con ese nombre en algun torneo perteneciente"
                );
            }
        }

        // ============================
        // MANEJO DEL ENCARGADO
        // ============================
        String email = dto.getEncargadoEmail();

        if (email == null || email.isBlank()) {

            equipo.setEncargado(null);

        } else {

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("No existe un usuario con ese email"));

            if (!"ENCARGADOEQUIPO".equals(usuario.getRol())) {
                throw new RuntimeException("El usuario no es un encargado");
            }

            boolean yaAsignado = equipoRepository
                    .existsByEncargadoAndIdNot(usuario, id);

            if (yaAsignado) {
                throw new RuntimeException("Este encargado ya está asignado a otro equipo");
            }

            equipo.setEncargado(usuario);
        }

        // ============================
        // RESTO DE CAMPOS
        // ============================
        equipo.setNombre(dto.getNombre());
        equipo.setLocalidad(dto.getLocalidad());
        equipo.setEscudo(dto.getEscudo());
        equipo.setEstado(dto.getEstado());

        // 💾 Guardar equipo
        equipoRepository.save(equipo);

        // ============================
        // 🔥 SINCRONIZAR EQUIPO_ZONA
        // ============================
        List<EquipoZona> participaciones =
                equipoZonaRepository.findByEquipoId(id);

        for (EquipoZona ez : participaciones) {
            ez.setNombreEquipo(dto.getNombre());
        }

        equipoZonaRepository.saveAll(participaciones);

        return EquipoMapper.toDTO(equipo);
    }



    public EquipoDTO asignarCancha(Long idEquipo, Long idCancha) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Cancha cancha = canchaRepository.findById(idCancha)
                .orElseThrow(() -> new RuntimeException("Cancha no encontrada"));

        equipo.setLocalia(cancha);

        Equipo actualizado = equipoRepository.save(equipo);
        return EquipoMapper.toDTO(actualizado);
    }
    public EquipoDTO asignarZona(Long idEquipo, Long idZona) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Zona zona = zonaRepository.findById(idZona)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));



        Equipo actualizado = equipoRepository.save(equipo);
        return EquipoMapper.toDTO(actualizado);
    }

    public EquipoDTO registrarJugador(Long idEquipo, Long idJugador) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));


        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // agregar jugador al equipo
        equipo.addJugador(jugador);
        jugador.setEquipo(equipo);

        jugadorRepository.save(jugador);

        Equipo actualizado = equipoRepository.save(equipo);

        return EquipoMapper.toDTO(actualizado);
    }

    public EquipoDTO eliminarJugador(Long idEquipo, Long idJugador) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // Validar que el jugador realmente pertenece a ese equipo
        if (jugador.getEquipo() == null || !jugador.getEquipo().getId().equals(idEquipo)) {
            throw new RuntimeException("El jugador no pertenece a este equipo");
        }

        // Remover desde ambos lados (MUY IMPORTANTE)
        equipo.getJugadores().remove(jugador);
        jugador.setEquipo(null);

        // Guardar cambios
        jugadorRepository.save(jugador);
        Equipo actualizado = equipoRepository.save(equipo);

        return EquipoMapper.toDTO(actualizado);
    }





}
