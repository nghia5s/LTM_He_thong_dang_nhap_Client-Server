
<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   HỆ THỐNG ĐĂNG NHẬP CLIENT-SERVER
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 📖 1. Giới thiệu hệ thống
Đề tài này xây dựng một hệ thống đăng nhập hoạt động theo mô hình Client–Server, cho phép người dùng đăng ký, đăng nhập và quản lý tài khoản. Điểm nổi bật là hệ thống có phân quyền Admin với giao diện quản trị riêng để theo dõi và quản lý danh sách người dùng.
Mục tiêu
-Cung cấp một hệ thống đăng nhập an toàn, đơn giản, dễ mở rộng.

-Quản lý người dùng tập trung thông qua server và tệp dữ liệu users.txt.

-Phân quyền tài khoản:

User: chỉ đăng nhập và sử dụng ứng dụng.
    
Admin: có quyền xem danh sách và xóa tài khoản.
    
## 🔧 2. Công nghệ sử dụng
-Ngôn ngữ: Java

-Giao diện: Swing (JFrame, JTable)

-Kết nối mạng: Socket TCP

-Lưu trữ dữ liệu: File users.txt

## 🚀 3. Hình ảnh các chức năng

<p align="center">
  <img src="docs/p (1).jpg" alt="Ảnh 1" width="800"/>
</p>

<p align="center">
  <img src="docs/p (3).jpg" alt="Ảnh 1" width="800"/>
</p>

<p align="center">
  <img src="docs/p (2).jpg" alt="Ảnh 1" width="800"/>
</p>

<p align="center">
  <img src="docs/p (4).jpg" alt="Ảnh 1" width="800"/>
</p>

<p align="center">
  <img src="docs/p (6).jpg" alt="Ảnh 1" width="800"/>
</p>

<p align="center">
  <img src="docs/p (5).jpg" alt="Ảnh 1" width="800"/>
</p>


## 📝 4. Hướng dẫn cài đặt và sử dụng

## 1. Cài đặt môi trường

Trước khi chạy dự án, cần chuẩn bị:

* **Java Development Kit (JDK)** 8 trở lên

  * Tải tại: [https://adoptium.net/](https://adoptium.net/)
  * Kiểm tra bằng lệnh: `java -version`
* **Git** để tải mã nguồn từ GitHub

  * Tải tại: [https://git-scm.com/](https://git-scm.com/)
  * Kiểm tra bằng lệnh: `git --version`
* IDE đề xuất: **IntelliJ IDEA** hoặc **Eclipse**

---

## 2. Tải dự án từ GitHub

1. Mở terminal hoặc cmd.
2. Chạy lệnh sau để tải dự án:

   ```bash
   git clone <https://github.com/nghia5s/LTM_He_thong_dang_nhap_Client-Server.git>
   ```
3. Truy cập vào thư mục dự án:

   ```bash
   cd <ten-thu-muc-du-an>
   ```

---

## 3. Mở dự án trên IDE

1. Mở IDE (IntelliJ hoặc Eclipse).
2. Chọn **Open Project** và chọn thư mục vừa tải về.
3. Đợi IDE load toàn bộ cấu trúc dự án.

---

## 4. Chạy Server

1. Mở file `server.java` trong package `login`.
2. Nhấn **Run** để chạy.
3. Console sẽ hiện thông báo `Server đang chạy trên cổng 12345`.

---

## 5. Chạy Client

1. Mở file `client.java` (hoặc file giao diện Client).
2. Nhấn **Run** để khởi động giao diện đăng nhập.
3. Đăng nhập bằng tài khoản:

   * Admin mặc định: `admin` / `admin123`

---

## 6. Tính năng

* Đăng nhập và đăng ký tài khoản mới.
* Admin đăng nhập để xem danh sách user.
* Admin có thể xóa tài khoản người dùng.

---

Vậy là bạn đã tải và chạy thành công hệ thống đăng nhập Client–Server.


## Thông tin cá nhân
**Họ tên**: Trần Hiếu Nghĩa.  
**Lớp**: CNTT 16-03.  
**Email**: nt313201@gmail.com.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.
