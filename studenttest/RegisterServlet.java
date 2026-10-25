package com.studenttest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (PrintWriter out = response.getWriter()) {

            // Validate registration details.
            if (name == null || email == null || password == null
                    || name.isBlank() || email.isBlank()
                    || password.length() < 8) {

                response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

                showPage(
                    out,
                    "Invalid registration details",
                    "Please enter your name and email. Your password must contain at least 8 characters.",
                    "!",
                    "Try Again",
                    "register.html",
                    false
                );
                return;
            }

            String sql = "INSERT INTO students "
                + "(name, email, password_hash) VALUES (?, ?, ?)";

            String passwordHash =
                PasswordUtil.hashPassword(password);

            try (Connection con = DBUtil.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name.trim());
                ps.setString(2, email.trim());
                ps.setString(3, passwordHash);

                ps.executeUpdate();

                showPage(
                    out,
                    "Registration successful!",
                    "Your account has been created. You can now sign in to your Student Online Test account.",
                    "✓",
                    "Go to Login",
                    "login.html",
                    true
                );

            } catch (SQLException e) {

                if ("23000".equals(e.getSQLState())) {

                    response.setStatus(
                        HttpServletResponse.SC_CONFLICT);

                    showPage(
                        out,
                        "Email already registered",
                        "An account with this email may already exist. Please try another email or sign in to your account.",
                        "!",
                        "Try Again",
                        "register.html",
                        false
                    );

                } else {

                    getServletContext().log(
                        "Registration database error", e);

                    response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                    showPage(
                        out,
                        "Registration unsuccessful",
                        "We couldn't create your account right now. Please try again later.",
                        "!",
                        "Try Again",
                        "register.html",
                        false
                    );
                }
            }
        }
    }

    private void showPage(PrintWriter out,
                          String title,
                          String message,
                          String icon,
                          String buttonText,
                          String buttonLink,
                          boolean success) {

        String iconColor = success ? "#6ee7b7" : "#fca5a5";
        String iconBackground = success
            ? "rgba(16,185,129,.12)"
            : "rgba(239,68,68,.12)";
        String iconBorder = success
            ? "rgba(110,231,183,.3)"
            : "rgba(248,113,113,.3)";

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + " | Student Online Test</title>");

        out.println("<style>");
        out.println("*{box-sizing:border-box}");
        out.println("body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;font-family:'Segoe UI',Arial,sans-serif;color:#f4f5ff;background:radial-gradient(circle at top left,#25204b,transparent 45%),radial-gradient(circle at bottom right,rgba(34,211,238,.10),transparent 35%),#080b18}");
        out.println(".card{width:100%;max-width:460px;padding:42px 32px;text-align:center;background:#10152a;border:1px solid #292d48;border-radius:22px;box-shadow:0 25px 70px rgba(0,0,0,.4)}");
        out.println(".brand-icon{width:58px;height:58px;display:grid;place-items:center;margin:0 auto 26px;border-radius:16px;color:white;background:linear-gradient(135deg,#8b5cf6,#6366f1);font-size:28px}");
        out.println(".status-icon{width:76px;height:76px;display:grid;place-items:center;margin:0 auto 24px;border-radius:50%;font-size:36px;color:" + iconColor + ";background:" + iconBackground + ";border:1px solid " + iconBorder + "}");
        out.println("h1{margin:0 0 14px;font-size:27px;letter-spacing:-.5px;line-height:1.3}");
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
        out.println("<div class='status-icon' aria-hidden='true'>"
            + icon + "</div>");

        out.println("<h1>" + title + "</h1>");
        out.println("<p>" + message + "</p>");

        out.println("<a class='button' href='" + buttonLink
            + "'>" + buttonText + " &rarr;</a>");

        out.println("<a class='home' href='index.html'>&larr; Back to Home</a>");

        out.println("<div class='brand'>");
        out.println("Student Online Test &amp; Grade Evaluation System");
        out.println("</div>");

        out.println("</main>");
        out.println("</body>");
        out.println("</html>");
    }
}