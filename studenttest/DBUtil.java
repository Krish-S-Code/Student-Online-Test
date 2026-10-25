
package com.studenttest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String URL =
        "jdbc:mysql://localhost/student_online_test";

    private static final String USER = "";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC driver class not found.", e
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
