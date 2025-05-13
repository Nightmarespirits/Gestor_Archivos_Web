import { useAuthStore } from '@/store/auth';
import { useUserPermissionsStore } from '@/store/userPermissions';
import { storeToRefs } from 'pinia';

/**
 * Utility to get normalized role name from user object
 * @param {Object|string} userRole - Role from authStore.user.role
 * @returns {string} - Uppercase role name without 'ROLE_' prefix, or empty string
 */
function getNormalizedUserRoleName(userRole) {
  let roleName = '';
  if (typeof userRole === 'string') {
    roleName = userRole;
  } else if (userRole && typeof userRole === 'object') {
    // Adjust property names based on your actual user.role object structure
    roleName = userRole.name || userRole.roleName || '';
  }
  return roleName.toUpperCase().replace(/^ROLE_/, '');
}

/**
 * Crea un guard de autenticación y autorización para el router
 * @param {Object} router - Instancia de Vue Router
 * @param {Object} piniaInstance - Instancia de Pinia
 */
export function createAuthGuard(router, piniaInstance) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore(piniaInstance);
    const permissionStore = useUserPermissionsStore(piniaInstance);
    const { isAuthenticated, user, isInitialized } = storeToRefs(authStore);

    // Esperar a que el estado de autenticación se inicialice desde el localStorage/sesión
    // Esto evita redirecciones prematuras antes de saber si el usuario está logueado
    if (!isInitialized.value && authStore.isInitializing) {
       await new Promise(resolve => {
         const unsubscribe = authStore.$subscribe((mutation, state) => {
           if (state.isInitialized) {
             unsubscribe();
             resolve();
           }
         });
       });
    } else if (!isInitialized.value) {
      // Si después de la inicialización sigue sin estar inicializado (error?),
      // o si no se estaba inicializando, proceder con precaución.
      // Podrías redirigir a login o mostrar error si es necesario.
      // Por ahora, permitimos continuar si no requiere auth, sino redirigimos.
       if (to.meta.requiresAuth) {
         console.warn('Auth not initialized, redirecting to login for protected route.');
         return next({ name: 'Login', query: { redirect: to.fullPath } });
       } else {
         return next();
       }
    }

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiredPermissions = to.matched.flatMap(record => record.meta.requiredPermissions || []);
    const requiredRoles = to.matched.flatMap(record => record.meta.requiredRoles || []);

    // --- Redirección si no está autenticado ---
    if (requiresAuth && !isAuthenticated.value) {
      console.log(`[AuthGuard] Denied access to "${to.path}". User not authenticated. Redirecting to Login.`);
      return next({ name: 'Login', query: { redirect: to.fullPath } });
    }

    // --- Redirección si está autenticado y va a Login ---
    if (to.name === 'login' && isAuthenticated.value) {
      return next({ name: 'Dashboard' }); // O tu ruta principal post-login
    }

    // --- Verificación de Permisos y Roles (solo si está autenticado y la ruta requiere auth) ---
    if (requiresAuth && isAuthenticated.value) {
      // Cargar permisos si es necesario (solo una vez)
      if (!permissionStore.initialized) {
        try {
          await permissionStore.loadUserPermissions();
        } catch (error) {
          console.error('[AuthGuard] Error loading permissions:', error);
          // Decide qué hacer: mostrar error, redirigir a no autorizado, etc.
          return next({ name: 'Unauthorized', query: { error: 'permission_load_failed' } });
        }
      }

      // 1. Verificar Permisos Requeridos
      if (requiredPermissions.length > 0) {
        // Usamos hasAnyPermission: el usuario debe tener AL MENOS UNO de los permisos listados
        const hasRequiredPermission = permissionStore.hasAnyPermission(requiredPermissions);
        if (!hasRequiredPermission) {
           console.log(`[AuthGuard] Denied access to "${to.path}". Missing permissions. Required: ${requiredPermissions.join(', ')}`);
           return next({ name: 'Unauthorized' });
        }
      }

      // 2. Verificar Roles Requeridos
      if (requiredRoles.length > 0) {
        const userRoleName = getNormalizedUserRoleName(user.value?.role);
        const normalizedRequiredRoles = requiredRoles.map(r => r.toUpperCase().replace(/^ROLE_/, ''));

        // Permitir si el usuario tiene AL MENOS UNO de los roles requeridos
        const hasRequiredRole = normalizedRequiredRoles.includes(userRoleName);
        if (!userRoleName || !hasRequiredRole) {
          console.log(`[AuthGuard] Denied access to "${to.path}". Missing role. Required: ${requiredRoles.join(', ')}, User has: ${userRoleName}`);
          return next({ name: 'Unauthorized' });
        }
      }
    }

    // --- Si pasa todas las verificaciones ---
    return next();
  });
}
