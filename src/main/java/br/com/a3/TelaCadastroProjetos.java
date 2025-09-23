package br.com.a3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TelaCadastroProjetos extends JFrame {
    private DefaultTableModel modelo;
    private JTable tabela;

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TelaCadastroProjetos() {
        super("Cadastro de Projetos");
        setSize(1000, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{
                "id","nome","descricao","data_inicio","data_termino","status","gerente_id","gerente_nome"
        }, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bNovo = new JButton("Novo");
        JButton bEditar = new JButton("Editar");
        JButton bExcluir = new JButton("Excluir");
        JButton bRecarregar = new JButton("Recarregar");
        topo.add(bNovo); topo.add(bEditar); topo.add(bExcluir); topo.add(bRecarregar);
        add(topo, BorderLayout.NORTH);

        bRecarregar.addActionListener(e -> carregar());
        bNovo.addActionListener(e -> novo());
        bEditar.addActionListener(e -> editar());
        bExcluir.addActionListener(e -> excluir());

        carregar();
    }

    private void carregar() {
        modelo.setRowCount(0);
        String sql = "SELECT p.id,p.nome,p.descricao,p.data_inicio,p.data_termino,p.status,p.gerente_id,u.nome AS gerente_nome " +
                "FROM projetos p LEFT JOIN usuarios u ON u.id=p.gerente_id ORDER BY p.id DESC";
        try (Connection c = Banco.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        formatarDataBR(rs.getDate("data_inicio")),
                        formatarDataBR(rs.getDate("data_termino")),
                        rs.getString("status"),
                        rs.getInt("gerente_id"),
                        rs.getString("gerente_nome")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void novo() {
        String nome = input("Nome do projeto", "");
        if (vazio(nome)) return;
        String desc = input("Descrição", "");
        String di = input("Data início (dd/MM/yyyy) — vazio se não tiver", "");
        String dt = input("Data término (dd/MM/yyyy) — vazio se não tiver", "");
        String status = input("Status (Planejado/Em andamento/Concluído/Cancelado)", "Planejado");
        String gerenteId = input("Gerente ID (usuarios.id) — vazio se não tiver", "");

        LocalDate diLD = parseDataBR(di);   if (diLD == null && !vazio(di)) return;
        LocalDate dtLD = parseDataBR(dt);   if (dtLD == null && !vazio(dt)) return;

        String sql = "INSERT INTO projetos (nome, descricao, data_inicio, data_termino, status, gerente_id) VALUES (?,?,?,?,?,?)";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, desc);

            if (diLD == null) ps.setNull(3, Types.DATE);
            else ps.setDate(3, java.sql.Date.valueOf(diLD));

            if (dtLD == null) ps.setNull(4, Types.DATE);
            else ps.setDate(4, java.sql.Date.valueOf(dtLD));

            ps.setString(5, status);

            if (vazio(gerenteId)) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, Integer.parseInt(gerenteId));

            ps.executeUpdate();
            carregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma linha."); return; }

        Integer id = (Integer) modelo.getValueAt(row, 0);
        String nome = input("Nome", (String) modelo.getValueAt(row, 1)); if (vazio(nome)) return;
        String desc = input("Descrição", (String) modelo.getValueAt(row, 2));

        String diAtual = (String) modelo.getValueAt(row, 3); if ("null".equalsIgnoreCase(diAtual)) diAtual = "";
        String dtAtual = (String) modelo.getValueAt(row, 4); if ("null".equalsIgnoreCase(dtAtual)) dtAtual = "";

        String di = input("Data início (dd/MM/yyyy) — vazio se não tiver", diAtual);
        String dt = input("Data término (dd/MM/yyyy) — vazio se não tiver", dtAtual);
        String status = input("Status", (String) modelo.getValueAt(row, 5));
        String gerenteId = input("Gerente ID", String.valueOf(modelo.getValueAt(row, 6)));

        LocalDate diLD = parseDataBR(di);   if (diLD == null && !vazio(di)) return;
        LocalDate dtLD = parseDataBR(dt);   if (dtLD == null && !vazio(dt)) return;

        String sql = "UPDATE projetos SET nome=?, descricao=?, data_inicio=?, data_termino=?, status=?, gerente_id=? WHERE id=?";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, desc);

            if (diLD == null) ps.setNull(3, Types.DATE);
            else ps.setDate(3, java.sql.Date.valueOf(diLD));

            if (dtLD == null) ps.setNull(4, Types.DATE);
            else ps.setDate(4, java.sql.Date.valueOf(dtLD));

            ps.setString(5, status);

            if (vazio(gerenteId)) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, Integer.parseInt(gerenteId));

            ps.setInt(7, id);
            ps.executeUpdate();
            carregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao editar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void excluir() {
        int row = tabela.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma linha."); return; }
        Integer id = (Integer) modelo.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this, "Excluir projeto ID " + id + "?", "Confirma", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM projetos WHERE id=?";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            carregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static boolean vazio(String s) { return s == null || s.trim().isEmpty(); }

    private static LocalDate parseDataBR(String s) {
        if (vazio(s)) return null;
        try {
            return LocalDate.parse(s.trim(), BR);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Data inválida. Use o formato dd/MM/yyyy (ex.: 23/09/2025).");
            return null;
        }
    }

    private static String formatarDataBR(java.sql.Date d) {
        if (d == null) return "";
        return d.toLocalDate().format(BR);
    }

    private static String input(String rotulo, String valor) {
        return (String) JOptionPane.showInputDialog(null, rotulo + ":", "Informar",
                JOptionPane.PLAIN_MESSAGE, null, null, valor);
    }
}
