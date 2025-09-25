Projeto A3 -Sistema de Gestão de Equipe e Projeto

Aplicativo desktop (Java + Swing + MySQL) para cadastrar usuários/equipes/projetos, atribuir quem participa de quê e visualizar relatórios rápidos.

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

* Sumário

Tecnologias

Estrutura do repositório

Banco de Dados (SQL)

Como rodar (Maven + Swing)

Funcionalidades

Perfis e Permissões

Relatórios internos

Logins de exemplo

Erros comuns

Critérios de aceite usados

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Tecnologias

Java (interface com Swing)

Maven (gerenciamento de dependências)

MySQL (banco de dados)

IDE recomendada: IntelliJ IDEA

Datas no sistema: formato brasileiro dia/mês/ano (ex.: 23/09/2025)

Estrutura do repositório
projeto-a3/
├─ README.md
├─ .gitignore
├─ sql/
│  └─ projetoA3.sql            
                   
└─ maven/                       
   ├─ pom.xml
   └─ src/main/java/br/com/a3/
      ├─ Banco.java
      ├─ Inicio.java
      ├─ TelaLogin.java
      ├─ TelaPrincipal.java
      ├─ TelaCadastroUsuarios.java
      ├─ TelaCadastroEquipes.java
      ├─ TelaCadastroProjetos.java
      ├─ TelaAtribuicoes.java
      └─ TelaRelatoriosSimples.java


Prints sugeridos (opcional): coloque em docs/
docs/print-login.png, docs/print-principal.png, docs/print-relatorios.png

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 Banco de Dados (SQL)

Schema: projetoA3

Usuário do BD: projeto

Senha: 9876A3

Como importar:

Abra o MySQL Workbench → Server → Data Import.

Aponte para sql/projetoA3.sql.

Importe estrutura + dados.

Confirme se as tabelas foram criadas:
usuarios, equipes, projetos, equipe_membros, equipe_projetos.

Credenciais no código (ajuste se precisar) — maven/src/main/java/br/com/a3/Banco.java:

String url  = "jdbc:mysql://localhost:3306/projetoA3?useSSL=false&serverTimezone=UTC";
String user = "projeto";
String pass = "9876A3";

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

▶️ Como rodar (Maven + Swing)

Pré-requisitos

JDK 17+

MySQL 8+ rodando

Banco importado (projetoA3.sql)

Passo a passo

Abra o IntelliJ → Open → selecione a pasta maven/ (detectar como projeto Maven).

Aguarde o Maven baixar dependências (ou clique em Reload All Maven Projects).

Ajuste credenciais em Banco.java se necessário.

Rode a classe br.com.a3.Inicio → abre a tela de Login.

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 Perfis e Permissões
 
Perfil	Ver Principal	Cadastros	Atribuições	Relatórios
Administrador	✅	✅	✅	✅
Gerente	✅	✅	✅	✅
Colaborador	✅	❌	❌	✅

----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

 Relatórios internos

Contagem por equipe → quantas pessoas em cada equipe.

Participantes do Projeto X → equipes do projeto + integrantes.

Membros da Equipe Y → lista de pessoas de uma equipe.
 Logins de exemplo
Usuário	Senha	Perfil
maria	123	Administrador
joao	123	Colaborador
ana	123	Colaborador

Dica: cadastre/edite usuários em Cadastro → Usuário.

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

 Erros comuns

Data inválida → use dia/mês/ano (ex.: 23/09/2025).

Falha de conexão → confira URL/USUARIO/SENHA em Banco.java e se o MySQL está ligado.

Lista não atualiza → clique Recarregar na tela (ou Atualizar na principal).

Sem permissão → verifique o perfil do usuário logado.

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

 Critérios de aceite usados

Funcionalidades combinadas entregues (login/perfis, cadastros, atribuições, relatórios).

Sem bugs críticos nos fluxos principais.

Build compila e roda no IntelliJ (Inicio.java).

Banco importável via sql/projetoA3.sql.

Datas no padrão dia/mês/ano (entrada e exibição).

Repositório organizado (sql/, maven/, console/).
