package com.company.reservasavianca;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final Avion avion = new Avion();
    private final List<Cliente> clientes = new ArrayList<>();

    // Componentes
    private JTextField txtCedula, txtNombre;
    private JComboBox<String> cbClientes, cbAsientosReserva, cbAsientosCancela,
            cbAsientosCambiaActual, cbAsientosCambiaNuevo;
    private JTextArea areaBitacora, areaAlertas;
    private JTextArea areaListaEspera;
    private JPanel panelAvion;
    private JLabel lblModo;
    private JComboBox<String> cbVentanillaReserva;

    public MainFrame() {
        super("Avianca - Reservas concurrentes (Grupo 4)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // ==== Panel superior: registro cliente ====
        JPanel panelReg = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelReg.setBorder(new TitledBorder("Registrar cliente"));
        txtCedula = new JTextField(10);
        txtNombre = new JTextField(14);
        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrarCliente());
        panelReg.add(new JLabel("Cédula:"));   panelReg.add(txtCedula);
        panelReg.add(new JLabel("Nombre:"));   panelReg.add(txtNombre);
        panelReg.add(btnRegistrar);

        // ==== Centro: mapa del avión + operaciones ====
        panelAvion = new JPanel(new GridLayout(Avion.FILAS, Avion.COLS, 4, 4));
        panelAvion.setBorder(new TitledBorder("Mapa del avión"));
        dibujarAvion();

        JPanel panelOps = new JPanel(new GridLayout(4, 1, 4, 4));
        panelOps.setBorder(new TitledBorder("Operaciones"));

        // Reserva
        JPanel pR = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbVentanillaReserva = new JComboBox<>(new String[]{"1","2","3"});
        cbClientes = new JComboBox<>();
        cbAsientosReserva = new JComboBox<>(codigosAsientos());
        JButton btnReservar = new JButton("Reservar asiento");
        btnReservar.addActionListener(e -> reservarIndividual());
        pR.add(new JLabel("Ventanilla:")); pR.add(cbVentanillaReserva);
        pR.add(new JLabel("Cliente:"));    pR.add(cbClientes);
        pR.add(new JLabel("Asiento:"));    pR.add(cbAsientosReserva);
        pR.add(btnReservar);

        // Cancelar
        JPanel pC = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbAsientosCancela = new JComboBox<>(codigosAsientos());
        JButton btnCancelar = new JButton("Eliminar reservación");
        btnCancelar.addActionListener(e -> cancelarIndividual());
        pC.add(new JLabel("Asiento:")); pC.add(cbAsientosCancela);
        pC.add(btnCancelar);

        // Cambiar
        JPanel pM = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbAsientosCambiaActual = new JComboBox<>(codigosAsientos());
        cbAsientosCambiaNuevo = new JComboBox<>(codigosAsientos());
        JButton btnCambiar = new JButton("Cambiar asiento");
        btnCambiar.addActionListener(e -> cambiarIndividual());
        pM.add(new JLabel("Actual:")); pM.add(cbAsientosCambiaActual);
        pM.add(new JLabel("Nuevo:"));  pM.add(cbAsientosCambiaNuevo);
        pM.add(btnCambiar);

        // Simulación
        JPanel pS = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSimCon = new JButton("Simular CON exclusión");
        JButton btnSimSin = new JButton("Simular SIN exclusión");
        btnSimCon.addActionListener(e -> simular(true));
        btnSimSin.addActionListener(e -> simular(false));
        lblModo = new JLabel("Modo actual: --");
        pS.add(btnSimCon); pS.add(btnSimSin); pS.add(lblModo);

        panelOps.add(pR); panelOps.add(pC); panelOps.add(pM); panelOps.add(pS);

        JPanel panelCentro = new JPanel(new BorderLayout(8, 8));
        panelCentro.add(panelAvion, BorderLayout.CENTER);
        panelCentro.add(panelOps, BorderLayout.SOUTH);

        // ==== Derecha: bitácora, alertas y lista de espera ====
        areaBitacora = new JTextArea(); areaBitacora.setEditable(false);
        areaAlertas = new JTextArea();  areaAlertas.setEditable(false);
        areaListaEspera = new JTextArea(); areaListaEspera.setEditable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bitácora", new JScrollPane(areaBitacora));
        tabs.addTab("Alertas (carrera)", new JScrollPane(areaAlertas));
        tabs.addTab("Lista de espera", new JScrollPane(areaListaEspera));
        tabs.setPreferredSize(new Dimension(420, 0));

        add(panelReg, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(tabs, BorderLayout.EAST);
    }

    private String[] codigosAsientos() {
        List<String> l = new ArrayList<>();
        for (char f = 'A'; f < 'A' + Avion.FILAS; f++)
            for (int c = 1; c <= Avion.COLS; c++)
                l.add(f + String.valueOf(c));
        return l.toArray(new String[0]);
    }

    private void dibujarAvion() {
        panelAvion.removeAll();
        for (Map.Entry<String, String> e : avion.estadoMapa().entrySet()) {
            String codigo = e.getKey();
            Asiento a = avion.buscar(codigo);
            JButton b = new JButton(codigo);
            b.setOpaque(true);
            b.setBackground(a.isOcupado() ? new Color(255, 180, 180) : Color.WHITE);
            b.setToolTipText(e.getValue());
            b.setEnabled(false);
            panelAvion.add(b);
        }
        panelAvion.revalidate();
        panelAvion.repaint();
    }

    private void refrescarTodo() {
        dibujarAvion();
        StringBuilder sb = new StringBuilder();
        for (String s : avion.getBitacora()) sb.append(s).append("\n");
        areaBitacora.setText(sb.toString());
        StringBuilder sa = new StringBuilder();
        for (String s : avion.getAlertas()) sa.append(s).append("\n");
        areaAlertas.setText(sa.toString());
        StringBuilder le = new StringBuilder();
        avion.getListaEspera().snapshotTodo()
                .forEach((k, v) -> le.append("[").append(k).append("] -> ").append(v).append("\n"));
        areaListaEspera.setText(le.length() == 0 ? "(sin esperas)" : le.toString());
        lblModo.setText("Modo actual: "
                + (avion.isModoExclusion() ? "CON exclusión mutua" : "SIN exclusión mutua"));
    }

    private Cliente clienteSeleccionado() {
        int i = cbClientes.getSelectedIndex();
        return (i < 0 || clientes.isEmpty()) ? null : clientes.get(i);
    }

    private void registrarCliente() {
        String ced = txtCedula.getText().trim();
        String nom = txtNombre.getText().trim();
        if (ced.isEmpty() || nom.isEmpty()) { JOptionPane.showMessageDialog(this, "Datos completos"); return; }
        for (Cliente c : clientes)
            if (c.getCedula().equals(ced)) { JOptionPane.showMessageDialog(this, "Cédula ya registrada"); return; }
        Cliente c = new Cliente(ced, nom);
        clientes.add(c);
        cbClientes.addItem(c.getNombre());
        avion.log("Cliente registrado: " + c);
        txtCedula.setText(""); txtNombre.setText("");
        refrescarTodo();
    }

    private void reservarIndividual() {
        Cliente c = clienteSeleccionado();
        if (c == null) { JOptionPane.showMessageDialog(this, "Registre un cliente"); return; }
        int idVent = Integer.parseInt((String) cbVentanillaReserva.getSelectedItem());
        String asiento = (String) cbAsientosReserva.getSelectedItem();
        Ventanilla v = new Ventanilla(idVent, avion, c, asiento);
        v.start(); // hilo real
        new Thread(() -> { try { v.join(); } catch (Exception ignored) {}
                           SwingUtilities.invokeLater(this::refrescarTodo); }).start();
    }

    private void cancelarIndividual() {
        Cliente c = clienteSeleccionado();
        if (c == null) { JOptionPane.showMessageDialog(this, "Seleccione cliente"); return; }
        avion.cancelar((String) cbAsientosCancela.getSelectedItem(), c);
        refrescarTodo();
    }

    private void cambiarIndividual() {
        Cliente c = clienteSeleccionado();
        if (c == null) { JOptionPane.showMessageDialog(this, "Seleccione cliente"); return; }
        avion.cambiar((String) cbAsientosCambiaActual.getSelectedItem(),
                (String) cbAsientosCambiaNuevo.getSelectedItem(), c);
        refrescarTodo();
    }

    /** Simulación concurrente de 3 ventanillas sobre el mismo asiento. */
    private void simular(boolean conExclusion) {
        if (clientes.size() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Registre al menos 3 clientes para la simulación.");
            return;
        }
        avion.inicializar();
        avion.setModoExclusion(conExclusion);
        refrescarTodo();

        String asiento = (String) cbAsientosReserva.getSelectedItem();

        Ventanilla v1 = new Ventanilla(1, avion, clientes.get(0), asiento);
        Ventanilla v2 = new Ventanilla(2, avion, clientes.get(1), asiento);
        Ventanilla v3 = new Ventanilla(3, avion, clientes.get(2), asiento);

        // start() => concurrencia real; run() sería secuencial
        v1.start(); v2.start(); v3.start();

        new Thread(() -> {
            try { v1.join(); v2.join(); v3.join(); } catch (Exception ignored) {}
            SwingUtilities.invokeLater(() -> {
                avion.log("=== Fin de simulación "
                        + (conExclusion ? "CON" : "SIN") + " exclusión ===");
                refrescarTodo();
                String msg = conExclusion
                        ? "Simulación CON exclusión:\nUn único ganador. Los demás a lista de espera."
                        : "Simulación SIN exclusión:\nRevise la pestaña 'Alertas' para ver la condición de carrera.";
                JOptionPane.showMessageDialog(this, msg);
            });
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}