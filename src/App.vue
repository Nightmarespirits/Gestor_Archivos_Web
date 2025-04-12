<template>
  <router-view />
  <!-- Nota: El v-app se movió al default.vue layout -->
  <!-- Si tienes layouts que no usen v-app, podrías necesitar ajustar esto -->
</template>

<script setup>
import { onMounted } from 'vue';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();

onMounted(() => {
  // Verifica el estado de autenticación al cargar la aplicación
  authStore.checkAuth();
});
</script>
