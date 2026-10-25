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

@WebServlet("/ViewStudentsServlet")
public class ViewStudentsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin login required.");
            return;
        }

        String sql =
            "SELECT student_id, name, email, role, created_at " +
            "FROM students ORDER BY student_id";

        try (PrintWriter out = response.getWriter();
             Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            out.print("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Students | Student Online Test</title>
                  <style>
                    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');
                    :root{--bg:#090d19;--panel:#11182a;--border:#252e45;--text:#f1f5ff;--muted:#929db5;--purple:#9270ff;--cyan:#46d9e9;--green:#48e0a1}
                    *{box-sizing:border-box;margin:0;padding:0}
                    body{font-family:Inter,Arial,sans-serif;background:var(--bg);color:var(--text);min-height:100vh}
                    a{color:inherit;text-decoration:none}.layout{display:flex;min-height:100vh}
                    .sidebar{width:255px;flex-shrink:0;background:#0d1221;border-right:1px solid var(--border);padding:28px 18px;display:flex;flex-direction:column}
                    .brand{display:flex;align-items:center;gap:12px;padding:0 10px 35px}
                    .brand-icon{width:42px;height:42px;display:grid;place-items:center;border-radius:13px;background:linear-gradient(135deg,#9672ff,#6042db);font-size:21px;font-weight:800}
                    .brand h1{font-size:16px;font-weight:800;letter-spacing:-.5px}.brand p{color:var(--muted);font-size:10px;margin-top:4px}
                    .nav-label{color:#64718d;font-size:10px;font-weight:700;letter-spacing:1.8px;padding:0 12px;margin-bottom:12px}
                    .nav{display:flex;flex-direction:column;gap:7px}.nav a{display:flex;align-items:center;gap:13px;padding:13px 14px;border-radius:10px;color:var(--muted);font-size:13px;transition:.2s}
                    .nav a:hover{color:white;background:#192239}.nav a.active{color:white;background:linear-gradient(100deg,#805cf52b,#805cf508);border:1px solid #805cf540}
                    .nav-icon{width:23px;text-align:center;font-size:17px}.sidebar-bottom{margin-top:auto;padding-top:30px}
                    .logout{display:block;text-align:center;padding:11px;border:1px solid var(--border);border-radius:10px;color:#ff929d;font-size:12px}
                    .main{flex:1;min-width:0;padding:30px 38px 45px;overflow:hidden}
                    .topbar{display:flex;align-items:center;justify-content:space-between;gap:18px;padding-bottom:26px;border-bottom:1px solid var(--border)}
                    .breadcrumb{color:var(--muted);font-size:12px;margin-bottom:9px}.breadcrumb span{color:var(--purple)}.topbar h2{font-size:23px;letter-spacing:-.7px}
                    .status{display:flex;align-items:center;gap:9px;border:1px solid var(--border);background:var(--panel);border-radius:30px;padding:10px 14px;color:#c5cce0;font-size:11px;white-space:nowrap}
                    .status-dot{width:7px;height:7px;background:var(--green);border-radius:50%;box-shadow:0 0 12px #48e0a180}
                    .intro{margin:29px 0 24px}.intro h3{font-size:27px;letter-spacing:-.8px;margin-bottom:8px}.intro h3 span{color:var(--purple)}.intro p{font-size:13px;line-height:1.7;color:var(--muted)}
                    .panel{background:linear-gradient(145deg,#141c30,#101727);border:1px solid var(--border);border-radius:16px;overflow:hidden}
                    .panel-head{display:flex;justify-content:space-between;align-items:center;gap:16px;padding:22px 23px;border-bottom:1px solid var(--border)}
                    .panel-head h4{font-size:16px}.panel-head p{color:var(--muted);font-size:11px;margin-top:6px}
                    .tag{color:#b9a8ff;font-size:10px;background:#8b5cf51a;border:1px solid #8b5cf535;border-radius:30px;padding:8px 11px;white-space:nowrap}
                    .table-wrap{overflow-x:auto}table{width:100%;border-collapse:collapse;min-width:720px}
                    th{padding:15px 18px;text-align:left;background:#0d1323;color:#aeb9d2;font-size:10px;letter-spacing:.8px;text-transform:uppercase;border-bottom:1px solid var(--border)}
                    td{padding:16px 18px;border-bottom:1px solid #252e4580;font-size:12px;color:#dce3f5;vertical-align:middle}
                    tbody tr:hover{background:#8b5cf509}tbody tr:last-child td{border-bottom:none}
                    .id{display:inline-block;color:var(--cyan);font-weight:700;background:#46d9e910;border:1px solid #46d9e92c;padding:6px 9px;border-radius:7px}
                    .student{font-weight:600;color:#f1f5ff}.email{color:var(--muted)}
                    .role{display:inline-block;padding:6px 9px;border-radius:7px;background:#8b5cf51a;border:1px solid #8b5cf535;color:#c9baff;font-size:10px;font-weight:700}
                    .empty{padding:35px;text-align:center;color:var(--muted);font-size:13px}
                    .bottom{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-top:22px}
                    .back{display:inline-flex;align-items:center;gap:9px;padding:11px 15px;border:1px solid #8b5cf55c;border-radius:9px;color:#d0c5ff;background:#8b5cf510;font-size:12px;font-weight:700}
                    .back:hover{background:#805cf5;color:white}.footnote{font-size:10px;color:#64718b}
                    @media(max-width:850px){.sidebar{width:210px;padding:23px 12px}.main{padding:25px 22px 35px}}
                    @media(max-width:600px){.layout{display:block}.sidebar{width:100%;padding:16px;border-right:none;border-bottom:1px solid var(--border)}.brand{padding:0 3px 17px}.nav-label,.sidebar-bottom{display:none}.nav{flex-direction:row;overflow-x:auto}.nav a{flex-shrink:0;padding:10px 12px;font-size:11px}.nav-icon{width:auto}.main{padding:22px 16px 30px}.topbar h2{font-size:19px}.status{padding:9px;font-size:10px}.intro h3{font-size:23px}.panel-head{padding:18px}.tag{display:none}}
                  </style>
                </head>
                <body>
                <div class="layout">
                  <aside class="sidebar">
                    <a href="admin-dashboard.html" class="brand">
                      <div class="brand-icon">E</div>
                      <div><h1>Student Online Test<span style="color:#9270ff">.</span></h1><p>ONLINE EXAMINATION</p></div>
                    </a>
                    <div class="nav-label">WORKSPACE</div>
                    <nav class="nav">
                      <a href="admin-dashboard.html"><span class="nav-icon">◈</span>Dashboard</a>
                      <a href="create-test.html"><span class="nav-icon">▤</span>Create Test</a>
                      <a href="AdminResultsServlet"><span class="nav-icon">▥</span>Student Results</a>
                      <a href="ViewStudentsServlet" class="active"><span class="nav-icon">♙</span>Students</a>
                    </nav>
                    <div class="sidebar-bottom"><a href="index.html" class="logout">↪ &nbsp; Logout</a></div>
                  </aside>
                  <main class="main">
                    <header class="topbar">
                      <div><div class="breadcrumb">Workspace / <span>Students</span></div><h2>Registered Students</h2></div>
                      <div class="status"><span class="status-dot"></span>Admin Workspace</div>
                    </header>
                    <section class="intro">
                      <h3>Student <span>Directory</span></h3>
                      <p>View registered accounts, student details, and account roles.</p>
                    </section>
                    <section class="panel">
                      <div class="panel-head"><div><h4>All Registered Students</h4><p>Student accounts are listed by registration ID.</p></div><div class="tag">USER MANAGEMENT</div></div>
                      <div class="table-wrap">
                        <table>
                          <thead><tr><th>Student ID</th><th>Name</th><th>Email</th><th>Role</th><th>Registered At</th></tr></thead>
                          <tbody>
                """);

            boolean found = false;
            while (rs.next()) {
                found = true;
                out.println("<tr>");
                out.println("<td><span class='id'>#" + rs.getInt("student_id") + "</span></td>");
                out.println("<td><span class='student'>" + escapeHtml(rs.getString("name")) + "</span></td>");
                out.println("<td><span class='email'>" + escapeHtml(rs.getString("email")) + "</span></td>");
                out.println("<td><span class='role'>" + escapeHtml(rs.getString("role")) + "</span></td>");
                out.println("<td>" + escapeHtml(String.valueOf(rs.getTimestamp("created_at"))) + "</td>");
                out.println("</tr>");
            }

            if (!found) {
                out.println("<tr><td colspan='5' class='empty'>No registered students found.</td></tr>");
            }

            out.print("""
                          </tbody>
                        </table>
                      </div>
                    </section>
                    <div class="bottom">
                      <a class="back" href="admin-dashboard.html">← &nbsp; Back to Dashboard</a>
                      <div class="footnote">© 2026 Student Online Test · Admin Workspace</div>
                    </div>
                  </main>
                </div>
                </body>
                </html>
                """);

        } catch (SQLException e) {
            throw new ServletException("Could not load registered students.", e);
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
