<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card class="mb-4">
          <v-card-title class="headline d-flex align-center">
            <v-icon class="mr-2">mdi-text-box-search-outline</v-icon>
            Búsqueda de Documentos
          </v-card-title>
          
          <v-card-text>
            <v-row>
              <!-- Búsqueda por título -->
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="searchTitle"
                  label="Buscar por título"
                  prepend-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  @keyup.enter="searchDocuments"
                  clearable
                ></v-text-field>
              </v-col>
              
              <!-- Filtro por tipo de documento -->
              <v-col cols="12" md="4">
                <v-select
                  v-model="selectedDocumentType"
                  :items="documentTypes"
                  item-title="name"
                  item-value="id"
                  label="Tipo de documento"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  prepend-inner-icon="mdi-file-document-outline"
                  return-object
                  clearable
                ></v-select>
              </v-col>
              
              <!-- Filtro por etiqueta -->
              <v-col cols="12" md="4">
                <v-select
                  v-model="selectedTag"
                  :items="tags"
                  item-title="name"
                  item-value="id"
                  label="Etiqueta"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  prepend-inner-icon="mdi-tag-outline"
                  return-object
                  clearable
                ></v-select>
              </v-col>
            </v-row>

            <v-row>
              <!-- Búsqueda por descripción -->
              <v-col cols="12" md="3">
                <v-text-field
                  v-model="searchDescription"
                  label="Buscar por descripción"
                  prepend-inner-icon="mdi-text-box-search"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  clearable
                ></v-text-field>
              </v-col>
              
              <!-- Búsqueda por autor -->
              <v-col cols="12" md="3">
                <v-text-field
                  v-model="searchAuthor"
                  label="Buscar por autor (nombre o apellidos)"
                  prepend-inner-icon="mdi-account"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  clearable
                ></v-text-field>
              </v-col>
              
              <!-- Filtro por fecha desde -->
              <v-col cols="12" md="3">
                <v-text-field
                  v-model="dateFrom"
                  label="Desde fecha"
                  type="date"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  clearable
                ></v-text-field>
              </v-col>
              
              <!-- Filtro por fecha hasta -->
              <v-col cols="12" md="3">
                <v-text-field
                  v-model="dateTo"
                  label="Hasta fecha"
                  type="date"
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  clearable
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row class="mt-4">
              <!-- Botones de acción -->
              <v-col cols="12" md="4" class="d-flex align-center">
                <v-btn 
                  color="primary" 
                  class="mr-2" 
                  prepend-icon="mdi-magnify"
                  @click="searchDocuments"
                >
                  Buscar
                </v-btn>
                <v-btn 
                  variant="outlined" 
                  prepend-icon="mdi-refresh"
                  @click="resetFilters"
                >
                  Limpiar
                </v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
        
        <!-- Resultados de la búsqueda -->
        <v-card>
          <v-card-title class="d-flex justify-space-between align-center">
            <span>Resultados ({{ totalItems }} en total)</span>
            <PermissionButton 
              :permissions="['DOCUMENT_CREATE']"
              color="primary" 
              prepend-icon="mdi-file-plus" 
              @click="navigateToCreateDocument"
            >
              Nuevo Documento
            </PermissionButton>
          </v-card-title>
          
          <v-card-text>
            <v-data-table-server
              v-model:items-per-page="itemsPerPage"
              v-model:page="page"
              :headers="headers"
              :items-length="totalItems"
              :items="searchResults"
              :loading="loading"
              @update:options="handleTableOptionsUpdate"
              class="elevation-1"
              hover
              :no-data-text="'No se encontraron documentos'"
              :no-results-text="'No se encontraron resultados'"
            >
              <!-- Columna de título con enlace a detalle -->
              <template v-slot:item.title="{ item }">
                <a 
                  href="#" 
                  class="text-decoration-none text-primary"
                  @click.prevent="viewDocumentDetails(item.id)"
                >
                  {{ item.title }}
                </a>
              </template>
              
              <!-- Columna de tipo de documento -->
              <template v-slot:item.documentType="{ item }">
                <v-chip
                  v-if="item.documentType"
                  color="primary"
                  size="small"
                >
                  {{ item.documentType }}
                </v-chip>
                <v-chip
                  v-else
                  color="grey"
                  size="small"
                >
                  Sin tipo
                </v-chip>
              </template>

              <!-- Columna de fecha -->
              <template v-slot:item.uploadDate="{ item }">
                {{ formatDate(item.uploadDate) }}
              </template>
              
              <!-- Columna de descripción -->
              <template v-slot:item.description="{ item }">
                <span class="text-truncate d-inline-block" style="max-width: 200px;" :title="item.description">
                  {{ item.description || 'Sin descripción' }}
                </span>
              </template>
              
              <!-- Columna de acciones -->
              <template v-slot:item.actions="{ item }">
                <PermissionButton
                  :permissions="['DOCUMENT_READ']"
                  color="info"
                  @click="viewDocumentDetails(item.id)"
                  :disabled="loading"
                  :tooltip="'Ver documento'"
                  prependIcon="mdi-eye"
                  :iconButton="true"
                  variant="plain"
                />
                <PermissionButton
                  :permissions="['FILE_DOWNLOAD']"
                  prependIcon="mdi-download"
                  variant="plain"
                  color="success"
                  @click="downloadDocument(item.id)"
                  :disabled="loading"
                  :tooltip="'Descargar documento'"
                />
                <PermissionButton
                  :permissions="['DOCUMENT_UPDATE']"
                  prependIcon="mdi-pencil"
                  variant="plain"
                  color="primary"
                  @click="editDocument(item.id)"
                  :disabled="loading"
                  :tooltip="'Editar documento'"
                />
              </template>
            </v-data-table-server>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Snackbar para notificaciones -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
    >
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar.show = false">
          Cerrar
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useDocumentsStore } from '@/store/documents';
import PermissionButton from '@/components/common/PermissionButton.vue';

