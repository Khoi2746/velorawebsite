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

        <div class="filter-bar">
          <div class="custom-dropdown" :class="{ active: activeDropdown === 'price' }" @click="toggleDropdown('price')">
            <div class="dropdown-selected">{{ filters.priceText || 'KHOẢNG GIÁ' }}</div>
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
            <div class="dropdown-selected">{{ filters.brandText || 'THƯƠNG HIỆU' }}</div>
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
            <div class="dropdown-selected">{{ filters.categoryText || 'LOẠI SẢN PHẨM' }}</div>
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
            <div class="dropdown-selected">{{ filters.genderText || 'GIỚI TÍNH' }}</div>
            <Transition name="luxe-fade-slide">
              <div class="dropdown-options" v-show="activeDropdown === 'gender'">
                <div class="option-item" @click.stop="selectOption('gender', '', 'GIỚI TÍNH')">GIỚI TÍNH</div>
                <div class="option-item" @click.stop="selectOption('gender', 'Nam', 'Nam')">Nam</div>
                <div class="option-item" @click.stop="selectOption('gender', 'Nữ', 'Nữ')">Nữ</div>
              </div>
            </Transition>
          </div>
        </div>

        <!-- DANH SÁCH SẢN PHẨM PHÂN TRANG -->
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
                  {{ product.giaBan > 100000000 ? 'Liên hệ để biết thêm chi tiết' : formatPrice(product.giaBan) }}
                </div>
              </div>

              <div class="product-action">
                <router-link :to="`/san-pham/${product.maSanPham}`" class="btn-contact">
                  XEM CHI TIẾT
                </router-link>
              </div>
            </div>
          </div>

          <!-- THANH PHÂN TRANG -->
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

        <div v-else class="empty-state">
          <p>Không tìm thấy sản phẩm nào phù hợp hoặc đang tải dữ liệu...</p>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'
import Info from '../info.vue'

const route = useRoute()

const products = ref([])
const filteredProducts = ref([])
const brands = ref([])
const categories = ref([])
const activeDropdown = ref(null)

// Cấu hình phân trang: 3 dòng sản phẩm (4 cột x 3 dòng = 12 sản phẩm)
const currentPage = ref(1)
const pageSize = ref(12)

const filters = ref({
  search: '', 
  price: '', priceText: '',
  brand: '', brandText: '',
  category: '', categoryText: '',
  gender: '', genderText: ''
})

// Tính tổng số trang
const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / pageSize.value) || 1
})

// Cắt danh sách sản phẩm theo trang hiện tại
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
</style>