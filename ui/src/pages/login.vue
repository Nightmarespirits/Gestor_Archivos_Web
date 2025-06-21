<template>
  <div class="login-bg-centered v-theme--light">
    <div class="login-card-ref">
      <img
        src="/IESTPFFAA_logo.png"
        alt="IESTPFFAA Logo"
        class="login-logo-ref"
      />
      <div class="login-title-ref">INICIAR SESIÓN</div>
      <div class="login-underline-ref"></div>
      <v-form @submit.prevent="handleLogin" class="login-form-ref">
        <v-text-field
          v-model="username"
          label="Nombre de Usuario"
          required
          prepend-inner-icon="mdi-account"
          :disabled="loading"
          variant="outlined"
          class="login-input-ref"
          color="primary"
          :rules="[v => !!v || 'El nombre de usuario es requerido']"
        ></v-text-field>
        <v-text-field
          v-model="password"
          :type="showPassword ? 'text' : 'password'"
          label="Contraseña"
          required
          prepend-inner-icon="mdi-lock"
          :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append-inner="showPassword = !showPassword"
          :disabled="loading"
          variant="outlined"
          class="login-input-ref"
          color="primary"
          :rules="[v => !!v || 'La contraseña es requerida']"
        ></v-text-field>
        <!-- Mensaje de error para credenciales inválidas -->
        <v-alert
          v-if="errorMessage"
          type="error"
          density="compact"
          class="mb-4"
          variant="tonal"
          closable
          @click:close="errorMessage = ''"
        >
          {{ errorMessage }}
        </v-alert>
        <v-btn 
          color="primary" 
          block 
          class="login-btn-ref"
          type="submit"
          :loading="loading"
          :disabled="loading"
          size="large"
        >
          Ingresar
        </v-btn>
      </v-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useAuthStore } from '@/store/auth';
import { useTheme } from 'vuetify';

const authStore = useAuthStore();
const username = ref('');
const password = ref('');
const loading = ref(false);
const showPassword = ref(false);
const showErrors = ref(false);
const errorMessage = ref(''); // Variable para el mensaje de error

// Forzar tema claro en login
const themeInstance = useTheme();
const previousTheme = ref(themeInstance.global.name.value);

onMounted(() => {
  previousTheme.value = themeInstance.global.name.value;
  themeInstance.global.name.value = 'light';
});

onBeforeUnmount(() => {
  themeInstance.global.name.value = previousTheme.value;
});

async function handleLogin() {
  showErrors.value = true;
  // Limpiar mensaje de error previo
  errorMessage.value = '';
  
  if (!username.value || !password.value) {
    loading.value = false;
    return;
  }
  loading.value = true;
  
  try {
    await authStore.login({ 
      username: username.value, 
      password: password.value 
    });

    // La redirección se maneja dentro del store
  } catch (error) {
    console.error('Error de login:', error);
    
    // Verificar si es un error de credenciales inválidas (401)
    if (error?.message?.includes('401') || 
        error?.message?.includes('Invalid credentials') || 
        error?.message?.toLowerCase().includes('credenciales')) {
      errorMessage.value = 'Credenciales inválidas. Por favor, verifique su usuario y contraseña.';
    } else {
      errorMessage.value = error?.message || 'Ocurrió un error inesperado al iniciar sesión.';
    }
    
    // Asegurarse de que el botón de login se habilite nuevamente
    setTimeout(() => {
      loading.value = false;
    }, 500);
  }
}
</script>

<style scoped>
.login-bg-centered {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #e3e6f3 0%, #f5f6fa 100%);
  overflow: hidden;
}
.login-bg-centered::before {
  content: "";
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 220px;
  z-index: 0;
  background: url('data:image/svg+xml;utf8,<svg width="100%25" height="100%25" viewBox="0 0 1440 320" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill="%236a82fb" fill-opacity="0.22" d="M0,160L60,154.7C120,149,240,139,360,154.7C480,171,600,213,720,197.3C840,181,960,107,1080,101.3C1200,96,1320,160,1380,192L1440,224L1440,320L1380,320C1320,320,1200,320,1080,320C960,320,840,320,720,320C600,320,480,320,360,320C240,320,120,320,60,320L0,320Z"></path></svg>');
  background-size: cover;
  animation: waveAnim 8s linear infinite alternate;
}
@keyframes waveAnim {
  0% { background-position-x: 0; }
  100% { background-position-x: 100px; }
}
.login-card-ref {
  background: #fff;
  border-radius: 28px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  padding: 0.7rem 2.2rem 2rem 2.2rem;
  max-width: 370px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.login-logo-ref {
  width: 90%;
  max-width: 320px;
  height: 130px;
  object-fit: cover;
  object-position: center top;
  display: block;
  margin-top: -32px;
  margin-bottom: 0rem;
}
.login-title-ref {
  font-size: 1.6rem;
  font-weight: 700;
  color: #2d217c;
  margin-top: 0.7rem;
  margin-bottom: 0.1rem;
  letter-spacing: 1px;
  text-align: center;
}
.login-underline-ref {
  width: 70px;
  height: 3px;
  background: #2d217c;
  border-radius: 2px;
  margin: 0.1rem auto 0.7rem auto;
}
.login-form-ref {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
}
.login-field-group {
  width: 100%;
  margin-bottom: 0.5rem;
}
.login-label-ref {
  font-size: 1.05rem;
  font-weight: 600;
  color: #2d217c;
  margin-bottom: 0.15rem;
  display: block;
  letter-spacing: 0.5px;
}
.required-asterisk {
  color: #e53935;
  font-size: 1.1em;
  font-weight: 700;
}
.login-input-ref {
  background: #fff !important;
  border-radius: 16px !important;
  box-shadow: 0 2px 12px 0 rgba(31, 38, 135, 0.08) !important;
  width: 100%;
  margin-bottom: 0.1rem;
  font-size: 1.08rem;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.login-input-ref input {
  background: transparent !important;
  border-radius: 16px !important;
  font-size: 1.08rem;
  padding: 0.7rem 0.9rem;
}
.login-input-ref .v-field__prepend-inner .v-icon {
  color: #2d217c !important;
  font-size: 1.3rem !important;
}
.login-input-ref .v-field__append-inner .v-icon {
  color: #2d217c !important;
  font-size: 1.3rem !important;
}
.login-input-ref.v-input--focused {
  box-shadow: 0 4px 16px 0 rgba(45, 33, 124, 0.13) !important;
  border-color: #2d217c !important;
}
.login-error-ref {
  color: #e53935;
  font-size: 0.97rem;
  margin-top: 0.1rem;
  margin-left: 0.2rem;
  width: 100%;
  text-align: left;
  font-weight: 500;
  letter-spacing: 0.2px;
}
.login-btn-ref {
  background: linear-gradient(90deg, #6a82fb 0%, #b0b6e6 100%);
  color: #fff !important;
  font-weight: 700;
  font-size: 1.1rem;
  border-radius: 18px;
  box-shadow: 0 4px 16px 0 rgba(45, 33, 124, 0.10);
  text-transform: none;
  letter-spacing: 1px;
  width: 100%;
  margin-top: 0.9rem;
  transition: background 0.2s, box-shadow 0.2s;
}
.login-btn-ref:hover {
  background: linear-gradient(90deg, #4f5bd5 0%, #6a82fb 100%);
  box-shadow: 0 6px 24px 0 rgba(45, 33, 124, 0.13);
}
.login-btn-ref:disabled {
  opacity: 0.7;
  background: linear-gradient(90deg, #b0b6e6 0%, #a1b6e6 100%);
  color: #fff !important;
}
</style>
