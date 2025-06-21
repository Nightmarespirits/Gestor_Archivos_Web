<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between align-center">
        <span>Gestión de Inventarios</span>
        <v-btn 
          color="primary" 
          prepend-icon="mdi-plus"
          :to="{ name: 'inventario-create' }"
          v-if="canCreateInventario"
        >
          Nuevo Inventario
        </v-btn>
      </v-card-title>

      <v-card-text>
        <!-- Filtros y búsqueda -->
        <v-row>
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="filters.titulo"
              label="Buscar por título"
              hide-details
              density="compact"
              variant="outlined"
              prepend-inner-icon="mdi-magnify"
              @keyup.enter="loadInventarios"
              clearable
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="filters.unidadAdministrativa"
              label="Unidad Administrativa"
              hide-details
              density="compact"
              variant="outlined"
              clearable
              @keyup.enter="loadInventarios"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="4">
            <v-select
              v-model="filters.estado"
              :items="inventarioStates"
              label="Estado"
              hide-details
              density="compact"
              variant="outlined"
              clearable
              @update:model-value="loadInventarios"
            ></v-select>
          </v-col>
        </v-row>

        <v-row class="mt-2">
          <v-col cols="12" sm="4">
            <v-menu
              v-model="menuFechaInicio"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="auto"
            >
              <template v-slot:activator="{ props }">
                <v-text-field
                  v-model="filters.fechaInicio"
                  label="Fecha Desde"
                  prepend-inner-icon="mdi-calendar"
                  readonly
                  density="compact"
                  variant="outlined"
                  hide-details
                  v-bind="props"
                  clearable
                  @click:clear="filters.fechaInicio = null"
                ></v-text-field>
              </template>
              <v-date-picker
                v-model="filters.fechaInicio"
                @update:model-value="menuFechaInicio = false"
              ></v-date-picker>
            </v-menu>
          </v-col>
          <v-col cols="12" sm="4">
            <v-menu
              v-model="menuFechaFin"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="auto"
            >
              <template v-slot:activator="{ props }">
                <v-text-field
                  v-model="filters.fechaFin"
                  label="Fecha Hasta"
                  prepend-inner-icon="mdi-calendar"
                  readonly
                  density="compact"
                  variant="outlined"
                  hide-details
                  v-bind="props"
                  clearable
                  @click:clear="filters.fechaFin = null"
                ></v-text-field>
              </template>
              <v-date-picker
                v-model="filters.fechaFin"
                @update:model-value="menuFechaFin = false"
              ></v-date-picker>
            </v-menu>
          </v-col>
          <v-col cols="12" sm="4" class="d-flex justify-end align-center">
            <v-btn
              color="primary"
              @click="loadInventarios"
              prepend-icon="mdi-filter"
              class="mr-2"
            >
              Filtrar
            </v-btn>
            <v-btn
              color="secondary"
              @click="resetFilters"
              prepend-icon="mdi-refresh"
            >
              Limpiar
            </v-btn>
          </v-col>
        </v-row>

        <!-- Tabla de inventarios -->
        <v-data-table
          :headers="headers"
          :items="inventarios"
          :loading="loading"
          :items-per-page="pagination.size"
          :page="pagination.page + 1"
          :server-items-length="pagination.totalItems"
          @update:page="onPageChange"
          @update:items-per-page="onItemsPerPageChange"
          @update:sort-by="onSortChange"
          class="mt-4"
        >
          <!-- Columna de estado -->
          <template v-slot:item.estado="{ item }">
            <v-chip
              :color="getInventarioStateColor(item.estado)"
              text-color="white"
              size="small"
            >
              {{ item.estado }}
            </v-chip>
          </template>

          <!-- Columna de fecha -->
          <template v-slot:item.fechaCreacion="{ item }">
            {{ formatDate(item.fechaCreacion) }}
          </template>

          <!-- Columna de acciones -->
          <template v-slot:item.actions="{ item }">
            <v-tooltip text="Ver detalles">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  color="info"
                  v-bind="props"
                  :to="{ name: 'inventario-view', params: { id: item.id } }"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
              </template>
            </v-tooltip>

            <v-tooltip text="Editar" v-if="canEditInventario(item)">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  color="warning"
                  v-bind="props"
                  class="ml-2"
                  :to="{ name: 'inventario-edit', params: { id: item.id } }"
                >
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
              </template>
            </v-tooltip>

            <v-tooltip text="Eliminar" v-if="canDeleteInventario(item)">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  color="error"
                  v-bind="props"
                  class="ml-2"
                  @click="confirmDelete(item)"
                >
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </template>
            </v-tooltip>

            <v-menu>
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  v-bind="props"
                  class="ml-2"
                >
                  <v-icon>mdi-dots-vertical</v-icon>
                </v-btn>
              </template>
              <v-list dense>
                <v-list-item
                  @click="downloadInventario(item.id, 'excel')"
                  prepend-icon="mdi-file-excel"
                  title="Descargar Excel"
                ></v-list-item>
                <v-list-item
                  @click="downloadInventario(item.id, 'pdf')"
                  prepend-icon="mdi-file-pdf-box"
                  title="Descargar PDF"
                ></v-list-item>
                <v-divider v-if="canChangeState(item)"></v-divider>
                <v-list-item
                  v-if="item.estado === 'BORRADOR' && canChangeState(item)"
                  @click="changeState(item.id, 'FINALIZADO')"
                  prepend-icon="mdi-check-circle"
                  title="Finalizar inventario"
                ></v-list-item>
                <v-list-item
                  v-if="item.estado === 'FINALIZADO' && canChangeState(item)"
                  @click="changeState(item.id, 'APROBADO')"
                  prepend-icon="mdi-check-circle-outline"
                  title="Aprobar inventario"
                ></v-list-item>
              </v-list>
            </v-menu>
          </template>

          <!-- Sin resultados -->
          <template v-slot:no-data>
            <div class="text-center pa-5">
              <v-icon size="large" icon="mdi-file-document-outline" color="grey"></v-icon>
              <div class="text-body-1 mt-2">No se encontraron inventarios</div>
              <v-btn
                color="primary"
                class="mt-4"
                :to="{ name: 'inventario-create' }"
                v-if="canCreateInventario"
              >
                Crear nuevo inventario
              </v-btn>
            </div>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- Diálogo de confirmación para eliminar -->
    <v-dialog v-model="deleteDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h5">
          Confirmar eliminación
        </v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar el inventario "{{ selectedInventario?.titulo }}"?
          <div class="red--text mt-2">Esta acción no se puede deshacer.</div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="secondary" @click="deleteDialog = false">
            Cancelar
          </v-btn>
          <v-btn color="error" @click="deleteInventario">
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <!-- Snackbar para notificaciones -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="snackbar.timeout">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useInventariosStore } from '@/store/inventarios';
import { useAuthStore } from '@/store/auth';

