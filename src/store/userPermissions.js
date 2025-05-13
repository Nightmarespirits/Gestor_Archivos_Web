import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useAuthStore } from './auth';
import { api } from '@/services/api'; // Importar el servicio API centralizado

/**
 * Store para gestionar los permisos del usuario actual.
 * Este store se encarga de:
 * 1. Cargar los permisos del usuario al iniciar sesión
 * 2. Proporcionar métodos para verificar si un usuario tiene un permiso específico
 * 3. Almacenar y actualizar los permisos cuando cambia el rol del usuario
 */
export const useUserPermissionsStore = defineStore('userPermissions', () => {
  // Estado
  const permissions = ref([]);
  const roleId = ref(null);
  const roleName = ref(null);
  const initialized = ref(false);
  const loading = ref(false);
  const error = ref(null);
  
  // Store de autenticación
  const authStore = useAuthStore();
  
  // Getters
  
  /**
   * Obtiene los nombres de los permisos del usuario actual
   */
  const permissionNames = computed(() => {
    return permissions.value.map(p => p.name);
  });
  
  /**
   * Comprueba si el usuario actual tiene un permiso específico
   * @param {string} permissionName - Nombre del permiso a verificar
   * @returns {boolean} - true si el usuario tiene el permiso, false en caso contrario
   */
  const hasPermission = (permissionName) => {
    // Si no se ha inicializado, asumimos que no tiene permiso
    if (!initialized.value) return false;
    
    // Convertir nombre de permiso a formato estándar
    const normalizedPermName = permissionName.toUpperCase();
    
    // Comprobar si el usuario tiene el permiso específico
    return permissionNames.value.some(p => p === normalizedPermName);
  };
  
  /**
   * Comprueba si el usuario tiene todos los permisos especificados
   * @param {Array<string>} requiredPermissions - Array de nombres de permisos a verificar
   * @returns {boolean} - true si el usuario tiene todos los permisos, false en caso contrario
   */
  const hasAllPermissions = (requiredPermissions) => {
    // Si el array está vacío, devolver true
    if (!requiredPermissions || !requiredPermissions.length) return true;
    
    // Comprobar todos los permisos
    return requiredPermissions.every(permName => hasPermission(permName));
  };
  
  /**
   * Comprueba si el usuario tiene al menos uno de los permisos especificados
   * @param {Array<string>} requiredPermissions - Array de nombres de permisos a verificar
   * @returns {boolean} - true si el usuario tiene al menos uno de los permisos, false en caso contrario
   */
  const hasAnyPermission = (requiredPermissions) => {
    // Si el array está vacío, devolver true
    if (!requiredPermissions || !requiredPermissions.length) return true;
    
    // Comprobar si tiene al menos un permiso
    return requiredPermissions.some(permName => hasPermission(permName));
  };
  
  /**
   * Verifica si el usuario es administrador basado en el rol
   */
  const isAdmin = computed(() => {
    const role = authStore.user?.role;
    if (!role) return false;
    
    const roleName = typeof role === 'string' ? role : role.name;
    return roleName === 'ADMIN' || roleName === 'ROLE_ADMIN';
  });
  
  // Acciones
  
  /**
   * Carga los permisos del usuario actual según su rol
   * @returns {Promise<Array>} - Array de permisos
   */
  async function loadUserPermissions() {
    try {
      loading.value = true;
      const user = authStore.user;
      
      if (!user || !user.role) {
        error.value = 'No hay información de usuario o rol';
        return [];
      }
      
      // Obtener información del rol
      let userRole = user.role;
      
      // Normalizar estructura del rol
      let roleToUse;
      if (typeof userRole === 'string') {
        roleName.value = userRole;
        // Buscar el ID del rol por su nombre
        const roleFetched = await getRoleByName(userRole);
        roleToUse = roleFetched.id;
      } else if (userRole && typeof userRole === 'object') {
        roleName.value = userRole.name || userRole.roleName;
        roleToUse = userRole.id;
      }
      
      if (!roleToUse) {
        error.value = 'No se pudo determinar el ID del rol';
        return [];
      }
      
      roleId.value = roleToUse;
      
      // Obtener permisos del rol directamente usando el ID
      const roleWithPermissions = await api.get(`/roles/${roleToUse}`);
      
      // Guardar permisos y marcar como inicializado
      permissions.value = roleWithPermissions.permissions || [];
      initialized.value = true;
      
      return permissions.value;
    } catch (err) {
      console.error('Error al cargar permisos del usuario:', err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * Obtiene un rol por su nombre
   * @param {string} name - Nombre del rol
   * @returns {Promise<Object>} - Datos del rol
   */
  async function getRoleByName(name) {
    try {
      const roles = await api.get('/roles', {
        params: { name: name }
      });
      
      // Buscar el rol por nombre
      const role = Array.isArray(roles) ? 
        roles.find(r => r.name === name) : null;
      
      if (!role) {
        throw new Error(`Rol "${name}" no encontrado`);
      }
      
      roleId.value = role.id;
      return role;
    } catch (err) {
      console.error(`Error al obtener rol por nombre (${name}):`, err);
      throw err;
    }
  }
  
  /**
   * Reinicia el store
   */
  function reset() {
    permissions.value = [];
    roleId.value = null;
    roleName.value = null;
    initialized.value = false;
    loading.value = false;
    error.value = null;
  }
  
  return {
    // Estado
    permissions,
    roleId,
    roleName,
    initialized,
    loading,
    error,
    
    // Getters
    permissionNames,
    isAdmin,
    
    // Métodos
    hasPermission,
    hasAllPermissions,
    hasAnyPermission,
    
    // Acciones
    loadUserPermissions,
    reset
  };
});
