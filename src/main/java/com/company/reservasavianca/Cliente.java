package com.company.reservasavianca;


public class Cliente {
    private final String cedula;
    private final String nombre;

    public Cliente(String cedula, String nombre) {
        this.cedula = cedula;
        this.nombre = nombre;
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre + " (" + cedula + ")"; }
}