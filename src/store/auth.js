import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

// Define la URL base de tu API. Ajusta si es necesario.
const API_BASE_URL = import.meta.env.VITE_BASE_URL; // Access the environment variable directly

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('authToken') || null);
  const user = ref(JSON.parse(localStorage.getItem('authUser') || '{}'));
  const isInitialized = ref(false); 
  const router = useRouter();

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
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      });

      if (!response.ok) {
        let errorMessage = `Error ${response.status}: ${response.statusText}`;
        try {
          // Intenta obtener un mensaje de error más específico si la respuesta es JSON
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json();
            // Busca un campo 'message' o similar, ajusta según tu API
            errorMessage = errorData.message || errorData.error || JSON.stringify(errorData);
          } else {
            // Si no es JSON, intenta leer como texto
            const textError = await response.text();
            if (textError) {
              errorMessage = textError;
            }
          }
        } catch (e) {
          // Si falla el parseo JSON o text(), usa el mensaje genérico
          console.error('Could not parse error response:', e);
        }
        throw new Error(errorMessage);
      }

      const data = await response.json();
      console.log('Login response data:', data); // Log para depuración
      
      // Asegurarse de que el rol esté correctamente estructurado
      let userRole = data.role;
      
      // Si el rol es un string, convertirlo a objeto para mantener consistencia
      if (typeof userRole === 'string') {
        userRole = { name: userRole };
      } else if (typeof userRole === 'object' && userRole !== null) {
        // Si ya es un objeto, asegurarse de que tenga la propiedad name
        if (!userRole.name && userRole.roleName) {
          userRole.name = userRole.roleName;
        }
      }
      
      const userData = { 
        id: data.id,
        username: data.username,
        email: data.email,
        fullName: data.fullName,
        role: userRole
      };
      
      console.log('User data to be stored:', userData); // Log para depuración
      setAuthData(data.token, userData);
      isInitialized.value = true; // Marcar como inicializado después de login exitoso
      // Redirige al dashboard o a la página principal después del login
      router.push('/'); 
    } catch (error) {
      console.error('Error detallado en store auth.js:', error); // Loguear error completo aquí
      clearAuthData(); // Limpia datos si el login falla
      isInitialized.value = true; // Marcar como inicializado incluso si falla
      // Podrías manejar el error mostrando un mensaje al usuario
      throw error; // Propaga el error para manejarlo en el componente
    }
  }

  function logout() {
    clearAuthData();
    // No resetear isInitialized porque ya sabemos el estado de la autenticación
    // Redirige a la página de login
    router.push('/login');
  }

  // Función para verificar el estado de autenticación al cargar la app
  async function checkAuth() {
    console.log('Auth store: checking authentication state...');
    try {
      const storedToken = localStorage.getItem('authToken');
      if (!storedToken) {
          clearAuthData();
          console.log('Auth store: no token found');
      } else {
          // Obtener y analizar el usuario almacenado
          const storedUser = JSON.parse(localStorage.getItem('authUser') || '{}');
          console.log('Auth store: stored user data:', storedUser);
          
          // Verificar si el usuario tiene la estructura correcta de rol
          if (storedUser.role) {
            if (typeof storedUser.role === 'string') {
              storedUser.role = { name: storedUser.role };
            } else if (typeof storedUser.role === 'object' && !storedUser.role.name && storedUser.role.roleName) {
              storedUser.role.name = storedUser.role.roleName;
            }
          }
          
          token.value = storedToken;
          user.value = storedUser;
          console.log('Auth store: token found, user restored with role:', user.value?.role);
          
          // Aquí podrías hacer una llamada a un endpoint para validar el token
          // Si el token no es válido, llama a clearAuthData()
      }
    } catch (error) {
      console.error('Error checking auth:', error);
      clearAuthData();
    } finally {
      // Siempre marcamos como inicializado, independientemente del resultado
      isInitialized.value = true;
      console.log('Auth store: initialization completed, authenticated:', isAuthenticated.value);
    }
  }

  return { 
    token, 
    user, 
    isAuthenticated,
    isInitialized, // Exponemos la nueva propiedad 
    login, 
    logout, 
    checkAuth 
  };
});
