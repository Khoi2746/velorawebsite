<template>
  <div class="shop-page">
    <Header />

    <main class="shop-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">SẢN PHẨM</h1>
          <div class="title-divider">
            <span class="diamond"></span>
          </div>
        </div>

        <!-- THANH BỘ LỌC ĐƯỢC TỐI ƯU GIAO DIỆN -->
        <div class="filter-bar">
          <div class="custom-dropdown" :class="{ active: activeDropdown === 'price' }" @click="toggleDropdown('price')">
            <div class="dropdown-selected">
              <span>{{ filters.priceText || 'KHOẢNG GIÁ' }}</span>
              <i class="fa-solid fa-chevron-down"></i>
            </div>
            <Transition name="luxe-fade-slide">
              <div class="dropdown-options" v-show="activeDropdown === 'price'">
                <div class="option-item" @click.stop="selectOption('price', '', 'KHOẢNG GIÁ')">KHOẢNG GIÁ</div>
                <div class="option-item" @click.stop="selectOption('price', 'under-100m', 'DƯỚI 100 TRIỆU')">DƯỚI 100 TRIỆU</div>
                <div class="option-item" @click.stop="selectOption('price', '100m-500m', '100 TRIỆU - 500 TRIỆU')">100 TRIỆU - 500 TRIỆU</div>
                <div class="option-item" @click.stop="selectOption('price', 'over-500m', 'TRÊN 500 TRIỆU')">TRÊN 500 TRIỆU</div>
              </div>
            </Transition>
          </div>

          <div class="custom-dropdown" :class="{ active: activeDropdown === 'brand' }" @click="toggleDropdown('brand')">
            <div class="dropdown-selected">
              <span>{{ filters.brandText || 'THƯƠNG HIỆU' }}</span>
              <i class="fa-solid fa-chevron-down"></i>
            </div>
            <Transition name="luxe-fade-slide">
              <div class="dropdown-options" v-show="activeDropdown === 'brand'">
                <div class="option-item" @click.stop="selectOption('brand', '', 'THƯƠNG HIỆU')">THƯƠNG HIỆU</div>
                <div class="option-item" v-for="b in brands" :key="b.maThuongHieu" @click.stop="selectOption('brand', b.maThuongHieu, b.tenThuongHieu)">
                  {{ b.tenThuongHieu }}
                </div>
              </div>
            </Transition>
          </div>

          <div class="custom-dropdown" :class="{ active: activeDropdown === 'category' }" @click="toggleDropdown('category')">
            <div class="dropdown-selected">
              <span>{{ filters.categoryText || 'LOẠI SẢN PHẨM' }}</span>
              <i class="fa-solid fa-chevron-down"></i>
            </div>
            <Transition name="luxe-fade-slide">
              <div class="dropdown-options" v-show="activeDropdown === 'category'">
                <div class="option-item" @click.stop="selectOption('category', '', 'LOẠI SẢN PHẨM')">LOẠI SẢN PHẨM</div>
                <div class="option-item" v-for="cat in categories" :key="cat.maLoai" @click.stop="selectOption('category', cat.maLoai, cat.tenLoai)">
                  {{ cat.tenLoai }}
                </div>
              </div>
            </Transition>
          </div>

          <div class="custom-dropdown" :class="{ active: activeDropdown === 'gender' }" @click="toggleDropdown('gender')">
            <div class="dropdown-selected">
              <span>{{ filters.genderText || 'GIỚI TÍNH' }}</span>
              <i class="fa-solid fa-chevron-down"></i>
            </div>
            <Transition name="luxe-fade-slide">
              <div class="dropdown-options" v-show="activeDropdown === 'gender'">
                <div class="option-item" @click.stop="selectOption('gender', '', 'GIỚI TÍNH')">GIỚI TÍNH</div>
                <div class="option-item" @click.stop="selectOption('gender', 'Nam', 'Nam')">Nam</div>
                <div class="option-item" @click.stop="selectOption('gender', 'Nữ', 'Nữ')">Nữ</div>
              </div>
            </Transition>
          </div>
        </div>

        <template v-if="filteredProducts.length > 0">
          <div class="product-grid">
            <div class="product-card" v-for="product in paginatedProducts" :key="product.maSanPham">
              <div class="tag-new" v-if="product.loaiSanPham">
                {{ product.loaiSanPham.tenLoai }}
              </div>

              <router-link :to="`/san-pham/${product.maSanPham}`" class="product-image-wrapper">
                <img
                  :src="product.anhDaiDien && product.anhDaiDien.startsWith('http') ? product.anhDaiDien : '/img/' + product.anhDaiDien"
                  :alt="product.tenSanPham" class="product-image" />
              </router-link>

              <div class="product-info">
                <router-link :to="`/san-pham/${product.maSanPham}`" class="product-name-link">
                  <h3 class="product-name">{{ product.tenSanPham }}</h3>
                </router-link>

                <div class="product-price">
                  {{ product.giaBan > 100000000 ? 'Liên hệ báo giá' : formatPrice(product.giaBan) }}
                </div>

                <!-- BỔ SUNG: Hiển thị thông tin Bảo hành ở đây cho khách nhìn thấy trực quan -->
                <div class="product-warranty" v-if="product.thoiGianBaoHanh">
                  <i class="fa-solid fa-shield-halved"></i> Bảo hành chính hãng: <b>{{ product.thoiGianBaoHanh }} tháng</b>
                </div>
              </div>

              <div class="product-action">
                <router-link :to="`/san-pham/${product.maSanPham}`" class="btn-contact">
                  XEM CHI TIẾT
                </router-link>
              </div>
            </div>
          </div>

          <div class="pagination-container" v-if="totalPages > 1">
            <button 
              class="page-btn" 
              :disabled="currentPage === 1" 
              @click="currentPage--"
            >
              <i class="fas fa-chevron-left"></i> Trước
            </button>
            
            <div class="page-numbers">
              <button 
                v-for="page in totalPages" 
                :key="page" 
                class="page-number-btn"
                :class="{ active: page === currentPage }"
                @click="currentPage = page"
              >
                {{ page }}
              </button>
            </div>

            <button 
              class="page-btn" 
              :disabled="currentPage === totalPages" 
              @click="currentPage++"
            >
              Sau <i class="fas fa-chevron-right"></i>
            </button>
          </div>
        </template>

        <div class="empty-product-state" v-else>
          <div class="empty-icon-wrapper">
            <i class="fa-regular fa-hourglass-half"></i>
          </div>
          <h3 class="empty-title">Tuyệt Tác Đang Được Tuyển Chọn</h3>
          <p class="empty-desc">
            Những cỗ máy thời gian đến từ thương hiệu này hiện đang trong quá trình cập bến Velora Boutique. 
            <br/>
            Quý khách có thể liên hệ với chuyên viên để đặt trước mẫu đồng hồ yêu thích, hoặc tiếp tục khám phá các bộ sưu tập danh giá khác của chúng tôi.
          </p>
          <div class="empty-actions">
            <router-link to="/lien-he-tu-van" class="btn-vvip">
              LIÊN HỆ TƯ VẤN VVIP
            </router-link>
          </div>
        </div>

      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const route = useRoute()
