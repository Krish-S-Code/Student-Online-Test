
package com.studenttest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AddQuestionServlet")
public class AddQuestionServlet extends HttpServlet {

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
            response.sendError(403, "Please log in as an administrator.");
            return;
        }

        int adminId = (Integer) session.getAttribute("studentId");

        String testIdText = request.getParameter("testId");
        String question = request.getParameter("question");
        String optionA = request.getParameter("optionA");
        String optionB = request.getParameter("optionB");
        String optionC = request.getParameter("optionC");
        String optionD = request.getParameter("optionD");
        String correctOption = request.getParameter("correctOption");
        String marksText = request.getParameter("marks");

        if (testIdText == null || question == null
                || optionA == null || optionB == null
                || optionC == null || optionD == null
                || correctOption == null || marksText == null
                || question.isBlank() || optionA.isBlank()
                || optionB.isBlank() || optionC.isBlank()
                || optionD.isBlank()) {
            response.sendError(400, "Please complete all question fields.");
            return;
        }

        int testId;
        int marks;

        try {
            testId = Integer.parseInt(testIdText.trim());
            marks = Integer.parseInt(marksText.trim());
        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid test ID or marks.");
            return;
        }

        correctOption = correctOption.trim();

        if (testId < 1 || marks < 1 || marks > 100
                || question.trim().length() > 5000
                || optionA.trim().length() > 500
                || optionB.trim().length() > 500
                || optionC.trim().length() > 500
                || optionD.trim().length() > 500
                || !correctOption.matches("[ABCD]")) {
            response.sendError(400, "One or more fields are invalid.");
            return;
        }

        try (Connection con = DBUtil.getConnection()) {

            con.setAutoCommit(false);

            try {
                int questionLimit;
                boolean finished;

                // Lock the test row before checking the question count.
                String checkSql =
                    "SELECT is_finished, question_limit FROM tests "
                    + "WHERE test_id = ? AND created_by = ? FOR UPDATE";

                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setInt(1, testId);
                    ps.setInt(2, adminId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            showMessage(response, "Test Not Found",
                                "This test was not found or does not belong to your administrator account.");
                            return;
                        }

                        finished = rs.getBoolean("is_finished");
                        questionLimit = rs.getInt("question_limit");
                    }
                }

                if (finished) {
                    con.rollback();
                    showMessage(response, "Test Already Finished",
                        "This test is locked. You cannot add more questions.");
                    return;
                }

                if (questionLimit < 1) {
                    con.rollback();
                    showMessage(response, "Invalid Question Limit",
                        "The configured question limit is invalid. Please check the test settings.");
                    return;
                }

                int currentCount;

                String countSql =
                    "SELECT COUNT(*) FROM questions WHERE test_id = ?";

                try (PreparedStatement ps = con.prepareStatement(countSql)) {
                    ps.setInt(1, testId);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        currentCount = rs.getInt(1);
                    }
                }

                if (currentCount >= questionLimit) {
                    con.rollback();
                    showMessage(response, "Question Limit Reached",
                        "This test allows only " + questionLimit
                        + " question(s). You have already added "
                        + currentCount + ". No more questions can be added.");
                    return;
                }

                String insertSql =
                    "INSERT INTO questions "
                    + "(test_id, question_text, option_a, option_b, "
                    + "option_c, option_d, correct_option, marks) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, testId);
                    ps.setString(2, question.trim());
                    ps.setString(3, optionA.trim());
                    ps.setString(4, optionB.trim());
                    ps.setString(5, optionC.trim());
                    ps.setString(6, optionD.trim());
                    ps.setString(7, correctOption);
                    ps.setInt(8, marks);

                    ps.executeUpdate();
                }

                // Keep actual count updated without changing question_limit.
                String updateSql =
                    "UPDATE tests SET total_questions = ? "
                    + "WHERE test_id = ? AND created_by = ?";

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, currentCount + 1);
                    ps.setInt(2, testId);
                    ps.setInt(3, adminId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "Could not update the actual question count.");
                    }
                }

                con.commit();

                response.sendRedirect(
                    "add-question.html?testId=" + testId + "&saved=1");

            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }

        } catch (SQLException e) {
            getServletContext().log("Failed to save question", e);
            response.sendError(
                500, "Unable to save the question. Check server logs.");
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
            out.println("<title>" + escapeHtml(title) + " | Student Online Test</title>");
            out.println("<style>");
            out.println("*{box-sizing:border-box;margin:0;padding:0}");
            out.println("body{font-family:Arial,sans-serif;min-height:100vh;"
                    + "display:flex;align-items:center;justify-content:center;"
                    + "padding:24px;color:#f1f5f9;"
                    + "background:radial-gradient(ellipse at top left,#25204b,#0b1020 65%,#070b16)}");
            out.println(".card{width:100%;max-width:520px;text-align:center;"
                    + "background:#14192c;padding:38px 30px;border:1px solid #303653;"
                    + "border-radius:22px;box-shadow:0 22px 65px #0006}");
            out.println("h1{font-size:25px;margin-bottom:14px;color:#fff}");
            out.println("p{color:#b8c2d9;font-size:15px;line-height:1.7;margin-bottom:26px}");
            out.println("a{display:inline-block;margin:8px;padding:13px 18px;"
                    + "border-radius:11px;background:linear-gradient(135deg,#7c3aed,#0891b2);"
                    + "color:white;text-decoration:none;font-weight:bold}");
            out.println("</style></head><body><main class='card'>");
            out.println("<h1>" + escapeHtml(title) + "</h1>");
            out.println("<p>" + escapeHtml(message) + "</p>");
            out.println("<a href='add-question.html'>Back to Add Question</a>");
            out.println("<a href='admin-dashboard.html'>Admin Dashboard</a>");
            out.println("</main></body></html>");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "Submit the question form to save a question.");
    }
}
