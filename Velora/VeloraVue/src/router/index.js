import { createRouter, createWebHistory } from 'vue-router'

// Sửa lại đường dẫn: trỏ vào thư mục '../components/' và dùng đúng tên file của em
import HomeView from '../components/User/TrangChu.vue'
import DangNhap from '../components/Login.vue'
import DangKy from '../components/SignIn.vue'
import ThuongHieu from '../components/User/ThuongHieu.vue'
import SanPham from '../components/User/SanPham.vue'

// Các file Admin
import AdminDashboard from '../components/Admin/AdminDashboard.vue'
import QuanLySanPham from '../components/Admin/QuanLySanPham.vue'
import QuanLyNguoiDung from '../components/Admin/QuanLyNguoiDung.vue'
import QuanLyDonHang from '../components/Admin/QuanLyDonHang.vue'
import QuanLyKho from '../components/Admin/QuanLyKho.vue'
import XuatHoaDon from '../components/Admin/XuatHoaDon.vue'
import QuanLyThuongHieu from '../components/Admin/QuanLyThuongHieu.vue'
import PhieuNhap from '../components/Admin/PhieuNhapKho.vue' // IMPORT THÊM TRANG PHIẾU NHẬP
import LoaiSanPham from '../components/Admin/LoaiSanPham.vue'
import QuanLyMaGiamGia from '../components/Admin/QuanLyMaGiamGia.vue' // IMPORT TRANG QUẢN LÝ MÃ GIẢM GIÁ
import QuanLyThongKe from '../components/Admin/QuanLyThongKe.vue' // IMPORT THÊM TRANG THỐNG KÊ DOANH THU

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/dang-nhap',
      name: 'DangNhap',
      component: DangNhap
    },
    {
      path: '/dang-ky',
      name: 'DangKy',
      component: DangKy
    },
    {
      path: '/thuong-hieu',
      name: 'ThuongHieu',
      component: ThuongHieu
    },
    {
      path: '/dong-ho-co-san',
      name: 'SanPham',
      component: SanPham
    },
    
    // ================== VÙNG ROUTE CỦA ADMIN ==================
    {
      path: '/admin/dashboard',
      name: 'AdminDashboard',
      component: AdminDashboard,
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/products',
      name: 'QLSanPham',
      component: QuanLySanPham,
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/users',
      name: 'QLNguoiDung',
      component: QuanLyNguoiDung,
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/orders',
      name: 'QLDonHang',
      component: QuanLyDonHang,
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/inventory',
      name: 'AdminInventory',
      component: QuanLyKho,
      meta: { requiresAdmin: true } // ĐÃ BỔ SUNG BẢO MẬT CHO TRANG KHO
    },
    {
      path: '/admin/receipts',
      name: 'PhieuNhap',
      component: PhieuNhap,
      meta: { requiresAdmin: true } // ROUTE CHO TRANG DUYỆT PHIẾU NHẬP KHỎ
    },
    {
      path: '/admin/invoices',
      name: 'XuatHoaDon',
      component: XuatHoaDon,
      meta: { requiresAdmin: true } // ĐÃ THÊM ROUTE CHO XUẤT HÓA ĐƠN
    },
    {
      path: '/admin/categories',
      name: 'QuanLyLoaiSanPham',
      component: LoaiSanPham, // Đổi thành tên biến vừa import ở trên
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/manufacturers',
      name: 'QuanLyThuongHieu',
      component: QuanLyThuongHieu,
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/ma-giam-gia',
      name: 'QuanLyMaGiamGia',
      component: QuanLyMaGiamGia, // ROUTE CHO TRANG QUẢN LÝ MÃ GIẢM GIÁ
      meta: { requiresAdmin: true }
    },
     {
      path: '/admin/statistics',
      name: 'QuanLyThongKe',
      component: QuanLyThongKe,
      meta: { requiresAdmin: true } // ROUTE CHO TRANG THỐNG KÊ DOANH THU BIỂU ĐỒ
    }
  ]
})

export default router