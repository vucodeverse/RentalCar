--CREATE DATABASE RentalCar;
GO
USE RentalCar;
GO

/* ========================
   ĐỊA ĐIỂM
======================== */
CREATE TABLE Locations (
    locationId INT IDENTITY(1,1) PRIMARY KEY,
    city NVARCHAR(100) NOT NULL,
    address NVARCHAR(255)
);

/* ========================
   KHÁCH HÀNG
======================== */
CREATE TABLE Customers (
    customerId INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    fullName NVARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    dateOfBirth DATE CHECK (dateOfBirth <= DATEADD(YEAR, -18, CAST(GETDATE() AS DATE))),
    createAt DATETIME DEFAULT GETDATE(),
    locationId INT NULL,
    password_hash VARBINARY(64) NOT NULL,
    password_salt VARBINARY(32) NOT NULL,
    isVerified BIT NOT NULL DEFAULT 0,
    verifyCode VARCHAR(6) NULL,
    verifyCodeExpire DATETIME NULL,
    CONSTRAINT FK_Customers_Locations FOREIGN KEY (locationId) REFERENCES Locations(locationId),
    CONSTRAINT CK_Customers_Phone CHECK (phone IS NULL OR phone LIKE '[0-9]%'),
    CONSTRAINT CK_Customers_Email CHECK (email IS NULL OR email LIKE '%@%.%')
);

/* ========================
   NGƯỜI DÙNG & PHÂN QUYỀN
======================== */
CREATE TABLE Roles (
    roleId INT IDENTITY(1,1) PRIMARY KEY,
    roleName VARCHAR(50) UNIQUE NOT NULL -- STAFF, MANAGER, ADMIN
);

CREATE TABLE Users (
    userId INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    fullName NVARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    --dateOfBirth DATE CHECK (dateOfBirth <= DATEADD(YEAR, -18, CAST(GETDATE() AS DATE))),
    createAt DATETIME DEFAULT GETDATE(),
    locationId INT NULL,
    password_hash VARBINARY(64) NOT NULL,
    password_salt VARBINARY(32) NOT NULL,
    roleId INT NOT NULL,
    CONSTRAINT FK_Users_Locations FOREIGN KEY (locationId) REFERENCES Locations(locationId),
    CONSTRAINT FK_Users_Roles FOREIGN KEY (roleId) REFERENCES Roles(roleId),
    CONSTRAINT CK_Users_Phone CHECK (phone IS NULL OR phone LIKE '[0-9]%'),
    CONSTRAINT CK_Users_Email CHECK (email IS NULL OR email LIKE '%@%.%')
);

/* ========================
   DANH MỤC XE
======================== */
CREATE TABLE Categories (
    categoryId INT IDENTITY(1,1) PRIMARY KEY,
    categoryName NVARCHAR(50)
);

CREATE TABLE Fuels (
    fuelId INT IDENTITY(1,1) PRIMARY KEY,
    fuelType NVARCHAR(50)
);

CREATE TABLE Seatings (
    seatingId INT IDENTITY(1,1) PRIMARY KEY,
    seatingType INT NOT NULL CHECK (seatingType > 0)
);

CREATE TABLE Cars (
    carId INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100),
    [year] INT CHECK ([year] >= 1900 AND [year] <= YEAR(GETDATE())),
    [description] NVARCHAR(255),
    image VARCHAR(255),
    categoryId INT NULL,
    fuelId INT NULL,
    seatingId INT NULL,
    CONSTRAINT FK_Cars_Categories FOREIGN KEY (categoryId) REFERENCES Categories(categoryId),
    CONSTRAINT FK_Cars_Fuels FOREIGN KEY (fuelId) REFERENCES Fuels(fuelId),
    CONSTRAINT FK_Cars_Seatings FOREIGN KEY (seatingId) REFERENCES Seatings(seatingId)
);

CREATE TABLE Vehicles (
    vehicleId INT IDENTITY(1,1) PRIMARY KEY,
    carId INT NOT NULL,
    plateNumber VARCHAR(30) UNIQUE NOT NULL,
    isActive BIT NOT NULL DEFAULT 1,
    locationId INT NULL,
    CONSTRAINT FK_Vehicles_Cars FOREIGN KEY (carId) REFERENCES Cars(carId),
    CONSTRAINT FK_Vehicles_Locations FOREIGN KEY (locationId) REFERENCES Locations(locationId)
);

/* Giá theo model (Cars) */
CREATE TABLE CarPrices (
    priceId INT IDENTITY(1,1) PRIMARY KEY,
    carId INT NOT NULL,
    dailyPrice DECIMAL(12,2) NOT NULL CHECK (dailyPrice > 0),
    depositAmount DECIMAL(12,2) NULL CHECK (depositAmount IS NULL OR depositAmount >= 0),
    startDate DATE NOT NULL,
    endDate DATE NULL,
    createAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_CarPrices_Cars FOREIGN KEY (carId) REFERENCES Cars(carId),
    CONSTRAINT CK_CarPrices_Date CHECK (endDate IS NULL OR endDate > startDate)
);
CREATE INDEX IX_CarPrices_carId ON CarPrices(carId);

