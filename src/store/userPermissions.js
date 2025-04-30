import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useAuthStore } from './auth';

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
  
  // URL base de la API
  const API_BASE_URL = import.meta.env.VITE_BASE_URL;
  
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
    if (!requiredPermissions || requiredPermissions.length === 0) return true;
    return requiredPermissions.every(p => hasPermission(p));
  };
  
  /**
   * Comprueba si el usuario tiene alguno de los permisos especificados
   * @param {Array<string>} permissionList - Array de nombres de permisos a verificar
   * @returns {boolean} - true si el usuario tiene al menos uno de los permisos, false en caso contrario
   */
  const hasAnyPermission = (permissionList) => {
    if (!permissionList || permissionList.length === 0) return false;
    return permissionList.some(p => hasPermission(p));
  };
  
  /**
   * Determina si el usuario actual es administrador
   */
  const isAdmin = computed(() => {
    return roleName.value === 'ADMIN';
  });
  
  // Acciones
  
  /**
   * Carga los permisos del usuario basados en su rol
   * @returns {Promise<void>}
   */
  async function loadUserPermissions() {
    if (!authStore.isAuthenticated) {
      permissions.value = [];
      roleId.value = null;
      roleName.value = null;
      initialized.value = true;
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      // Obtener el rol del usuario desde el store de autenticación
      const userRole = authStore.user?.role;
      
      if (!userRole) {
        permissions.value = [];
        roleId.value = null;
        roleName.value = null;
        initialized.value = true;
        loading.value = false;
        return;
      }
      
      // Normalizar el objeto de rol (puede venir en diferentes formatos)
      let roleObj = userRole;
      if (typeof userRole === 'string') {
        roleObj = { name: userRole };
      }
      
      // Guardar información del rol
      roleId.value = roleObj.id;
      roleName.value = roleObj.name || roleObj.roleName;
      
      // Si no tenemos el ID del rol, intentamos obtenerlo por nombre
      if (roleName.value && !roleId.value) {
        await fetchRoleByName(roleName.value);
      }
      
      // Si tenemos el ID del rol, obtenemos sus permisos
      if (roleId.value) {
        await fetchRolePermissions(roleId.value);
      } else {
        permissions.value = [];
      }
      
      initialized.value = true;
    } catch (err) {
      console.error('Error al cargar permisos del usuario:', err);
      error.value = err.message;
      permissions.value = [];
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * Obtiene un rol por su nombre
   * @param {string} name - Nombre del rol a buscar
   * @returns {Promise<object>} - Datos del rol
   */
  async function fetchRoleByName(name) {
    try {
      const token = localStorage.getItem('authToken');
      if (!token) {
        throw new Error('No se encontró token de autenticación');
      }
      
      const response = await fetch(`${API_BASE_URL}/roles/name/${name}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      const roleData = await response.json();
      roleId.value = roleData.id;
      return roleData;
    } catch (err) {
      console.error(`Error al obtener rol por nombre (${name}):`, err);
      throw err;
    }
  }
  
  /**
   * Obtiene los permisos de un rol específico
   * @param {number} id - ID del rol
   * @returns {Promise<Array>} - Array de permisos
   */
  async function fetchRolePermissions(id) {
    try {
      const token = localStorage.getItem('authToken');
      if (!token) {
        throw new Error('No se encontró token de autenticación');
      }
      
      const response = await fetch(`${API_BASE_URL}/roles/${id}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      
      console.log('Response----------------:', response);
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      const roleData = await response.json();
      permissions.value = roleData.permissions || [];
      return permissions.value;
    } catch (err) {
      console.error(`Error al obtener permisos del rol (ID: ${id}):`, err);
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
    fetchRolePermissions,
    reset
  };
});
