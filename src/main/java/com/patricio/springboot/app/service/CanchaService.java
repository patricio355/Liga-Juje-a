package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.CanchaDTO;
import com.patricio.springboot.app.entity.Cancha;
import com.patricio.springboot.app.mapper.CanchaMapper;
import com.patricio.springboot.app.repository.CanchaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CanchaService {
    private CanchaRepository canchaRepository;


    public CanchaService(CanchaRepository canchaRepository)
    {
        this.canchaRepository=canchaRepository;
    }


    public Optional<CanchaDTO> buscarForID(Long id){
        return canchaRepository.findById(id).map(CanchaMapper::toDTO);
    }



    public List<CanchaDTO> listarCanchas(){
        return canchaRepository.findAll().stream().map(CanchaMapper::toDTO).toList();
    }

    public CanchaDTO crearCancha(CanchaDTO dto){
        Cancha cancha = CanchaMapper.toEntity(dto);
        Cancha guardado = canchaRepository.save(cancha);
        return CanchaMapper.toDTO(guardado);
    }

}
