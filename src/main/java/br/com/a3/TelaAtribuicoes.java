package br.com.a3;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TelaAtribuicoes extends JFrame {
    public TelaAtribuicoes() {
        super("Atribuições");
        setSize(500, 320);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3,2,8,8));

        JButton bVincUsrEq = new JButton("Vincular Usuário a Equipe");
        JButton bDesvUsrEq = new JButton("Desvincular Usuário de Equipe");
        JButton bVincEqProj = new JButton("Vincular Equipe a Projeto");
        JButton bDesvEqProj = new JButton("Desvincular Equipe de Projeto");
        JButton bFechar = new JButton("Fechar");

        add(bVincUsrEq); add(bDesvUsrEq);
        add(bVincEqProj); add(bDesvEqProj);
        add(new JLabel()); add(bFechar);

        bVincUsrEq.addActionListener(e -> vincularUsrEquipe());
        bDesvUsrEq.addActionListener(e -> desvincularUsrEquipe());
        bVincEqProj.addActionListener(e -> vincularEquipeProjeto());
        bDesvEqProj.addActionListener(e -> desvincularEquipeProjeto());
        bFechar.addActionListener(e -> dispose());
    }

    private void vincularUsrEquipe() {
        String equipeId = input("Equipe ID", "");
        String usuarioId = input("Usuário ID", "");
        if (vazio(equipeId) || vazio(usuarioId)) return;
        String sql = "INSERT INTO equipe_membros (equipe_id, usuario_id) VALUES (?,?)";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(equipeId));
            ps.setInt(2, Integer.parseInt(usuarioId));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Vinculado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void desvincularUsrEquipe() {
        String equipeId = input("Equipe ID", "");
        String usuarioId = input("Usuário ID", "");
        if (vazio(equipeId) || vazio(usuarioId)) return;
        String sql = "DELETE FROM equipe_membros WHERE equipe_id=? AND usuario_id=?";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(equipeId));
            ps.setInt(2, Integer.parseInt(usuarioId));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Desvinculado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void vincularEquipeProjeto() {
        String equipeId = input("Equipe ID", "");
        String projetoId = input("Projeto ID", "");
        if (vazio(equipeId) || vazio(projetoId)) return;
        String sql = "INSERT INTO equipe_projetos (equipe_id, projeto_id) VALUES (?,?)";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(equipeId));
            ps.setInt(2, Integer.parseInt(projetoId));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Vinculado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void desvincularEquipeProjeto() {
        String equipeId = input("Equipe ID", "");
        String projetoId = input("Projeto ID", "");
        if (vazio(equipeId) || vazio(projetoId)) return;
        String sql = "DELETE FROM equipe_projetos WHERE equipe_id=? AND projeto_id=?";
        try (Connection c = Banco.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(equipeId));
            ps.setInt(2, Integer.parseInt(projetoId));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Desvinculado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static boolean vazio(String s) { return s == null || s.trim().isEmpty(); }
    private static String input(String rotulo, String valor) {
        return (String) JOptionPane.showInputDialog(null, rotulo + ":", "Informar",
                JOptionPane.PLAIN_MESSAGE, null, null, valor);
    }
}
