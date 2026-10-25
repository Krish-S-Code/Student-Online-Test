
package com.studenttest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/CreateTestServlet")
public class CreateTestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || !"ADMIN".equalsIgnoreCase(
                    String.valueOf(session.getAttribute("role")))
                || !(session.getAttribute("studentId") instanceof Integer)) {

            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Please log in as an administrator."
            );
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String durationText = request.getParameter("duration");
        String questionLimitText = request.getParameter("questionLimit");

        if (title == null || title.isBlank()
                || description == null || description.isBlank()
                || durationText == null || durationText.isBlank()
                || questionLimitText == null
                || questionLimitText.isBlank()) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "All test fields are required."
            );
            return;
        }

        title = title.trim();
        description = description.trim();

        if (title.length() > 150) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Test name must not exceed 150 characters."
            );
            return;
        }

        int duration;
        int questionLimit;

        try {
            duration = Integer.parseInt(durationText.trim());
            questionLimit = Integer.parseInt(questionLimitText.trim());
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Duration and question limit must be whole numbers."
            );
            return;
        }

        if (duration < 1 || duration > 1440) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Duration must be between 1 and 1440 minutes."
            );
            return;
        }

        if (questionLimit < 1 || questionLimit > 200) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Question limit must be between 1 and 200."
            );
            return;
        }

        int adminId = (Integer) session.getAttribute("studentId");

        String sql = "INSERT INTO tests "
                + "(title, description, created_by, duration_minutes, "
                + "question_limit, total_questions) "
                + "VALUES (?, ?, ?, ?, ?, 0)";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, adminId);
            ps.setInt(4, duration);
            ps.setInt(5, questionLimit);

            int rowsInserted = ps.executeUpdate();

            if (rowsInserted != 1) {
                throw new ServletException("Test was not created.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int testId = keys.getInt(1);

                    response.sendRedirect(
                        "add-question.html?testId=" + testId
                    );
                    return;
                }
            }

            throw new ServletException(
                "Could not retrieve the new test ID."
            );

        } catch (Exception e) {
            getServletContext().log("Failed to create test", e);

            if (e instanceof ServletException) {
                throw (ServletException) e;
            }

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to create the test. Please check server logs."
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "Submit the Create Test form to create a test."
        );
    }
}
