/**
 * Directiva personalizada para controlar acceso a elementos de la UI según permisos
 * Uso:
 * - v-permission="'FILE_DOWNLOAD'" (requiere un permiso específico)
 * - v-permission="['DOCUMENT_CREATE', 'DOCUMENT_UPDATE']" (requiere al menos uno de los permisos listados)
 * - v-permission.all="['DOCUMENT_DELETE', 'ROLE_MANAGE']" (requiere todos los permisos listados)
 * - v-permission.role="'ADMIN'" (requiere un rol específico)
 * - v-permission.role="['ADMIN', 'SUPERADMIN']" (requiere uno de los roles listados)
 */

import { useUserPermissionsStore } from '@/store/userPermissions';
import { useAuthStore } from '@/store/auth';

export const vPermission = {
  beforeMount(el, binding, vnode) {
    const permissionStore = useUserPermissionsStore();
    const authStore = useAuthStore();
    
    const modifiers = binding.modifiers;
    const value = binding.value;
    
    if (!value) {
      console.warn('v-permission utilizada sin valor. El elemento permanecerá visible.');
      return;
    }
    
    // Ocultar elemento inicialmente hasta que se verifiquen los permisos
    el._originalDisplay = el.style.display === 'none' ? 'none' : '';
    el.style.display = 'none';
    
    // Función para evaluar permisos una vez que se han cargado
    const evaluatePermissions = () => {
      let hasAccess = false;
      
      if (modifiers.role) {
        // Verificar rol(es) específico(s)
        const userRole = authStore.user?.role?.name?.toUpperCase() || '';
        
        // Soportar tanto un solo rol como un array de roles
        const requiredRoles = Array.isArray(value) 
          ? value.map(r => String(r).toUpperCase())
          : [String(value).toUpperCase()];
        
        // Comprobar si el usuario tiene alguno de los roles requeridos
        hasAccess = requiredRoles.some(role => {
          return userRole === role || 
                 (role === 'ADMIN' && userRole === 'ADMINISTRADOR') || 
                 (role === 'ADMINISTRADOR' && userRole === 'ADMIN');
        });
      } else if (modifiers.all) {
        // Verificar si tiene TODOS los permisos requeridos (AND)
        const permList = Array.isArray(value) ? value : [value];
        hasAccess = permissionStore.hasAllPermissions(permList);
      } else {
        // Por defecto, verificar si tiene AL MENOS UNO de los permisos (OR)
        const permList = Array.isArray(value) ? value : [value];
        hasAccess = permissionStore.hasAnyPermission(permList);
      }
      
      // Mostrar/ocultar el elemento según los permisos
      if (hasAccess) {
        el.style.display = el._originalDisplay;
      } else {
        // Mantener el elemento oculto
        el.style.display = 'none';
      }
      
      return hasAccess;
    };
    
    // Cargar permisos si es necesario y luego evaluar
    if (!permissionStore.initialized) {
      permissionStore.loadUserPermissions().then(() => {
        evaluatePermissions();
      }).catch(error => {
        console.error('Error al cargar permisos para directiva v-permission:', error);
        el.style.display = 'none'; // En caso de error, ocultamos por seguridad
      });
    } else {
      // Permisos ya están cargados, evaluamos directamente
      evaluatePermissions();
    }
  },
  
  // Actualizar la directiva cuando cambian los permisos o el valor de binding
  updated(el, binding, vnode) {
    // Solo reevaluamos si el valor o los modificadores cambiaron
    if (binding.oldValue !== binding.value || 
        JSON.stringify(binding.oldModifiers) !== JSON.stringify(binding.modifiers)) {
      this.beforeMount(el, binding, vnode);
    }
  },
  
  // Limpiar cuando el elemento se desmonta
  unmounted(el) {
    // Eliminar propiedades personalizadas
    delete el._originalDisplay;
  }
};

export default {
  install(app) {
    app.directive('permission', vPermission);
  }
};
