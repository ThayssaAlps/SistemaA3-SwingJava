package br.com.a3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Banco {
    public static final String URL  = "jdbc:mysql://localhost:3306/projetoA3?useSSL=false&serverTimezone=UTC";
    public static final String USUARIO = "projeto";
    public static final String SENHA = "9876A3";

    public static Connection get() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException("Driver JDBC não encontrado", e); }
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}

