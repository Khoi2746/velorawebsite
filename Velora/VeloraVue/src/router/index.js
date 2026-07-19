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
import TuVan from '../components/User/TuVan.vue'
import ThongTinNguoiDung from '../components/User/ThongTinNguoiDung.vue'
import LienHeTuVan from '../components/User/LienHeTuVan.vue'

// Các file giao diện Quản trị (Admin)
import AdminDashboard from '../components/Admin/AdminDashboard.vue'
import QuanLySanPham from '../components/Admin/QuanLySanPham.vue'
import QuanLyNguoiDung from '../components/Admin/QuanLyNguoiDung.vue'
import QuanLyDonHang from '../components/Admin/QuanLyDonHang.vue'
import QuanLyKho from '../components/Admin/QuanLyKho.vue'
import XuatHoaDon from '../components/Admin/XuatHoaDon.vue'
import QuanLyThuongHieu from '../components/Admin/QuanLyThuongHieu.vue'
import PhieuNhap from '../components/Admin/PhieuNhapKho.vue'
import LoaiSanPham from '../components/Admin/LoaiSanPham.vue'
import QuanLyMaGiamGia from '../components/Admin/QuanLyMaGiamGia.vue'
import QuanLyThongKe from '../components/Admin/QuanLyThongKe.vue'
// IMPORT THÊM TRANG QUẢN LÝ LỊCH HẸN Ở ĐÂY
import AdminLichHen from '../components/Admin/AdminLichHen.vue' 

//Các file giao diện Tư Vấn Viên
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
    { path: '/tu-van', name: 'TuVan', component: TuVan },
    { path: '/san-pham/:id', name: 'ChiTietSanPham', component: ChiTietSanPham },
    { path: '/lien-he-tu-van', name: 'LienHeTuVan', component: LienHeTuVan },

    // ================== VÙNG ROUTE BẢO MẬT CỦA ADMIN ==================
    { path: '/admin/dashboard', name: 'AdminDashboard', component: AdminDashboard, meta: { requiresAdmin: true } },
    { path: '/admin/products', name: 'QLSanPham', component: QuanLySanPham, meta: { requiresAdmin: true } },
    { path: '/admin/users', name: 'QLNguoiDung', component: QuanLyNguoiDung, meta: { requiresAdmin: true } },
    { path: '/admin/orders', name: 'QLDonHang', component: QuanLyDonHang, meta: { requiresAdmin: true } },
    { path: '/admin/inventory', name: 'AdminInventory', component: QuanLyKho, meta: { requiresAdmin: true } },
    { path: '/admin/receipts', name: 'PhieuNhap', component: PhieuNhap, meta: { requiresAdmin: true } },
    { path: '/admin/invoices', name: 'XuatHoaDon', component: XuatHoaDon, meta: { requiresAdmin: true } },
    { path: '/admin/categories', name: 'QuanLyLoaiSanPham', component: LoaiSanPham, meta: { requiresAdmin: true } },
    { path: '/admin/manufacturers', name: 'QuanLyThuongHieu', component: QuanLyThuongHieu, meta: { requiresAdmin: true } },
    { path: '/admin/ma-giam-gia', name: 'QuanLyMaGiamGia', component: QuanLyMaGiamGia, meta: { requiresAdmin: true } },
    { path: '/admin/statistics', name: 'QuanLyThongKe', component: QuanLyThongKe, meta: { requiresAdmin: true } },
    
    // ĐĂNG KÝ THÊM ĐƯỜNG DẪN ADMIN LỊCH HẸN Ở ĐÂY
    { path: '/admin/lich-hen', name: 'QuanLyLichHen', component: AdminLichHen, meta: { requiresAdmin: true } },

    //=================Tư vấn Viên++++++++++++++++++++++++++++++
    {
      path: '/admin/tu-van-khach-hang',
      name: 'TuVanKhachHang',
      component: TuVanDashboard,
      meta: { requiresAdmin: true }
    }
  ]
})

// ================== 🔥 HỆ THỐNG KIỂM SOÁT REAL-TIME ==================
router.beforeEach(async (to, from, next) => {
  const loggedInUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

  // 1. Check trạng thái tài khoản (Khóa/Mở)
  if (loggedInUser && loggedInUser.email) {
    try {
      const res = await fetch(`http://localhost:8080/api/auth/check-status?email=${loggedInUser.email}`);
      if (res.ok) {
        const currentStatus = await res.text();
        if (currentStatus === 'KHOA' || currentStatus === 'BI_KHOA') {
          alert('Tài khoản của bạn đã bị khóa! Hệ thống sẽ tự động đăng xuất.');
          localStorage.removeItem('user');
          return next('/dang-nhap');
        }
      }
    } catch (error) {
      console.error("Lỗi kiểm tra trạng thái:", error);
    }
  }

  // 2. Chốt chặn quyền Admin / Staff / Chuyên viên
  if (to.meta.requiresAdmin) {
    const allowedRoles = ['ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_CHUYEN_VIEN_TU_VAN'];
    if (!loggedInUser || !allowedRoles.includes(loggedInUser.vaiTro)) {
      alert('Bạn không có quyền truy cập vào khu vực quản trị Velora!');
      return next('/dang-nhap');
    }
  }

  next(); // Cho phép chuyển trang
});

export default router