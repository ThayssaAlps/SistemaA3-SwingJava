package br.com.a3;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TelaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtSenha;
    private JLabel lblStatus;

    public TelaLogin() {
        super("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 260);
        setLayout(null);
        setResizable(false);

        JLabel l1 = new JLabel("Usuário");
        l1.setBounds(60, 50, 100, 25);
        add(l1);

        txtUsuario = new JTextField();
        txtUsuario.setBounds(150, 50, 300, 25);
        add(txtUsuario);

        JLabel l2 = new JLabel("Senha");
        l2.setBounds(60, 90, 100, 25);
        add(l2);

        txtSenha = new JPasswordField();
        txtSenha.setBounds(150, 90, 300, 25);
        add(txtSenha);

        JButton btnLogin = new JButton("Entrar");
        btnLogin.setBounds(360, 130, 90, 30);
        btnLogin.addActionListener(e -> fazerLogin());
        add(btnLogin);

        lblStatus = new JLabel("Aguardando login");
        lblStatus.setBounds(60, 170, 390, 25);
        lblStatus.setForeground(Color.DARK_GRAY);
        add(lblStatus);

        setLocationRelativeTo(null);
    }

    private void fazerLogin() {
        String login = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());
        if (login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha usuário e senha.");
            return;
        }
        String sql = "SELECT id, nome, perfil FROM usuarios WHERE login=? AND senha=?";
        try (Connection c = Banco.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, senha);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    String perfil = rs.getString("perfil");
                    new TelaPrincipal(nome, perfil).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos.");
                    lblStatus.setText("Falha no login");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            lblStatus.setText("Erro de conexão");
            ex.printStackTrace();
        }
    }
}
