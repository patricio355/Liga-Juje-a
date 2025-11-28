package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("eliminacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidoEliminacion extends Partido {

    @ManyToOne
    @JoinColumn(name = "perdedor_id")
    private Equipo perdedor;

}

