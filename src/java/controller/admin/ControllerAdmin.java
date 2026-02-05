package controller.admin;

import dto.LocationDTO;
import dto.RoleDTO;
import dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import service.RoleService;
import service.UserService;
import util.EmailUtil;
import util.MessageUtil;
import util.di.DIContainer;
import util.exception.BusinessException;
import util.exception.DataAccessException;
import util.exception.ValidationException;

@WebServlet(name = "ControllerAdmin", urlPatterns = {"/ControllerAdmin"})
public class ControllerAdmin extends HttpServlet {

    private UserService userService;
    private RoleService roleService;

    @Override
    public void init() throws ServletException {
        try {
            userService = DIContainer.get(UserService.class);
            roleService = DIContainer.get(RoleService.class);
        } catch (Exception e) {
            throw new ServletException("Init ControllerAdmin failed", e);
        }
    }

    // ======================= COMMON =======================
    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("roleName"))) {
            request.setAttribute("error", "Bạn không có quyền truy cập trang này!");
            request.getRequestDispatcher("auth/login.jsp").forward(request, response);
            return false;
        }
        return true;
    }

    // ======================= ADD USER =======================
    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String username = request.getParameter("username");
            String fullname = request.getParameter("fullname");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String password = request.getParameter("password");
            String roleIdStr = request.getParameter("roleid");
            String locationIdStr = request.getParameter("locationid");

            UserDTO user = new UserDTO();
            user.setUsername(username);
            user.setFullName(fullname);
            user.setEmail(email);
            user.setPhone(phone);

            if (roleIdStr != null && !roleIdStr.isEmpty()) {
                user.setRoleId(Integer.parseInt(roleIdStr));
            }
            if (locationIdStr != null && !locationIdStr.isEmpty()) {
                user.setLocationId(Integer.parseInt(locationIdStr));
            }

            // CALL SERVICE
            userService.addUser(user, password);

            // SEND MAIL
            if (email != null && !email.isEmpty()) {
                String roleName = switch (user.getRoleId()) {
                    case 1 ->
                        "Staff";
                    case 2 ->
                        "Manager";
                    case 3 ->
                        "Admin";
                    default ->
                        "User";
                };
                EmailUtil.sendCredentials(email, fullname, username, password, roleName);
            }

            // SUCCESS → reset form
            response.sendRedirect("HomeAdmin");
            return;

        } catch (ValidationException | BusinessException | DataAccessException e) {

            // ====== ERROR: GIỮ FORM ======
            request.setAttribute("error", MessageUtil.getErrorFromException(e));

            UserDTO formUser = new UserDTO();
            formUser.setUsername(request.getParameter("username"));
            formUser.setFullName(request.getParameter("fullname"));
            formUser.setEmail(request.getParameter("email"));
            formUser.setPhone(request.getParameter("phone"));

            String roleIdStr = request.getParameter("roleid");
            if (roleIdStr != null && !roleIdStr.isEmpty()) {
                formUser.setRoleId(Integer.parseInt(roleIdStr));
            }

            String locationIdStr = request.getParameter("locationid");
            if (locationIdStr != null && !locationIdStr.isEmpty()) {
                formUser.setLocationId(Integer.parseInt(locationIdStr));
            }

            request.setAttribute("formUser", formUser);
            request.setAttribute("showAddForm", true);

            // LOAD DATA
            request.setAttribute("users", userService.getAllUser());
            request.setAttribute("roles", roleService.getAllRole());
            request.setAttribute("locations", userService.getAllLocation());

            request.getRequestDispatcher("admin/adminhome.jsp")
                    .forward(request, response);
            return;

        } catch (Exception e) {
            request.setAttribute("error", MessageUtil.getError("error.system.admin"));
            request.getRequestDispatcher("HomeAdmin").forward(request, response);
        }
    }

    // ======================= DELETE =======================
    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String userIdStr = request.getParameter("userId");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                userService.deleteUser(Integer.parseInt(userIdStr));
                request.setAttribute("message",
                        MessageUtil.getError("error.user.delete.success"));
            }
        } catch (Exception e) {
            request.setAttribute("error",
                    MessageUtil.getError("error.system.admin"));
        }
        request.getRequestDispatcher("HomeAdmin").forward(request, response);
    }

    // ======================= EDIT =======================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userIdStr = request.getParameter("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.sendRedirect("HomeAdmin");
            return;
        }

        UserDTO user = userService.getUserById(Integer.parseInt(userIdStr));

        request.setAttribute("editUser", user);
        request.setAttribute("roles", roleService.getAllRole());
        request.setAttribute("locations", userService.getAllLocation());

        request.getRequestDispatcher("admin/edituser.jsp").forward(request, response);
    }

    // ======================= UPDATE =======================
    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            UserDTO user = new UserDTO();
            user.setUserId(Integer.parseInt(request.getParameter("userId")));
            user.setUsername(request.getParameter("username"));
            user.setFullName(request.getParameter("fullname"));
            user.setEmail(request.getParameter("email"));
            user.setPhone(request.getParameter("phone"));

            String roleIdStr = request.getParameter("roleid");
            if (roleIdStr != null && !roleIdStr.isEmpty()) {
                user.setRoleId(Integer.parseInt(roleIdStr));
            }

            String locationIdStr = request.getParameter("locationid");
            if (locationIdStr != null && !locationIdStr.isEmpty()) {
                user.setLocationId(Integer.parseInt(locationIdStr));
            }

            userService.updateUser(user);
            request.setAttribute("message",
                    MessageUtil.getError("error.user.update.success"));
            request.getRequestDispatcher("HomeAdmin").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error",
                    MessageUtil.getError("error.system.admin"));
            request.getRequestDispatcher("HomeAdmin").forward(request, response);
        }
    }

    // ======================= DISPATCH =======================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action");

        if ("create".equalsIgnoreCase(action)) {
            addUser(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            deleteUser(request, response);
        } else if ("edit".equalsIgnoreCase(action)) {
            showEditForm(request, response);
        } else if ("update".equalsIgnoreCase(action)) {
            updateUser(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
