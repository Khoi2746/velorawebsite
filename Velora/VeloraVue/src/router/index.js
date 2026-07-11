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
      path: '/thong-tin-ca-nhan',
      name: 'ThongTinNguoiDung',
      component: ThongTinNguoiDung
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
    {
      path: '/gio-hang',
      name: 'GioHang',
      component: GioHang
    },
    {
      path: '/don-hang',
      name: 'DonHang',
      component: DonHang
    },
    {
      path: '/tu-van',
      name:'TuVan',
      component: TuVan
    },
    {
      path: '/san-pham/:id', // ĐÃ THÊM: Khai báo đường dẫn động để bắt ID sản phẩm khi bấm xem chi tiết
      name: 'ChiTietSanPham',
      component: ChiTietSanPham
    },
    
    // ================== VÙNG ROUTE BẢO MẬT CỦA ADMIN ==================
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
      meta: { requiresAdmin: true } 
    },
    {
      path: '/admin/receipts',
      name: 'PhieuNhap',
      component: PhieuNhap,
      meta: { requiresAdmin: true } 
    },
    {
      path: '/admin/invoices',
      name: 'XuatHoaDon',
      component: XuatHoaDon,
      meta: { requiresAdmin: true } 
    },
    {
      path: '/admin/categories',
      name: 'QuanLyLoaiSanPham',
      component: LoaiSanPham, 
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
      component: QuanLyMaGiamGia, 
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/statistics',
      name: 'QuanLyThongKe',
      component: QuanLyThongKe,
      meta: { requiresAdmin: true }
    }
  ]
})

// ================== 🔥 HỆ THỐNG KIỂM SOÁT VÀ TRỤC XUẤT REAL-TIME VUE GUARD ==================
router.beforeEach(async (to, from, next) => {
  // Lấy thông tin user hiện tại từ bộ nhớ trình duyệt
  const loggedInUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

  // 1. Nếu có user đang đăng nhập dở dang -> Bắt buộc check trạng thái sống động từ DB
  if (loggedInUser && loggedInUser.email) {
    try {
      const res = await fetch(`http://localhost:8080/api/auth/check-status?email=${loggedInUser.email}`);
      
      if (res.ok) {
        const currentStatus = await res.text();
        
        // Phát hiện tài khoản dính trạng thái khóa -> Thực thi cưỡng chế đăng xuất lập tức
        if (currentStatus === 'KHOA' || currentStatus === 'BI_KHOA') {
          alert('Tài khoản của bạn đã bị khóa bởi Ban quản trị hệ thống Velora! Hệ thống sẽ tự động đăng xuất.');
          localStorage.removeItem('user'); // Xóa bỏ token / session lậu tại trình duyệt
          
          if (to.name !== 'DangNhap') {
            return next('/dang-nhap'); // Đá văng về trang Đăng Nhập công khai
          }
        }
      }
    } catch (error) {
      console.error("Lỗi kết nối kiểm tra trạng thái tự động:", error);
    }
  }

  // 2. Chặn truy cập trái phép vào trang Admin
  if (to.meta.requiresAdmin) {
    if (!loggedInUser || (loggedInUser.vaiTro !== 'ROLE_ADMIN' && loggedInUser.vaiTro !== 'ROLE_STAFF')) {
      alert('Bạn không có quyền truy cập vào khu vực quản trị Velora!');
      return next('/dang-nhap');
    }
  }

  next(); // Cho phép chuyển trang an toàn
});

export default router