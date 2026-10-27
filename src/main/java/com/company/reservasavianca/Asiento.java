package com.company.reservasavianca;

public class Asiento {
    private final String codigo;
    private boolean ocupado;
    private Cliente dueno;

    public Asiento(String codigo) {
        this.codigo = codigo;
        this.ocupado = false;
        this.dueno = null;
    }

    public String getCodigo() { return codigo; }
    public boolean isOcupado() { return ocupado; }
    public Cliente getDueno() { return dueno; }

    public void ocupar(Cliente c) {
        this.ocupado = true;
        this.dueno = c;
    }

    public void liberar() {
        this.ocupado = false;
        this.dueno = null;
    }
}