package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.FixtureFechaDTO;
import com.patricio.springboot.app.dto.PartidoCreateDTO;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.PartidoMapper;
import com.patricio.springboot.app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartidoService {

    private final EquipoZonaRepository equipoZonaRepository;
    private final PartidoRepository partidoRepository;
    private final EquipoZonaService equipoZonaService;
    private final EquipoRepository equipoRepository;
    private final CanchaRepository canchaRepository;
    private final ZonaRepository zonaRepository;
    private final EstadisticaJugadorRepository estadisticaRepo;
    private final SolicitudCierrePartidoRepository solicitudRepo;
    private final ProgramacionFechaRepository programacionRepo;


    @Transactional
    @Caching(evict = {
            // Borra la memoria de la lista de partidos por zona (la cajita de la derecha)
            @CacheEvict(value = "partidosPorZona", allEntries = true),
            // Borra la memoria del fixture general (la pantalla grande)
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            // Limpia el dashboard general
            @CacheEvict(value = "dashboardTorneos", allEntries = true)
    })
    public Partido crearPartido(PartidoCreateDTO dto) {


        // VALIDAR EQUIPOS EN LA ZONA

        if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoLocalId(), dto.getZonaId()) == null) {
            throw new RuntimeException("El equipo local NO está inscripto en la zona.");
        }

        if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoVisitanteId(), dto.getZonaId()) == null) {
            throw new RuntimeException("El equipo visitante NO está inscripto en la zona.");
        }

        Partido partido = new Partido();

        partido.setEquipoLocal(
                equipoRepository.findById(dto.getEquipoLocalId())
                        .orElseThrow(() -> new RuntimeException("Equipo local no encontrado"))
        );

        partido.setEquipoVisitante(
                equipoRepository.findById(dto.getEquipoVisitanteId())
                        .orElseThrow(() -> new RuntimeException("Equipo visitante no encontrado"))
        );

        partido.setCancha(
                canchaRepository.findById(dto.getCanchaId())
                        .orElseThrow(() -> new RuntimeException("Cancha no encontrada"))
        );

        partido.setZona(
                zonaRepository.findById(dto.getZonaId())
                        .orElseThrow(() -> new RuntimeException("Zona no encontrada"))
        );

