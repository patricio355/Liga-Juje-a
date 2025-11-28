package com.patricio.springboot.app.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue(value = "Encargado")
@Getter
@Setter
@NoArgsConstructor
public class Encargado extends Usuario{
}
