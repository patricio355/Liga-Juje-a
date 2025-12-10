package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.CanchaDTO;
import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.dto.JugadorDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.JugadorMapper;
import com.patricio.springboot.app.repository.CanchaRepository;
import com.patricio.springboot.app.repository.EquipoRepository;
import com.patricio.springboot.app.repository.JugadorRepository;
import com.patricio.springboot.app.repository.ZonaRepository;
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

    public EquipoService(EquipoRepository equipoRepository , ZonaRepository zonaRepository, CanchaRepository canchaRepository , JugadorRepository jugadorRepository) {
        this.equipoRepository = equipoRepository;
        this.zonaRepository = zonaRepository;
        this.canchaRepository = canchaRepository;
        this.jugadorRepository = jugadorRepository;
    }


    public Equipo getEquipoById(Long idEquipo) {
        return equipoRepository.findById(idEquipo).orElseThrow( () -> new RuntimeException("equipo no encontrado"));
    }

    public List<EquipoDTO> listarEquipos() {
        return equipoRepository.findAll()
                .stream()
                .map(EquipoMapper::toDTO)
                .toList();
    }

    public List<EquipoDTO> listarEquiposActivos() {
        return equipoRepository.findAllByEstado(true)
                .stream()
                .map(EquipoMapper::toDTO)
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

        // ENCARGADO
//        if (dto.getEncargadoId() != null) {
//            Encargado encargado = encargadoRepository.findById(dto.getEncargadoId())
//                    .orElseThrow(() -> new RuntimeException("Encargado inexistente"));
            equipo.setEncargado(null);
//        }



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
                .orElseThrow(() -> new RuntimeException("Equipo no existe"));

        equipo.setNombre(dto.getNombre());
        equipo.setCamisetaSuplente(dto.getCamisetaSuplente());
        equipo.setCamisetaTitular(dto.getCamisetaTitular());
        equipo.setEscudo(dto.getEscudo());
        equipo.setLocalia(equipo.getLocalia());
        equipo.setEncargado(equipo.getEncargado());
        equipoRepository.save(equipo);
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
