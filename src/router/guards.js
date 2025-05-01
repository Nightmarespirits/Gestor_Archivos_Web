import { useAuthStore } from '@/store/auth';
import { useUserPermissionsStore } from '@/store/userPermissions';
import { storeToRefs } from 'pinia';

/**
 * Crea un guard de autenticación y autorización para el router
 * @param {Object} router - Instancia de Vue Router
 * @param {Object} piniaInstance - Instancia de Pinia
 */
export function createAuthGuard(router, piniaInstance) {
  router.beforeEach(async (to, from, next) => {
    // Obtención reactiva del estado de autenticación y permisos
    const authStore = useAuthStore(piniaInstance);
    const permissionStore = useUserPermissionsStore(piniaInstance);
    const { isAuthenticated, user, isInitialized } = storeToRefs(authStore);

    // Si la autenticación no se ha inicializado, permitimos continuar
    if (!isInitialized.value) {
      return next();
    }

    // Si la ruta requiere autenticación y el usuario no está autenticado
    if (to.meta.requiresAuth && !isAuthenticated.value) {
      return next({ name: 'Login' });
    }

    // Si el usuario está autenticado y trata de acceder a la página de login
    if (to.name === 'Login' && isAuthenticated.value) {
      return next('/');
    }

    // Verificación de permisos y roles para rutas protegidas
    if (to.meta.requiresAuth && isAuthenticated.value) {
      // Cargar permisos si aún no están cargados
      if (!permissionStore.initialized) {
        await permissionStore.loadUserPermissions();
      }

      // --- NUEVA LÓGICA DE PERMISOS Y ROLES COMO LISTAS ---
      // Verificar permisos requeridos (si están definidos)
      if (Array.isArray(to.meta.requiredPermissions) && to.meta.requiredPermissions.length > 0) {
        const userPermissions = permissionStore.permissions || [];
        const hasSomePermission = to.meta.requiredPermissions.some(p => userPermissions.includes(p));
        if (!hasSomePermission) {
          return next({ name: 'Unauthorized' });
        }
      }
      // Verificar roles requeridos (si están definidos)
      if (Array.isArray(to.meta.requiredRoles) && to.meta.requiredRoles.length > 0) {
        const userRole = user.value?.role;
        // Soportar tanto string como objeto de rol
        let userRoleName = '';
        if (typeof userRole === 'string') {
          userRoleName = userRole;
        } else if (userRole && typeof userRole === 'object') {
          userRoleName = userRole.name || userRole.roleName || '';
        }
        userRoleName = userRoleName.toUpperCase().replace('ROLE_', '');
        // Permitir si el usuario tiene al menos uno de los roles requeridos
        const hasSomeRole = to.meta.requiredRoles.map(r => r.toUpperCase().replace('ROLE_', '')).includes(userRoleName);
        if (!userRoleName || !hasSomeRole) {
          return next({ name: 'Unauthorized' });
        }
      }
    }
    // En todos los demás casos, permitir la navegación
    return next();
  });
}
