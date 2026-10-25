
package com.studenttest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ResultsServlet")
public class ResultsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !"STUDENT".equalsIgnoreCase(
                    String.valueOf(session.getAttribute("role")))
                || !(session.getAttribute("studentId") instanceof Integer)) {
            response.sendRedirect("login.html");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        response.setContentType("text/html;charset=UTF-8");

        String sql =
            "SELECT t.title, ta.score, ta.total_marks, ta.submitted_at "
            + "FROM test_attempts ta "
            + "JOIN tests t ON ta.test_id = t.test_id "
            + "WHERE ta.student_id = ? "
            + "ORDER BY ta.submitted_at DESC";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             PrintWriter out = response.getWriter()) {

            ps.setInt(1, studentId);

            out.println("<!DOCTYPE html><html lang='en'><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<title>My Results | Student Online Test</title>");

            out.println("<style>");
            out.println("*{box-sizing:border-box}");
            out.println("body{margin:0;font-family:Arial,Helvetica,sans-serif;"
                + "background:#0b1020;color:#edf0ff;line-height:1.6}");
            out.println("header{background:rgba(18,22,43,.96);"
                + "border-bottom:1px solid #292e4d;padding:20px 6%;"
                + "display:flex;justify-content:space-between;align-items:center;"
                + "flex-wrap:wrap;gap:12px}");
            out.println(".brand{margin:0;font-size:22px;font-weight:800;"
                + "background:linear-gradient(90deg,#c4b5fd,#67e8f9);"
                + "background-clip:text;-webkit-background-clip:text;color:transparent}");
            out.println(".header-tag{font-size:12px;color:#67e8f9;"
                + "background:#142c3a;border:1px solid #28515e;"
                + "padding:7px 12px;border-radius:20px}");
            out.println(".container{max-width:950px;margin:35px auto;padding:0 18px 45px}");
            out.println(".eyebrow{font-size:12px;letter-spacing:2px;"
                + "font-weight:bold;color:#67e8f9;text-transform:uppercase}");
            out.println("h1{font-size:clamp(27px,4vw,36px);margin:8px 0;color:#fff}");
            out.println(".subtitle{color:#a6aecb;margin-bottom:28px}");
            out.println(".summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));"
                + "gap:15px;margin-bottom:25px}");
            out.println(".summary-card{background:linear-gradient(145deg,#191b32,#111426);"
                + "border:1px solid #303653;padding:20px;border-radius:16px}");
            out.println(".summary-label{font-size:13px;color:#a6aecb}");
            out.println(".summary-value{font-size:27px;font-weight:bold;margin-top:6px;"
                + "color:#67e8f9;overflow-wrap:anywhere}");
            out.println(".result-card{background:linear-gradient(145deg,#191b32,#111426);"
                + "border:1px solid #303653;padding:24px;margin:17px 0;"
                + "border-radius:18px;box-shadow:0 12px 30px rgba(0,0,0,.15)}");
            out.println(".result-top{display:flex;justify-content:space-between;"
                + "align-items:flex-start;gap:12px;flex-wrap:wrap}");
            out.println(".test-title{font-size:20px;color:#fff;margin:0;overflow-wrap:anywhere}");
            out.println(".grade{display:inline-block;padding:7px 13px;border-radius:10px;"
                + "font-weight:bold;background:#292044;color:#c4b5fd;"
                + "border:1px solid #514080}");
            out.println(".score{font-size:27px;font-weight:bold;margin:17px 0 5px;"
                + "background:linear-gradient(90deg,#c4b5fd,#67e8f9);"
                + "background-clip:text;-webkit-background-clip:text;color:transparent}");
            out.println(".details{display:flex;flex-wrap:wrap;gap:10px 22px;"
                + "font-size:13px;color:#a6aecb;margin:12px 0 18px}");
            out.println(".progress{height:8px;border-radius:20px;"
                + "background:#292e49;overflow:hidden}");
            out.println(".progress-fill{height:100%;border-radius:20px;"
                + "background:linear-gradient(90deg,#7c3aed,#22d3ee)}");
            out.println(".empty{background:#14192c;border:1px dashed #3a4261;"
                + "padding:35px 22px;text-align:center;border-radius:18px}");
            out.println(".empty-icon{font-size:35px;margin-bottom:10px}");
            out.println(".empty h2{margin:5px 0;color:#fff}");
            out.println(".muted{color:#a6aecb}");
            out.println(".btn{display:inline-block;margin-top:20px;padding:12px 20px;"
                + "border-radius:11px;text-decoration:none;font-weight:bold;"
                + "font-size:14px;transition:transform .2s}");
            out.println(".btn:hover{transform:translateY(-2px)}");
            out.println(".primary{color:white;background:linear-gradient(135deg,#7c3aed,#0891b2)}");
            out.println(".secondary{color:#dce3fb;background:#202640;"
                + "border:1px solid #3a4261;margin-left:8px}");
            out.println("footer{text-align:center;color:#737e9f;font-size:12px;padding:20px}");
            out.println("@media(max-width:600px){header{padding:16px 18px}"
                + ".container{padding:0 12px 30px;margin-top:24px}"
                + ".result-card{padding:19px}.score{font-size:24px}"
                + ".secondary{margin-left:0}.btn{margin-right:5px}}");
            out.println("</style></head><body>");

            out.println("<header>");
            out.println("<h2 class='brand'>Student Online Test</h2>");
            out.println("<span class='header-tag'>MY RESULTS</span>");
            out.println("</header>");

            out.println("<main class='container'>");
            out.println("<div class='eyebrow'>Your performance</div>");
            out.println("<h1>My Test Results</h1>");
            out.println("<p class='subtitle'>Review your scores, grades, and previous test submissions.</p>");

            boolean found = false;
            int attempts = 0;
            int totalScore = 0;
            int totalPossible = 0;

            // Buffer results so summary statistics can be displayed first.
            StringBuilder resultCards = new StringBuilder();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    found = true;
                    attempts++;

                    String title = escapeHtml(rs.getString("title"));
                    int score = rs.getInt("score");
                    int total = rs.getInt("total_marks");

                    totalScore += score;
                    totalPossible += total;

                    double percentage = total > 0
                        ? score * 100.0 / total : 0.0;

                    String grade;
                    if (percentage >= 90) {
                        grade = "A+";
                    } else if (percentage >= 80) {
                        grade = "A";
                    } else if (percentage >= 70) {
                        grade = "B";
                    } else if (percentage >= 60) {
                        grade = "C";
                    } else if (percentage >= 50) {
                        grade = "D";
                    } else {
                        grade = "F";
                    }

                    Timestamp submittedAt = rs.getTimestamp("submitted_at");
                    String date = submittedAt == null
                        ? "Not available" : submittedAt.toString();

                    resultCards.append("<section class='result-card'>");
                    resultCards.append("<div class='result-top'>");
                    resultCards.append("<h2 class='test-title'>")
                        .append(title).append("</h2>");
                    resultCards.append("<span class='grade'>Grade: ")
                        .append(grade).append("</span>");
                    resultCards.append("</div>");

                    resultCards.append("<div class='score'>")
                        .append(score).append(" / ").append(total)
                        .append("</div>");

                    resultCards.append("<div class='details'>");
                    resultCards.append("<span>Percentage: ")
                        .append(String.format(java.util.Locale.US, "%.2f", percentage))
                        .append("%</span>");
                    resultCards.append("<span>Submitted: ")
                        .append(escapeHtml(date)).append("</span>");
                    resultCards.append("</div>");

                    double progress = Math.max(0, Math.min(100, percentage));

                    resultCards.append("<div class='progress'>");
                    resultCards.append("<div class='progress-fill' style='width:")
                        .append(String.format(java.util.Locale.US, "%.2f", progress))
                        .append("%'></div>");
                    resultCards.append("</div>");
                    resultCards.append("</section>");
                }
            }

            if (!found) {
                out.println("<section class='empty'>");
                out.println("<div class='empty-icon'>&#128221;</div>");
                out.println("<h2>No results yet</h2>");
                out.println("<p class='muted'>Complete a test to see your scores and grades here.</p>");
                out.println("<a class='btn primary' href='AvailableTestsServlet'>Browse Available Tests</a>");
                out.println("</section>");
            } else {
                double overallPercentage = totalPossible > 0
                    ? totalScore * 100.0 / totalPossible : 0.0;

                out.println("<section class='summary'>");

                out.println("<div class='summary-card'>");
                out.println("<div class='summary-label'>Tests Completed</div>");
                out.println("<div class='summary-value'>" + attempts + "</div>");
                out.println("</div>");

                out.println("<div class='summary-card'>");
                out.println("<div class='summary-label'>Combined Score</div>");
                out.println("<div class='summary-value'>" + totalScore
                    + " / " + totalPossible + "</div>");
                out.println("</div>");

                out.println("<div class='summary-card'>");
                out.println("<div class='summary-label'>Overall Percentage</div>");
                out.println("<div class='summary-value'>"
                    + String.format(java.util.Locale.US, "%.2f", overallPercentage)
                    + "%</div>");
                out.println("</div>");

                out.println("</section>");

                out.println(resultCards.toString());
            }

            out.println("<a class='btn primary' href='student-dashboard.html'>Back to Dashboard</a>");
            out.println("<a class='btn secondary' href='AvailableTestsServlet'>Available Tests</a>");
            out.println("</main>");
            out.println("<footer>Student Online Test &bull; Keep learning and improving.</footer>");
            out.println("</body></html>");

        } catch (Exception e) {
            getServletContext().log("Unable to load student results", e);
            if (!response.isCommitted()) {
                response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load results right now.");
            }
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
}