// Stores
const documentsStore = useDocumentsStore();
const router = useRouter();

// Estado del componente
const searchTitle = ref('');
const searchDescription = ref('');
const searchAuthor = ref('');
const selectedDocumentType = ref(null);
const selectedTag = ref(null);
const dateFrom = ref('');
const dateTo = ref('');
const documentTypes = ref([]);
const tags = ref([]);
const loading = ref(false);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Almacenar resultados de búsqueda
const searchResults = ref([]);

// Variables para paginación del lado del servidor
const page = ref(1); // Vuetify usa paginación basada en 1
const itemsPerPage = ref(10);
const totalItems = ref(0);
const sortBy = ref([{ key: 'uploadDate', order: 'desc' }]);

// Columnas de la tabla
const headers = [
  { title: 'ID', key: 'id', align: 'start', sortable: true },
  { title: 'Título', key: 'title', align: 'start', sortable: true, width: '20%' },
  { title: 'Descripción', key: 'description', align: 'start', sortable: true, width: '25%' },
  { title: 'Tipo', key: 'documentType', align: 'center', sortable: true },
  { title: 'Fecha de Subida', key: 'uploadDate', align: 'center', sortable: true },
  { title: 'Autor', key: 'authorName', align: 'center', sortable: true },
  { title: 'Acciones', key: 'actions', align: 'center', sortable: false },
];

// Nota: Ya no necesitamos filtrado del lado del cliente
// ya que ahora estamos usando la API con paginación y filtrado del lado del servidor

// Lifecycle
onMounted(async () => {
  loading.value = true;
  try {
    // Cargar datos maestros
    await Promise.all([
      fetchDocumentTypes(),
      fetchTags(),
      fetchDocuments()
    ]);
  } catch (error) {
    console.error('Error durante la inicialización:', error);
    showError('Error al cargar datos. Por favor, inténtelo de nuevo.');
  } finally {
    loading.value = false;
  }
});

// Observar cambios en los filtros para actualizar la búsqueda
watch([selectedDocumentType, selectedTag, dateFrom, dateTo, searchAuthor, searchDescription], () => {
  searchDocuments();
});

// Manejador para cambios en las opciones de la tabla
async function handleTableOptionsUpdate(options) {
  console.log('Opciones de tabla actualizadas:', options);
  
  // Actualizar opciones de paginación y ordenamiento
  page.value = options.page;
  itemsPerPage.value = options.itemsPerPage;
  
  // Manejar ordenamiento
  if (options.sortBy && options.sortBy.length > 0) {
    sortBy.value = options.sortBy;
  } else {
    // Valor por defecto: ordenar por fecha de subida descendente (más recientes primero)
    sortBy.value = [{ key: 'uploadDate', order: 'desc' }];
  }
  
  // Cargar datos con las nuevas opciones
  await searchDocuments();
}

