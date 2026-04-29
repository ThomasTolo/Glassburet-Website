import { createRouter, createWebHashHistory } from 'vue-router'
import Hjem from '../pages/Hjem.vue'
import Quotes from '../pages/Quotes.vue'
import Liners from '../pages/Liners.vue'
import Galleri from '../pages/Galleri.vue'
import Aktiviteter from '../pages/Aktiviteter.vue'
import Poeng from '../pages/Poeng.vue'
import Spill from '../pages/Spill.vue'

export const router = createRouter({
  history: createWebHashHistory(),
  linkExactActiveClass: 'active',
  scrollBehavior: () => ({ top: 0, behavior: 'smooth' }),
  routes: [
    { path: '/',             component: Hjem        },
    { path: '/quotes',       component: Quotes      },
    { path: '/liners',       component: Liners      },
    { path: '/galleri',      component: Galleri     },
    { path: '/aktiviteter',  component: Aktiviteter },
    { path: '/poeng',        component: Poeng       },
    { path: '/spill',        component: Spill       },
  ],
})
