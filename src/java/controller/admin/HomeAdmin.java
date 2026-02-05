
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.admin;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import dto.*;
import jakarta.servlet.http.HttpSession;
import service.*;
import util.di.DIContainer;

/**
 *
 * @author DELL
 */
@WebServlet(name = "HomeAdmin", urlPatterns = {"/HomeAdmin"})
public class HomeAdmin extends HttpServlet {

    private UserService userService;
    private RoleService roleService;

    @Override
    public void init() throws ServletException {
        try {
            userService = DIContainer.get(UserService.class);
            roleService = DIContainer.get(RoleService.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // ✅ Check quyền ADMIN (tạm thời)
        if (session == null || !"ADMIN".equals(session.getAttribute("roleName"))) {
            request.setAttribute("errors", "Bạn không có quyền truy cập trang này!");
            request.getRequestDispatcher("auth/login.jsp").forward(request, response);
            return;
        }

        /* ================= SEARCH + FILTER ================= */

        String keyword = request.getParameter("keyword");
        String roleIdRaw = request.getParameter("roleId");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");

        Integer roleId = null;
        if (roleIdRaw != null && !roleIdRaw.isEmpty()) {
            roleId = Integer.parseInt(roleIdRaw);
        }

        int page = 1;
        int pageSize = 5;

        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
        } catch (NumberFormatException ignored) {
        }

        /* ================= CALL SERVICE ================= */

        List<UserDTO> users = userService.searchUsers(
                keyword,
                roleId,
                fromDate,
                toDate,
                page,
                pageSize
        );

        int totalUsers = userService.countSearchUsers(
                keyword,
                roleId,
                fromDate,
                toDate
        );

        int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

        /* ================= LOAD COMMON DATA ================= */

        List<RoleDTO> roles = roleService.getAllRole();
        List<LocationDTO> locations = userService.getAllLocation();

        /* ================= SET ATTRIBUTE ================= */

        request.setAttribute("users", users);
        request.setAttribute("roles", roles);
        request.setAttribute("locations", locations);

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("keyword", keyword);
        request.setAttribute("roleId", roleId);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);

        request.getRequestDispatcher("admin/adminhome.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}

