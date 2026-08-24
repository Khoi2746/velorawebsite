import { createRouter, createWebHistory } from 'vue-router'

// Các file giao diện Người dùng (User)
import HomeView from '../components/User/TrangChu.vue'
import DangNhap from '../components/Login.vue'
import DangKy from '../components/SignIn.vue'
import ThuongHieu from '../components/User/ThuongHieu.vue'
import SanPham from '../components/User/SanPham.vue'
import ChiTietSanPham from '../components/User/ChiTietSanPham.vue'
import GioHang from '../components/User/GioHang.vue'
import DonHang from '../components/User/DonHang.vue'
import BaoHanh from '../components/User/BaoHanh.vue'
import ThongTinNguoiDung from '../components/User/ThongTinNguoiDung.vue'
import LienHeTuVan from '../components/User/LienHeTuVan.vue'
import QuenMatKhau from '../components/User/QuenMatKhau.vue'
import CapNhatThongTinOauth2 from '../components/User/CapNhatThongTinOauth2.vue';

// Import các file Thanh toán, Quét QR và Hoàn tiền
import ThanhToanDonHang from '../components/User/ThanhToanDonHang.vue'
import ThanhToanQR from '../components/User/ThanhToanQR.vue'
import YeucauHoantien from '../components/User/YeucauHoantien.vue'

// Các file giao diện Quản trị (Admin)
import AdminDashboard from '../components/Admin/AdminDashboard.vue'
import QuanLySanPham from '../components/Admin/QuanLySanPham.vue'
import QuanLyNguoiDung from '../components/Admin/QuanLyNguoiDung.vue'
import QuanLyDonHang from '../components/Admin/QuanLyDonHang.vue'
import QuanLyKho from '../components/Admin/QuanLyKho.vue'
import XuatHoaDon from '../components/Admin/XuatHoaDon.vue'
import QuanLyThuongHieu from '../components/Admin/QuanLyThuongHieu.vue'
import QuanLyBaoHanh from '../components/Admin/QuanlyBaoHanh.vue'
import PhieuNhap from '../components/Admin/PhieuNhapKho.vue'
import LoaiSanPham from '../components/Admin/LoaiSanPham.vue'
import QuanLyMaGiamGia from '../components/Admin/QuanLyMaGiamGia.vue'
import QuanLyThongKe from '../components/Admin/QuanLyThongKe.vue'
import AdminLichHen from '../components/Admin/AdminLichHen.vue'
import QuanLyHoanTien from '../components/Admin/QuanLyHoanTien.vue' 
import QuanLySOC from '../components/Admin/QuanLySOC.vue'

// Các file giao diện Tư Vấn Viên
import TuVanDashboard from '../components/ChuyenVienTuVan/TuVanDashboard.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/dang-nhap', name: 'DangNhap', component: DangNhap },
    { path: '/dang-ky', name: 'DangKy', component: DangKy },
    { path: '/thong-tin-ca-nhan', name: 'ThongTinNguoiDung', component: ThongTinNguoiDung },
    { path: '/thuong-hieu', name: 'ThuongHieu', component: ThuongHieu },
    { path: '/dong-ho-co-san', name: 'SanPham', component: SanPham },
    { path: '/gio-hang', name: 'GioHang', component: GioHang },
    { path: '/don-hang', name: 'DonHang', component: DonHang },
    { path: '/bao-hanh', name: 'BaoHanh', component: BaoHanh },
    { path: '/san-pham/:id', name: 'ChiTietSanPham', component: ChiTietSanPham },
    { path: '/lien-he-tu-van', name: 'LienHeTuVan', component: LienHeTuVan },
    { path: '/quen-mat-khau', name: 'QuenMatKhau', component: QuenMatKhau },

    // ROUTE THANH TOÁN, QUÉT MÃ QR & HOÀN TIỀN
    { path: '/checkout', name: 'Checkout', component: ThanhToanDonHang },
    { path: '/scan-qr', name: 'ScanQR', component: ThanhToanQR },
    { path: '/yeu-cau-hoan-tien', name: 'YeuCauHoanTien', component: YeucauHoantien },

    // ================== VÙNG ROUTE BẢO MẬT CỦA ADMIN ==================
    { path: '/admin/dashboard', name: 'AdminDashboard', component: AdminDashboard, meta: { requiresAdmin: true } },
    { path: '/admin/products', name: 'QLSanPham', component: QuanLySanPham, meta: { requiresAdmin: true } },
    { path: '/admin/users', name: 'QLNguoiDung', component: QuanLyNguoiDung, meta: { requiresAdmin: true } },
    { path: '/admin/orders', name: 'QLDonHang', component: QuanLyDonHang, meta: { requiresAdmin: true } },
    { path: '/admin/quan-ly-hoan-tien', name: 'QLHoanTien', component: QuanLyHoanTien, meta: { requiresAdmin: true } },
    { path: '/admin/inventory', name: 'AdminInventory', component: QuanLyKho, meta: { requiresAdmin: true } },
    { path: '/admin/receipts', name: 'PhieuNhap', component: PhieuNhap, meta: { requiresAdmin: true } },
    { path: '/admin/invoices', name: 'XuatHoaDon', component: XuatHoaDon, meta: { requiresAdmin: true } },
    { path: '/admin/categories', name: 'QuanLyLoaiSanPham', component: LoaiSanPham, meta: { requiresAdmin: true } },
    { path: '/admin/manufacturers', name: 'QuanLyThuongHieu', component: QuanLyThuongHieu, meta: { requiresAdmin: true } },
    { path: '/admin/ma-giam-gia', name: 'QuanLyMaGiamGia', component: QuanLyMaGiamGia, meta: { requiresAdmin: true } },
    { path: '/admin/statistics', name: 'QuanLyThongKe', component: QuanLyThongKe, meta: { requiresAdmin: true } },
    { path: '/admin/quan-ly-bao-hanh', name: 'QuanLyBaoHanh', component: QuanLyBaoHanh, meta: { requiresAdmin: true } },
    { path: '/admin/lich-hen', name: 'QuanLyLichHen', component: AdminLichHen, meta: { requiresAdmin: true } },
    {
      path: '/admin/soc',
      name: 'QuanLySOC',
      component: QuanLySOC,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/cap-nhat-thong-tin',
      name: 'CapNhatThongTin',
      component: CapNhatThongTinOauth2,
      meta: { requiresAuth: true }
    },

    // ================== TƯ VẤN VIÊN ==================
    {
      path: '/admin/tu-van-khach-hang',
      name: 'TuVanKhachHang',
      component: TuVanDashboard,
      meta: { requiresAdmin: true }
    }
  ]
})

