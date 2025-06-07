<template>
  <v-navigation-drawer
    v-model="drawerModel"
    :rail="rail && !isMobile"
    :expand-on-hover="!isMobile"
    :permanent="!isMobile"
    :temporary="isMobile"
    app
  >
    <v-list>
      <v-list-item
        :title="authStore.user?.fullName || 'Usuario'"
        :subtitle="authStore.user?.email || ''"
        nav
      >
        <template v-slot:prepend>
          <user-initials-avatar :fullName="authStore.user?.fullName || 'Usuario'" :size="40" />
        </template>
        <template v-slot:append>
          <v-menu>
            <template v-slot:activator="{ props }">
              <v-btn
                variant="text"
                icon="mdi-chevron-down"
                v-bind="props"
              ></v-btn>
            </template>
            <v-list>
              <v-list-item @click="handleLogout" prepend-icon="mdi-logout">
                <v-list-item-title>Cerrar Sesión</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </template>
      </v-list-item>
    </v-list>

    <v-divider></v-divider>

    <v-list density="compact" nav>
      <!-- Dashboard - Visible para todos -->
      <v-list-item prepend-icon="mdi-view-dashboard" title="Dashboard" value="dashboard" to="/"></v-list-item>

      <!-- Buscar Documentos - Visible para cualquiera con DOCUMENT_READ -->
      <v-list-item 
        v-if="hasPermission('DOCUMENT_READ')" 
        prepend-icon="mdi-text-box-search-outline" 
        title="Buscar Documentos" 
        value="search_docs" 
        to="/documents/search">
      </v-list-item>

      <!-- Gestión Documentos - Visible para roles que pueden crear/editar documentos -->
      <v-list-item 
        v-if="hasPermission('DOCUMENT_CREATE') || hasPermission('DOCUMENT_UPDATE')" 
        prepend-icon="mdi-file-document-outline" 
        title="Gestión Documentos" 
        value="documents" 
        to="/documents">
      </v-list-item>
      <!-- Control de Transferencia Documental - Visible principalmente para administración -->
      <v-list-group
        v-if="hasAdminAccess"
        value="transferencia"
      >
        <template v-slot:activator="{ props }">
          <v-list-item
            v-bind="props"
            prepend-icon="mdi-file-move-outline"
            title="Transferencia Documental"
          ></v-list-item>
        </template>
        
        <v-list-item
          prepend-icon="mdi-view-list-outline"
          title="Inventario General"
          value="inventario_general"
          to="/inventarios"
        ></v-list-item>
        
        <v-list-item
          prepend-icon="mdi-file-document-multiple-outline"
          title="Registro de Transferencia"
          value="registro_transferencia"
          to="/registro-transferencia"
        ></v-list-item>
        
        <v-list-item
          prepend-icon="mdi-bookshelf"
          title="Catálogo Transferencia"
          value="catalogo_transferencia"
          to="/catalogo-transferencia"
        ></v-list-item>
      </v-list-group>
      <!-- Etiquetas y Tipos - Visible principalmente para administración -->
      <v-list-item 
        v-if="hasAdminAccess" 
        prepend-icon="mdi-tag-multiple-outline" 
        title="Etiquetas y Tipos" 
        value="tags_types" 
        to="/tags-types">
      </v-list-item>

      <!-- Gestión Usuarios - Visible para roles con USER_READ/UPDATE (ADMIN, MANAGER) -->
      <v-list-item 
        v-if="hasPermission('USER_READ')" 
        prepend-icon="mdi-account-group-outline" 
        title="Gestión Usuarios" 
        value="users" 
        to="/users">
      </v-list-item>

      <!-- Historial Actividades - Visible para permisos administrativos o AUDIT_LOG_VIEW -->
      <v-list-item 
        v-if="hasPermission('AUDIT_LOG_VIEW') || hasAdminAccess" 
        prepend-icon="mdi-history" 
        title="Historial Actividades" 
        value="history" 
        to="/activity-logs">
      </v-list-item>

      <!-- Accesos y Controles - Solo para SUPERADMIN (ROLE_MANAGE, SYSTEM_CONFIG) -->
      <v-list-item 
        v-if="hasPermission('ROLE_MANAGE')" 
        prepend-icon="mdi-security" 
        title="Accesos y Controles" 
        value="access" 
        to="/access-control">
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/store/auth';
import { useUserPermissionsStore } from '@/store/userPermissions';
import UserInitialsAvatar from './UserInitialsAvatar.vue';

const props = defineProps({
  drawer: Boolean,
  rail: Boolean,
  isMobile: Boolean
});

const emit = defineEmits(['update:drawer', 'update:rail']);

// Manejar el two-way binding para drawer
const drawerModel = computed({
  get: () => props.drawer,
  set: (value) => emit('update:drawer', value)
});

const authStore = useAuthStore();
const permissionStore = useUserPermissionsStore();

// Función para verificar si el usuario tiene un permiso específico
function hasPermission(permission) {
  return permissionStore.hasPermission(permission);
}

// Verificar si el usuario tiene acceso de nivel administrativo
const hasAdminAccess = computed(() => {
  // Verificar si el usuario tiene un rol administrativo (ADMIN o SUPERADMIN)
  const role = authStore.user?.role?.name?.toUpperCase();
  return role === 'ADMIN' || role === 'SUPERADMIN' || role === 'ADMINISTRADOR';
});

// Verificar si el usuario es específicamente un super administrador
const isSuperAdmin = computed(() => {
  const role = authStore.user?.role?.name?.toUpperCase();
  return role === 'SUPERADMIN';
});

// Verificar si el usuario es específicamente un Manager
const isManager = computed(() => {
  const role = authStore.user?.role?.name?.toUpperCase();
  return role === 'MANAGER' || role === 'GESTOR';
});

// Verificar si el usuario es un archivista
const isArchivist = computed(() => {
  const role = authStore.user?.role?.name?.toUpperCase();
  return role === 'ARCHIVIST' || role === 'ARCHIVISTA';
});

function handleLogout() {
  authStore.logout();
}
</script>

<style scoped>
.v-navigation-drawer {
  transition: width 0.2s ease-in-out;
}
</style>
