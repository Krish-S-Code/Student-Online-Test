package com.studenttest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        response.sendRedirect("login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null
                || email.isBlank() || password.isEmpty()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Email and password are required."
            );
            return;
        }

        String sql = "SELECT student_id, name, password_hash, role "
                   + "FROM students WHERE email = ?";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next() && PasswordUtil.verifyPassword(
                        password, rs.getString("password_hash"))) {

                    String role = rs.getString("role");

                    // Invalidate the previous session.
                    HttpSession oldSession = request.getSession(false);

                    if (oldSession != null) {
                        oldSession.invalidate();
                    }

                    // Create a new session.
                    HttpSession session = request.getSession(true);

                    session.setAttribute(
                        "studentId",
                        Integer.valueOf(rs.getInt("student_id"))
                    );

                    session.setAttribute(
                        "studentName",
                        rs.getString("name")
                    );

                    session.setAttribute("role", role);
                    session.setMaxInactiveInterval(30 * 60);

                    if ("ADMIN".equalsIgnoreCase(role)) {
                        response.sendRedirect("admin-dashboard.html");

                    } else if ("STUDENT".equalsIgnoreCase(role)) {
                        response.sendRedirect("student-dashboard.html");

                    } else {
                        session.invalidate();

                        response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Unknown account role."
                        );
                    }

                    return;
                }
            }

            // Redesigned invalid-login page.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/html;charset=UTF-8");

            try (PrintWriter out = response.getWriter()) {

                out.println("<!DOCTYPE html>");
                out.println("<html lang='en'>");
                out.println("<head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                out.println("<title>Login Failed | Student Online Test</title>");

                out.println("<style>");
                out.println("*{box-sizing:border-box}");
                out.println("body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;font-family:'Segoe UI',Arial,sans-serif;color:#f4f5ff;background:radial-gradient(circle at top left,#25204b,transparent 45%),radial-gradient(circle at bottom right,rgba(34,211,238,.10),transparent 35%),#080b18}");
                out.println(".card{width:100%;max-width:440px;padding:42px 32px;text-align:center;background:#10152a;border:1px solid #292d48;border-radius:22px;box-shadow:0 25px 70px rgba(0,0,0,.4)}");
                out.println(".brand-icon{width:58px;height:58px;display:grid;place-items:center;margin:0 auto 26px;border-radius:16px;color:white;background:linear-gradient(135deg,#8b5cf6,#6366f1);font-size:28px}");
                out.println(".error-icon{width:76px;height:76px;display:grid;place-items:center;margin:0 auto 24px;border-radius:50%;color:#fca5a5;background:rgba(239,68,68,.12);border:1px solid rgba(248,113,113,.3);font-size:36px}");
                out.println("h1{margin:0 0 14px;font-size:27px;letter-spacing:-.5px}");
                out.println("p{margin:0 0 28px;color:#a0a8c5;font-size:14px;line-height:1.8}");
                out.println(".button{display:block;width:100%;padding:15px;border-radius:10px;color:white;background:linear-gradient(100deg,#8b5cf6,#6366f1);text-decoration:none;font-size:14px;font-weight:700;transition:transform .2s}");
                out.println(".button:hover{transform:translateY(-2px)}");
                out.println(".home{display:inline-block;margin-top:22px;color:#22d3ee;font-size:13px;text-decoration:none}");
                out.println(".home:hover{text-decoration:underline}");
                out.println(".brand{margin-top:30px;color:#66708e;font-size:11px;line-height:1.6}");
                out.println("@media(max-width:400px){.card{padding:32px 22px}h1{font-size:24px}}");
                out.println("</style>");

                out.println("</head>");
                out.println("<body>");

                out.println("<main class='card'>");

                out.println("<div class='brand-icon' aria-hidden='true'>✦</div>");
                out.println("<div class='error-icon' aria-hidden='true'>!</div>");

                out.println("<h1>Login unsuccessful</h1>");

                out.println("<p>");
                out.println("We couldn't verify your email and password.");
                out.println(" Please check your details and try again.");
                out.println("</p>");

                out.println("<a class='button' href='login.html'>Try Again &rarr;</a>");

                out.println("<a class='home' href='index.html'>&larr; Back to Home</a>");

                out.println("<div class='brand'>");
                out.println("Student Online Test &amp; Grade Evaluation System");
                out.println("</div>");

                out.println("</main>");
                out.println("</body>");
                out.println("</html>");
            }

        } catch (Exception e) {

            getServletContext().log(
                "Login failed due to server error", e
            );

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to log in right now. Please try again."
            );
        }
    }
}
