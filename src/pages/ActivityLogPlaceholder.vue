<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <span class="text-h5">Historial de Actividades</span>
            <v-spacer></v-spacer>
            <v-text-field
              v-model="search"
              append-icon="mdi-magnify"
              label="Buscar"
              single-line
              hide-details
              density="compact"
              class="ml-2"
              style="max-width: 300px"
            ></v-text-field>
          </v-card-title>

          <v-card-text>
            <!-- Filtros -->
            <v-row class="mb-4">
              <v-col cols="12" sm="6" md="3">
                <v-select
                  v-model="selectedActionType"
                  :items="actionTypes"
                  label="Tipo de Acción"
                  clearable
                  @update:model-value="filterByActionType"
                ></v-select>
              </v-col>
              <v-col cols="12" sm="6" md="3">
                <v-menu
                  ref="startMenu"
                  v-model="startMenu"
                  :close-on-content-click="false"
                  :return-value.sync="startDate"
                  transition="scale-transition"
                  offset-y
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="startDate"
                      label="Fecha Inicio"
                      prepend-icon="mdi-calendar"
                      readonly
                      v-bind="props"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="startDate"
                    @click:save="saveStartDate"
                    @click:cancel="startMenu = false"
                  >
                    <template v-slot:actions>
                      <v-btn
                        color="primary"
                        variant="text"
                        @click="saveStartDate"
                      >
                        OK
                      </v-btn>
                      <v-btn
                        color="error"
                        variant="text"
                        @click="startMenu = false"
                      >
                        Cancelar
                      </v-btn>
                    </template>
                  </v-date-picker>
                </v-menu>
              </v-col>
              <v-col cols="12" sm="6" md="3">
                <v-menu
                  ref="endMenu"
                  v-model="endMenu"
                  :close-on-content-click="false"
                  :return-value.sync="endDate"
                  transition="scale-transition"
                  offset-y
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="endDate"
                      label="Fecha Fin"
                      prepend-icon="mdi-calendar"
                      readonly
                      v-bind="props"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="endDate"
                    @click:save="saveEndDate"
                    @click:cancel="endMenu = false"
                  >
                    <template v-slot:actions>
                      <v-btn
                        color="primary"
                        variant="text"
                        @click="saveEndDate"
                      >
                        OK
                      </v-btn>
                      <v-btn
                        color="error"
                        variant="text"
                        @click="endMenu = false"
                      >
                        Cancelar
                      </v-btn>
                    </template>
                  </v-date-picker>
                </v-menu>
              </v-col>
              <v-col cols="12" sm="6" md="3" class="d-flex align-center">
                <v-btn
                  color="primary"
                  @click="applyDateFilter"
                  :disabled="!startDate || !endDate"
                >
                  Aplicar Filtro
                </v-btn>
                <v-btn
                  class="ml-2"
                  @click="clearFilters"
                  variant="outlined"
                >
                  Limpiar
                </v-btn>
              </v-col>
            </v-row>

            <!-- Tabla de Actividades -->
            <v-data-table
              :headers="headers"
              :items="filteredLogs"
              :loading="store.loading"
              :items-per-page="itemsPerPage"
              class="elevation-1"
            >
              <template v-slot:item.timestamp="{ item }">
                {{ formatDate(item.timestamp) }}
              </template>
              <template v-slot:item.user="{ item }">
                {{ item.user.username }}
              </template>
              <template v-slot:loading>
                <v-skeleton-loader
                  v-for="n in 5"
                  :key="n"
                  type="list-item-avatar-three-line"
                ></v-skeleton-loader>
              </template>
            </v-data-table>

            <!-- Paginación -->
            <div class="d-flex align-center justify-end mt-4">
              <v-pagination
                v-model="currentPage"
                :length="store.pagination.totalPages"
                @update:model-value="handlePageChange"
              ></v-pagination>
              <v-select
                v-model="itemsPerPage"
                :items="[10, 20, 50, 100]"
                label="Items por página"
                style="max-width: 150px"
                class="ml-4"
                @update:model-value="handleItemsPerPageChange"
              ></v-select>
            </div>
          </v-card-text>
        </v-card>

        <!-- Snackbar para errores -->
        <v-snackbar
          v-model="showError"
          color="error"
          timeout="5000"
        >
          {{ store.error }}
          <template v-slot:actions>
            <v-btn
              color="white"
              variant="text"
              @click="showError = false"
            >
              Cerrar
            </v-btn>
          </template>
        </v-snackbar>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useActivityLogsStore } from '@/store/activityLogs';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

