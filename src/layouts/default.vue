<template>
  <v-app :theme="theme">
    <v-navigation-drawer
      v-model="drawer"
      :rail="rail && !isMobile"
      :expand-on-hover="!isMobile"
      :permanent="!isMobile"
      :temporary="isMobile"
      app
    >
      <v-list>
        <v-list-item
          :prepend-avatar="userAvatarPlaceholder"
          :title="authStore.user?.fullName || 'Usuario'"
          :subtitle="authStore.user?.email || ''"
          nav
        >
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

    <v-app-bar app color="primary" density="compact">
      <v-app-bar-nav-icon v-if="isMobile" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-btn v-if="!isMobile" icon @click.stop="rail = !rail">
        <v-icon>{{ rail ? 'mdi-menu-open' : 'mdi-menu' }}</v-icon>
      </v-btn>

      <v-toolbar-title>Archivo IESTPFFAA</v-toolbar-title>

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

      <v-btn icon @click="toggleTheme">
        <v-icon>{{ theme === 'light' ? 'mdi-weather-night' : 'mdi-white-balance-sunny' }}</v-icon>
      </v-btn>
    </v-app-bar>

    <v-main>
      <v-container fluid>
        <router-view />
      </v-container>
    </v-main>

    <v-footer app class="pa-3 text-center d-flex flex-column" style="font-size: 0.8em;">
      <span>&copy; {{ new Date().getFullYear() }} IESTPFFAA - Todos los derechos reservados</span>
    </v-footer>
  </v-app>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useTheme } from 'vuetify';
import { useAuthStore } from '@/store/auth';

const themeInstance = useTheme();
const theme = ref(localStorage.getItem('appTheme') || 'light');

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
  themeInstance.global.name.value = theme.value;
  localStorage.setItem('appTheme', theme.value);
}

onMounted(() => {
  themeInstance.global.name.value = theme.value;
});

const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < 960);
const drawer = ref(!isMobile.value);
const rail = ref(false);

const handleResize = () => {
  const wasMobile = isMobile.value;
  windowWidth.value = window.innerWidth;
  const nowMobile = isMobile.value;

  if (wasMobile && !nowMobile) {
    drawer.value = true;
    rail.value = false;
  } else if (!wasMobile && nowMobile) {
    drawer.value = false;
  }
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});

const authStore = useAuthStore();
const userAvatarPlaceholder = ref('https://cdn.vuetifyjs.com/images/avatars/avatar-1.png');

function handleLogout() {
  authStore.logout();
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
</script>

<style scoped>
.v-footer {
  justify-content: center;
}

.v-navigation-drawer {
  transition: width 0.2s ease-in-out;
}
</style>
