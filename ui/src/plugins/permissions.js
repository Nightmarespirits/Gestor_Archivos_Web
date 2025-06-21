// src/plugins/permissions.js
import { useUserPermissionsStore } from '@/store/userPermissions';
import { useAuthStore } from '@/store/auth';

/**
 * Plugin para directivas relacionadas con permisos
 * Proporciona directivas Vue para controlar la visualización de elementos según permisos
 */
export default {
  install(app) {
    // Directiva v-permission - Muestra un elemento solo si el usuario tiene el permiso especificado
    // Uso: v-permission="'USER_CREATE'" o v-permission="['DOCUMENT_UPDATE', 'DOCUMENT_DELETE']"
    app.directive('permission', {
      beforeMount(el, binding) {
        const permStore = useUserPermissionsStore();
        
        if (!permStore.initialized) {
          // Ocultar mientras se cargan los permisos
          el.style.display = 'none';
          return;
        }

        // Se puede pasar un solo permiso como string o un array de permisos
        const requiredPermissions = Array.isArray(binding.value) ? binding.value : [binding.value];
        
        // Por defecto, necesitamos cualquiera de los permisos (OR)
        const hasRequiredPermission = permStore.hasAnyPermission(requiredPermissions);
        
        if (!hasRequiredPermission) {
          // Si no hay permisos, eliminar el elemento del DOM
          el.parentNode && el.parentNode.removeChild(el);
        }
      },
      
      updated(el, binding) {
        const permStore = useUserPermissionsStore();
        
        // Se puede pasar un solo permiso como string o un array de permisos
        const requiredPermissions = Array.isArray(binding.value) ? binding.value : [binding.value];
        
        // Por defecto, necesitamos cualquiera de los permisos (OR)
        const hasRequiredPermission = permStore.hasAnyPermission(requiredPermissions);
        
        if (!hasRequiredPermission) {
          // Si no hay permisos, ocultar el elemento
          el.style.display = 'none';
        } else {
          // Si tiene permisos, asegurarse de que sea visible
          el.style.display = '';
        }
      }
    });
    
    // Directiva v-role - Muestra un elemento solo si el usuario tiene el rol especificado
    // Uso: v-role="'ADMIN'" o v-role="['ADMIN', 'SUPERADMIN']"
    app.directive('role', {
      beforeMount(el, binding) {
        const authStore = useAuthStore();
        
        if (!authStore.isInitialized) {
          // Ocultar mientras se verifica la autenticación
          el.style.display = 'none';
          return;
        }
        
        const userRole = authStore.user?.role?.name?.toUpperCase();
        if (!userRole) {
          // Si no hay rol, eliminar el elemento del DOM
          el.parentNode && el.parentNode.removeChild(el);
          return;
        }
        
        // Se puede pasar un solo rol como string o un array de roles
        const requiredRoles = Array.isArray(binding.value) 
          ? binding.value.map(r => r.toUpperCase()) 
          : [binding.value.toUpperCase()];
        
        const hasRequiredRole = requiredRoles.includes(userRole) || 
                               (requiredRoles.includes('ADMIN') && userRole === 'ADMINISTRADOR') ||
                               (requiredRoles.includes('ADMINISTRADOR') && userRole === 'ADMIN');
        
        if (!hasRequiredRole) {
          // Si no tiene el rol requerido, eliminar el elemento del DOM
          el.parentNode && el.parentNode.removeChild(el);
        }
      },
      
      updated(el, binding) {
        const authStore = useAuthStore();
        
        const userRole = authStore.user?.role?.name?.toUpperCase();
        if (!userRole) {
          // Si no hay rol, ocultar el elemento
          el.style.display = 'none';
          return;
        }
        
        // Se puede pasar un solo rol como string o un array de roles
        const requiredRoles = Array.isArray(binding.value) 
          ? binding.value.map(r => r.toUpperCase()) 
          : [binding.value.toUpperCase()];
        
        const hasRequiredRole = requiredRoles.includes(userRole) || 
                               (requiredRoles.includes('ADMIN') && userRole === 'ADMINISTRADOR') ||
                               (requiredRoles.includes('ADMINISTRADOR') && userRole === 'ADMIN');
        
        if (!hasRequiredRole) {
          // Si no tiene el rol requerido, ocultar el elemento
          el.style.display = 'none';
        } else {
          // Si tiene el rol requerido, asegurarse de que sea visible
          el.style.display = '';
        }
      }
    });
  }
};
