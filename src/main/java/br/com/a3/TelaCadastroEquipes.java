package br.com.a3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TelaCadastroEquipes extends JFrame {
    private DefaultTableModel modelo;
    private JTable tabela;

    public TelaCadastroEquipes() {
        super("Cadastro de Equipes");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{"id","nome","descricao"}, 0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
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
        String sql = "SELECT id,nome,descricao FROM equipes ORDER BY id DESC";
        try (Connection c = Banco.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{ rs.getInt("id"), rs.getString("nome"), rs.getString("descricao") });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void novo() {
        String nome = input("Nome da equipe", "");
        if (vazio(nome)) return;
        String desc = input("Descrição", "");
        String sql = "INSERT INTO equipes (nome, descricao) VALUES (?,?)";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, desc);
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
        String nome = input("Nome", (String) modelo.getValueAt(row, 1));
        if (vazio(nome)) return;
        String desc = input("Descrição", (String) modelo.getValueAt(row, 2));

        String sql = "UPDATE equipes SET nome=?, descricao=? WHERE id=?";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, desc);
            ps.setInt(3, id);
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
        int ok = JOptionPane.showConfirmDialog(this, "Excluir equipe ID " + id + "?", "Confirma", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM equipes WHERE id=?";
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
    private static String input(String rotulo, String valor) {
        return (String) JOptionPane.showInputDialog(null, rotulo + ":", "Informar", JOptionPane.PLAIN_MESSAGE, null, null, valor);
    }
}