const router = useRouter()

const products = ref([])
const filteredProducts = ref([])
const brands = ref([])
const categories = ref([])
const activeDropdown = ref(null)

const currentPage = ref(1)
const pageSize = ref(9)

const filters = ref({
  search: '', 
  price: '', priceText: '',
  brand: '', brandText: '',
  category: '', categoryText: '',
  gender: '', genderText: ''
})

const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / pageSize.value) || 1
})

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredProducts.value.slice(start, end)
})

const toggleDropdown = (dropdownName) => {
  if (activeDropdown.value === dropdownName) {
    activeDropdown.value = null
  } else {
    activeDropdown.value = dropdownName
  }
}

const applyFilters = () => {
  let result = [...products.value]

  if (filters.value.search) {
    const keyword = filters.value.search.toLowerCase()
    result = result.filter(p => 
      p.tenSanPham.toLowerCase().includes(keyword) || 
      (p.thuongHieu && p.thuongHieu.tenThuongHieu.toLowerCase().includes(keyword))
    )
  }

  if (filters.value.brand) {
    result = result.filter(p => p.maThuongHieu === parseInt(filters.value.brand) || (p.thuongHieu && p.thuongHieu.maThuongHieu === parseInt(filters.value.brand)))
  }

  if (filters.value.price) {
    if (filters.value.price === 'under-100m') result = result.filter(p => p.giaBan < 100000000)
    else if (filters.value.price === '100m-500m') result = result.filter(p => p.giaBan >= 100000000 && p.giaBan <= 500000000)
    else if (filters.value.price === 'over-500m') result = result.filter(p => p.giaBan > 500000000)
  }

  if (filters.value.category) {
    result = result.filter(p => p.loaiSanPham && p.loaiSanPham.maLoai === parseInt(filters.value.category))
  }

  if (filters.value.gender) {
    result = result.filter(p => p.gioiTinh && p.gioiTinh.toLowerCase() === filters.value.gender.toLowerCase())
  }

  filteredProducts.value = result
  currentPage.value = 1 
}

