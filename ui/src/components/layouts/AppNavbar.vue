<template>
  <v-app-bar app  density="compact">
    <v-app-bar-nav-icon v-if="isMobile" @click="toggleDrawer"></v-app-bar-nav-icon>
    <v-btn v-if="!isMobile" icon @click="toggleRail">
      <v-icon>{{ rail ? 'mdi-menu-open' : 'mdi-menu' }}</v-icon>
    </v-btn>

    <v-toolbar-title>Gestor de Archivos</v-toolbar-title>

    <v-spacer></v-spacer>

    <v-responsive max-width="260" class="mr-4">
      <v-text-field
        density="compact"
        flat
        hide-details
        rounded="lg"
        variant="solo-filled"
        prepend-inner-icon="mdi-magnify"
        placeholder="Buscar..."
        clearable
        v-model="searchQuery"
        @click:clear="clearSearch"
        @keyup.enter="performSearch"
      ></v-text-field>
    </v-responsive>
    <template v-slot:append>
      <div class="pa-2">
        <v-btn block color="secondary" @click="handleLogout" prepend-icon="mdi-logout">
          Cerrar Sesión
        </v-btn>
      </div>
    </template>
    <v-btn icon @click="toggleTheme">
      <v-icon>{{ theme === 'light' ? 'mdi-weather-night' : 'mdi-white-balance-sunny' }}</v-icon>
    </v-btn>
  </v-app-bar>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/store/auth';


const props = defineProps({
  theme: String,
  isMobile: Boolean,
  rail: Boolean,
  drawer: Boolean
});

const emit = defineEmits(['update:drawer', 'update:rail', 'toggle-theme']);
const authStore = useAuthStore();

function toggleDrawer() {
  emit('update:drawer', !props.drawer);
}

function toggleRail() {
  emit('update:rail', !props.rail);
}

function toggleTheme() {
  emit('toggle-theme');
}

const searchQuery = ref('');

function performSearch() {
  if (searchQuery.value.trim()) {
    console.log('Performing search for:', searchQuery.value);
  }
}

function clearSearch() {
  searchQuery.value = '';
}

function handleLogout() {
  authStore.logout();
}
</script>
