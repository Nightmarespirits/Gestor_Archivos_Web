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
      <v-list-item prepend-icon="mdi-view-dashboard" title="Dashboard" value="dashboard" to="/"></v-list-item>

      <v-list-item prepend-icon="mdi-text-box-search-outline" title="Buscar Documentos" value="search_docs" to="/search-documents"></v-list-item>

      <template v-if="authStore.user?.role?.name === 'ADMIN'">
        <v-list-item prepend-icon="mdi-file-document-outline" title="Gestión Documentos" value="documents" to="/documents"></v-list-item>
        <v-list-item prepend-icon="mdi-tag-multiple-outline" title="Etiquetas y Tipos" value="tags_types" to="/tags-types"></v-list-item>
        <v-list-item prepend-icon="mdi-account-group-outline" title="Gestión Usuarios" value="users" to="/users"></v-list-item>
        <v-list-item prepend-icon="mdi-history" title="Historial Actividades" value="history" to="/activity-log"></v-list-item>
        <v-list-item prepend-icon="mdi-security" title="Accesos y Controles" value="access" to="/access-control"></v-list-item>
      </template>
    </v-list>

    <template v-slot:append>
      <div class="pa-2" v-if="!rail || isMobile">
        <v-btn block color="secondary" @click="handleLogout" prepend-icon="mdi-logout">
          Cerrar Sesión
        </v-btn>
      </div>
    </template>
  </v-navigation-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/store/auth';
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

function handleLogout() {
  authStore.logout();
}
</script>

<style scoped>
.v-navigation-drawer {
  transition: width 0.2s ease-in-out;
}
</style>
