import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useActivityLogsStore } from './activityLogs';
import { api } from '@/services/api'; // Importar el servicio API centralizado

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('authToken') || null);
  const user = ref(JSON.parse(localStorage.getItem('authUser') || '{}'));
  const isInitialized = ref(false); 
  const router = useRouter();
  const activityLogsStore = useActivityLogsStore();

  const isAuthenticated = computed(() => !!token.value);

  function setAuthData(newToken, newUser) {
    token.value = newToken;
    user.value = newUser;
    localStorage.setItem('authToken', newToken);
    localStorage.setItem('authUser', JSON.stringify(newUser));
  }

  function clearAuthData() {
    token.value = null;
    user.value = {};
    localStorage.removeItem('authToken');
    localStorage.removeItem('authUser');
  }

  async function login(credentials) {
    try {
      // Usar el servicio API para autenticar al usuario
      const userData = await api.post('/auth/login', credentials, {
        debug: true // Activar logs para depuración
      });
      
      // Extraer token y datos del usuario
      const { token: newToken, ...userData_ } = userData;
      router.push('/');
      // Guardar datos de autenticación
      setAuthData(newToken, userData_);
      
      // Registrar actividad de inicio de sesión
      try {
        await activityLogsStore.logActivity('LOGIN', 'Inicio de sesión exitoso');
      } catch (logError) {
        console.error('Error al registrar actividad de inicio de sesión:', logError);
      }
      
      return userData_;
    } catch (error) {
      console.error('Error de autenticación:', error);
      throw error;
    }
  }

  function logout() {
    // Registrar actividad de cierre de sesión antes de eliminar el token
    if (isAuthenticated.value) {
      try {
        activityLogsStore.logActivity('LOGOUT', 'Cierre de sesión manual');
      } catch (error) {
        console.error('Error al registrar actividad de cierre de sesión:', error);
      }
    }
    
    // Limpiar datos de autenticación
    clearAuthData();
    
    // Redirigir a la página de login
    router.push('/login');
  }

  // Función para verificar el estado de autenticación al cargar la app
  async function checkAuth() {
    try {
      isInitialized.value = false; // Marcar como no inicializado mientras verificamos
      const storedToken = localStorage.getItem('authToken');
      
      if (!storedToken) {
        clearAuthData();
        return false;
      } 
      
      // Obtener y analizar el usuario almacenado
      const storedUser = JSON.parse(localStorage.getItem('authUser') || '{}');
      
      // Normalizar la estructura del rol
      if (storedUser.role) {
        if (typeof storedUser.role === 'string') {
          storedUser.role = { name: storedUser.role };
        } else if (typeof storedUser.role === 'object' && !storedUser.role.name && storedUser.role.roleName) {
          storedUser.role.name = storedUser.role.roleName;
        }
      }
      
      // Validar el token con el servidor
      try {
        // Comentado: implementar cuando el endpoint exista en el backend
        // await api.get('/auth/validate-token');
        
        // Por ahora asumimos que el token es válido si existe
        token.value = storedToken;
        user.value = storedUser;
        return true;
      } catch (error) {
        console.error('Token inválido:', error);
        clearAuthData();
        return false;
      }
    } catch (error) {
      console.error('Error al verificar autenticación:', error);
      clearAuthData();
      return false;
    } finally {
      // Siempre marcamos como inicializado, independientemente del resultado
      isInitialized.value = true;
    }
  }

  return { 
    token, 
    user, 
    isAuthenticated,
    isInitialized, 
    login, 
    logout, 
    checkAuth 
  };
});