/* ========================
   HỢP ĐỒNG
======================== */
CREATE TABLE Contracts (
    contractId INT IDENTITY(1,1) PRIMARY KEY,
    customerId INT NOT NULL,
    staffId INT NULL,
    startDate DATETIME NOT NULL,
    endDate DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','ACCEPTED','REJECTED')),
    createAt DATETIME DEFAULT GETDATE(),
    totalAmount DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (totalAmount >= 0),
    depositAmount DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (depositAmount >= 0),
	idCardFrontPath NVARCHAR(255) NULL,     -- đường dẫn ảnh CCCD mặt trước
    idCardBackPath NVARCHAR(255) NULL,      -- đường dẫn ảnh CCCD mặt sau
    staffReviewDate DATETIME NULL,          -- ngày nhân viên duyệt
    staffReviewBy INT NULL,                 -- ID nhân viên duyệt
    rejectionReason NVARCHAR(255) NULL,     -- lý do từ chối (nếu bị reject)
    CONSTRAINT FK_Contracts_Customers FOREIGN KEY (customerId) REFERENCES Customers(customerId),
    CONSTRAINT FK_Contracts_Users FOREIGN KEY (staffId) REFERENCES Users(userId),
    CONSTRAINT CK_Contracts_Date CHECK (endDate > startDate)
);

CREATE TABLE ContractDetails (
    contractDetailId INT IDENTITY(1,1) PRIMARY KEY,
    contractId INT NOT NULL,
    vehicleId INT NOT NULL,
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    rentStartDate DATETIME NOT NULL,
    rentEndDate DATETIME NOT NULL,
    note NVARCHAR(255),
    CONSTRAINT FK_ContractDetails_Contracts FOREIGN KEY (contractId) REFERENCES Contracts(contractId),
    CONSTRAINT FK_ContractDetails_Vehicles FOREIGN KEY (vehicleId) REFERENCES Vehicles(vehicleId),
    CONSTRAINT CK_ContractDetails_Date CHECK (rentEndDate > rentStartDate)
);
CREATE INDEX IX_ContractDetails_contractId ON ContractDetails(contractId);
CREATE INDEX IX_ContractDetails_vehicle_dates ON ContractDetails(vehicleId, rentStartDate, rentEndDate);

/* ========================
   GIỎ HÀNG
======================== */
CREATE TABLE Carts (
    cartId INT IDENTITY(1,1) PRIMARY KEY,
    customerId INT UNIQUE NOT NULL,
    createAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Carts_Customers FOREIGN KEY (customerId) REFERENCES Customers(customerId)
);

CREATE TABLE Orders (
    cartDetailId INT IDENTITY(1,1) PRIMARY KEY,
    cartId INT NOT NULL,
    vehicleId INT NOT NULL,
    rentStartDate DATETIME NOT NULL,
    rentEndDate DATETIME NOT NULL,
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    CONSTRAINT FK_Orders_Carts FOREIGN KEY (cartId) REFERENCES Carts(cartId),
    CONSTRAINT FK_Orders_Vehicles FOREIGN KEY (vehicleId) REFERENCES Vehicles(vehicleId),
    CONSTRAINT CK_Orders_Date CHECK (rentEndDate > rentStartDate)
);
CREATE INDEX IX_Orders_cartId ON Orders(cartId);
CREATE INDEX IX_Orders_vehicle_dates ON Orders(vehicleId, rentStartDate, rentEndDate);

/* ========================
   THANH TOÁN
======================== */
CREATE TABLE PaymentMethods (
    methodId INT IDENTITY(1,1) PRIMARY KEY,
    methodName NVARCHAR(50)
);

CREATE TABLE Payments (
    paymentId INT IDENTITY(1,1) PRIMARY KEY,
    contractId INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    methodId INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','COMPLETED','FAILED')),
    paymentDate DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Payments_Contracts FOREIGN KEY (contractId) REFERENCES Contracts(contractId),
    CONSTRAINT FK_Payments_Methods FOREIGN KEY (methodId) REFERENCES PaymentMethods(methodId)
);
CREATE INDEX IX_Payments_contractId ON Payments(contractId);

/* ========================
   PHẢN HỒI & SỰ CỐ
======================== */
CREATE TABLE Feedbacks (
    feedbackId INT IDENTITY(1,1) PRIMARY KEY,
    customerId INT NOT NULL,
    vehicleId INT NOT NULL,
    comment NVARCHAR(255),
    createAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Feedbacks_Customers FOREIGN KEY (customerId) REFERENCES Customers(customerId),
    CONSTRAINT FK_Feedbacks_Cars FOREIGN KEY (vehicleId) REFERENCES Vehicles(vehicleId)
);

CREATE TABLE IncidentTypes (
    incidentTypeId INT IDENTITY(1,1) PRIMARY KEY,
    typeName NVARCHAR(100)
);

