<template>
  <v-container fluid>

    <!-- Acciones rápidas -->
    <v-row class="mb-4">
      <v-col cols="12">
        <v-card elevation="1" class="rounded-lg">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-lightning-bolt</v-icon>
            Acciones Rápidas
          </v-card-title>
          <v-card-text>
            <v-row justify="center" align="center">
              <v-col cols="12" sm="6" md="3" class="text-center">
                <PermissionButton
                  :permissions="['DOCUMENT_CREATE']"
                  prependIcon="mdi-magnify"
                  color="primary"
                  variant="elevated"
                  size="large"
                  @click="navigateToCreate('/search-documents')"
                  class="action-btn"
                >
                  Buscar Documentos
                </PermissionButton>
              </v-col>
              <v-col cols="12" sm="6" md="3" class="text-center">
                <PermissionButton
                  :permissions="['DOCUMENT_CREATE']"
                  prependIcon="mdi-plus"
                  color="success"
                  variant="elevated"
                  size="large"
                  @click="navigateToCreate('/documents/create')"
                  class="action-btn"
                >
                  Nuevo Documento
                </PermissionButton>
              </v-col>
              <v-col cols="12" sm="6" md="3" class="text-center">
                <PermissionButton
                  :permissions="['USER_CREATE']"
                  prependIcon="mdi-account-plus"
                  color="info"
                  variant="elevated"
                  size="large"
                  @click="navigateToCreate('/users/create')"
                  class="action-btn"
                >
                  Nuevo Usuario
                </PermissionButton>
              </v-col>
              <v-col cols="12" sm="6" md="3" class="text-center">
                <PermissionButton
                  :permissions="['ACTIVITY_LOG_VIEW']"
                  prependIcon="mdi-history"
                  color="warning"
                  variant="elevated"
                  size="large"
                  @click="navigateToCreate('/activity-logs')"
                  class="action-btn"
                >
                  Ver Actividades
                </PermissionButton>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- Últimos documentos y actividades -->
    <v-row>
      <!-- Documentos recientes -->
      <v-col cols="12" md="6">
        <v-card elevation="1" class="rounded-lg" height="100%">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-file-document-multiple</v-icon>
            Documentos Recientes
            <v-spacer></v-spacer>
            <PermissionButton
              :permissions="['DOCUMENT_VIEW']"
              prependIcon="mdi-eye"
              :iconButton="true"
              color="primary"
              @click="navigateToCreate('/documents')"
            />
          </v-card-title>
          <div v-if="loadingDocuments" class="d-flex justify-center align-center pa-4">
            <v-progress-circular indeterminate color="primary"></v-progress-circular>
          </div>
          <v-list v-else>
            <v-list-item 
              v-for="document in recentDocuments" 
              :key="document.id"
              :to="{ path: `/documents/${document.id}` }"
              class="rounded my-1"
            >
              <template v-slot:prepend>
                <v-avatar color="grey-lighten-3" class="mr-2">
                  <v-icon color="primary">mdi-file-document-outline</v-icon>
                </v-avatar>
              </template>
              <v-list-item-title>{{ document.title }}</v-list-item-title>
              <v-list-item-subtitle>
                <span class="text-caption">{{ formatDate(document.uploadDate) }}</span>
                <v-chip v-if="document.type" size="x-small" class="ml-2">{{ document.type.name }}</v-chip>
              </v-list-item-subtitle>
            </v-list-item>
            <v-list-item v-if="recentDocuments.length === 0">
              <v-list-item-title class="text-center text-grey">No hay documentos disponibles</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-card>
      </v-col>

      <!-- Actividades recientes -->
      <v-col cols="12" md="6">
        <v-card elevation="1" class="rounded-lg" height="100%">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-history</v-icon>
            Actividades Recientes
            <v-spacer></v-spacer>
            <PermissionButton
              :permissions="['ACTIVITY_LOG_VIEW']"
              prependIcon="mdi-history"
              :iconButton="true"
              color="warning"
              @click="navigateToCreate('/activity-logs')"
            />
          </v-card-title>
          <v-data-table
            :headers="activityHeaders"
            :items="recentActivityLogs"
            :items-per-page="5"
            :sort-by="[{ key: 'timestamp', order: 'desc' }]"
            class="elevation-0"
            :loading="loadingLogs"
          >
            <template v-slot:item.actionType="{ item }">
              <v-chip
                :color="getActionTypeColor(item.actionType)"
                size="small"
                class="text-uppercase"
              >
                {{ item.actionType }}
              </v-chip>
            </template>
            <template v-slot:item.user="{ item }">
              {{ item.user ? item.user.username : 'Sistema' }}
            </template>
            <template v-slot:item.timestamp="{ item }">
              {{ formatDate(item.timestamp) }}
            </template>
            <template v-slot:bottom>
              <div class="text-center pt-2 pb-2">
                <PermissionButton
                  :permissions="['ACTIVITY_LOG_VIEW']"
                  prependIcon="mdi-history"
                  :iconButton="true"
                  color="warning"
                  @click="navigateToCreate('/activity-logs')"
                />
              </div>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useActivityLogsStore } from '@/store/activityLogs';
