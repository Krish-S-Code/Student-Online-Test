
package com.studenttest;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {
    public static void main(String[] args) {
        try (Connection con = DBUtil.getConnection()) {
            System.out.println("SUCCESS: Connected to MySQL!");
            System.out.println("Database: " +
                con.getCatalog());
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            System.out.println(e.getMessage());
        }
    }
}