const store = useActivityLogsStore();

// Estado local
const search = ref('');
const currentPage = ref(1);
const itemsPerPage = ref(20);
const selectedActionType = ref(null);
const startDate = ref(null);
const endDate = ref(null);
const startMenu = ref(false);
const endMenu = ref(false);
const showError = ref(false);

const saveStartDate = () => {
  startMenu.value = false;
};

const saveEndDate = () => {
  endMenu.value = false;
};

// Tipos de acción disponibles
const actionTypes = [
  'LOGIN',
  'LOGOUT',
  'CREATE_DOCUMENT',
  'UPDATE_DOCUMENT',
  'DELETE_DOCUMENT',
  'DOWNLOAD_DOCUMENT',
  'CREATE_USER',
  'UPDATE_USER',
  'DELETE_USER'
];

// Headers para la tabla
const headers = [
  { title: 'ID', align: 'start', key: 'id' },
  { title: 'Usuario', key: 'user' },
  { title: 'Tipo de Acción', key: 'actionType' },
  { title: 'Descripción', key: 'description' },
  { title: 'Fecha y Hora', key: 'timestamp' },
  { title: 'Dirección IP', key: 'ipAddress' }
];

// Computed properties
const filteredLogs = computed(() => {
  if (!search.value) return store.logs;
  
  return store.logs.filter(log => 
    log.description.toLowerCase().includes(search.value.toLowerCase()) ||
    log.actionType.toLowerCase().includes(search.value.toLowerCase()) ||
    log.user.username.toLowerCase().includes(search.value.toLowerCase())
  );
});

// Methods
const formatDate = (date) => {
  return format(new Date(date), 'dd/MM/yyyy HH:mm:ss', { locale: es });
};

const handlePageChange = async (page) => {
  try {
    await store.fetchLogs({ page: page - 1, size: itemsPerPage.value });
  } catch (error) {
    showError.value = true;
  }
};

const handleItemsPerPageChange = async () => {
  currentPage.value = 1;
  try {
    await store.fetchLogs({ page: 0, size: itemsPerPage.value });
  } catch (error) {
    showError.value = true;
  }
};

const filterByActionType = async () => {
  if (selectedActionType.value) {
    try {
      await store.fetchLogsByActionType(selectedActionType.value);
    } catch (error) {
      showError.value = true;
    }
  } else {
    handlePageChange(currentPage.value);
  }
};

const applyDateFilter = async () => {
  if (startDate.value && endDate.value) {
    try {
      const start = new Date(startDate.value).toISOString();
      const end = new Date(endDate.value).toISOString();
      await store.fetchLogsByTimeRange(start, end);
    } catch (error) {
      showError.value = true;
    }
  }
};

const clearFilters = () => {
  search.value = '';
  selectedActionType.value = null;
  startDate.value = null;
  endDate.value = null;
  currentPage.value = 1;
  handlePageChange(1);
};

// Watch para errores
watch(() => store.error, (newError) => {
  if (newError) {
    showError.value = true;
  }
});

// Carga inicial de datos
onMounted(async () => {
  try {
    await store.fetchLogs();
  } catch (error) {
    showError.value = true;
  }
});
</script>

<style scoped>
.v-data-table {
  width: 100%;
}
</style>