import { useDocumentsStore } from '@/store/documents';
import { useUsersStore } from '@/store/users';
import { useUserPermissionsStore } from '@/store/userPermissions';
import { useRouter } from 'vue-router';
import PermissionButton from '@/components/common/PermissionButton.vue';

// Stores
const activityLogsStore = useActivityLogsStore();
const documentsStore = useDocumentsStore();
const usersStore = useUsersStore();
const userPermissionsStore = useUserPermissionsStore();
const router = useRouter();

// Estados reactivos
const loadingLogs = ref(false);
const loadingDocuments = ref(false);
const loadingUsers = ref(false);

// Datos para las estadísticas
const documentStats = ref({
  total: 0,
  tags: 0
});
const userStats = ref({
  total: 0
});
const activityStats = ref({
  total: 0,
  today: 0
});

// Datos para las tablas
const recentActivityLogs = ref([]);
const recentDocuments = ref([]);


// Encabezados para la tabla de actividades
const activityHeaders = [
  { title: 'Acción', key: 'actionType', align: 'center', width: '150' },
  { title: 'Usuario', key: 'username', align: 'center', width: '150' },
  { 
    title: 'Fecha', 
    key: 'timestamp', 
    align: 'center', 
    width: '170',
    sortable: true
  }
];

// Cargar datos del dashboard
onMounted(async () => {
  await Promise.all([
    loadRecentActivity(),
    loadRecentDocuments()
  ]);
});

// Funciones para cargar datos
async function loadRecentActivity() {
  try {
    loadingLogs.value = true;
    const response = await activityLogsStore.fetchLogs();
    recentActivityLogs.value = response || [];
    
    // Calcular actividades de hoy
    const today = new Date().toISOString().split('T')[0];
    activityStats.value.today = recentActivityLogs.value.filter(log => 
      log.timestamp && log.timestamp.startsWith(today)
    ).length;
    
    activityStats.value.total = response.length || 0;
  } catch (error) {
    console.error('Error al cargar actividades recientes:', error);
  } finally {
    loadingLogs.value = false;
  }
}

async function loadRecentDocuments() {
  try {
    loadingDocuments.value = true;
    await documentsStore.fetchDocuments();
    
    // Ordenar documentos por fecha de subida en orden descendente (más recientes primero)
    const sortedDocuments = [...documentsStore.getDocuments()].sort((a, b) => {
      const dateA = new Date(a.uploadDate || a.createdAt || 0);
      const dateB = new Date(b.uploadDate || b.createdAt || 0);
      return dateB - dateA; // Orden descendente
    });
    
    // Tomar los 5 más recientes
    recentDocuments.value = sortedDocuments.slice(0, 5);
    documentStats.value.total = documentsStore.getDocuments().length;
    
    // Calcular número de etiquetas únicas
    const tagIds = new Set();
    documentsStore.getDocuments().forEach(doc => {
      if (doc.tags && Array.isArray(doc.tags)) {
        doc.tags.forEach(tag => tagIds.add(tag.id));
      }
    });
    documentStats.value.tags = tagIds.size;
  } catch (error) {
    console.error('Error al cargar documentos recientes:', error);
  } finally {
    loadingDocuments.value = false;
  }
}

async function loadStatistics() {
  try {
    loadingUsers.value = true;
    // Cargar estadísticas de usuarios si existe el endpoint
    try {
      const users = await usersStore.fetchUsers();
      userStats.value.total = users.length;
    } catch (error) {
      console.error('Error al cargar estadísticas de usuarios:', error);
      userStats.value.total = 0;
    }
  } catch (error) {
    console.error('Error al cargar estadísticas:', error);
  } finally {
    loadingUsers.value = false;
  }
}

// Utilidades
function formatDate(dateString) {
  if (!dateString) return 'N/A';
  
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;
  
  return date.toLocaleString('es-ES', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function getActionTypeColor(actionType) {
  const colorMap = {
    'LOGIN': 'success',
    'LOGOUT': 'grey',
    'CREATE_DOCUMENT': 'primary',
    'UPDATE_DOCUMENT': 'info',
    'DELETE_DOCUMENT': 'error',
    'DOWNLOAD_DOCUMENT': 'purple',
    'VIEW_DOCUMENT': 'blue-grey',
    'CREATE_USER': 'teal',
    'UPDATE_USER': 'amber',
    'DELETE_USER': 'deep-orange'
  };
  
  return colorMap[actionType] || 'grey';
}

function navigateToCreate(path) {
  router.push({ path });
}
</script>

<style scoped>
.v-card {
  transition: all 0.2s ease-in-out;
}

.v-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1) !important;
}
.action-btn {
  width: 100%;
  height: 50px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}
</style>
