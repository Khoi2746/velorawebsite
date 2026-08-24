# 📑 BẢNG PHÂN CHIA CÔNG VIỆC DỰ ÁN VELORA

---

## 👤 THÀNH VIÊN: ANH TÚ (FULLSTACK - LỊCH HẸN, MÃ GIẢM GIÁ, QUẢN LÝ KHO & PHIẾU NHẬP KHO)

### 🎯 Chức năng phụ trách:

* **Client:**
  * Đặt lịch hẹn tư vấn trực tiếp xem đồng hồ (`LienHeTuVan.vue`).

* **Admin:**
  * Quản lý & Duyệt Lịch hẹn tư vấn, xem Lịch sử hẹn (`AdminLichHen.vue`).
  * Tạo mới & Quản lý danh sách Mã giảm giá / Voucher (`QuanLyMaGiamGia.vue`).
  * Quản lý kho hàng (`QuanLyKho.vue`): Tự động cộng dồn số lượng tồn kho.
  * Lập & Duyệt Phiếu nhập kho mới (`PhieuNhapKho.vue`).
  * Hệ thống Gửi Email tự động cho khách hàng khi Lịch hẹn được cập nhật (kèm file PDF xác nhận).

---

### 📁 Danh sách File Backend phụ trách:
* `LichHenController.java` (API Đặt & Duyệt lịch hẹn)
* `EmailLichHen.java`, `EmailService.java` (API & Service Gửi Email tự động kèm PDF)
* `MaGiamGiaController.java` (API Tạo & Quản lý Mã giảm giá Voucher)
* `PhieuNhapKhoController.java` (API Lập phiếu nhập kho & Tự động cộng tồn kho)
* `LichHen.java`, `MaGiamGia.java`, `PhieuNhapKho.java`, `ChiTietPhieuNhap.java` (Entities)

---

### 🎨 Danh sách File Frontend phụ trách:
* `LienHeTuVan.vue` (Khách đặt lịch hẹn tư vấn)
* `AdminLichHen.vue` (Admin quản lý & duyệt lịch hẹn)
* `QuanLyMaGiamGia.vue` (Admin tạo mới & quản lý mã giảm giá)
* `QuanLyKho.vue` (Admin quản lý tồn kho)
* `PhieuNhapKho.vue` (Admin lập & duyệt phiếu nhập kho)

---

### 💡 Kiến thức cần học & Kỹ thuật sử dụng:
* Spring `JavaMailSender` (Gửi Email HTML tự động & đính kèm file OpenPDF).
* Java Date Time API & Timezone.
* Logic tính toán mã giảm giá Voucher.
* Logic tính toán cộng dồn tồn kho tự động.
* Đồng bộ giao diện Modal Popup hiển thị ở chính giữa màn hình.
