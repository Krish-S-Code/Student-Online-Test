
package com.studenttest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/FinishTestServlet")
public class FinishTestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || !"ADMIN".equalsIgnoreCase(
                    String.valueOf(session.getAttribute("role")))
                || !(session.getAttribute("studentId") instanceof Integer)) {
            response.sendError(403, "Please log in as an administrator.");
            return;
        }

        int adminId = (Integer) session.getAttribute("studentId");
        String testIdText = request.getParameter("testId");
        int testId;

        try {
            testId = Integer.parseInt(testIdText);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(400, "Invalid test ID.");
            return;
        }

        if (testId < 1) {
            response.sendError(400, "Invalid test ID.");
            return;
        }

        try (Connection con = DBUtil.getConnection()) {

            con.setAutoCommit(false);

            try {
                boolean alreadyFinished;
                int questionLimit;

                String checkSql =
                    "SELECT is_finished, question_limit FROM tests "
                    + "WHERE test_id = ? AND created_by = ? FOR UPDATE";

                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setInt(1, testId);
                    ps.setInt(2, adminId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            response.sendError(
                                404,
                                "Test not found or not created by this administrator.");
                            return;
                        }

                        alreadyFinished = rs.getBoolean("is_finished");
                        questionLimit = rs.getInt("question_limit");
                    }
                }

                if (alreadyFinished) {
                    con.rollback();
                    showMessage(response, "Test Already Finished",
                        "This test has already been locked.");
                    return;
                }

                int questionCount;

                String countSql =
                    "SELECT COUNT(*) FROM questions WHERE test_id = ?";

                try (PreparedStatement ps = con.prepareStatement(countSql)) {
                    ps.setInt(1, testId);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        questionCount = rs.getInt(1);
                    }
                }

                if (questionCount == 0) {
                    con.rollback();
                    showMessage(response, "No Questions Found",
                        "Add at least one question before finishing the test.");
                    return;
                }

                if (questionLimit < 1 || questionCount > questionLimit) {
                    con.rollback();
                    showMessage(response, "Question Limit Error",
                        "This test allows " + questionLimit
                        + " question(s), but " + questionCount
                        + " are saved. Correct the question count before finishing.");
                    return;
                }

                String updateSql =
                    "UPDATE tests SET total_questions = ?, is_finished = 1 "
                    + "WHERE test_id = ? AND created_by = ? "
                    + "AND is_finished = 0";

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, questionCount);
                    ps.setInt(2, testId);
                    ps.setInt(3, adminId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        showMessage(response, "Unable to Finish Test",
                            "The test status changed. Please refresh and try again.");
                        return;
                    }
                }

                con.commit();

                showMessage(response, "Test Finished Successfully",
                    "Your test is now locked. Total questions: "
                    + questionCount + " out of " + questionLimit + ".");

            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }

        } catch (SQLException e) {
            getServletContext().log("Failed to finish test", e);
            response.sendError(
                500, "Unable to finish the test. Check server logs.");
        }
    }

    private void showMessage(HttpServletResponse response,
                             String title, String message)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html lang='en'><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>" + title + "</title>");
            out.println("<style>");
            out.println("body{font-family:Arial,sans-serif;min-height:100vh;"
                    + "display:flex;align-items:center;justify-content:center;"
                    + "padding:20px;background:#0b1020;color:#f1f5f9}");
            out.println(".card{max-width:500px;width:100%;text-align:center;"
                    + "background:#14192c;padding:32px;border:1px solid #303653;"
                    + "border-radius:18px}");
            out.println("h2{margin-bottom:16px;color:#67e8f9}");
            out.println("p{line-height:1.7;color:#cbd5e1}");
            out.println("a{display:inline-block;margin-top:22px;padding:12px 18px;"
                    + "background:linear-gradient(135deg,#7c3aed,#0891b2);"
                    + "color:white;text-decoration:none;border-radius:9px}");
            out.println("</style></head><body><main class='card'>");
            out.println("<h2>" + title + "</h2>");
            out.println("<p>" + message + "</p>");
            out.println("<a href='admin-dashboard.html'>Back to Admin Dashboard</a>");
            out.println("</main></body></html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "Use the Finish Test button to finish a test.");
    }
}
