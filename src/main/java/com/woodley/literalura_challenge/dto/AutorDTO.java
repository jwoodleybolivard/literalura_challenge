package com.woodley.literalura_challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutorDTO {
    @JsonProperty("name")
    private String nombre;
    @JsonProperty("birth_year")
    private String anoNacimiento;
    @JsonProperty("death_year")
    private String anoFallecimiento;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAnoNacimiento() {
        return anoNacimiento;
    }

    public void setAnoNacimiento(String anoNacimiento) {
        this.anoNacimiento = anoNacimiento;
    }

    public String getAnoFallecimiento() {
        return anoFallecimiento;
    }

    public void setAnoFallecimiento(String anoFallecimiento) {
        this.anoFallecimiento = anoFallecimiento;
    }
}
