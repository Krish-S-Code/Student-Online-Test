
package com.studenttest;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("studentId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            String sql = "SELECT student_id, name, email, role, created_at "
                       + "FROM students WHERE student_id = ?";

            try (Connection con = DBUtil.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    out.println("<!DOCTYPE html>");
                    out.println("<html lang='en'><head>");
                    out.println("<meta charset='UTF-8'>");
                    out.println("<meta name='viewport' "
                              + "content='width=device-width, initial-scale=1.0'>");
                    out.println("<title>My Profile | Student Online Test</title>");

                    out.println("<style>");
                    out.println("*{box-sizing:border-box;margin:0;padding:0}");
                    out.println("body{font-family:Arial,sans-serif;"
                              + "min-height:100vh;padding:35px 18px;"
                              + "background:radial-gradient(circle at top left,"
                              + "#25204b,#0b1020 55%);color:#e5e7eb}");

                    out.println(".container{max-width:650px;margin:35px auto}");
                    out.println(".brand{color:#67e8f9;font-size:13px;"
                              + "font-weight:bold;letter-spacing:2px;"
                              + "text-transform:uppercase;margin-bottom:12px}");

                    out.println("h1{font-size:32px;margin-bottom:10px;"
                              + "color:#fff}");
                    out.println(".subtitle{color:#a5b4cc;margin-bottom:28px;"
                              + "line-height:1.6}");

                    out.println(".card{background:rgba(20,25,44,.95);"
                              + "border:1px solid #303853;border-radius:20px;"
                              + "padding:30px;box-shadow:0 20px 50px "
                              + "rgba(0,0,0,.25)}");

                    out.println(".avatar{width:75px;height:75px;"
                              + "border-radius:50%;display:flex;"
                              + "align-items:center;justify-content:center;"
                              + "background:linear-gradient(135deg,"
                              + "#7c3aed,#0891b2);font-size:30px;"
                              + "font-weight:bold;color:white;margin-bottom:18px}");

                    out.println(".profile-name{font-size:23px;"
                              + "font-weight:bold;color:white;margin-bottom:7px}");
                    out.println(".role{display:inline-block;padding:6px 12px;"
                              + "border-radius:20px;background:#12323d;"
                              + "color:#67e8f9;font-size:12px;"
                              + "font-weight:bold;margin-bottom:25px}");

                    out.println(".details{display:grid;grid-template-columns:"
                              + "1fr 1fr;gap:15px}");
                    out.println(".detail{background:#0f1528;"
                              + "border:1px solid #2b344c;"
                              + "border-radius:12px;padding:16px;"
                              + "min-width:0}");
                    out.println(".detail-label{font-size:12px;"
                              + "color:#94a3b8;margin-bottom:9px;"
                              + "text-transform:uppercase;letter-spacing:1px}");
                    out.println(".detail-value{font-size:15px;"
                              + "color:#f1f5f9;overflow-wrap:anywhere;"
                              + "line-height:1.5}");

                    out.println(".actions{display:flex;flex-wrap:wrap;"
                              + "gap:12px;margin-top:25px}");
                    out.println(".btn{display:inline-block;text-align:center;"
                              + "padding:12px 18px;border-radius:10px;"
                              + "text-decoration:none;font-weight:bold;"
                              + "font-size:14px;transition:.2s}");
                    out.println(".primary{background:linear-gradient(135deg,"
                              + "#7c3aed,#0891b2);color:white}");
                    out.println(".secondary{border:1px solid #46516d;"
                              + "color:#cbd5e1;background:#171d32}");
                    out.println(".btn:hover{transform:translateY(-2px);"
                              + "filter:brightness(1.1)}");
                    out.println(".empty{color:#cbd5e1;padding:20px 0}");

                    out.println("@media(max-width:500px){"
                              + ".details{grid-template-columns:1fr}"
                              + ".card{padding:22px}h1{font-size:27px}"
                              + ".container{margin:15px auto}"
                              + ".btn{width:100%}}");

                    out.println("</style></head><body>");
                    out.println("<main class='container'>");
                    out.println("<div class='brand'>Student Online Test</div>");
                    out.println("<h1>My Profile</h1>");
                    out.println("<p class='subtitle'>"
                              + "View your account information and "
                              + "student details.</p>");

                    out.println("<section class='card'>");

                    if (rs.next()) {

                        String name = rs.getString("name");
                        String email = rs.getString("email");
                        String role = rs.getString("role");

                        String initial = "S";
                        if (name != null && !name.trim().isEmpty()) {
                            initial = name.trim().substring(0, 1)
                                           .toUpperCase();
                        }

                        out.println("<div class='avatar'>"
                                  + escape(initial) + "</div>");

                        out.println("<div class='profile-name'>"
                                  + escape(name) + "</div>");

                        out.println("<span class='role'>"
                                  + escape(role) + "</span>");

                        out.println("<div class='details'>");

                        out.println("<div class='detail'>"
                                  + "<div class='detail-label'>Student ID</div>"
                                  + "<div class='detail-value'>"
                                  + rs.getInt("student_id")
                                  + "</div></div>");

                        out.println("<div class='detail'>"
                                  + "<div class='detail-label'>Email Address</div>"
                                  + "<div class='detail-value'>"
                                  + escape(email)
                                  + "</div></div>");

                        out.println("<div class='detail'>"
                                  + "<div class='detail-label'>Account Role</div>"
                                  + "<div class='detail-value'>"
                                  + escape(role)
                                  + "</div></div>");

                        out.println("<div class='detail'>"
                                  + "<div class='detail-label'>Account Created</div>"
                                  + "<div class='detail-value'>"
                                  + escape(String.valueOf(
                                      rs.getTimestamp("created_at")))
                                  + "</div></div>");

                        out.println("</div>");

                    } else {
                        out.println("<p class='empty'>"
                                  + "Profile not found.</p>");
                    }

                    out.println("<div class='actions'>");
                    out.println("<a class='btn primary' "
                              + "href='student-dashboard.html'>"
                              + "Back to Dashboard</a>");
                    out.println("<a class='btn secondary' "
                              + "href='AvailableTestsServlet'>"
                              + "View Available Tests</a>");
                    out.println("</div>");

                    out.println("</section></main></body></html>");
                }
            }

        } catch (Exception e) {
            throw new ServletException("Unable to load profile.", e);
        }
    }

    private String escape(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
