package com.patricio.springboot.app.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@DiscriminatorValue(value = "ENCARGADOEQUIPO")
@Getter
@Setter
@NoArgsConstructor
public class EncargadoEquipo extends Usuario{
}
