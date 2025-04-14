// src/router/guards.js
import { useAuthStore } from '@/store/auth';
import { storeToRefs } from 'pinia'; // Needed to properly get reactive state outside components

export function createAuthGuard(router, piniaInstance) {
  router.beforeEach(async (to, from, next) => { // Make the guard async
    // It's crucial to get a fresh instance of the store *inside* the guard
    // or pass the pinia instance and use it
    const authStore = useAuthStore(piniaInstance);
    // Use storeToRefs for reactivity AND get isInitialized
    const { isAuthenticated, user, isInitialized } = storeToRefs(authStore);

    console.log('[AuthGuard] Running for route:', to.path);
    console.log('[AuthGuard] Store Initialized:', isInitialized.value);
    console.log('[AuthGuard] Is Authenticated:', isAuthenticated.value);
    console.log('[AuthGuard] User Data:', JSON.stringify(user.value)); // Log user data

    const requiresAuth = to.meta.requiresAuth;
    const requiredRole = to.meta.requiredRole;

    // Si la autenticación no se ha inicializado, permitimos continuar
    // App.vue mostrará el spinner de carga y luego redirigirá si es necesario
    if (!isInitialized.value) {
      console.log('[AuthGuard] Auth store not initialized yet. App.vue will handle display.');
      return next();
    }

    // Si la ruta requiere autenticación y el usuario no está autenticado
    if (requiresAuth && !isAuthenticated.value) {
      console.log('[AuthGuard] Route requires auth but user is not authenticated.');
      return next({ name: 'Login', query: { redirect: to.fullPath } });
    }

    // Si el usuario está autenticado y trata de acceder a la página de login
    if (to.name === 'Login' && isAuthenticated.value) {
      console.log('[AuthGuard] Authenticated user trying to access login page.');
      return next('/');
    }

    // Si la ruta requiere un rol específico
    if (requiresAuth && requiredRole && isAuthenticated.value) {
      // Obtener el rol del usuario (podría estar en diferentes formatos)
      const userRoleObj = user.value?.role;
      console.log('[AuthGuard] User role object:', userRoleObj);
      
      // Extraer el nombre del rol, considerando diferentes estructuras posibles
      let userRole = '';
      if (typeof userRoleObj === 'string') {
        userRole = userRoleObj;
      } else if (userRoleObj && typeof userRoleObj === 'object') {
        userRole = userRoleObj.name || userRoleObj.roleName || '';
      }
      
      console.log('[AuthGuard] Required Role:', requiredRole, 'User Role extracted:', userRole);
      
      // Normalizar los roles para comparación (convertir a mayúsculas y eliminar prefijos comunes)
      const normalizedUserRole = userRole.toUpperCase().replace('ROLE_', '');
      const normalizedRequiredRole = requiredRole.toUpperCase().replace('ROLE_', '');
      
      console.log('[AuthGuard] Normalized comparison:', normalizedUserRole, 'vs', normalizedRequiredRole);
      
      // Verificar si el rol del usuario coincide con alguno de los roles aceptados
      const isAuthorized = normalizedUserRole === normalizedRequiredRole || 
                          (normalizedRequiredRole === 'ADMIN' && normalizedUserRole === 'ADMINISTRADOR') ||
                          (normalizedRequiredRole === 'ADMINISTRADOR' && normalizedUserRole === 'ADMIN');
      
      if (!userRole || !isAuthorized) {
        console.log('[AuthGuard] Role mismatch or missing, redirecting to Unauthorized.');
        return next({ name: 'Unauthorized' });
      }
    }

    // En todos los demás casos, permitir la navegación
    console.log('[AuthGuard] Navigation allowed.');
    return next();
  });
}
