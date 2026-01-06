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
    private final EquipoRepository equipoRepository;
    private final CanchaRepository canchaRepository;
    private final ZonaRepository zonaRepository;
    private final SolicitudCierrePartidoRepository solicitudRepo;
    private final ProgramacionFechaRepository programacionRepo;
    private final UsuarioRepository usuarioRepository;


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

        // 1. VALIDAR EQUIPOS EN LA ZONA
        if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoLocalId(), dto.getZonaId()) == null) {
            throw new RuntimeException("El equipo local NO está inscripto en la zona.");
        }

        if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoVisitanteId(), dto.getZonaId()) == null) {
            throw new RuntimeException("El equipo visitante NO está inscripto en la zona.");
        }

        // 2. CONSTRUIR ENTIDAD PARTIDO
        Partido partido = new Partido();

        partido.setEquipoLocal(
                equipoRepository.findById(dto.getEquipoLocalId())
                        .orElseThrow(() -> new RuntimeException("Equipo local no encontrado"))
        );

        partido.setEquipoVisitante(
                equipoRepository.findById(dto.getEquipoVisitanteId())
                        .orElseThrow(() -> new RuntimeException("Equipo visitante no encontrado"))
        );

        Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                .orElseThrow(() -> new RuntimeException("Cancha no encontrada"));
        partido.setCancha(cancha);

        Zona zona = zonaRepository.findById(dto.getZonaId())
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));
        partido.setZona(zona);

        // Parseo de los nuevos campos String del DTO
        if (dto.getFecha() != null) {
            partido.setFecha(java.time.LocalDate.parse(dto.getFecha()));
        }

        if (dto.getHora() != null) {
            partido.setHora(java.time.LocalTime.parse(dto.getHora()));
        }

        partido.setVeedor(dto.getVeedor());
        partido.setEstado("PENDIENTE");
        partido.setNumeroFecha(dto.getNumeroFecha());

        // Guardar Partido
        Partido partidoGuardado = partidoRepository.save(partido);

        // 3. CREAR REGISTRO EN PROGRAMACION_FECHA
        // Esto asegura que el partido aparezca en el calendario de gestión y público
        ProgramacionFecha pf = new ProgramacionFecha();
        pf.setZona(zona);
        pf.setPartido(partidoGuardado);
        pf.setNumeroFecha(dto.getNumeroFecha());
        pf.setFecha(partidoGuardado.getFecha());
        pf.setHora(partidoGuardado.getHora());
        pf.setCancha(cancha);
        pf.setEstado("PROGRAMADO"); // Estado inicial para el calendario

        programacionRepo.save(pf);

        return partidoGuardado;
    }



    public List<FixtureFechaDTO> obtenerFixturePorZona(Long zonaId) {

        List<Partido> partidos =
                partidoRepository.findByZonaIdOrderByNumeroFechaAscFechaAscHoraAsc(zonaId);

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

        if (partidoRepository.existsByZonaId(zonaId)) {
            throw new RuntimeException("El fixture ya fue generado");
        }

        List<Equipo> lista = new ArrayList<>(equipos);
        boolean impar = lista.size() % 2 != 0;
        if (impar) lista.add(null);

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

                // 1. Crear y guardar el Partido
                Partido p = new Partido();
                p.setZona(zona);
                p.setEquipoLocal(local);
                p.setEquipoVisitante(visitante);
                p.setNumeroFecha(numeroFecha);
                p.setEstado("PENDIENTE");


                Partido partidoGuardado = partidoRepository.save(p);

                // 2. Crear y guardar la Programación Automática
                ProgramacionFecha pf = new ProgramacionFecha();
                pf.setZona(zona);
                pf.setNumeroFecha(numeroFecha);
                pf.setPartido(partidoGuardado);
                pf.setEstado("PROGRAMADO"); // Estado inicial

                // Estos quedan nulos hasta que se editen en el panel de gestión
                pf.setFecha(null);
                pf.setHora(null);
                pf.setCancha(null);

                programacionRepo.save(pf);
            }

            // Rotación clásica Round Robin
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


    @Transactional
    @Caching(evict = {
            // Limpia la programación de la zona/fecha específica
            @CacheEvict(value = "programacion", allEntries = true),
            // Limpia el fixture visual para que se vea la nueva cancha/hora
            @CacheEvict(value = "torneoDetalle", allEntries = true)
    })
    public void actualizarDetallesProgramacion(
            Long partidoId,
            String fechaStr, // Recibimos como String para parsear
            String horaStr,  // Recibimos como String para parsear
            String canchaNombre,
            String arbitro
    ) {
        // 1. Buscamos el registro en la tabla de programación
        ProgramacionFecha pf = programacionRepo.findByPartidoId(partidoId)
                .orElseThrow(() -> new RuntimeException("Este partido aún no ha sido programado en una fecha"));

        // Obtenemos la entidad Partido vinculada
        Partido partido = pf.getPartido();

        // 2. Procesar FECHA (LocalDate)
        if (fechaStr != null && !fechaStr.isEmpty()) {
            java.time.LocalDate fecha = java.time.LocalDate.parse(fechaStr);
            pf.setFecha(fecha);       // Guarda en programacion_fecha
            partido.setFecha(fecha);  // Guarda en partido
        }

        // 3. Procesar HORA (LocalTime)
        if (horaStr != null && !horaStr.isEmpty()) {
            java.time.LocalTime hora = java.time.LocalTime.parse(horaStr);
            pf.setHora(hora);         // Guarda en programacion_fecha
            partido.setHora(hora);    // Guarda en partido
        }

        // 4. Actualizar Cancha
        if (canchaNombre != null && !canchaNombre.isEmpty()) {
            Cancha cancha = canchaRepository.findByNombre(canchaNombre)
                    .orElseGet(() -> {
                        Cancha nueva = new Cancha();
                        nueva.setNombre(canchaNombre);
                        nueva.setEstado(true);
                        return canchaRepository.save(nueva);
                    });
            pf.setCancha(cancha);
            partido.setCancha(cancha);
        }

        // 5. Actualizar Árbitro
        if (arbitro != null && !arbitro.isBlank()) {
            Usuario user = usuarioRepository.findByNombre(arbitro)
                    .orElseThrow(() -> new RuntimeException("El árbitro seleccionado no existe"));

            // Asumiendo que Arbitro extiende de Usuario o usa su ID
            Arbitro realArbitro = new Arbitro();
            realArbitro.setId(user.getId());
            partido.setArbitro(realArbitro);
        }

        // 6. Persistencia final en ambas tablas
        programacionRepo.save(pf);
        partidoRepository.save(partido);
    }

}
