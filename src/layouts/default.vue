<template>
  <v-app :theme="theme">
    <!-- Sidebar Component -->
    <app-sidebar 
      v-model:drawer="drawer"
      :rail="rail"
      :isMobile="isMobile"
    />

    <!-- Navbar Component -->
    <app-navbar
      :theme="theme"
      :isMobile="isMobile"
      :rail="rail"
      :drawer="drawer"
      @update:drawer="drawer = $event"
      @update:rail="rail = $event"
      @toggle-theme="toggleTheme"
    />

    <v-main>
      <v-container fluid>
        <router-view />
      </v-container>
    </v-main>

    <!-- Footer Component -->
    <app-footer />
  </v-app>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useTheme } from 'vuetify';
import AppSidebar from '@/components/layouts/AppSidebar.vue';
import AppNavbar from '@/components/layouts/AppNavbar.vue';
import AppFooter from '@/components/layouts/AppFooter.vue';

const themeInstance = useTheme();
const theme = ref(localStorage.getItem('appTheme') || 'light');

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
  themeInstance.global.name.value = theme.value;
  localStorage.setItem('appTheme', theme.value);
}

onMounted(() => {
  themeInstance.global.name.value = theme.value;
});

const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < 960);
const drawer = ref(!isMobile.value);
const rail = ref(false);

const handleResize = () => {
  const wasMobile = isMobile.value;
  windowWidth.value = window.innerWidth;
  const nowMobile = isMobile.value;

  if (wasMobile && !nowMobile) {
    drawer.value = true;
    rail.value = false;
  } else if (!wasMobile && nowMobile) {
    drawer.value = false;
  }
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.v-footer {
  justify-content: center;
}

.v-navigation-drawer {
  transition: width 0.2s ease-in-out;
}
</style>
