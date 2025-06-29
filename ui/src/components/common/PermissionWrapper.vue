<template>
  <div v-if="hasAccess">
    <slot></slot>
  </div>
</template>

<script setup>
import { computed, onMounted} from 'vue';
import { useUserPermissionsStore } from '@/store/userPermissions';
import { useAuthStore } from '@/store/auth';

const props = defineProps({
  // Lista de permisos - si hay varios, se cumple con tener AL MENOS UNO (OR)
  permissions: {
    type: [String, Array],
    default: () => []
  },
  // Lista de permisos que TODOS deben cumplirse (AND)
  requireAllPermissions: {
    type: [String, Array],
    default: () => []
  },
  // Lista de roles - si hay varios, se cumple con tener AL MENOS UNO (OR)
  roles: {
    type: [String, Array],
    default: () => []
  }
});

const permissionStore = useUserPermissionsStore();
const authStore = useAuthStore();

// Verificar permisos individuales
const hasPermission = computed(() => {
  if (!props.permissions.length) return true;
  
  const permList = Array.isArray(props.permissions) ? props.permissions : [props.permissions];
  return permissionStore.hasAnyPermission(permList);
});

// Verificar todos los permisos requeridos
const hasAllRequiredPermissions = computed(() => {
  if (!props.requireAllPermissions.length) return true;
  
  const permList = Array.isArray(props.requireAllPermissions) 
    ? props.requireAllPermissions 
    : [props.requireAllPermissions];
  return permissionStore.hasAllPermissions(permList);
});

// Verificar roles
const hasRole = computed(() => {
  if (!props.roles.length) return true;
  
  const roleList = Array.isArray(props.roles) ? props.roles : [props.roles];
  const userRole = authStore.user?.role?.name?.toUpperCase() || '';
  
  return roleList.some(role => {
    const normalizedRole = role.toUpperCase();
    return userRole === normalizedRole || 
           (normalizedRole === 'ADMIN' && userRole === 'ADMINISTRADOR') || 
           (normalizedRole === 'ADMINISTRADOR' && userRole === 'ADMIN');
  });
});

// Acceso final - DEMO MODE: siempre se concede acceso
const hasAccess = computed(() => {
  // Se ignoran todas las restricciones para la demo
  return true;
});

onMounted(() => {
  permissionStore.loadUserPermissions();
});
</script>
