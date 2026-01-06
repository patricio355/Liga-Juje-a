package com.patricio.springboot.app.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue(value = "VEEDOR")
@Getter
@Setter
@NoArgsConstructor
public class Veedor extends Usuario {
}