//        partido.setEtapa(
//                etapaRepository.findById(dto.getEtapaId())
//                        .orElseThrow(() -> new RuntimeException("Etapa no encontrada"))
//        );
//
//        partido.setArbitro(
//                arbitroRepository.findById(dto.getArbitroId())
//                        .orElseThrow(() -> new RuntimeException("Árbitro no encontrado"))
//        );

        partido.setFecha(LocalDate.parse(dto.getFecha()));
        partido.setVeedor(dto.getVeedor());
        partido.setEstado("PENDIENTE");
        partido.setNumeroFecha(dto.getNumeroFecha());
        partido.setFechaHora(dto.getFechaHora());

        return partidoRepository.save(partido);
    }


    // LÓGICA FUTBOLÍSTICA DE SUMA DE PUNTOS

    private void actualizarStats(EquipoZona ez, int golesHechos, int golesRecibidos) {

        ez.setPartidosJugados(ez.getPartidosJugados() + 1);
        ez.setGolesAFavor(ez.getGolesAFavor() + golesHechos);
        ez.setGolesEnContra(ez.getGolesEnContra() + golesRecibidos);

        if (golesHechos > golesRecibidos) {
            ez.setGanados(ez.getGanados() + 1);
            ez.setPuntos(ez.getPuntos() + 3);
        } else if (golesHechos == golesRecibidos) {
            ez.setEmpatados(ez.getEmpatados() + 1);
            ez.setPuntos(ez.getPuntos() + 1);
        } else {
            ez.setPerdidos(ez.getPerdidos() + 1);
        }
    }




    public List<FixtureFechaDTO> obtenerFixturePorZona(Long zonaId) {

        List<Partido> partidos =
                partidoRepository.findByZonaIdOrderByNumeroFechaAscFechaHoraAsc(zonaId);

        return partidos.stream()
                .collect(Collectors.groupingBy(
                        Partido::getNumeroFecha,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> new FixtureFechaDTO(
                        entry.getKey(),
                        entry.getValue()
                                .stream()
                                .map(PartidoMapper::toDTO)
                                .toList()
                ))
                .toList();
    }
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "torneoDetalle", allEntries = true), // Limpia todos los detalles
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true)
    })
    public void generarFixtureInicialZona(Long zonaId) {

        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        List<Equipo> equipos = zona.getEquiposZona()
                .stream()
                .map(EquipoZona::getEquipo)
                .distinct()
                .toList();

        if (equipos.size() < 2) return;

        // no permitir regenerar
        if (partidoRepository.existsByZonaId(zonaId)) {
            throw new RuntimeException("El fixture ya fue generado");
        }

        List<Equipo> lista = new ArrayList<>(equipos);

        boolean impar = lista.size() % 2 != 0;
        if (impar) lista.add(null); // BYE

        int n = lista.size();
        int rondas = n - 1;

        for (int ronda = 0; ronda < rondas; ronda++) {

            int numeroFecha = ronda + 1;

            for (int i = 0; i < n / 2; i++) {
                Equipo a = lista.get(i);
                Equipo b = lista.get(n - 1 - i);

                if (a == null || b == null) continue;

                Equipo local = (ronda % 2 == 0) ? a : b;
                Equipo visitante = (ronda % 2 == 0) ? b : a;

                Partido p = new Partido();
                p.setZona(zona);
                p.setEquipoLocal(local);
                p.setEquipoVisitante(visitante);
                p.setNumeroFecha(numeroFecha);
                p.setEstado("PENDIENTE");

                partidoRepository.save(p);
            }

            // rotación clásica
            Equipo ultimo = lista.remove(lista.size() - 1);
            lista.add(1, ultimo);
        }
    }





    @Transactional
    public void regenerarFixtureZona(Long zonaId) {

        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        List<Equipo> equipos = zona.getEquiposZona()
                .stream()
                .map(EquipoZona::getEquipo)
                .toList();

        if (equipos.size() < 2) return;

        // 🔹 Última fecha existente
        Integer ultimaFecha =
                partidoRepository.findMaxNumeroFechaByZonaId(zonaId)
                        .orElse(0);

        generarPartidosFaltantes(zona, equipos, ultimaFecha + 1);
    }
    private void generarPartidosFaltantes(
            Zona zona,
            List<Equipo> equipos,
            int fechaInicial
    ) {
        List<Equipo> lista = new ArrayList<>(equipos);

        if (lista.size() % 2 != 0) lista.add(null);

        int n = lista.size();
        int rondas = n - 1;
        int fecha = fechaInicial;

        for (int ronda = 0; ronda < rondas; ronda++) {

            boolean creoAlgoEnFecha = false;

            for (int i = 0; i < n / 2; i++) {
                Equipo a = lista.get(i);
                Equipo b = lista.get(n - 1 - i);

                if (a == null || b == null) continue;

                // 🔹 Si ya existe partido entre A y B → no crear
                boolean existe =
                        partidoRepository.existsEntreEquipos(
                                zona.getId(),
                                a.getId(),
                                b.getId()
                        );

                if (existe) continue;

                Partido p = new Partido();
                p.setZona(zona);
                p.setEquipoLocal(a);
                p.setEquipoVisitante(b);
                p.setNumeroFecha(fecha);
                p.setEstado("PENDIENTE");

                partidoRepository.save(p);
                creoAlgoEnFecha = true;
            }

            if (creoAlgoEnFecha) {
                fecha++;
            }

            // rotación round-robin
            Equipo ultimo = lista.remove(lista.size() - 1);
            lista.add(1, ultimo);
        }
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "programacion", allEntries = true),
            @CacheEvict(value = "torneos", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true) // Si tenés cacheada la tabla
    })
    public void cerrarPartidoDirecto(
            Long partidoId,
            Integer golesLocal,
            Integer golesVisitante
    ) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        // 🔒 Evitar cerrar dos veces
        if ("FINALIZADO".equals(partido.getEstado())) {
            throw new RuntimeException("El partido ya está cerrado");
        }

        //  Validación opcional (recomendada)
        // Si hubo goles, deben existir estadísticas
