<template>
  <TopNav />
  <main>
    <RouterView v-slot="{ Component }">
      <Transition name="page" mode="out-in">
        <component :is="Component" :key="$route.path" />
      </Transition>
    </RouterView>
  </main>
  <AppFooter />
</template>

<script setup>
import TopNav from './components/TopNav.vue'
import AppFooter from './components/AppFooter.vue'
</script>
