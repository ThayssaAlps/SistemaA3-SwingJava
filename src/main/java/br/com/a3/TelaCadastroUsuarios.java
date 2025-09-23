package br.com.a3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TelaCadastroUsuarios extends JFrame {

    private DefaultTableModel modelo;
    private JTable tabela;

    public TelaCadastroUsuarios() {
        super("Cadastro de Usuários");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{
                "id","nome","cpf","email","cargo","login","senha","perfil"
        }, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bNovo = new JButton("Novo");
        JButton bEditar = new JButton("Editar");
        JButton bExcluir = new JButton("Excluir");
        JButton bRecarregar = new JButton("Recarregar");
        botoes.add(bNovo); botoes.add(bEditar); botoes.add(bExcluir); botoes.add(bRecarregar);
        add(botoes, BorderLayout.NORTH);

        bRecarregar.addActionListener(e -> carregar());
        bNovo.addActionListener(e -> novo());
        bEditar.addActionListener(e -> editar());
        bExcluir.addActionListener(e -> excluir());

        carregar();
    }

    private void carregar() {
        modelo.setRowCount(0);
        String sql = "SELECT id, nome, cpf, email, cargo, login, senha, perfil FROM usuarios ORDER BY id DESC";
        try (Connection c = Banco.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("email"),
                        rs.getString("cargo"),
                        rs.getString("login"),
                        rs.getString("senha"),
                        rs.getString("perfil")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void novo() {
        String nome  = JOptionPane.showInputDialog(this, "Nome completo:");
        if (vazio(nome)) return;
        String cpf   = JOptionPane.showInputDialog(this, "CPF (000.000.000-00):");
        if (vazio(cpf)) return;
        String email = JOptionPane.showInputDialog(this, "E-mail:");
        if (vazio(email)) return;
        String cargo = JOptionPane.showInputDialog(this, "Cargo:");
        if (vazio(cargo)) return;
        String login = JOptionPane.showInputDialog(this, "Login:");
        if (vazio(login)) return;
        String senha = JOptionPane.showInputDialog(this, "Senha:");
        if (vazio(senha)) return;
        String perfil = JOptionPane.showInputDialog(this, "Perfil (Administrador/Gerente/Colaborador):");
        if (vazio(perfil)) return;

        String sql = "INSERT INTO usuarios (nome, cpf, email, cargo, login, senha, perfil) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cpf);
            ps.setString(3, email);
            ps.setString(4, cargo);
            ps.setString(5, login);
            ps.setString(6, senha);
            ps.setString(7, perfil);
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

        Integer id   = (Integer) modelo.getValueAt(row, 0);
        String nome  = input("Nome",  (String) modelo.getValueAt(row, 1)); if (vazio(nome)) return;
        String cpf   = input("CPF",   (String) modelo.getValueAt(row, 2)); if (vazio(cpf)) return;
        String email = input("E-mail",(String) modelo.getValueAt(row, 3)); if (vazio(email)) return;
        String cargo = input("Cargo", (String) modelo.getValueAt(row, 4)); if (vazio(cargo)) return;
        String login = input("Login", (String) modelo.getValueAt(row, 5)); if (vazio(login)) return;
        String senha = input("Senha", (String) modelo.getValueAt(row, 6)); if (vazio(senha)) return;
        String perfil= input("Perfil",(String) modelo.getValueAt(row, 7)); if (vazio(perfil)) return;

        String sql = "UPDATE usuarios SET nome=?, cpf=?, email=?, cargo=?, login=?, senha=?, perfil=? WHERE id=?";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cpf);
            ps.setString(3, email);
            ps.setString(4, cargo);
            ps.setString(5, login);
            ps.setString(6, senha);
            ps.setString(7, perfil);
            ps.setInt(8, id);
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
        int ok = JOptionPane.showConfirmDialog(this, "Excluir usuário ID " + id + "?", "Confirma", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM usuarios WHERE id=?";
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
    private static String input(String rotulo, String valorAtual) {
        return (String) JOptionPane.showInputDialog(null, rotulo + ":", "Editar",
                JOptionPane.PLAIN_MESSAGE, null, null, valorAtual);
    }
}