const selectOption = (type, value, text) => {
  filters.value[type] = value
  filters.value[`${type}Text`] = text
  activeDropdown.value = null
  applyFilters()
}

const closeDropdowns = (e) => {
  if (!e.target.closest('.custom-dropdown')) {
    activeDropdown.value = null
  }
}

const formatPrice = (value) => {
  if (!value) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const loadData = async () => {
  try {
    const resProducts = await fetch('http://localhost:8080/api/san-pham')
    if (resProducts.ok) {
      const dataProd = await resProducts.json()
      products.value = dataProd.filter(p => p.trangThai === 'CON_HANG' || p.trangThai === 1)
      filteredProducts.value = products.value
    }
    
    const resBrands = await fetch('http://localhost:8080/api/thuong-hieu')
    if (resBrands.ok) {
      brands.value = await resBrands.json()
    }
    
    const resCategories = await fetch('http://localhost:8080/api/loai-san-pham')
    if (resCategories.ok) {
      categories.value = await resCategories.json()
    }

    syncFiltersFromUrl()

  } catch (error) {
    console.error('Lỗi kết nối API hệ thống:', error)
  }
}

const syncFiltersFromUrl = () => {
  filters.value.search = route.query.search || ''

  if (route.query.brand) {
    const brandIdFromUrl = parseInt(route.query.brand)
    const targetBrand = brands.value.find(b => b.maThuongHieu === brandIdFromUrl)
    if (targetBrand) {
      filters.value.brand = brandIdFromUrl
      filters.value.brandText = targetBrand.tenThuongHieu
    }
  } else {
    filters.value.brand = ''
    filters.value.brandText = ''
  }

  applyFilters()
}

watch(() => route.query, () => {
  syncFiltersFromUrl()
})

onMounted(() => {
  loadData()
  document.addEventListener('click', closeDropdowns)
})
onUnmounted(() => {
  document.removeEventListener('click', closeDropdowns)
})
</script>

<style scoped>
/* GIAO DIỆN BỔ SUNG ĐỂ HIỂN THỊ THÔNG TIN BẢO HÀNH & TINH CHỈNH GỌN GÀNG */
.product-warranty {
  font-size: 0.85rem;
  color: #6c757d;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.product-warranty i {
  color: #c5a059; /* Màu ánh kim đặc trưng cho website đồng hồ */
}

.custom-dropdown .dropdown-selected {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.custom-dropdown .dropdown-selected i {
  font-size: 0.75rem;
  transition: transform 0.3s ease;
}

.custom-dropdown.active .dropdown-selected i {
  transform: rotate(180deg);
}
</style>

<style scoped>
@import "../CSS/User/SanPham.css";

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 40px;
  margin-bottom: 20px;
}

.page-btn {
  background: #fff;
  border: 1px solid #d1d5db;
  color: #333;
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #c5a880;
  border-color: #c5a880;
  color: #fff;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 6px;
}

.page-number-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #d1d5db;
  color: #333;
  font-size: 13px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-number-btn:hover {
  border-color: #c5a880;
  color: #c5a880;
}

.page-number-btn.active {
  background: #c5a880;
  border-color: #c5a880;
  color: #fff;
  font-weight: 600;
}

.empty-product-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 70px 20px;
  background-color: #fcfbf9; 
  border: 1px solid rgba(197, 168, 128, 0.3);
  border-radius: 8px;
  margin: 50px auto;
  max-width: 750px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.02);
}

.empty-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(197, 168, 128, 0.15);
  margin-bottom: 24px;
}

.empty-icon-wrapper i {
  font-size: 24px;
  color: #c5a880; 
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.empty-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  max-width: 600px;
  margin-bottom: 35px;
}

.empty-actions {
  display: flex;
  justify-content: center;
}

.btn-vvip {
  display: inline-block;
  background-color: #1a1a1a;
  color: #fff;
  padding: 14px 36px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1.5px;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.3s ease;
  border: 1px solid #1a1a1a;
}

.btn-vvip:hover {
  background-color: #c5a880;
  border-color: #c5a880;
  color: #fff;
}
</style>