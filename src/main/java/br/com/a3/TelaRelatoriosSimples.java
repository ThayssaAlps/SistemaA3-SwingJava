package br.com.a3;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class TelaRelatoriosSimples extends JFrame {

    private JComboBox<String> cbTipo;
    private JComboBox<ItemProjeto> cbProjeto;
    private JComboBox<ItemEquipe> cbEquipe;
    private JButton bGerar;
    private JTextArea area;

    public TelaRelatoriosSimples() {
        super("Relatórios");
        setSize(700, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Tipo:"));

        cbTipo = new JComboBox<>(new String[]{
                "Contagem de pessoas por equipe (todas)",
                "Quem participa do Projeto...",
                "Quem faz parte da Equipe..."
        });
        cbTipo.addActionListener(e -> alternarInputs());
        topo.add(cbTipo);

        topo.add(new JLabel("Projeto:"));
        cbProjeto = new JComboBox<>();
        topo.add(cbProjeto);

        topo.add(new JLabel("Equipe:"));
        cbEquipe = new JComboBox<>();
        topo.add(cbEquipe);

        bGerar = new JButton("Gerar");
        bGerar.addActionListener(e -> gerar());
        topo.add(bGerar);

        JButton bFechar = new JButton("Fechar");
        bFechar.addActionListener(e -> dispose());
        topo.add(bFechar);

        add(topo, BorderLayout.NORTH);

        area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(area), BorderLayout.CENTER);

        carregarProjetos();
        carregarEquipes();
        alternarInputs();
    }

    private void alternarInputs() {
        String tipo = (String) cbTipo.getSelectedItem();
        boolean precisaProjeto = "Quem participa do Projeto...".equals(tipo);
        boolean precisaEquipe  = "Quem faz parte da Equipe...".equals(tipo);
        cbProjeto.setEnabled(precisaProjeto);
        cbEquipe.setEnabled(precisaEquipe);
    }

    private void carregarProjetos() {
        cbProjeto.removeAllItems();
        String sql = "SELECT id, nome FROM projetos ORDER BY nome";
        try (Connection c = Banco.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) cbProjeto.addItem(new ItemProjeto(rs.getInt("id"), rs.getString("nome")));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar projetos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void carregarEquipes() {
        cbEquipe.removeAllItems();
        String sql = "SELECT id, nome FROM equipes ORDER BY nome";
        try (Connection c = Banco.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) cbEquipe.addItem(new ItemEquipe(rs.getInt("id"), rs.getString("nome")));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar equipes: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void gerar() {
        area.setText("");
        String tipo = (String) cbTipo.getSelectedItem();

        if ("Contagem de pessoas por equipe (todas)".equals(tipo)) {
            gerarContagemPorEquipeTodas();
            return;
        }

        if ("Quem participa do Projeto...".equals(tipo)) {
            ItemProjeto p = (ItemProjeto) cbProjeto.getSelectedItem();
            if (p == null) { JOptionPane.showMessageDialog(this, "Selecione um projeto."); return; }
            gerarParticipantesDoProjeto(p.id, p.nome);
            return;
        }

        if ("Quem faz parte da Equipe...".equals(tipo)) {
            ItemEquipe e = (ItemEquipe) cbEquipe.getSelectedItem();
            if (e == null) { JOptionPane.showMessageDialog(this, "Selecione uma equipe."); return; }
            gerarMembrosDaEquipe(e.id, e.nome);
        }
    }

    private void gerarContagemPorEquipeTodas() {
        StringBuilder sb = new StringBuilder();
        sb.append("====== CONTAGEM DE PESSOAS POR EQUIPE ======\n\n");
        String sql =
                "SELECT e.id, e.nome, COUNT(em.usuario_id) AS qtd " +
                        "FROM equipes e " +
                        "LEFT JOIN equipe_membros em ON em.equipe_id = e.id " +
                        "GROUP BY e.id, e.nome " +
                        "ORDER BY e.nome";
        int totalPessoas = 0;
        try (Connection c = Banco.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int qtd = rs.getInt("qtd");
                totalPessoas += qtd;
                sb.append(String.format("- %s (ID %d): %d pessoa(s)\n",
                        safe(rs.getString("nome")), rs.getInt("id"), qtd));
            }
            sb.append("\nTotal geral de vínculos (soma de pessoas nas equipes): ").append(totalPessoas);
            area.setText(sb.toString());
            area.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar contagem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void gerarParticipantesDoProjeto(int projetoId, String nomeProjeto) {
        StringBuilder sb = new StringBuilder();
        sb.append("====== QUEM PARTICIPA DO PROJETO ======\n");
        sb.append("Projeto: ").append(safe(nomeProjeto)).append(" (ID ").append(projetoId).append(")\n\n");

        String sqlEquipes =
                "SELECT e.id, e.nome " +
                        "FROM equipes e " +
                        "JOIN equipe_projetos ep ON ep.equipe_id = e.id " +
                        "WHERE ep.projeto_id = ? " +
                        "ORDER BY e.nome";

        String sqlMembros =
                "SELECT u.nome " +
                        "FROM equipe_membros em " +
                        "JOIN usuarios u ON u.id = em.usuario_id " +
                        "WHERE em.equipe_id = ? " +
                        "ORDER BY u.nome";

        try (Connection c = Banco.get()) {
            // Equipes do projeto
            ArrayList<ItemEquipe> equipesDoProjeto = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sqlEquipes)) {
                ps.setInt(1, projetoId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        equipesDoProjeto.add(new ItemEquipe(rs.getInt("id"), rs.getString("nome")));
                    }
                }
            }

            if (equipesDoProjeto.isEmpty()) {
                sb.append("(Sem equipes vinculadas a este projeto)\n");
                area.setText(sb.toString());
                area.setCaretPosition(0);
                return;
            }

            int total = 0;
            for (ItemEquipe e : equipesDoProjeto) {
                sb.append(e.nome).append(" (ID ").append(e.id).append(")\n");
                int subtotal = 0;
                try (PreparedStatement ps2 = c.prepareStatement(sqlMembros)) {
                    ps2.setInt(1, e.id);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            subtotal++;
                            total++;
                            sb.append("  - ").append(safe(rs2.getString("nome"))).append("\n");
                        }
                    }
                }
                if (subtotal == 0) sb.append("  (Sem membros nesta equipe)\n");
                sb.append("\n");
            }
            sb.append("Total de pessoas vinculadas ao projeto (soma das equipes): ").append(total).append("\n");

            area.setText(sb.toString());
            area.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório do projeto: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void gerarMembrosDaEquipe(int equipeId, String nomeEquipe) {
        StringBuilder sb = new StringBuilder();
        sb.append("====== QUEM FAZ PARTE DA EQUIPE ======\n");
        sb.append("Equipe: ").append(safe(nomeEquipe)).append(" (ID ").append(equipeId).append(")\n\n");

        String sql =
                "SELECT u.nome " +
                        "FROM equipe_membros em " +
                        "JOIN usuarios u ON u.id = em.usuario_id " +
                        "WHERE em.equipe_id = ? " +
                        "ORDER BY u.nome";

        int total = 0;
        try (Connection c = Banco.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, equipeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    total++;
                    sb.append(" - ").append(safe(rs.getString("nome"))).append("\n");
                }
            }
            if (total == 0) sb.append("(Nenhum membro nesta equipe)\n");
            sb.append("\nTotal: ").append(total).append(" pessoa(s)\n");

            area.setText(sb.toString());
            area.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar membros da equipe: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String safe(String s) { return (s == null) ? "" : s; }

    private static class ItemProjeto {
        int id; String nome;
        ItemProjeto(int id, String nome) { this.id = id; this.nome = nome; }
        @Override public String toString() { return id + " - " + (nome == null ? "" : nome); }
    }

    private static class ItemEquipe {
        int id; String nome;
        ItemEquipe(int id, String nome) { this.id = id; this.nome = nome; }
        @Override public String toString() { return id + " - " + (nome == null ? "" : nome); }
    }
}
