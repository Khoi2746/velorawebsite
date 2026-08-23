<template>
  <div class="search-toggle-wrapper">
    <!-- Thanh tìm kiếm kiểu Premium Toggle -->
    <div class="premium-search" :class="{ open: isOpen }">
      <!-- Icon kính lúp để bấm mở -->
      <span class="premium-icon" @click="toggleSearch">
        <i class="fa-solid fa-magnifying-glass"></i>
      </span>
      
      <!-- Ô nhập từ khóa -->
      <input 
        ref="searchInputRef"
        type="text" 
        class="premium-input" 
        v-model="keyword"
        @keyup.enter="handleSearch"
        placeholder="Tìm kiếm tuyệt tác đồng hồ..." 
      />

      <!-- Nút đóng / xóa (X) -->
      <span class="close-btn" @click="closeSearch" v-if="isOpen">
        <i class="fa-solid fa-xmark"></i>
      </span>
    </div>

    <!-- Nhãn hướng dẫn phím Esc -->
    <div class="esc-hint" v-if="isOpen">
      <span class="esc-key">Esc</span> Nhấn để đóng
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isOpen = ref(false)
const keyword = ref('')
const searchInputRef = ref(null)

// Mở hoặc đóng thanh tìm kiếm
const toggleSearch = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => {
      searchInputRef.value?.focus()
    })
  } else {
    keyword.value = ''
  }
}

// Đóng thanh tìm kiếm
const closeSearch = () => {
  isOpen.value = false
  keyword.value = ''
}

// Xử lý khi nhấn Enter để tìm kiếm
const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/dong-ho-co-san', query: { search: keyword.value.trim() } })
    closeSearch()
  }
}

// Lắng nghe sự kiện bàn phím để bấm nút Esc thì tự thu gọn lại
const handleKeyDown = (e) => {
  if (e.key === 'Escape' && isOpen.value) {
    closeSearch()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
.search-toggle-wrapper {
  position: relative;
  display: inline-block;
}

/* Trạng thái thu gọn (Hình tròn nhỏ chứa icon kính lúp) */
.premium-search {
  position: relative;
  width: 46px;
  height: 46px;
  background: #ffffff;
  border: 2px solid #c5a880; /* Màu vàng đồng đặc trưng của Velora */
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  align-items: center;
  z-index: 100;
}

/* Trạng thái mở rộng (Thanh input dài) */
.premium-search.open {
  width: 360px;
  border-radius: 12px;
  padding: 0 10px;
  box-shadow: 0 10px 30px rgba(197, 168, 128, 0.25);
}

/* Icon kính lúp */
.premium-icon {
  min-width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #c5a880;
  font-size: 16px;
  flex-shrink: 0;
  transition: color 0.2s;
}

.premium-icon:hover {
  color: #1a1a1a;
}

/* Ô nhập liệu */
.premium-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  font-family: 'Times New Roman', Times, serif;
  background: transparent;
  color: #1a1a1a;
  padding: 0 8px;
  opacity: 0;
  transition: opacity 0.3s ease 0.2s;
}

.premium-search.open .premium-input {
  opacity: 1;
}

.premium-input::placeholder {
  color: #888;
  font-style: italic;
}

/* Nút X đóng */
.close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #777;
  font-size: 14px;
  border-radius: 50%;
  transition: all 0.2s;
  flex-shrink: 0;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #1a1a1a;
}

/* Nhãn hướng dẫn phím Esc phía dưới */
.esc-hint {
  position: absolute;
  top: 55px;
  right: 0;
  background: #1a1a1a;
  color: #fff;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 101;
  white-space: nowrap;
  animation: fadeInDown 0.3s ease;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.esc-key {
  background: #333;
  border: 1px solid #555;
  padding: 1px 5px;
  border-radius: 4px;
  font-family: monospace;
  color: #c5a880;
  font-weight: bold;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>