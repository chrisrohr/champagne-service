import './index.css'
import '@fortawesome/fontawesome-free/css/all.min.css'

import {createApp, markRaw} from 'vue'
import {createPinia} from 'pinia'

import App from './App.vue'
import router from './router'
import {initAxios} from "@/plugins/axios";

const app = createApp(App)

const pinia = createPinia();
pinia.use(({ store }) => { store.router = markRaw(router) });

app.use(pinia)
app.use(router)

initAxios(router);

app.mount('#app')
