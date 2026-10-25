package com.studenttest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AvailableTestsServlet")
public class AvailableTestsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !"STUDENT".equalsIgnoreCase(
                    String.valueOf(session.getAttribute("role")))) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Please log in as a student.");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String sql =
            "SELECT t.test_id, t.title, t.description, "
            + "t.duration_minutes, COUNT(q.question_id) AS question_count "
            + "FROM tests t "
            + "JOIN questions q ON t.test_id = q.test_id "
            + "GROUP BY t.test_id, t.title, t.description, "
            + "t.duration_minutes "
            + "ORDER BY t.created_at DESC";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter out = response.getWriter()) {

            String context = request.getContextPath();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<title>Available Tests | Student Online Test</title>");

            out.println("<style>");

            out.println("*{box-sizing:border-box;margin:0;padding:0}");

            out.println("body{"
                + "font-family:'Segoe UI',Arial,sans-serif;"
                + "min-height:100vh;"
                + "color:#f5f3ff;"
                + "background:"
                + "radial-gradient(ellipse at 10% 10%,#49258b44,transparent 40%),"
                + "radial-gradient(ellipse at 90% 80%,#07598533,transparent 40%),"
                + "#090b16;"
                + "}");

            out.println(".navbar{"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:space-between;"
                + "gap:16px;"
                + "padding:22px 6%;"
                + "border-bottom:1px solid #ffffff12;"
                + "background:#101225cc;"
                + "}");

            out.println(".brand{"
                + "font-size:21px;"
                + "font-weight:800;"
                + "letter-spacing:-.5px;"
                + "}");

            out.println(".brand span{color:#a78bfa}");

            out.println(".nav-link{"
                + "color:#c4b5fd;"
                + "text-decoration:none;"
                + "font-size:14px;"
                + "font-weight:600;"
                + "}");

            out.println(".nav-link:hover{color:#67e8f9}");

            out.println(".hero{"
                + "padding:45px 6% 30px;"
                + "}");

            out.println(".eyebrow{"
                + "display:inline-block;"
                + "padding:7px 12px;"
                + "border:1px solid #8b5cf644;"
                + "border-radius:20px;"
                + "background:#8b5cf615;"
                + "color:#c4b5fd;"
                + "font-size:12px;"
                + "font-weight:700;"
                + "letter-spacing:1px;"
                + "text-transform:uppercase;"
                + "margin-bottom:17px;"
                + "}");

            out.println("h1{"
                + "font-size:clamp(30px,4vw,43px);"
                + "letter-spacing:-1.2px;"
                + "line-height:1.2;"
                + "margin-bottom:12px;"
                + "}");

            out.println(".gradient-text{"
                + "background:linear-gradient(90deg,#c4b5fd,#67e8f9);"
                + "-webkit-background-clip:text;"
                + "background-clip:text;"
                + "-webkit-text-fill-color:transparent;"
                + "}");

            out.println(".subtitle{"
                + "color:#a1a1b5;"
                + "font-size:15px;"
                + "line-height:1.7;"
                + "max-width:650px;"
                + "}");

            out.println(".container{"
                + "width:88%;"
                + "max-width:1150px;"
                + "margin:12px auto 60px;"
                + "}");

            out.println(".section-heading{"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:space-between;"
                + "gap:12px;"
                + "margin-bottom:22px;"
                + "}");

            out.println(".section-heading h2{"
                + "font-size:20px;"
                + "font-weight:700;"
                + "}");

            out.println(".count{"
                + "color:#a5b4fc;"
                + "font-size:13px;"
                + "}");

            out.println(".test-grid{"
                + "display:grid;"
                + "grid-template-columns:repeat(auto-fit,minmax(270px,1fr));"
                + "gap:22px;"
                + "}");

            out.println(".card{"
                + "position:relative;"
                + "display:flex;"
                + "flex-direction:column;"
                + "min-width:0;"
                + "padding:26px;"
                + "border:1px solid #ffffff14;"
                + "border-radius:19px;"
                + "background:linear-gradient(145deg,#191b32,#111426);"
                + "box-shadow:0 14px 40px #0002;"
                + "transition:transform .2s,border-color .2s,box-shadow .2s;"
                + "}");

            out.println(".card:hover{"
                + "transform:translateY(-5px);"
                + "border-color:#8b5cf677;"
                + "box-shadow:0 20px 45px #0004;"
                + "}");

            out.println(".card-top{"
                + "display:flex;"
                + "align-items:center;"
                + "gap:14px;"
                + "margin-bottom:20px;"
                + "}");

            out.println(".test-icon{"
                + "width:48px;"
                + "height:48px;"
                + "flex-shrink:0;"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:center;"
                + "border-radius:14px;"
                + "background:linear-gradient(135deg,#7c3aed,#0891b2);"
                + "font-size:23px;"
                + "}");

            out.println(".test-label{"
                + "font-size:11px;"
                + "color:#67e8f9;"
                + "text-transform:uppercase;"
                + "letter-spacing:1px;"
                + "font-weight:700;"
                + "margin-bottom:5px;"
                + "}");

            out.println("h3{"
                + "font-size:20px;"
                + "line-height:1.4;"
                + "overflow-wrap:anywhere;"
                + "color:#f5f3ff;"
                + "}");

            out.println(".description{"
                + "color:#a1a1b5;"
                + "font-size:14px;"
                + "line-height:1.7;"
                + "margin-bottom:23px;"
                + "overflow-wrap:anywhere;"
                + "}");

            out.println(".meta{"
                + "display:flex;"
                + "flex-wrap:wrap;"
                + "gap:10px;"
                + "margin-top:auto;"
                + "margin-bottom:23px;"
                + "}");

            out.println(".meta-item{"
                + "padding:9px 12px;"
                + "border:1px solid #ffffff12;"
                + "border-radius:10px;"
                + "background:#0c1020;"
                + "color:#d4d4e8;"
                + "font-size:12px;"
                + "}");

            out.println(".meta-item strong{"
                + "color:#c4b5fd;"
                + "font-size:14px;"
                + "margin-right:4px;"
                + "}");

            out.println(".btn{"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:center;"
                + "gap:9px;"
                + "width:100%;"
                + "padding:13px 18px;"
                + "border-radius:11px;"
                + "background:linear-gradient(100deg,#7c3aed,#6366f1);"
                + "color:white;"
                + "text-decoration:none;"
                + "font-size:14px;"
                + "font-weight:700;"
                + "box-shadow:0 8px 22px #7c3aed22;"
                + "transition:filter .2s,transform .2s;"
                + "}");

            out.println(".btn:hover{"
                + "filter:brightness(1.15);"
                + "transform:translateY(-1px);"
                + "}");

            out.println(".empty{"
                + "padding:50px 24px;"
                + "border:1px dashed #ffffff25;"
                + "border-radius:18px;"
                + "background:#15172999;"
                + "text-align:center;"
                + "}");

            out.println(".empty-icon{"
                + "font-size:42px;"
                + "margin-bottom:16px;"
                + "}");

            out.println(".empty h3{margin-bottom:10px}");

            out.println(".empty p{"
                + "color:#a1a1b5;"
                + "font-size:14px;"
                + "line-height:1.7;"
                + "}");

            out.println(".footer{"
                + "padding:24px;"
                + "border-top:1px solid #ffffff12;"
                + "text-align:center;"
                + "color:#71738b;"
                + "font-size:12px;"
                + "}");

            out.println("@media(max-width:600px){"
                + ".navbar{padding:18px 5%;}"
                + ".brand{font-size:17px;}"
                + ".hero{padding:35px 5% 25px;}"
                + ".container{width:90%;}"
                + ".card{padding:21px;}"
                + ".section-heading{align-items:flex-start;flex-direction:column;}"
                + "}");

            out.println("</style>");
            out.println("</head>");
            out.println("<body>");

            // Navigation
            out.println("<nav class='navbar'>");
            out.println("<div class='brand'>Student <span>Online Test</span></div>");
            out.println("<a class='nav-link' href='" + context
                + "/student-dashboard.html'>"
                + "&#8592; Dashboard</a>");
            out.println("</nav>");

            // Page heading
            out.println("<section class='hero'>");
            out.println("<div class='eyebrow'>"
                + "&#10022; Student Learning Portal</div>");
            out.println("<h1>Available <span class='gradient-text'>Tests</span></h1>");
            out.println("<p class='subtitle'>"
                + "Choose a test, challenge your knowledge, and track your learning progress."
                + "</p>");
            out.println("</section>");

            out.println("<main class='container'>");
            out.println("<div class='section-heading'>");
            out.println("<h2>Explore your tests</h2>");

            boolean found = false;
            int testCount = 0;

            // Keep the original database query and test-opening route.
            // Buffer the test cards so the total count can be displayed.
            StringBuilder cards = new StringBuilder();

            while (rs.next()) {
                found = true;
                testCount++;

                int testId = rs.getInt("test_id");
                int questionCount = rs.getInt("question_count");
                int duration = rs.getInt("duration_minutes");

                String title = escapeHtml(rs.getString("title"));

                String description = rs.getString("description");
                description = escapeHtml(
                    description == null || description.trim().isEmpty()
                        ? "Get started and test your knowledge."
                        : description);

                cards.append("<article class='card'>");

                cards.append("<div class='card-top'>");
                cards.append("<div class='test-icon' aria-hidden='true'>")
                    .append("&#128221;</div>");

                cards.append("<div>");
                cards.append("<div class='test-label'>Knowledge Check</div>");
                cards.append("<h3>").append(title).append("</h3>");
                cards.append("</div>");
                cards.append("</div>");

                cards.append("<p class='description'>")
                    .append(description).append("</p>");

                cards.append("<div class='meta'>");

                cards.append("<div class='meta-item'>")
                    .append("<strong>").append(questionCount)
                    .append("</strong> Questions</div>");

                cards.append("<div class='meta-item'>")
                    .append("<strong>").append(duration)
                    .append("</strong> Minutes</div>");

                cards.append("</div>");

                cards.append("<a class='btn' href='")
                    .append(context)
                    .append("/TakeTestServlet?testId=")
                    .append(testId)
                    .append("'>Open Test <span>&#8594;</span></a>");

                cards.append("</article>");
            }

            out.println("<span class='count'>" + testCount
                + (testCount == 1 ? " test available" : " tests available")
                + "</span>");

            out.println("</div>");

            if (found) {
                out.println("<div class='test-grid'>");
                out.println(cards.toString());
                out.println("</div>");
            } else {
                out.println("<div class='empty'>");
                out.println("<div class='empty-icon'>&#128221;</div>");
                out.println("<h3>No tests available yet</h3>");
                out.println("<p>Tests will appear here once they have "
                    + "questions added by your administrator.</p>");
                out.println("</div>");
            }

            out.println("</main>");

            out.println("<footer class='footer'>");
            out.println("Student Online Test &copy; 2026");
            out.println("</footer>");

            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            getServletContext().log("Unable to load available tests", e);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to load tests right now.");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
