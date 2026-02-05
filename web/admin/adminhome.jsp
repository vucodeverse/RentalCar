<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    
    response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
    response.setHeader("Pragma","no-cache");
    response.setDateHeader("Expires", 0);
    
    String role = (String) session.getAttribute("roleName");
    if (role == null) {
        response.sendRedirect("LoginAdmin");
        return;
    }
    if (!"ADMIN".equalsIgnoreCase(role)) {
        request.setAttribute("error", "Bạn không có quyền truy cập trang này!");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Trang quản trị - CarGo</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">

        <!-- Thanh điều hướng -->
        <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
            <div class="container-fluid d-flex justify-content-between align-items-center">
                <!-- Left: Logo -->
                <a class="navbar-brand" href="HomeAdmin">🚗 Admin</a>

                <!-- Right: Nút thêm và logout -->
                <div>
                    <button class="btn btn-primary" type="button" data-toggle="collapse" data-target="#addUserForm">
                        + Thêm User
                    </button>
                    <a href="LogoutServlet" class="btn btn-outline-light">Đăng xuất</a>
                </div>
            </div>
        </nav>


        <div class="container-fluid mt-4">
            <h3 class="mb-3 text-primary text-center">Quản lý người dùng</h3>

            <!-- Thông báo -->
            <c:if test="${not empty message}">
                <div class="alert alert-success">${message}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <!-- Form thêm người dùng (ẩn lúc đầu) -->
            <div class="collapse mb-4" id="addUserForm">
                <div class="card shadow-lg border-primary">
                    <div class="card-header bg-primary text-white text-center fs-5">
                        Thêm người dùng mới
                    </div>
                    <div class="card-body">
                        <form action="ControllerAdmin" method="post" class="row g-3">
                            <input type="hidden" name="action" value="create">

                            <!-- Username -->
                            <div class="col-md-6">
                                <label class="form-label">Tên đăng nhập:</label>
                                <input type="text" name="username" class="form-control"
                                       value="${formUser.username}"
                                       placeholder="Nhập tên đăng nhập" required>
                            </div>

                            <!-- Full name -->
                            <div class="col-md-6">
                                <label class="form-label">Họ tên:</label>
                                <input type="text" name="fullname" class="form-control"
                                       value="${formUser.fullName}"
                                       placeholder="Nhập họ tên">
                            </div>

                            <!-- Email -->
                            <div class="col-md-6">
                                <label class="form-label">Email:</label>
                                <input type="email" name="email" class="form-control"
                                       value="${formUser.email}"
                                       placeholder="Nhập email" required>
                            </div>

                            <!-- Phone -->
                            <div class="col-md-6">
                                <label class="form-label">Số điện thoại:</label>
                                <input type="text" name="phone" class="form-control"
                                       value="${formUser.phone}"
                                       placeholder="Nhập số điện thoại" required>
                            </div>

                            <!-- Role -->
                            <div class="col-md-6">
                                <label class="form-label">Vai trò:</label>
                                <select name="roleid" class="form-control" required>
                                    <option value="">-- Chọn vai trò --</option>
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r.roleId}"
                                                <c:if test="${formUser.roleId == r.roleId}">
                                                    selected
                                                </c:if>>
                                            ${r.roleName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Location -->
                            <div class="col-md-6">
                                <label class="form-label">Thành phố:</label>
                                <select name="locationid" class="form-control">
                                    <option value="">-- Chọn thành phố --</option>
                                    <c:forEach var="l" items="${locations}">
                                        <option value="${l.locationId}"
                                                <c:if test="${formUser.locationId == l.locationId}">
                                                    selected
                                                </c:if>>
                                            ${l.city}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Password -->
                            <div class="col-md-6">
                                <label class="form-label">Mật khẩu:</label>
                                <input type="password" name="password" class="form-control"
                                       placeholder="Nhập mật khẩu" required>
                            </div>

                            <!-- Submit -->
                            <div class="col-12 d-flex justify-content-end mt-3">
                                <button type="submit" class="btn btn-primary px-5"
                                        onclick="return confirm('Bạn có chắc chắn muốn tạo tài khoản này?')">
                                    Lưu
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>


            <form method="get" action="HomeAdmin" class="card card-body mb-3">
                <div class="row">

                    <!-- Search -->
                    <div class="col-md-3">
                        <input type="text" name="keyword"
                               value="${param.keyword}"
                               class="form-control"
                               placeholder="Tìm username / email / phone">
                    </div>

                    <!-- Filter by role (type) -->
                    <div class="col-md-2">
                        <select name="roleId" class="form-control">
                            <option value="">-- Vai trò --</option>
                            <c:forEach var="r" items="${roles}">
                                <option value="${r.roleId}"
                                        ${param.roleId == r.roleId ? 'selected' : ''}>
                                    ${r.roleName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Filter from date -->
                    <div class="col-md-2">
                        <input type="date" name="fromDate"
                               value="${param.fromDate}"
                               class="form-control">
                    </div>

                    <!-- Filter to date -->
                    <div class="col-md-2">
                        <input type="date" name="toDate"
                               value="${param.toDate}"
                               class="form-control">
                    </div>

                    <!-- Submit -->
                    <div class="col-md-3">
                        <button type="submit" class="btn btn-primary">
                            🔍 Tìm kiếm
                        </button>
                        <a href="HomeAdmin" class="btn btn-secondary">
                            Reset
                        </a>
                    </div>

                </div>
            </form>


            <!-- Danh sách người dùng -->
            <div class="card shadow-sm" style="margin-bottom: 2%">
                <div class="card-header bg-secondary text-white text-center">Danh sách người dùng</div>
                <div class="card-body p-0">
                    <table class="table table-striped mb-0 text-center align-middle table-bordered">
                        <thead class="thead-dark">
                            <tr>
                                <th>ID</th>
                                <th>Tên đăng nhập</th>
                                <th>Họ tên</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Thành phố</th>
                                <th>Vai trò</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr>
                                    <td>${user.userId}</td>
                                    <td>${user.username}</td>
                                    <td>${user.fullName}</td>
                                    <td>${user.email}</td>
                                    <td>${user.phone}</td>
                                    <td>${user.city}</td>
                                    <td>
                                        <span class="badge bg-info">${user.roleName}</span>
                                    </td>
                                    <td>
                                        <!-- Ẩn nút Xóa/Sửa nếu người dùng là ADMIN -->
                                        <c:if test="${user.roleName ne 'ADMIN'}">
                                            <form action="ControllerAdmin" method="post" style="display:inline;">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="userId" value="${user.userId}">
                                                <button type="submit" class="btn btn-danger btn-sm"
                                                        onclick="return confirm('Bạn có chắc chắn muốn xóa tài khoản này?')">
                                                    Xóa
                                                </button>
                                            </form>
                                            <a href="ControllerAdmin?action=edit&userId=${user.userId}" 
                                               class="btn btn-warning btn-sm"

                                               >Sửa</a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <!-- Pagination -->
            <c:if test="${totalPages > 1}">
                <nav class="mt-3">
                    <ul class="pagination justify-content-center">

                        <!-- Previous -->
                        <c:if test="${currentPage > 1}">
                            <li class="page-item">
                                <a class="page-link"
                                   href="HomeAdmin?page=${currentPage - 1}">
                                    &laquo;
                                </a>
                            </li>
                        </c:if>

                        <!-- Page numbers -->
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link" href="HomeAdmin?page=${i}">
                                    ${i}
                                </a>
                            </li>
                        </c:forEach>

                        <!-- Next -->
                        <c:if test="${currentPage < totalPages}">
                            <li class="page-item">
                                <a class="page-link"
                                   href="HomeAdmin?page=${currentPage + 1}">
                                    &raquo;
                                </a>
                            </li>
                        </c:if>

                    </ul>
                </nav>
            </c:if>

        </div>

        
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>
        <c:if test="${showAddForm}">
            <script>
                $(document).ready(function () {
                    $('#addUserForm').collapse('show');
                });
            </script>
        </c:if>
    </body>
</html>
