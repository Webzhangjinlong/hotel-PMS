import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './assets/css/variables.scss'
import './assets/css/element-theme.scss'

// 鍒涘缓搴旂敤瀹炰緥
const app = createApp(App)

// 娉ㄥ唽鎵€鏈?Element Plus 鍥炬爣
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 浣跨敤鎻掍欢
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// NProgress 閰嶇疆
NProgress.configure({ showSpinner: false })

// 鎸傝浇搴旂敤
app.mount('#app')

