package com.patricio.springboot.app.entity;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue(value = "ENCARGADOTORNEO")
@Getter
@Setter
@NoArgsConstructor
public class EncargadoTorneo extends Usuario {
}
