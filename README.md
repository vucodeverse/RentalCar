# RentalCar (Hệ thống Thuê Xe Ô Tô - Car Rental System)
## 📖 Giới thiệu (Introduction)
CarGo là một ứng dụng web Quản lý và Cho Thuê Xe Ô Tô trực tuyến. Dự án cung cấp nền tảng kết nối trực tiếp giữa khách hàng có nhu cầu thuê xe ô tô và ban quản lý/nhân viên hỗ trợ. Người dùng có thể tìm kiếm, thêm xe vào giỏ hàng và đặt lịch thuê xe một cách nhanh chóng, đi kèm với hệ thống giám sát và phê duyệt hợp đồng chi tiết.
## 🚀 Tính năng nổi bật (Key Features)
### 👤 Dành cho Khách hàng (Customers)
- **Quản lý tài khoản:** Đăng ký, đăng nhập.
- **Tìm kiếm xe:** Tìm kiếm và lọc xe theo danh mục (Sedan, SUV, Hatchback, MPV...), loại nhiên liệu, số lượng ghế ngồi.
- **Đặt xe (Booking):** Đưa xe vào giỏ hàng cá nhân, lên lịch đặt thuê xe theo thời gian cụ thể (Start Date, End Date).
- **Thanh toán & Hợp đồng:** Thanh toán linh hoạt qua nhiều phương thức (Tiền mặt, Thẻ tín dụng, Chuyển khoản), theo dõi trạng thái hợp đồng thuê.
- **Phản hồi:** Gửi đánh giá và bình luận về xe sau quá trình sử dụng (Feedbacks).
### 🛠 Dành cho Nhân viên & Quản lý (Staff & Management)
- **Quản lý xe (Cars & Vehicles):** Quản lý chung thông tin mẫu xe (Cars), theo dõi các xe thực tế theo biển số và trạng thái khả dụng (Vehicles).
- **Quản lý giá thuê:** Cập nhật bảng giá (CarPrices) tính theo ngày và tiền đặt cọc tùy theo từng mẫu xe.
- **Phê duyệt hợp đồng:** Kiểm duyệt các hợp đồng cho thuê xe mới, đối chiếu CMND/CCCD (hai mặt) của khách hàng để xét duyệt (Staff Review).
- **Quản lý sự cố (Incidents):** Ghi nhận các sự cố phát sinh (va quệt, vi phạm giao thông) trong quá trình thuê và xử lý cọc tiền phạt.
## 💻 Công nghệ sử dụng (Tech Stack)
- **Back-end:** Java EE (Servlets, JSP).
- **Cơ sở dữ liệu:** Microsoft SQL Server (sử dụng mẫu thiết kế DAO map với CSDL bằng JDBC Object).
- **Front-end:** HTML, CSS, JavaScript.
- **Mô hình kiến trúc:** MVC (Model - View - Controller). Tách luồng Data, Logic và UI rõ ràng.
- **Công cụ phát triển:** NetBeans IDE, Apache Ant.
## 🗄 Cấu trúc Cơ Sở Dữ Liệu (Database Schema)
Dự án bao gồm 17 bảng liên kết thông qua khóa ngoại, phục vụ nghiệp vụ quản lý toàn diện:
- **Người dùng:** `Customers`, `Users`, `Roles` (STAFF, MANAGER, ADMIN).
- **Phân loại & Phương tiện:** `Cars`, `Vehicles`, `Categories`, `Fuels`, `Seatings`, `Locations`.
- **Giao dịch:** `Contracts`, `ContractDetails`, `Carts`, `Orders`, `Payments`, `PaymentMethods`, `CarPrices`.
- **Sự cố & Phản hồi:** `Feedbacks`, `Incidents`, `IncidentTypes`.
