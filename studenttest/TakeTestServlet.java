
package com.studenttest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/TakeTestServlet")
public class TakeTestServlet extends HttpServlet {

    private static class Question {
        int id;
        String text;
        String a, b, c, d;
        String correct;
        int marks;

        Question(int id, String text, String a, String b,
                 String c, String d, String correct, int marks) {
            this.id = id;
            this.text = text;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.correct = correct;
            this.marks = marks;
        }
    }

    private boolean isStudent(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null ||
            !"STUDENT".equalsIgnoreCase(
                String.valueOf(session.getAttribute("role"))) ||
            !(session.getAttribute("studentId") instanceof Integer)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Please log in with a student account.");
            return false;
        }

        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        if (!isStudent(request, response)) return;

        int testId;
        try {
            testId = Integer.parseInt(request.getParameter("testId"));
            if (testId <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid test ID.");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String title = null;
        int duration = 30;
        List<Question> questions = new ArrayList<>();

        String testSql =
            "SELECT title, duration_minutes FROM tests WHERE test_id = ?";

        String questionSql =
            "SELECT question_id, question_text, option_a, option_b, " +
            "option_c, option_d, correct_option, marks " +
            "FROM questions WHERE test_id = ? ORDER BY question_id";

        try (Connection con = DBUtil.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(testSql)) {
                ps.setInt(1, testId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        title = rs.getString("title");
                        duration = rs.getInt("duration_minutes");
                    }
                }
            }

            if (title == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Test not found.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(questionSql)) {
                ps.setInt(1, testId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        questions.add(new Question(
                            rs.getInt("question_id"),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_option"),
                            rs.getInt("marks")
                        ));
                    }
                }
            }

        } catch (Exception e) {
            getServletContext().log("Unable to load test", e);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to load this test.");
            return;
        }

        if (questions.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "This test has no questions.");
            return;
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html lang='en'><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<title>" + escapeHtml(title) + " | Student Online Test</title>");

            out.println("<style>");
            out.println("*{box-sizing:border-box}");
            out.println("html{scroll-behavior:smooth}");
            out.println("body{margin:0;font-family:Arial,Helvetica,sans-serif;"
                + "background:#0b1020;color:#edf0ff;line-height:1.6}");
            out.println("header{background:rgba(18,22,43,.96);"
                + "border-bottom:1px solid #292e4d;padding:18px 6%;"
                + "display:flex;justify-content:space-between;align-items:center;"
                + "gap:16px;flex-wrap:wrap;position:sticky;top:0;z-index:10;"
                + "box-shadow:0 8px 25px rgba(0,0,0,.2)}");
            out.println(".brand{margin:0;font-size:21px;font-weight:800;"
                + "background:linear-gradient(90deg,#c4b5fd,#67e8f9);"
                + "background-clip:text;-webkit-background-clip:text;color:transparent}");
            out.println(".timer{background:#211d3d;border:1px solid #514080;"
                + "padding:9px 16px;border-radius:12px;font-weight:bold;"
                + "color:#67e8f9;min-width:130px;text-align:center}");
            out.println(".timer.warning{color:#fca5a5;border-color:#ef4444;"
                + "background:#351b2b}");
            out.println(".container{max-width:900px;margin:35px auto;padding:0 18px 50px}");
            out.println(".hero{background:linear-gradient(145deg,#191b32,#111426);"
                + "border:1px solid #303653;border-radius:22px;padding:28px;"
                + "margin-bottom:24px;box-shadow:0 15px 40px rgba(0,0,0,.2)}");
            out.println(".eyebrow{color:#67e8f9;font-size:12px;font-weight:bold;"
                + "letter-spacing:2px;text-transform:uppercase}");
            out.println("h1{font-size:clamp(25px,4vw,34px);line-height:1.25;"
                + "margin:10px 0;color:#fff;overflow-wrap:anywhere}");
            out.println(".muted{color:#a6aecb}");
            out.println(".stats{display:flex;gap:12px;flex-wrap:wrap;margin-top:20px}");
            out.println(".stat{background:#11162a;border:1px solid #303653;"
                + "border-radius:12px;padding:11px 15px;color:#c9d1ed;font-size:14px}");
            out.println(".question-card{background:#14192c;border:1px solid #2b3350;"
                + "border-radius:18px;padding:24px;margin:18px 0;"
                + "transition:border-color .2s,transform .2s}");
            out.println(".question-card:focus-within{border-color:#818cf8}");
            out.println(".question-top{display:flex;align-items:flex-start;"
                + "justify-content:space-between;gap:12px}");
            out.println(".question-number{color:#67e8f9;font-size:13px;"
                + "font-weight:bold;letter-spacing:1px}");
            out.println(".marks{font-size:12px;color:#c4b5fd;background:#292044;"
                + "padding:5px 10px;border-radius:20px;white-space:nowrap}");
            out.println(".question-text{font-size:18px;font-weight:600;"
                + "margin:12px 0 20px;color:#f1f5ff;overflow-wrap:anywhere}");
            out.println(".option{display:flex;align-items:flex-start;gap:12px;"
                + "padding:13px 15px;margin:9px 0;border:1px solid #303653;"
                + "border-radius:12px;background:#101528;cursor:pointer;"
                + "transition:all .18s;overflow-wrap:anywhere}");
            out.println(".option:hover{border-color:#818cf8;background:#1b2040}");
            out.println(".option:has(input:checked){border-color:#67e8f9;"
                + "background:#142c3a;box-shadow:0 0 0 1px #67e8f9}");
            out.println(".option input{accent-color:#22d3ee;margin-top:5px;"
                + "flex-shrink:0;width:16px;height:16px;cursor:pointer}");
            out.println(".option-label{color:#67e8f9;font-weight:bold;min-width:22px}");
            out.println(".option-text{color:#dce3fb}");
            out.println(".submit-area{text-align:center;padding:22px 0 10px}");
            out.println(".btn{display:inline-block;border:0;border-radius:12px;"
                + "padding:14px 25px;font-size:15px;font-weight:bold;"
                + "cursor:pointer;text-decoration:none;transition:transform .2s,"
                + "opacity .2s}");
            out.println(".btn:hover{transform:translateY(-2px)}");
            out.println(".btn-primary{color:#fff;background:linear-gradient(135deg,#7c3aed,#0891b2);"
                + "box-shadow:0 8px 24px rgba(124,58,237,.25)}");
            out.println(".btn-secondary{color:#dce3fb;background:#202640;"
                + "border:1px solid #3a4261}");
            out.println(".btn:disabled{opacity:.55;cursor:not-allowed;transform:none}");
            out.println(".notice{color:#a6aecb;font-size:13px;margin-top:14px}");
            out.println(".progress-track{height:5px;background:#292e49;"
                + "border-radius:10px;overflow:hidden;margin-top:18px}");
            out.println(".progress-fill{height:100%;width:0%;"
                + "background:linear-gradient(90deg,#8b5cf6,#22d3ee);"
                + "transition:width .2s}");
            out.println("footer{text-align:center;color:#737e9f;font-size:12px;"
                + "padding:20px}");
            out.println("@media(max-width:600px){header{padding:15px 18px}"
                + ".container{margin:20px auto;padding:0 12px 35px}"
                + ".hero,.question-card{padding:19px;border-radius:15px}"
                + ".question-text{font-size:16px}.btn{width:100%}}");
            out.println("</style></head><body>");

            out.println("<header>");
            out.println("<h2 class='brand'>Student Online Test</h2>");
            out.println("<div class='timer' id='timer'>Time: --:--</div>");
            out.println("</header>");

            out.println("<main class='container'>");

            out.println("<section class='hero'>");
            out.println("<div class='eyebrow'>Assessment in progress</div>");
            out.println("<h1>" + escapeHtml(title) + "</h1>");
            out.println("<p class='muted'>Read each question carefully and select one answer.</p>");

            out.println("<div class='stats'>");
            out.println("<div class='stat'>📝 " + questions.size() + " Questions</div>");
            out.println("<div class='stat'>⏱ " + duration + " Minutes</div>");
            out.println("<div class='stat'>✓ Single-choice answers</div>");
            out.println("</div>");

            out.println("<div class='progress-track'><div class='progress-fill' id='progressFill'></div></div>");
            out.println("<p class='muted' id='progressText' style='font-size:13px;margin-bottom:0'>0 of "
                + questions.size() + " answered</p>");
            out.println("</section>");

            out.println("<form id='testForm' method='post' action='TakeTestServlet'>");
            out.println("<input type='hidden' name='testId' value='" + testId + "'>");

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);

                out.println("<section class='question-card'>");
                out.println("<div class='question-top'>");
                out.println("<span class='question-number'>QUESTION " + (i + 1) + "</span>");
                out.println("<span class='marks'>" + q.marks + (q.marks == 1 ? " mark" : " marks") + "</span>");
                out.println("</div>");

                out.println("<h3 class='question-text'>" + escapeHtml(q.text) + "</h3>");

                String[] values = {"A", "B", "C", "D"};
                String[] options = {q.a, q.b, q.c, q.d};

                for (int j = 0; j < 4; j++) {
                    if (options[j] == null) continue;

                    out.println("<label class='option'>");
                    out.println("<input type='radio' name='q_" + q.id
                        + "' value='" + values[j] + "'>");
                    out.println("<span class='option-label'>" + values[j] + ".</span>");
                    out.println("<span class='option-text'>" + escapeHtml(options[j]) + "</span>");
                    out.println("</label>");
                }

                out.println("</section>");
            }

            out.println("<div class='submit-area'>");
            out.println("<button class='btn btn-primary' id='submitBtn' type='submit'>Submit Test &amp; Finish</button>");
            out.println("<p class='notice'>Your answers will be saved when you submit. "
                + "When time runs out, the test will submit automatically.</p>");
            out.println("<p style='margin-top:18px'><a class='btn btn-secondary' "
                + "href='student-dashboard.html'>Back to Dashboard</a></p>");
            out.println("</div>");

            out.println("</form>");
            out.println("</main>");
            out.println("<footer>Student Online Test &bull; Stay focused and do your best.</footer>");

            out.println("<script>");
            out.println("let seconds = " + (Math.max(0, duration) * 60) + ";");
            out.println("const timer = document.getElementById('timer');");
            out.println("const form = document.getElementById('testForm');");
            out.println("const submitBtn = document.getElementById('submitBtn');");
            out.println("const radios = form.querySelectorAll('input[type=radio]');");
            out.println("const progressFill = document.getElementById('progressFill');");
            out.println("const progressText = document.getElementById('progressText');");
            out.println("const totalQuestions = " + questions.size() + ";");
            out.println("let submitted = false;");
            out.println("function updateProgress(){");
            out.println("const groups = new Set();");
            out.println("radios.forEach(r=>{if(r.checked)groups.add(r.name);});");
            out.println("const count=groups.size;");
            out.println("progressFill.style.width=(count/totalQuestions*100)+'%';");
            out.println("progressText.textContent=count+' of '+totalQuestions+' answered';");
            out.println("}");
            out.println("radios.forEach(r=>r.addEventListener('change',updateProgress));");
            out.println("function updateTimer(){");
            out.println("const m=Math.floor(seconds/60);");
            out.println("const s=seconds%60;");
            out.println("timer.textContent='Time: '+String(m).padStart(2,'0')+':'+String(s).padStart(2,'0');");
            out.println("if(seconds<=60)timer.classList.add('warning');");
            out.println("if(seconds<=0){");
            out.println("if(!submitted){submitted=true;submitBtn.disabled=true;form.submit();}");
            out.println("return;");
            out.println("}");
            out.println("seconds--;");
            out.println("}");
            out.println("form.addEventListener('submit',function(e){");
            out.println("if(submitted){e.preventDefault();return;}");
            out.println("if(!confirm('Submit this test now? You cannot change your answers after submission.')){");
            out.println("e.preventDefault();return;");
            out.println("}");
            out.println("submitted=true;submitBtn.disabled=true;");
            out.println("submitBtn.textContent='Submitting...';");
            out.println("});");
            out.println("updateProgress();updateTimer();");
            out.println("setInterval(()=>{if(!submitted)updateTimer();},1000);");
            out.println("</script></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        if (!isStudent(request, response)) return;

        int testId;
        try {
            testId = Integer.parseInt(request.getParameter("testId"));
            if (testId <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid test ID.");
            return;
        }

        HttpSession session = request.getSession(false);
        int studentId = (Integer) session.getAttribute("studentId");

        List<Question> questions = new ArrayList<>();

        String testSql =
            "SELECT question_id, question_text, option_a, option_b, " +
            "option_c, option_d, correct_option, marks " +
            "FROM questions WHERE test_id = ? ORDER BY question_id";

        int score = 0;
        int totalMarks = 0;

        try (Connection con = DBUtil.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(testSql)) {
                ps.setInt(1, testId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Question q = new Question(
                            rs.getInt("question_id"),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_option"),
                            rs.getInt("marks")
                        );

                        questions.add(q);
                        totalMarks += q.marks;

                        String selected = request.getParameter("q_" + q.id);

                        if (selected != null &&
                            selected.matches("[A-D]") &&
                            selected.equalsIgnoreCase(q.correct)) {
                            score += q.marks;
                        }
                    }
                }
            }

            if (questions.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "No questions found for this test.");
                return;
            }

            con.setAutoCommit(false);

            try {
                int attemptId;

                String insertAttempt =
                    "INSERT INTO test_attempts " +
                    "(test_id, student_id, score, total_marks, submitted_at) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

                try (PreparedStatement ps = con.prepareStatement(
                        insertAttempt,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, testId);
                    ps.setInt(2, studentId);
                    ps.setInt(3, score);
                    ps.setInt(4, totalMarks);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "Could not retrieve attempt ID.");
                        }
                        attemptId = keys.getInt(1);
                    }
                }

                String insertAnswer =
                    "INSERT INTO student_answers " +
                    "(attempt_id, question_id, selected_option) " +
                    "VALUES (?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(insertAnswer)) {
                    for (Question q : questions) {
                        String selected = request.getParameter("q_" + q.id);

                        if (selected != null && selected.matches("[A-D]")) {
                            ps.setInt(1, attemptId);
                            ps.setInt(2, q.id);
                            ps.setString(3, selected);
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();

                if (e.getSQLState() != null &&
                    e.getSQLState().startsWith("23")) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println(
                        "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>"
                        + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                        + "<style>"
                        + "body{margin:0;background:#0b1020;color:#edf0ff;font-family:Arial;"
                        + "display:grid;place-items:center;min-height:100vh;padding:20px;box-sizing:border-box}"
                        + ".card{background:#14192c;border:1px solid #303653;border-radius:20px;"
                        + "padding:35px;max-width:480px;text-align:center}"
                        + "a{display:inline-block;background:linear-gradient(135deg,#7c3aed,#0891b2);"
                        + "padding:12px 20px;border-radius:10px;color:white;text-decoration:none;margin-top:15px}"
                        + "</style></head><body><div class='card'>"
                        + "<h2>Test Already Submitted</h2>"
                        + "<p>Your submission for this test has already been recorded.</p>"
                        + "<a href='student-dashboard.html'>Back to Dashboard</a>"
                        + "</div></body></html>");
                    return;
                }

                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            getServletContext().log("Unable to save test submission", e);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Could not save your result. Please contact the administrator.");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html lang='en'><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width,initial-scale=1'>");
            out.println("<title>Test Result | Student Online Test</title>");

            out.println("<style>");
            out.println("*{box-sizing:border-box}");
            out.println("body{margin:0;min-height:100vh;display:grid;place-items:center;"
                + "padding:20px;font-family:Arial,sans-serif;background:#0b1020;color:#edf0ff}");
            out.println(".card{background:linear-gradient(145deg,#191b32,#111426);"
                + "border:1px solid #303653;border-radius:24px;padding:40px 30px;"
                + "max-width:520px;width:100%;text-align:center;"
                + "box-shadow:0 20px 60px rgba(0,0,0,.3)}");
            out.println(".icon{width:70px;height:70px;margin:0 auto 20px;border-radius:50%;"
                + "display:grid;place-items:center;background:#173b3c;color:#67e8f9;font-size:34px}");
            out.println(".eyebrow{color:#67e8f9;font-size:12px;letter-spacing:2px;font-weight:bold}");
            out.println("h1{font-size:30px;margin:12px 0;color:#fff}");
            out.println(".score{font-size:clamp(32px,8vw,46px);font-weight:800;margin:25px 0;"
                + "background:linear-gradient(90deg,#c4b5fd,#67e8f9);"
                + "background-clip:text;-webkit-background-clip:text;color:transparent}");
            out.println("p{color:#a6aecb;line-height:1.7}");
            out.println(".btn{display:inline-block;margin:8px 4px;padding:12px 18px;"
                + "border-radius:10px;text-decoration:none;font-weight:bold;font-size:14px}");
            out.println(".primary{color:white;background:linear-gradient(135deg,#7c3aed,#0891b2)}");
            out.println(".secondary{color:#dce3fb;background:#202640;border:1px solid #3a4261}");
            out.println("</style></head><body>");

            out.println("<main class='card'>");
            out.println("<div class='icon'>&#10003;</div>");
            out.println("<div class='eyebrow'>ASSESSMENT COMPLETED</div>");
            out.println("<h1>Test Submitted!</h1>");
            out.println("<p>Your answers have been submitted successfully.</p>");
            out.println("<div class='score'>" + score + " / " + totalMarks + "</div>");
            out.println("<p>You can return to your dashboard to continue.</p>");
            out.println("<a class='btn primary' href='student-dashboard.html'>Dashboard</a>");
            out.println("<a class='btn secondary' href='result.html'>My Results</a>");
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
}