CREATE TABLE Incidents (
    incidentId INT IDENTITY(1,1) PRIMARY KEY,
    [description] NVARCHAR(255) NOT NULL,
    fineAmount DECIMAL(12,2) NOT NULL CHECK (fineAmount >= 0),
    incidentDate DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','RESOLVED','CANCELLED')),
    contractDetailId INT NOT NULL,
    incidentTypeId INT NOT NULL,
    CONSTRAINT FK_Incidents_IncidentTypes FOREIGN KEY (incidentTypeId) REFERENCES IncidentTypes(incidentTypeId),
    CONSTRAINT FK_Incidents_ContractDetails FOREIGN KEY (contractDetailId) REFERENCES ContractDetails(contractDetailId)
);

/* ========================
   INDEXES
======================== */
CREATE INDEX IX_Contracts_customerId ON Contracts(customerId);
CREATE INDEX IX_Contracts_staffId ON Contracts(staffId);
CREATE INDEX IX_Users_locationId ON Users(locationId);
CREATE INDEX IX_Customers_locationId ON Customers(locationId);

/* ========================
   DỮ LIỆU MẪU
======================== */
INSERT INTO Locations (city) VALUES 
(N'Hà Nội'), (N'Hồ Chí Minh'), (N'Cần Thơ'), (N'Đà Nẵng');

INSERT INTO Roles (roleName) VALUES 
('STAFF'), ('MANAGER'), ('ADMIN');

INSERT INTO PaymentMethods (methodName) VALUES 
('Cash'), ('Credit Card'), ('Banking');

INSERT INTO Seatings (seatingType) VALUES 
(2), (4), (5), (7), (9);

INSERT INTO Categories (categoryName) VALUES 
(N'Sedan'), (N'SUV'), (N'Hatchback'), (N'MPV'), (N'Sport');

INSERT INTO Fuels (fuelType) VALUES 
(N'Xăng'), (N'Dầu'), (N'Điện'), (N'Hybrid'), (N'Khí nén (CNG)');

INSERT INTO Cars (name, [year], [description], image, categoryId, fuelId, seatingId) VALUES
(N'Toyota Vios', 2022, N'Sedan 5 chỗ tiết kiệm nhiên liệu', 
'https://danchoioto.vn/wp-content/uploads/2024/09/xe-o-to-gia-re-danchoiotovn-2.jpg.webp', 1, 1, 1),
(N'Kia Morning', 2021, N'Xe nhỏ gọn, phù hợp đi nội thành', 
'https://danchoioto.vn/wp-content/uploads/2024/09/xe-o-to-gia-re-danchoiotovn-36.jpg.webp', 2, 2, 2),
(N'Ford Ranger', 2023, N'Bán tải mạnh mẽ, động cơ dầu', 
'https://danchoioto.vn/wp-content/uploads/2024/09/xe-o-to-gia-re-danchoiotovn-37.jpg.webp', 3, 3, 3),
(N'Tesla Model 3', 2024, N'Xe điện cao cấp, hiệu năng cao', 
'https://danchoioto.vn/wp-content/uploads/2024/09/xe-o-to-gia-re-danchoiotovn-1.jpg.webp', 4, 4, 4),
(N'Toyota Innova', 2022, N'MPV 7 chỗ, phù hợp gia đình', 
'https://danchoioto.vn/wp-content/uploads/2023/12/hyundai-accent-gia.jpg.webp', 4, 5, 4),
(N'Kia Sonet', 2024, N'SUV hạng A', 
'https://danchoioto.vn/wp-content/uploads/2024/09/xe-o-to-gia-re-danchoiotovn-25.jpg.webp', 4, 4, 3);

INSERT INTO Vehicles (carId, plateNumber, isActive, locationId) VALUES
(1, '30A-12345', 1, 1),
(1, '30A-67890', 1, 1),
(2, '29B-11223', 1, 1),
(2, '51C-44556', 1, 2),
(3, '51C-77889', 1, 2),
(3, '65D-99887', 1, 3),
(4, '43E-55667', 1, 3),
(5, '43E-88990', 1, 3);

INSERT INTO CarPrices (carId, dailyPrice, depositAmount, startDate, endDate) VALUES
(1, 800000, 2000000, '2024-01-01', NULL),
(2, 600000, 1500000, '2024-01-01', NULL),
(3, 1200000, 3000000, '2024-01-01', NULL),
(4, 2500000, 5000000, '2024-01-01', NULL),
(5, 1000000, 2500000, '2024-01-01', NULL),
(6, 8000000, 2000000, '2024-01-01', NULL);
GO

INSERT INTO CarPrices (carId, dailyPrice, depositAmount, startDate, endDate) VALUES
(9, 800000, 2000000, '2024-01-01', NULL),
(2, 600000, 1500000, '2024-01-01', NULL),
(3, 1200000, 3000000, '2024-01-01', NULL),
(4, 2500000, 5000000, '2024-01-01', NULL),
(5, 1000000, 2500000, '2024-01-01', NULL),
(6, 8000000, 2000000, '2024-01-01', NULL);

DELETE FROM CarPrices
