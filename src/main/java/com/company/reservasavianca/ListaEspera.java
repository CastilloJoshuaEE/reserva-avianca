package com.company.reservasavianca;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cola FIFO de clientes en espera por cada asiento.
 * Se usa ConcurrentHashMap + LinkedList sincronizada para poder
 * mostrar el orden de los hilos que esperan acceder al recurso.
 */
public class ListaEspera {
    private final Map<String, Deque<Cliente>> colas = new ConcurrentHashMap<>();

    public synchronized void encolar(String codigoAsiento, Cliente cliente) {
        Deque<Cliente> cola = colas.computeIfAbsent(codigoAsiento,
                k -> new LinkedList<>());
        // Evitar duplicados del mismo cliente en la misma cola
        for (Cliente c : cola) {
            if (c.getCedula().equals(cliente.getCedula())) return;
        }
        cola.addLast(cliente);
    }

    public synchronized Cliente desencolar(String codigoAsiento) {
        Deque<Cliente> cola = colas.get(codigoAsiento);
        if (cola == null || cola.isEmpty()) return null;
        return cola.pollFirst(); // FIFO
    }

    public synchronized int tamano(String codigoAsiento) {
        Deque<Cliente> cola = colas.get(codigoAsiento);
        return cola == null ? 0 : cola.size();
    }

    /** Devuelve una vista legible de la cola (para mostrarla en UI). */
    public synchronized String snapshot(String codigoAsiento) {
        Deque<Cliente> cola = colas.get(codigoAsiento);
        if (cola == null || cola.isEmpty()) return "(vacía)";
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Cliente c : cola) {
            sb.append(i++).append(". ").append(c.getNombre())
              .append("  ");
        }
        return sb.toString();
    }

    public synchronized void limpiar() { colas.clear(); }

    public synchronized Map<String, String> snapshotTodo() {
        Map<String, String> m = new LinkedHashMap<>();
        for (String k : new TreeSet<>(colas.keySet())) {
            m.put(k, snapshot(k));
        }
        return m;
    }
}