//        int totalGoles = golesLocal + golesVisitante;
//        if (totalGoles > 0 && partido.getEstadisticas().isEmpty()) {
//            throw new RuntimeException("No hay estadísticas cargadas");
//        }

        // ===============================
        // 🏁 CERRAR PARTIDO
        // ===============================
        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);
        partido.setEstado("FINALIZADO");

        if (golesLocal > golesVisitante) {
            partido.setGanador(partido.getEquipoLocal());
        } else if (golesVisitante > golesLocal) {
            partido.setGanador(partido.getEquipoVisitante());
        } else {
            partido.setGanador(null); // empate
        }



        ProgramacionFecha programacionFecha = programacionRepo
                .findByPartidoId(partido.getId())
                .orElseThrow(()->
                        new RuntimeException("Programacion no encontrada"));

        programacionFecha.setEstado("FINALIZADO");


        // ===============================
        // ACTUALIZAR EQUIPO_ZONA
        // ===============================

        EquipoZona ezLocal = equipoZonaRepository
                .findByZonaIdAndEquipoId(
                        partido.getZona().getId(),
                        partido.getEquipoLocal().getId()
                ).orElseThrow(() ->
                        new RuntimeException("Equipo local no pertenece a la zona")
                );

        EquipoZona ezVisitante = equipoZonaRepository
                .findByZonaIdAndEquipoId(
                        partido.getZona().getId(),
                        partido.getEquipoVisitante().getId()
                ).orElseThrow(() ->
                        new RuntimeException("Equipo visitante no pertenece a la zona")
                );

        // Partidos jugados
        ezLocal.setPartidosJugados(ezLocal.getPartidosJugados() + 1);
        ezVisitante.setPartidosJugados(ezVisitante.getPartidosJugados() + 1);

        // Goles
        ezLocal.setGolesAFavor(ezLocal.getGolesAFavor() + golesLocal);
        ezLocal.setGolesEnContra(ezLocal.getGolesEnContra() + golesVisitante);

        ezVisitante.setGolesAFavor(ezVisitante.getGolesAFavor() + golesVisitante);
        ezVisitante.setGolesEnContra(ezVisitante.getGolesEnContra() + golesLocal);

        // Resultado
        if (golesLocal > golesVisitante) {

            ezLocal.setGanados(ezLocal.getGanados() + 1);
            ezLocal.setPuntos(ezLocal.getPuntos() + 3);

            ezVisitante.setPerdidos(ezVisitante.getPerdidos() + 1);

        } else if (golesVisitante > golesLocal) {

            ezVisitante.setGanados(ezVisitante.getGanados() + 1);
            ezVisitante.setPuntos(ezVisitante.getPuntos() + 3);

            ezLocal.setPerdidos(ezLocal.getPerdidos() + 1);

        } else {
            // EMPATE
            ezLocal.setEmpatados(ezLocal.getEmpatados() + 1);
            ezVisitante.setEmpatados(ezVisitante.getEmpatados() + 1);

            ezLocal.setPuntos(ezLocal.getPuntos() + 1);
            ezVisitante.setPuntos(ezVisitante.getPuntos() + 1);
        }

        // ===============================
        // 💾 GUARDAR TODO
        // ===============================
        equipoZonaRepository.save(ezLocal);
        equipoZonaRepository.save(ezVisitante);
        partidoRepository.save(partido);
    }


    @Transactional
    public void solicitarCierre(
            Long partidoId,
            Usuario solicitante,
            Integer golesLocal,
            Integer golesVisitante
    ) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow();

        if (solicitudRepo.existsByPartidoAndEstado(
                partido, "PENDIENTE")) {
            throw new RuntimeException("Ya existe una solicitud pendiente");
        }

        SolicitudCierrePartido solicitud =
                new SolicitudCierrePartido();

        solicitud.setPartido(partido);
        solicitud.setSolicitante(solicitante);
        solicitud.setGolesLocal(golesLocal);
        solicitud.setGolesVisitante(golesVisitante);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());

        solicitudRepo.save(solicitud);
    }

    @Transactional
    public void aprobarSolicitud(Long solicitudId) {
        SolicitudCierrePartido solicitud =
                solicitudRepo.findById(solicitudId).orElseThrow();

        cerrarPartidoDirecto(
                solicitud.getPartido().getId(),
                solicitud.getGolesLocal(),
                solicitud.getGolesVisitante()
        );

        solicitud.setEstado("APROBADA");
        solicitudRepo.save(solicitud);
    }

    @Transactional
    public void rechazarSolicitud(Long solicitudId) {
        SolicitudCierrePartido solicitud =
                solicitudRepo.findById(solicitudId).orElseThrow();

        solicitud.setEstado("RECHAZADA");
        solicitudRepo.save(solicitud);
    }

    public List<Integer> obtenerFechasConPartidos(Long zonaId) {
        // Ahora solo devolverá [1, 2] si solo hay partidos en esas fechas
        return partidoRepository.findDistinctNumeroFechaByZonaId(zonaId);
    }

}
