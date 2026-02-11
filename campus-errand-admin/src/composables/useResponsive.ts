import { ref, onMounted, onUnmounted } from 'vue'

export function useResponsive() {
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isDesktop = ref(true)

  const checkScreen = () => {
    const width = window.innerWidth
    isMobile.value = width < 768
    isTablet.value = width >= 768 && width < 1024
    isDesktop.value = width >= 1024
  }

  onMounted(() => {
    checkScreen()
    window.addEventListener('resize', checkScreen)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', checkScreen)
  })

  return {
    isMobile,
    isTablet,
    isDesktop
  }
}
