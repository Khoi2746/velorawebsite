<template>
  <div v-if="visible" class="toast-overlay">
    <div class="toast-box">
      <canvas ref="canvasRef" width="200" height="200"></canvas>
      <p class="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'

const props = defineProps({
  visible: Boolean,
  message: String,
  type: String,
  loading: Boolean
})

const canvasRef = ref(null)
let animationId = null
let frame = 0
let phase = 'loading' 
let progress = 0      
let sparks = [] 

const draw = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  
  frame = 0
  phase = props.loading ? 'loading' : 'transition'
  progress = 0
  sparks = [] 

  const animate = () => {
    if (!props.visible) return

    ctx.clearRect(0, 0, 200, 200)

    if (phase === 'loading') {
      // ĐÃ CHUYỂN VÀO ĐÂY: Nhịp đập Tâm Vòng Tròn chỉ chạy khi đang loading
      let pulse = Math.sin(frame * 0.05) * 0.5 + 0.5 
      ctx.beginPath()
      ctx.arc(100, 100, 30, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(209, 170, 104, ${pulse * 0.08})` 
      ctx.fill()

      // Vẽ vòng tròn quỹ đạo
      ctx.beginPath()
      ctx.arc(100, 100, 40, 0, Math.PI * 2)
      ctx.strokeStyle = '#332d27'
      ctx.lineWidth = 4
      ctx.stroke()

      let angle = frame * 0.08 

      ctx.save()
      ctx.translate(100, 100)
      ctx.rotate(angle)
      
      let grad = ctx.createConicGradient(0, 0, 0)
      grad.addColorStop(0, 'rgba(209, 170, 104, 0)')
      grad.addColorStop(0.6, 'rgba(209, 170, 104, 0)')
      grad.addColorStop(1, '#d1aa68')
      
      ctx.beginPath()
      ctx.arc(0, 0, 40, 0, Math.PI * 2)
      ctx.strokeStyle = grad
      ctx.lineWidth = 4
      ctx.stroke()
      ctx.restore()

      let x = 100 + Math.cos(angle) * 40
      let y = 100 + Math.sin(angle) * 40

      if (Math.random() > 0.4) {
        sparks.push({
          x: x, 
          y: y,
          vx: (Math.random() - 0.5) * 1.5, 
          vy: (Math.random() - 0.5) * 1.5,
          alpha: 1, 
          size: Math.random() * 1.5 + 0.5 
        })
      }

      ctx.shadowBlur = 15
      ctx.shadowColor = '#d1aa68'
      ctx.fillStyle = '#fff'
      ctx.beginPath()
      ctx.arc(x, y, 6, 0, Math.PI * 2)
      ctx.fill()
      ctx.shadowBlur = 0
      
      frame++
    } 
    else if (phase === 'transition') {
      progress += 0.05 
      if (progress >= 1) {
        progress = 1
        phase = 'done' 
      }

      ctx.lineWidth = 8
      ctx.lineCap = 'round'
      ctx.lineJoin = 'round'
      ctx.strokeStyle = '#d1aa68'
      ctx.shadowColor = 'rgba(209, 170, 104, 0.4)'
      ctx.shadowBlur = 10
      ctx.beginPath()
      
      let particleX, particleY

      if (props.type === 'success') {
        const Ax = 65, Ay = 105, Bx = 95, By = 135, Cx = 140, Cy = 70
        if (progress < 0.4) {
          let p = progress / 0.4
          particleX = Ax + (Bx - Ax) * p
          particleY = Ay + (By - Ay) * p
          ctx.moveTo(Ax, Ay)
          ctx.lineTo(particleX, particleY)
        } else {
          let p = (progress - 0.4) / 0.6
          particleX = Bx + (Cx - Bx) * p
          particleY = By + (Cy - By) * p
          ctx.moveTo(Ax, Ay)
          ctx.lineTo(Bx, By)
          ctx.lineTo(particleX, particleY)
        }
      } else {
        const Ax = 70, Ay = 70, Bx = 130, By = 130
        const Cx = 130, Cy = 70, Dx = 70, Dy = 130
        if (progress < 0.5) {
          let p = progress / 0.5
          particleX = Ax + (Bx - Ax) * p
          particleY = Ay + (By - Ay) * p
          ctx.moveTo(Ax, Ay)
          ctx.lineTo(particleX, particleY)
        } else {
          let p = (progress - 0.5) / 0.5
          particleX = Cx + (Dx - Cx) * p
          particleY = Cy + (Dy - Cy) * p
          ctx.moveTo(Ax, Ay)
          ctx.lineTo(Bx, By) 
          ctx.moveTo(Cx, Cy)
          ctx.lineTo(particleX, particleY) 
        }
      }
      ctx.stroke()
      ctx.shadowBlur = 0

      if (Math.random() > 0.2) {
        sparks.push({
          x: particleX, y: particleY,
          vx: (Math.random() - 0.5) * 2, vy: (Math.random() - 0.5) * 2,
          alpha: 1, size: Math.random() * 2 + 0.5
        })
      }

      ctx.shadowBlur = 15
      ctx.shadowColor = '#d1aa68'
      ctx.fillStyle = '#fff'
      ctx.beginPath()
      ctx.arc(particleX, particleY, 5, 0, Math.PI * 2)
      ctx.fill()
      ctx.shadowBlur = 0
    } 
    else if (phase === 'done') {
      drawFinalIcon(ctx)
    }

    for (let i = sparks.length - 1; i >= 0; i--) {
      let s = sparks[i]
      s.x += s.vx
      s.y += s.vy
      s.alpha -= 0.02 
      
      if (s.alpha <= 0) {
        sparks.splice(i, 1) 
      } else {
        ctx.globalAlpha = s.alpha
        ctx.fillStyle = '#d1aa68'
        ctx.beginPath()
        ctx.arc(s.x, s.y, s.size, 0, Math.PI * 2)
        ctx.fill()
        ctx.globalAlpha = 1 
      }
    }

    animationId = requestAnimationFrame(animate)
  }
  
  animate()
}

const drawFinalIcon = (ctx) => {
  ctx.lineWidth = 8
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  ctx.strokeStyle = '#d1aa68'
  ctx.shadowColor = 'rgba(209, 170, 104, 0.4)'
  ctx.shadowBlur = 10
  
  ctx.beginPath()
  if (props.type === 'success') {
    ctx.moveTo(65, 105)
    ctx.lineTo(95, 135)
    ctx.lineTo(140, 70)
  } else {
    ctx.moveTo(70, 70)
    ctx.lineTo(130, 130)
    ctx.moveTo(130, 70)
    ctx.lineTo(70, 130)
  }
  ctx.stroke()
  ctx.shadowBlur = 0
}

watch(() => props.loading, (newVal) => {
  if (!newVal && props.visible) {
    phase = 'transition' 
    progress = 0
  }
})

watch(() => props.visible, async (newVal) => {
  if (newVal) {
    await nextTick()
    draw()
  } else {
    cancelAnimationFrame(animationId)
  }
})

onBeforeUnmount(() => cancelAnimationFrame(animationId))
</script>>

<style scoped>
/* Đã xóa backdrop-filter: blur() để nền không bị ám sương mù trắng */
.toast-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.8);
}

.toast-box {
  background: #1a1714;
  border: 1px solid #332d27;
  padding: 30px 40px;
  border-radius: 20px;
  text-align: center;
  width: 320px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.6);
}

canvas {
  display: block;
  margin: 0 auto;
}

.message {
  font-family: 'Times New Roman', serif;
  font-size: 17px;
  color: #d1aa68;
  margin-top: -10px;
  letter-spacing: 1px;
}
</style>