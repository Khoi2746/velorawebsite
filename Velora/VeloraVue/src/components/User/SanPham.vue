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
                {{ product.giaBan > 400000000 ? 'Giá chờ hàng' : formatPrice(product.giaBan) }}
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
    result = result.filter(p => p.moTaChiTiet && p.moTaChiTiet.toLowerCase().includes(filters.value.gender.toLowerCase()))
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
.shop-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fdfdfd;
}

.shop-content {
  flex: 1;
  padding: 60px 20px 120px;
}

.container {
  max-width: 1300px;
  margin: 0 auto;
}

.title-wrapper {
  text-align: center;
  margin-bottom: 50px;
}

.page-title {
  color: #24201D;
  font-size: 32px;
  font-weight: 300;
  letter-spacing: 6px;
  margin-bottom: 12px;
  text-transform: uppercase;
}

.page-subtitle {
  font-size: 13px;
  color: #888888;
  margin-top: 15px;
  letter-spacing: 0.5px;
}

.title-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.title-divider::before,
.title-divider::after {
  content: '';
  display: block;
  width: 80px;
  height: 1px;
  background-color: #d1aa68;
}

.diamond {
  width: 6px;
  height: 6px;
  background-color: #d1aa68;
  transform: rotate(45deg);
}

.filter-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 50px;
  margin-bottom: 60px;
  position: relative;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.custom-dropdown {
  position: relative;
  cursor: pointer;
  min-width: 160px;
}

.dropdown-selected {
  font-size: 12px;
  font-weight: 600;
  color: #333333;
  text-transform: uppercase;
  letter-spacing: 2px;
  padding: 10px 25px 10px 0;
  position: relative;
  transition: all 0.3s ease;
}

.dropdown-selected::after {
  content: '↓';
  font-size: 10px;
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  color: #bfa175;
  transition: all 0.3s ease;
}

.custom-dropdown.active .dropdown-selected,
.custom-dropdown:hover .dropdown-selected {
  color: #d1aa68;
}

.custom-dropdown.active .dropdown-selected::after {
  transform: translateY(-50%) rotate(180deg);
  color: #d1aa68;
}

.dropdown-options {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  min-width: 240px;
  background-color: #24201D;
  border: 1px solid #3d3530;
  z-index: 100;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  padding: 8px 0;
  border-radius: 4px;
}

.option-item {
  padding: 12px 20px;
  color: #cccccc;
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  transition: all 0.25s ease;
  text-align: center;
}

.option-item:hover {
  background-color: #332d29;
  color: #d1aa68;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px 30px;
}

.product-card {
  background-color: #ffffff;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 30px 20px;
  border: 1px solid #f2e0c9;
  border-radius: 4px;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 40px rgba(36, 32, 29, 0.06);
  border-color: #d1aa68;
}

.tag-new {
  position: absolute;
  top: 15px;
  left: 15px;
  background-color: #24201D;
  color: #d1aa68;
  font-size: 9px;
  font-weight: 600;
  padding: 3px 12px;
  letter-spacing: 1.5px;
  border-radius: 2px;
  z-index: 2;
  border: 1px solid #d1aa68;
}

.product-image-wrapper {
  width: 100%;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
  overflow: hidden;
  display: block;
}

.product-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  transition: transform 0.6s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.product-card:hover .product-image {
  transform: scale(1.06);
}

.product-info {
  margin-bottom: 25px;
  width: 100%;
}

.product-name-link {
  text-decoration: none;
  display: block;
}

.product-name {
  font-size: 15px;
  color: #24201D;
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
  text-transform: capitalize;
  transition: color 0.3s ease;
}

.product-name-link:hover .product-name {
  color: #d1aa68;
}

.product-price {
  font-size: 14px;
  color: #d1aa68;
  font-weight: 600;
  letter-spacing: 1px;
}

.product-action {
  width: 100%;
}

.btn-contact {
  background-color: #24201D;
  color: #ffffff;
  border: 1px solid #24201D;
  font-size: 11px;
  font-weight: 600;
  padding: 12px 0;
  letter-spacing: 2px;
  cursor: pointer;
  border-radius: 0px;
  transition: all 0.3s ease;
  text-decoration: none;
  display: block;
  width: 100%;
  text-align: center;
}

.btn-contact:hover {
  background-color: #d1aa68;
  border-color: #d1aa68;
  color: #24201D;
  box-shadow: 0 4px 15px rgba(209, 170, 104, 0.25);
}

.empty-state {
  text-align: center;
  padding: 100px;
  color: #888888;
  font-size: 14px;
  letter-spacing: 1px;
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 30px;
  }
  .filter-bar {
    gap: 25px;
    flex-wrap: wrap;
  }
}

@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  .page-title {
    font-size: 26px;
  }
}

.luxe-fade-slide-enter-active,
.luxe-fade-slide-leave-active {
  transition: all 0.3s ease;
}

.luxe-fade-slide-enter-from,
.luxe-fade-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -10px);
}
</style>