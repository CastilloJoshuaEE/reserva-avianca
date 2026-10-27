package com.company.reservasavianca;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Avion {
    public static final int FILAS = 6;
    public static final int COLS = 5; // 30 asientos => 6x5

    private final Asiento[][] avion = new Asiento[FILAS][COLS];
    private final ReentrantLock candado = new ReentrantLock(true); // justo (FIFO)
    private volatile boolean modoExclusion = true;
    private final ListaEspera listaEspera = new ListaEspera();

    // Bitácora de eventos para mostrar en pantalla
    private final List<String> bitacora = Collections.synchronizedList(new ArrayList<>());
    private final List<String> alertas  = Collections.synchronizedList(new ArrayList<>());

    public Avion() { inicializar(); }

    public void inicializar() {
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLS; c++) {
                String codigo = String.valueOf((char) ('A' + f)) + (c + 1);
                avion[f][c] = new Asiento(codigo);
            }
        }
        bitacora.clear();
        alertas.clear();
        listaEspera.limpiar();
    }

    public ListaEspera getListaEspera() { return listaEspera; }
    public void setModoExclusion(boolean b) { this.modoExclusion = b; }
    public boolean isModoExclusion() { return modoExclusion; }

    public synchronized void log(String s) {
        bitacora.add(s);
        System.out.println(s);
    }
    public synchronized void alerta(String s) {
        alertas.add(s);
        System.out.println("[ALERTA] " + s);
    }
    public List<String> getBitacora() { return new ArrayList<>(bitacora); }
    public List<String> getAlertas()  { return new ArrayList<>(alertas); }

    public Asiento buscar(String codigo) {
        for (int f = 0; f < FILAS; f++)
            for (int c = 0; c < COLS; c++)
                if (avion[f][c].getCodigo().equalsIgnoreCase(codigo))
                    return avion[f][c];
        return null;
    }

    /**
     * Reserva concurrente. Aquí está el corazón del ejemplo:
     *  - Con exclusión: candado.lock()/unlock() rodean verificación + asignación.
     *  - Sin exclusión: la verificación y la asignación están separadas por sleep,
     *    lo que provoca condiciones de carrera (doble reserva).
     */
    public void reservar(Ventanilla v, String codigo, Cliente cliente) {
    Asiento a = buscar(codigo);
    if (a == null) {
        log("Ventanilla " + v.getNumeroVentanilla() + ": asiento inválido " + codigo);
        return;
    }

    log("Ventanilla " + v.getNumeroVentanilla() + " intenta reservar " + codigo
            + " para " + cliente.getNombre());

    if (modoExclusion) candado.lock();
    try {
        boolean libre = !a.isOcupado();
        if (!libre) {
            listaEspera.encolar(codigo, cliente);
            log("Ventanilla " + v.getNumeroVentanilla() + ": asiento " + codigo
                    + " OCUPADO. " + cliente.getNombre() + " en lista de espera.");
            log("   Lista de espera [" + codigo + "]: "
                    + listaEspera.snapshot(codigo));
            return;
        }

        try { Thread.sleep(1000 + new Random().nextInt(2000)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        if (!modoExclusion && a.isOcupado()) {
            alerta("CONDICIÓN DE CARRERA en asiento " + codigo
                    + ": " + cliente.getNombre()
                    + " encontró ocupado por " + a.getDueno());
        }
        a.ocupar(cliente);
        log("Ventanilla " + v.getNumeroVentanilla() + " RESERVA OK: " + codigo
                + " -> " + cliente.getNombre());
    } finally {
        if (modoExclusion) candado.unlock();
    }
}
    public void cancelar(String codigo, Cliente cliente) {
        Asiento a = buscar(codigo);
        if (a == null) return;
        if (modoExclusion) candado.lock();
        try {
            if (a.isOcupado() && a.getDueno().getCedula().equals(cliente.getCedula())) {
                a.liberar();
                log("CANCELADA reserva de " + cliente.getNombre() + " en " + codigo);
                promoverLista(codigo);
            } else {
                log("No se puede cancelar: " + cliente.getNombre()
                        + " no posee el asiento " + codigo);
            }
        } finally {
            if (modoExclusion) candado.unlock();
        }
    }

    public void cambiar(String actual, String nuevo, Cliente cliente) {
        if (modoExclusion) candado.lock();
        try {
            Asiento aAct = buscar(actual);
            Asiento aNue = buscar(nuevo);
            if (aAct == null || aNue == null) return;
            if (aAct.isOcupado() && aAct.getDueno().getCedula().equals(cliente.getCedula())
                    && !aNue.isOcupado()) {
                aAct.liberar();
                aNue.ocupar(cliente);
                log("CAMBIO OK: " + cliente.getNombre() + " de " + actual + " a " + nuevo);
                promoverLista(actual);
            } else {
                log("Cambio rechazado para " + cliente.getNombre()
                        + " (" + actual + " -> " + nuevo + ")");
            }
        } finally {
            if (modoExclusion) candado.unlock();
        }
    }

    private void promoverLista(String codigo) {
        Cliente sig = listaEspera.desencolar(codigo);
        if (sig != null) {
            Asiento a = buscar(codigo);
            a.ocupar(sig);
            log("PROMOCIÓN desde lista de espera: " + sig.getNombre()
                    + " obtiene " + codigo);
            log("   Lista de espera restante [" + codigo + "]: "
                    + listaEspera.snapshot(codigo));
        }
    }

    public Map<String, String> estadoMapa() {
        Map<String, String> m = new LinkedHashMap<>();
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLS; c++) {
                Asiento a = avion[f][c];
                String txt = a.getCodigo() + ": "
                        + (a.isOcupado() ? a.getDueno().getNombre() : "LIBRE");
                m.put(a.getCodigo(), txt);
            }
        }
        return m;
    }
}