// Stores
const inventariosStore = useInventariosStore();
const authStore = useAuthStore();
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Estado
const loading = ref(false);
const deleteDialog = ref(false);
const selectedInventario = ref(null);
const menuFechaInicio = ref(false);
const menuFechaFin = ref(false);

// Filtros
const filters = reactive({
  titulo: '',
  unidadAdministrativa: '',
  fechaInicio: null,
  fechaFin: null,
  estado: null
});

// Obtener estados de inventario
//const inventarioStates = computed(() => inventariosStore.getInventarioStates);
const inventarioStates = ['BORRADOR', 'FINALIZADO', 'APROBADO'];
// Configuración de paginación
const pagination = reactive({
  page: 0,
  size: 10,
  totalItems: 0,
  totalPages: 0
});

// Configuración de ordenamiento
const sortOptions = reactive({
  sortBy: 'id',
  direction: 'desc'
});

// Columnas de la tabla
const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'Título', key: 'titulo', sortable: true },
  { title: 'Dependencia', key: 'dependencia', sortable: true },
  { title: 'Fecha de Creación', key: 'fechaCreacion', sortable: true },
  { title: 'Estado', key: 'estado', sortable: true },
  { title: 'Responsable', key: 'responsable', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false }
];

// Datos
//const inventarios = computed(() => inventariosStore.getInventarios() || []);
const inventarios = [
  {
    id: 1,
    titulo: 'Inventario 1',
    dependencia: 'Dependencia 1',
    fechaCreacion: '2023-01-01',
    estado: 'BORRADOR',
    responsable: 'Responsable 1'
  },
  {
    id: 2,
    titulo: 'Inventario 2',
    dependencia: 'Dependencia 2',
    fechaCreacion: '2023-02-01',
    estado: 'FINALIZADO',
    responsable: 'Responsable 2'
  },
  {
    id: 3,
    titulo: 'Inventario 3',
    dependencia: 'Dependencia 3',
    fechaCreacion: '2023-03-01',
    estado: 'APROBADO',
    responsable: 'Responsable 3'
  }
];

