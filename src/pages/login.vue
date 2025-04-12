<template>
  <v-container class="d-flex justify-center align-center" style="height: 100vh;">
    <v-card class="pa-5" width="600">
      <v-card-title class="text-h5 text-center">Iniciar Sesión</v-card-title>
      <v-card-text>
        <v-alert
          v-if="error"
          type="error"
          dense
          class="mb-4"
        >
          {{ error }}
        </v-alert>
        <v-form @submit.prevent="handleLogin">
          <v-text-field
            v-model="username"
            label="Nombre de Usuario"
            required
            prepend-inner-icon="mdi-account"
            :disabled="loading"
          ></v-text-field>
          <v-text-field
            v-model="password"
            label="Contraseña"
            type="password"
            required
            prepend-inner-icon="mdi-lock"
            :disabled="loading"
          ></v-text-field>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-btn 
          color="primary" 
          block 
          @click="handleLogin"
          :loading="loading"
          :disabled="loading"
        >
          Ingresar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/store/auth'; // Asegúrate que la ruta sea correcta

const authStore = useAuthStore();
const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref(null);

async function handleLogin() {
  loading.value = true;
  error.value = null; // Limpia errores anteriores
  try {
    await authStore.login({ 
      username: username.value, 
      password: password.value 
    });
    // La redirección se maneja dentro del store
  } catch (err) {
    console.error('Error completo capturado en componente login:', err); // Loguear el objeto completo
    error.value = err?.message || 'Ocurrió un error inesperado. Revisa la consola para más detalles.';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
/* Estilos opcionales para mejorar la apariencia */
</style>

<route lang="yaml">
meta:
  layout: blank
</route>
