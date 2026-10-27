package com.company.reservasavianca;

import java.util.List;

public class ResultadoSimulacion {
    public final boolean conExclusion;
    public final List<String> bitacora;
    public final List<String> alertas;
    public final java.util.Map<String, String> listaEspera;

    public ResultadoSimulacion(boolean conExclusion, List<String> b,
                               List<String> a, java.util.Map<String, String> le) {
        this.conExclusion = conExclusion;
        this.bitacora = b;
        this.alertas = a;
        this.listaEspera = le;
    }
}