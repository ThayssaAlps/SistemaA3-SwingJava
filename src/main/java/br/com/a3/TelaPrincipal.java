package br.com.a3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class TelaPrincipal extends JFrame {

    private JLabel lblUsuario;
    private DefaultTableModel modeloProjetos;
    private JTable tabelaProjetos;

    private JMenu mCadastro, mEquipe;
    private JMenuItem miCadEquipe, miCadProjeto, miCadUsuario, miAtribuicoes;

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TelaPrincipal(String nomeUsuario, String perfil) {
        super("Tela Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(criarMenu());
        aplicarPermissoes(perfil);

        JPanel topo = new JPanel(new BorderLayout());
        lblUsuario = new JLabel("Usuário: " + nomeUsuario + "  | Perfil: " + perfil);
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        topo.add(lblUsuario, BorderLayout.WEST);

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarProjetos());
        topo.add(btnAtualizar, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);

        modeloProjetos = new DefaultTableModel(
                new Object[]{"ID", "Projeto", "Início", "Entrega", "Status", "Gerente", "Equipes"}, 0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tabelaProjetos = new JTable(modeloProjetos);
        add(new JScrollPane(tabelaProjetos), BorderLayout.CENTER);

        carregarProjetos();
    }

    private JMenuBar criarMenu() {
        JMenuBar bar = new JMenuBar();

        mCadastro = new JMenu("Cadastro");
        miCadEquipe = new JMenuItem("Equipe");
        miCadProjeto = new JMenuItem("Projeto");
        miCadUsuario = new JMenuItem("Usuário");

        miCadEquipe.addActionListener(e -> new TelaCadastroEquipes().setVisible(true));
        miCadProjeto.addActionListener(e -> new TelaCadastroProjetos().setVisible(true));
        miCadUsuario.addActionListener(e -> new TelaCadastroUsuarios().setVisible(true));

        mCadastro.add(miCadEquipe);
        mCadastro.add(miCadProjeto);
        mCadastro.add(miCadUsuario);

        mEquipe = new JMenu("Equipe");
        miAtribuicoes = new JMenuItem("Atribuições (Usuário ↔ Equipe ↔ Projeto)");
        miAtribuicoes.addActionListener(e -> new TelaAtribuicoes().setVisible(true));
        mEquipe.add(miAtribuicoes);

        JMenu mAjuda = new JMenu("Ajuda");
        JMenuItem miSobre = new JMenuItem("Sobre");
        miSobre.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Projeto A3, Sistema de Gestão de Equipe e Projeto."));
        mAjuda.add(miSobre);

        JMenu mRelatorio = new JMenu("Relatório");
        JMenuItem miSimples = new JMenuItem("Relatórios Simples…");
        miSimples.addActionListener(e -> new TelaRelatoriosSimples().setVisible(true));
        mRelatorio.add(miSimples);

        JMenu mOpcao = new JMenu("Opção");
        JMenuItem miSair = new JMenuItem("Sair");
        miSair.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(
                    this,
                    "Você tem certeza que deseja sair?",
                    "Confirmar saída",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (resp == JOptionPane.YES_OPTION) System.exit(0);
        });
        mOpcao.add(miSair);

        bar.add(mCadastro);
        bar.add(mEquipe);
        bar.add(mAjuda);
        bar.add(mRelatorio);
        bar.add(mOpcao);
        return bar;
    }

    private void carregarProjetos() {
        modeloProjetos.setRowCount(0);
        String sql =
                "SELECT p.id, p.nome, p.data_inicio, p.data_termino, p.status, " +
                        "u.nome AS gerente, GROUP_CONCAT(e.nome SEPARATOR ', ') AS equipes " +
                        "FROM projetos p " +
                        "LEFT JOIN usuarios u ON u.id = p.gerente_id " +
                        "LEFT JOIN equipe_projetos ep ON ep.projeto_id = p.id " +
                        "LEFT JOIN equipes e ON e.id = ep.equipe_id " +
                        "GROUP BY p.id, p.nome, p.data_inicio, p.data_termino, p.status, u.nome " +
                        "ORDER BY p.id DESC";
        try (Connection c = Banco.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                java.sql.Date di = rs.getDate("data_inicio");
                java.sql.Date dt = rs.getDate("data_termino");
                String diBR = (di == null) ? "" : di.toLocalDate().format(BR);
                String dtBR = (dt == null) ? "" : dt.toLocalDate().format(BR);

                modeloProjetos.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nome"),
                        diBR,
                        dtBR,
                        rs.getString("status"),
                        rs.getString("gerente"),
                        rs.getString("equipes")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar projetos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void aplicarPermissoes(String perfil) {
        boolean podeGerir = perfilEhAdminOuGerente(perfil);
        mCadastro.setEnabled(podeGerir);
        miCadEquipe.setEnabled(podeGerir);
        miCadProjeto.setEnabled(podeGerir);
        miCadUsuario.setEnabled(podeGerir);
        miAtribuicoes.setEnabled(podeGerir);
    }

    private boolean perfilEhAdminOuGerente(String perfil) {
        if (perfil == null) return false;
        String p = perfil.trim().toLowerCase();
        return p.startsWith("admin") || p.startsWith("gerente");
    }
}
