<template>
  <div class="login-container">
    <!-- Left side with logo -->
    <div class="logo-side">
      <v-img
        src="/IESTPFFAA_logo.png"
        alt="IESTPFFAA Logo"
        max-height="250"
        contain
        class="logo-image"
      ></v-img>
    </div>

    <!-- Right side with login form -->
    <div class="login-side">
      <v-card class="login-card" elevation="0">
        <v-card-title class="text-h4 text-center mb-3">INICIAR SESION</v-card-title>
        <div class="text-subtitle-1 text-center mb-6 text-grey">ARCHVOS IESTPFFAA</div>
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
              variant="outlined"
              class="mb-4"
            ></v-text-field>
            <v-text-field
              v-model="password"
              label="Contraseña"
              type="password"
              required
              prepend-inner-icon="mdi-lock"
              :disabled="loading"
              variant="outlined"
              class="mb-6"
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-btn 
            color="primary" 
            block 
            variant="outlined"
            @click="handleLogin"
            :loading="loading"
            :disabled="loading"
            size="large"
            class="text-subtitle-1"
          >
            Ingresar
          </v-btn>
        </v-card-actions>
      </v-card>
    </div>
  </div>
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
    // Llamar al método login del store
    const userData = await authStore.login({ 
      username: username.value, 
      password: password.value 
    });
    
    // La redirección se maneja dentro del store
  } catch (err) {
    error.value = err?.message || 'Ocurrió un error inesperado.';
    
    // Asegurarse de que el botón de login se habilite nuevamente
    setTimeout(() => {
      loading.value = false;
    }, 500);
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

.logo-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  padding: 2rem;
}

.logo-image {
  max-width: 80%;
}

.login-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background-color: transparent;
}
</style>
