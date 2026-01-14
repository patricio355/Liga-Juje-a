package com.patricio.springboot.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "etapa_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtapaTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Integer orden;

    @ManyToOne
    @JoinColumn(name = "torneo_id")
    @JsonIgnore
    private Torneo torneo;


    private String tipo;  // GRUPOS, ELIMINACION, FINAL, ETC.

    @OneToMany(mappedBy = "etapa", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Partido> partidos;
}
