package com.company.reservasavianca;

public class Ventanilla extends Thread {
    private final Avion avion;
    private final Cliente cliente;
    private final String asientoSolicitado;
    private final int numeroVentanilla;

    public Ventanilla(int id, Avion avion, Cliente cliente, String asiento) {
        super("Ventanilla-" + id);
        this.numeroVentanilla = id;
        this.avion = avion;
        this.cliente = cliente;
        this.asientoSolicitado = asiento;
    }

    public int getNumeroVentanilla() {
        return numeroVentanilla;
    }

    @Override
    public void run() {
        avion.reservar(this, asientoSolicitado, cliente);
    }
}