<template>
  <div v-if="!authStore.isInitialized" class="loading-container">
    <!-- Loading indicator while checking authentication -->
    <div class="loading-spinner">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <div class="mt-3">Cargando...</div>
    </div>
  </div>
  <router-view v-else />
</template>

<script setup>
import { onBeforeMount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { useUserPermissionsStore } from '@/store/userPermissions';

const authStore = useAuthStore();
const permissionStore = useUserPermissionsStore();
const route = useRoute();
const router = useRouter();

// Verificar autenticación antes de montar el componente
onBeforeMount(async () => {
  try {
    // Verificar token en localStorage
    await authStore.checkAuth();
    
    // Si el usuario está autenticado, cargar sus permisos
    if (authStore.isAuthenticated) {
      await permissionStore.loadUserPermissions();
    }
    
    // Si el usuario está autenticado y la ruta actual es login, 
    // redirigir a la página principal
    if (authStore.isAuthenticated && route.path === '/login') {
      router.push('/');
    } 
    // Si el usuario NO está autenticado y la ruta requiere autenticación,
    // redirigir a login
    else if (!authStore.isAuthenticated && route.meta.requiresAuth) {
      router.push('/login');
    }
  } catch (error) {
    console.error('Error durante la inicialización:', error);
    // En caso de error, asegurarse de que el usuario vaya a login
    router.push('/login');
  }
});
</script>

<style scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