// Métodos
async function fetchDocuments() {
  try {
    loading.value = true;
    
    // Preparar opciones de paginación para la API
    const paginationOptions = {
      page: Math.max(0, page.value - 1), // API usa base 0, Vuetify usa base 1
      size: itemsPerPage.value,
      sortBy: sortBy.value.length > 0 ? sortBy.value[0].key : 'id',
      direction: sortBy.value.length > 0 ? sortBy.value[0].order : 'asc'
    };
    
    console.log('Cargando documentos con paginación:', paginationOptions);
    
    // Usar método paginado del store
    const result = await documentsStore.fetchPaginatedDocuments(paginationOptions);
    
    // Actualizar resultados y datos de paginación
    searchResults.value = result.items || [];
    totalItems.value = result.pagination?.totalItems || 0;
    
    console.log('Documentos cargados:', searchResults.value.length, 'de', totalItems.value, 'total');
    
    return searchResults.value;
  } catch (error) {
    console.error('Error al cargar documentos:', error);
    showError('Error al cargar documentos. Por favor, inténtelo de nuevo.');
    searchResults.value = [];
    totalItems.value = 0;
    return [];
  } finally {
    loading.value = false;
  }
}

async function fetchDocumentTypes() {
  try {
    documentTypes.value = await documentsStore.fetchDocumentTypes() || [];
  } catch (error) {
    console.error('Error al cargar tipos de documentos:', error);
    documentTypes.value = [];
  }
}

async function fetchTags() {
  try {
    tags.value = await documentsStore.fetchTags() || [];
  } catch (error) {
    console.error('Error al cargar etiquetas:', error);
    tags.value = [];
  }
}

async function searchDocuments() {
  try {
    loading.value = true;
    
    // Construir parámetros de búsqueda
    const searchParams = {};
    
    if (searchTitle.value.trim()) searchParams.title = searchTitle.value.trim();
    if (searchDescription.value.trim()) searchParams.description = searchDescription.value.trim();
    if (searchAuthor.value.trim()) searchParams.author = searchAuthor.value.trim();
    if (dateFrom.value) searchParams.fromDate = dateFrom.value;
    if (dateTo.value) searchParams.toDate = dateTo.value;
    
    // Agregar ID de tipo de documento si está seleccionado
    if (selectedDocumentType.value) {
      searchParams.documentTypeId = selectedDocumentType.value.id;
    }
    
    // Agregar etiqueta si está seleccionada
    if (selectedTag.value) {
      searchParams.tags = [selectedTag.value.name];
    }
    
    // Configurar opciones de paginación
    const paginationOptions = {
      page: Math.max(0, page.value - 1), // API usa base 0, Vuetify usa base 1
      size: itemsPerPage.value,
      sortBy: sortBy.value.length > 0 ? sortBy.value[0].key : 'id',
      direction: sortBy.value.length > 0 ? sortBy.value[0].order : 'asc'
    };
    
    console.log('Realizando búsqueda con parámetros:', { searchParams, paginationOptions });
    
    let result;
    
    // Si no hay filtros activos, cargar todos los documentos con paginación
    if (Object.keys(searchParams).length === 0) {
      result = await documentsStore.fetchPaginatedDocuments(paginationOptions);
    } else {
      // Realizar búsqueda con paginación
      result = await documentsStore.searchDocumentsPaginated(searchParams, paginationOptions);
    }
    
    console.log('Resultados de búsqueda:', result);
    
    // Actualizar resultados y datos de paginación
    searchResults.value = result.items || [];
    totalItems.value = result.pagination?.totalItems || 0;
    
    return searchResults.value;
  } catch (error) {
    console.error('Error en la búsqueda:', error);
    showError('Error al realizar la búsqueda. Por favor, inténtelo de nuevo.');
    searchResults.value = [];
    totalItems.value = 0;
    return [];
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  searchTitle.value = '';
  searchDescription.value = '';
  searchAuthor.value = '';
  selectedDocumentType.value = null;
  selectedTag.value = null;
  dateFrom.value = '';
  dateTo.value = '';
  fetchDocuments();
}

function viewDocumentDetails(id) {
  router.push({ name: 'DocumentDetail', params: { id } });
}

function editDocument(id) {
  router.push({ name: 'EditDocument', params: { id }, query: { edit: true } });
}

function navigateToCreateDocument() {
  router.push({ name: 'CreateDocument' });
}

async function downloadDocument(id) {
  try {
    await documentsStore.downloadDocument(id);
    showSuccess('Documento descargado correctamente');
  } catch (error) {
    console.error('Error al descargar documento:', error);
    showError('Error al descargar documento. Por favor, inténtelo de nuevo.');
  }
}

// Utilidades
function formatDate(dateString) {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}



function showSuccess(message) {
  snackbar.value = {
    show: true,
    text: message,
    color: 'success',
    timeout: 3000
  };
}

function showError(message) {
  snackbar.value = {
    show: true,
    text: message,
    color: 'error',
    timeout: 5000
  };
}
</script>

<style scoped>
.gap-1 {
  gap: 4px;
}
.w-auto {
  width: auto;
}
</style>