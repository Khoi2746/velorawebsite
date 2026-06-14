import { createRouter, createWebHistory } from 'vue-router'

// Sửa lại đường dẫn: trỏ vào thư mục '../components/' và dùng đúng tên file của em
import HomeView from '../components/User/TrangChu.vue'
import DangNhap from '../components/Login.vue'
import DangKy from '../components/SignIn.vue'
import ThuongHieu from '../components/User/ThuongHieu.vue'
import SanPham from '../components/User/SanPham.vue'
import AdminDashboard from '../components/Admin/AdminDashboard.vue'
import QuanLySanPham from '../components/Admin/QuanLySanPham.vue'
import QuanLyNguoiDung from '../components/Admin/QuanLyNguoiDung.vue'
import QuanLyDonHang from '../components/Admin/QuanLyDonHang.vue'

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
    meta: {requiresAdmin: true}
    }

  ]
})

export default router