// ================== HỆ THỐNG KIỂM SOÁT REAL-TIME CHUẨN SESSION BACKEND ==================
router.beforeEach(async (to, from, next) => {
  // 🔥 Dọn sạch ký hiệu #_=_ hoặc _=_ do Facebook tự động gắn vào URL ngay khi router quét qua
  if (window.location.hash.includes('_=_') || window.location.href.includes('_=_')) {
    const cleanUrl = window.location.href.replace(/#?_=_/g, '');
    window.history.replaceState({}, document.title, cleanUrl);
  }

  // 🔥 Hàm gom dữ liệu thông minh từ cả 2 nguồn (Ghi nhớ và Không ghi nhớ)
  const getUserData = () => {
    const local = localStorage.getItem('user');
    if (local) return JSON.parse(local);
    const session = sessionStorage.getItem('user');
    if (session) return JSON.parse(session);
    return null;
  };

  const loggedInUser = getUserData();

  // Kiểm tra trạng thái tài khoản thời gian thực nếu đã đăng nhập
  if (loggedInUser && loggedInUser.email) {
    try {
      const res = await fetch(`http://localhost:8080/api/auth/check-status?email=${loggedInUser.email}`);
      if (res.ok) {
        const currentStatus = await res.text();
        if (currentStatus === 'KHOA' || currentStatus === 'BI_KHOA') {
          alert('Tài khoản của bạn đã bị khóa! Hệ thống sẽ tự động đăng xuất.');
          // 🔥 Phải dọn sạch cả 2 kho để tránh kẹt phiên
          localStorage.removeItem('user');
          sessionStorage.removeItem('user');
          return next('/dang-nhap');
        }
      }
    } catch (error) {
      console.error("Lỗi kiểm tra trạng thái:", error);
    }
  }

  // Nếu người dùng vào route cần quyền Admin
  if (to.meta.requiresAdmin) {
    try {
      // BẮT BUỘC FETCH TỪ BACKEND ĐỂ CHECK QUYỀN CHÍNH XÁC (Không xài Local/Session ảo)
      const res = await fetch(`http://localhost:8080/api/auth/me`, {
        credentials: 'include' // Bắt buộc để gửi cookie session lên server
      });

      if (!res.ok) throw new Error("Unauthorized");

      const text = await res.text();
      if (!text) {
        alert('Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại!');
        localStorage.removeItem('user');
        sessionStorage.removeItem('user');
        return next('/dang-nhap');
      }

      const currentUser = JSON.parse(text);

      // Check tài khoản có bị khóa không lần 2 từ dữ liệu Session
      if (currentUser.trangThai === 'KHOA' || currentUser.trangThai === 'BI_KHOA') {
        alert('Tài khoản của bạn đã bị khóa! Hệ thống sẽ tự động đăng xuất.');
        localStorage.removeItem('user');
        sessionStorage.removeItem('user');
        return next('/dang-nhap');
      }

      // Check quyền
      const allowedRoles = ['ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_CHUYEN_VIEN_TU_VAN'];
      if (!allowedRoles.includes(currentUser.vaiTro)) {
        alert('Bạn không có quyền truy cập vào khu vực quản trị Velora!');
        return next('/'); // Đá về trang chủ thay vì bắt đăng nhập lại
      }

      // Vượt qua hết thì cho vào Admin
      next();

    } catch (error) {
      alert('Vui lòng đăng nhập bằng tài khoản quản trị để truy cập!');
      localStorage.removeItem('user');
      sessionStorage.removeItem('user');
      return next('/dang-nhap');
    }
  } else {
    // Nếu vào trang thường thì thả cửa cho đi qua (Check khóa đã có ở trên lo)
    next();
  }
});

export default router