<template>
  <div class="shop-page">
    <Header />

    <main class="shop-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">VELORA</h1>
          <div class="title-divider">
            <span class="diamond"></span>
          </div>
          <p class="page-subtitle">Khám phá những tuyệt tác thời gian cơ khí chính xác có sẵn tại cửa hàng</p>
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

        <div class="product-grid" v-if="filteredProducts.length > 0">
          <div class="product-card" v-for="product in filteredProducts" :key="product.maSanPham">

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

        <div v-else class="empty-state">
          <p>Không tìm thấy sản phẩm nào phù hợp hoặc đang tải dữ liệu...</p>
        </div>
      </div>
      <Info />
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue' // ĐÃ THÊM: watch để theo dõi URL thay đổi
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

// ĐÃ BỔ SUNG: Thêm trường search vào bộ lọc
const filters = ref({
  search: '', 
  price: '', priceText: '',
  brand: '', brandText: '',
  category: '', categoryText: '',
  gender: '', genderText: ''
})

const toggleDropdown = (dropdownName) => {
  if (activeDropdown.value === dropdownName) {
    activeDropdown.value = null
  } else {
    activeDropdown.value = dropdownName
  }
}

// BỘ LỌC TỔNG HỢP: Xử lý cả Hãng, Giá, Giới tính và TỪ KHÓA TÌM KIẾM
const applyFilters = () => {
  let result = [...products.value]

  // 1. Lọc theo TỪ KHÓA trên thanh tìm kiếm
  if (filters.value.search) {
    const keyword = filters.value.search.toLowerCase()
    result = result.filter(p => 
      // Tìm trong Tên sản phẩm HOẶC Tên thương hiệu
      p.tenSanPham.toLowerCase().includes(keyword) || 
      (p.thuongHieu && p.thuongHieu.tenThuongHieu.toLowerCase().includes(keyword))
    )
  }

  // 2. Lọc theo Thương hiệu
  if (filters.value.brand) {
    result = result.filter(p => p.maThuongHieu === parseInt(filters.value.brand) || (p.thuongHieu && p.thuongHieu.maThuongHieu === parseInt(filters.value.brand)))
  }
  // 3. Lọc theo Giá
  if (filters.value.price) {
    if (filters.value.price === 'under-100m') result = result.filter(p => p.giaBan < 100000000)
    else if (filters.value.price === '100m-500m') result = result.filter(p => p.giaBan >= 100000000 && p.giaBan <= 500000000)
    else if (filters.value.price === 'over-500m') result = result.filter(p => p.giaBan > 500000000)
  }
  // 4. Lọc theo Loại
  if (filters.value.category) {
    result = result.filter(p => p.loaiSanPham && p.loaiSanPham.maLoai === parseInt(filters.value.category))
  }
  // 5. Lọc theo Giới tính
if (filters.value.gender) {
  result = result.filter(p => p.gioiTinh && p.gioiTinh.toLowerCase() === filters.value.gender.toLowerCase())
}

  filteredProducts.value = result
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

    // Sau khi tải xong hết Data, mồi URL một lần để kích hoạt bộ lọc
    syncFiltersFromUrl()

  } catch (error) {
    console.error('Lỗi kết nối API hệ thống:', error)
  }
}

// BÍ KÍP: Hàm đồng bộ hóa bộ lọc với URL (kết hợp cả Search và Brand)
const syncFiltersFromUrl = () => {
  // Lấy keyword tìm kiếm
  filters.value.search = route.query.search || ''

  // Lấy ID thương hiệu
  if (route.query.brand) {
    const brandIdFromUrl = parseInt(route.query.brand)
    const targetBrand = brands.value.find(b => b.maThuongHieu === brandIdFromUrl)
    if (targetBrand) {
      filters.value.brand = brandIdFromUrl
      filters.value.brandText = targetBrand.tenThuongHieu
    }
  } else {
    // Nếu URL không có brand, reset lại filter brand
    filters.value.brand = ''
    filters.value.brandText = ''
  }

  // Chạy lệnh lọc sản phẩm
  applyFilters()
}

// RA-ĐA QUÉT URL: Nếu khách đang ở trang Sản Phẩm mà gõ tìm kiếm tiếp, hàm này sẽ tự nhảy
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
</style>