/**
 * Utilidades para gestión de permisos en componentes
 * 
 * Estas funciones simplifican la verificación de permisos dentro 
 * de componentes Vue, ya sea en métodos computados o en métodos
 */

import { useUserPermissionsStore } from '@/store/userPermissions';

/**
 * Verificar si el usuario actual tiene un permiso específico
 * @param {string} permission - Nombre del permiso a verificar
 * @returns {boolean} True si tiene el permiso, false en caso contrario
 */
export function hasPermission(permission) {
  const permissionStore = useUserPermissionsStore();
  return permissionStore.hasPermission(permission);
}

/**
 * Verificar si el usuario actual tiene todos los permisos especificados
 * @param {Array<string>} permissions - Lista de permisos a verificar
 * @returns {boolean} True si tiene todos los permisos, false en caso contrario
 */
export function hasAllPermissions(permissions) {
  const permissionStore = useUserPermissionsStore();
  return permissionStore.hasAllPermissions(permissions);
}

/**
 * Verificar si el usuario actual tiene alguno de los permisos especificados
 * @param {Array<string>} permissions - Lista de permisos a verificar
 * @returns {boolean} True si tiene al menos uno de los permisos, false en caso contrario
 */
export function hasAnyPermission(permissions) {
  const permissionStore = useUserPermissionsStore();
  return permissionStore.hasAnyPermission(permissions);
}

/**
 * Verificar si el usuario actual tiene el rol especificado
 * @param {string} roleName - Nombre del rol a verificar
 * @returns {boolean} True si tiene el rol, false en caso contrario
 */
export function hasRole(roleName) {
  const permissionStore = useUserPermissionsStore();
  // Normalizar nombres de rol para comparación
  const normalizedUserRole = permissionStore.roleName?.toUpperCase().replace('ROLE_', '');
  const normalizedRequiredRole = roleName.toUpperCase().replace('ROLE_', '');
  
  return normalizedUserRole === normalizedRequiredRole ||
         (normalizedRequiredRole === 'ADMIN' && normalizedUserRole === 'ADMINISTRADOR') ||
         (normalizedRequiredRole === 'ADMINISTRADOR' && normalizedUserRole === 'ADMIN');
}

/**
 * Verificar si el usuario actual es administrador
 * @returns {boolean} True si es administrador, false en caso contrario
 */
export function isAdmin() {
  const permissionStore = useUserPermissionsStore();
  return permissionStore.isAdmin;
}

/**
 * Crear un objeto con permisos verificados para usar en computed properties
 * @param {Object} permissionsMap - Mapa de nombres de propiedades a permisos o grupos de permisos
 * @returns {Object} Objeto con propiedades booleanas según los permisos
 * 
 * Ejemplo de uso:
 * 
 * const permissions = usePermissions({
 *   canCreateDocument: 'DOCUMENT_CREATE',
 *   canEditOrDelete: ['DOCUMENT_UPDATE', 'DOCUMENT_DELETE'],
 *   canDownload: { any: ['FILE_DOWNLOAD', 'DOCUMENT_READ'] }
 * });
 * 
 * // Acceso en la plantilla: v-if="permissions.canCreateDocument"
 */
export function usePermissions(permissionsMap) {
  const permissionStore = useUserPermissionsStore();
  const result = {};
  
  for (const [key, value] of Object.entries(permissionsMap)) {
    if (typeof value === 'string') {
      // Permiso único
      result[key] = permissionStore.hasPermission(value);
    } else if (Array.isArray(value)) {
      // Lista de permisos (require todos)
      result[key] = permissionStore.hasAllPermissions(value);
    } else if (typeof value === 'object') {
      // Objeto con configuración específica
      if (value.any && Array.isArray(value.any)) {
        result[key] = permissionStore.hasAnyPermission(value.any);
      } else if (value.role) {
        result[key] = hasRole(value.role);
      }
    }
  }
  
  return result;
}