// Permisos
const canCreateInventario = computed(() => {
  //return authStore.hasPermission('inventarios:create');
  return true;
});

function canEditInventario(item) {
  //return authStore.hasPermission('inventarios:edit') && 
       //  (item.estado === 'BORRADOR' || authStore.hasPermission('inventarios:edit:any'));
  return true;
}

function canDeleteInventario(item) {
  //return authStore.hasPermission('inventarios:delete') && 
    //     (item.estado === 'BORRADOR' || authStore.hasPermission('inventarios:delete:any'));
  return true;
}

function canChangeState(item) {
  /*if (item.estado === 'BORRADOR' && authStore.hasPermission('inventarios:finalize')) {
    return true;
  }
  if (item.estado === 'FINALIZADO' && authStore.hasPermission('inventarios:approve')) {
    return true;
  }*/
  return true;
}

// Métodos
async function loadInventarios() {
  try {
    loading.value = true;
    const response = await inventariosStore.searchInventariosPaginated(filters, {
      page: pagination.page,
      size: pagination.size,
      sortBy: sortOptions.sortBy,
      direction: sortOptions.direction
    });
    
    if (response) {
      pagination.totalItems = response.pagination.totalItems;
      pagination.totalPages = response.pagination.totalPages;
    }
  } catch (error) {
    showSnackbar('Error al cargar inventarios: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

// Mostrar snackbar
function showSnackbar(text, color = 'success', timeout = 3000) {
  snackbar.value = { show: true, text, color, timeout };
}
function resetFilters() {
  filters.titulo = '';
  filters.unidadAdministrativa = '';
  filters.fechaInicio = null;
  filters.fechaFin = null;
  filters.estado = null;
  loadInventarios();
}

function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date);
}

function confirmDelete(item) {
  selectedInventario.value = item;
  deleteDialog.value = true;
}

async function deleteInventario() {
  if (!selectedInventario.value) return;
  
  try {
    loading.value = true;
    const success = await inventariosStore.deleteInventario(selectedInventario.value.id);
    if (success) {
      showSnackbar('Inventario eliminado correctamente', 'success');
      deleteDialog.value = false;
      selectedInventario.value = null;
      await loadInventarios();
    } else {
      showSnackbar('Error al eliminar el inventario', 'error');
    }
  } catch (error) {
    showSnackbar('Error al eliminar: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

// Función para determinar el color según el estado del inventario
function getInventarioStateColor(estado) {
  if (!estado) return 'grey';
  
  switch (estado.toUpperCase()) {
    case 'BORRADOR':
      return 'blue';
    case 'FINALIZADO':
      return 'green';
    case 'APROBADO':
      return 'purple';
    case 'RECHAZADO':
      return 'red';
    case 'EN_REVISION':
      return 'orange';
    default:
      return 'grey';
  }
}

async function downloadInventario(id, format) {
  try {
    loading.value = true;
    const success = await inventariosStore.downloadInventario(id, format);
    if (!success) {
      showSnackbar(`Error al descargar el inventario en formato ${format}`, 'error');
    }
  } catch (error) {
    showSnackbar('Error al descargar: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

async function changeState(id, newState) {
  try {
    loading.value = true;
    await inventariosStore.changeInventarioState(id, newState);
    showSnackbar(`Estado del inventario actualizado a ${newState}`, 'success');
    await loadInventarios();
  } catch (error) {
    showSnackbar('Error al cambiar el estado: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

// Manejo de paginación
function onPageChange(page) {
  pagination.page = page - 1;
  loadInventarios();
}

function onItemsPerPageChange(size) {
  pagination.size = size;
  pagination.page = 0;
  loadInventarios();
}

function onSortChange(event) {
  if (event.length > 0) {
    const [sortItem] = event;
    sortOptions.sortBy = sortItem.key;
    sortOptions.direction = sortItem.order;
  } else {
    sortOptions.sortBy = 'id';
    sortOptions.direction = 'desc';
  }
  loadInventarios();
}

// Cargar datos al montar el componente
onMounted(async () => {
  await loadInventarios();
});
</script>
