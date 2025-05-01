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
              <!-- Búsqueda por autor -->
              <v-col cols="12" md="4">
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
              <v-col cols="12" md="4">
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
              <v-col cols="12" md="4">
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
            <span>Resultados ({{ filteredDocuments.length }})</span>
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
            <v-data-table
              :headers="headers"
              :items="filteredDocuments"
              :loading="loading"
              :items-per-page="10"
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
              <template v-slot:item.type="{ item }">
                <v-chip
                  v-if="item.type"
                  color="primary"
                  size="small"
                >
                  {{ item.type.name }}
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
            </v-data-table>
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

// Stores
const documentsStore = useDocumentsStore();
const router = useRouter();

// Estado del componente
const searchTitle = ref('');
const selectedDocumentType = ref(null);
const selectedTag = ref(null);
const searchAuthor = ref('');
const dateFrom = ref('');
const dateTo = ref('');
const documents = ref([]);
const documentTypes = ref([]);
const tags = ref([]);
const loading = ref(false);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Columnas de la tabla
const headers = [
  { title: 'ID', key: 'id', align: 'start', sortable: true },
  { title: 'Título', key: 'title', align: 'start', sortable: true, maxWidth: 200 },
  { title: 'Tipo', key: 'documentType', align: 'center', sortable: true },
  { title: 'Fecha de Subida', key: 'uploadDate', align: 'center', sortable: true },
  { title: 'Autor', key: 'authorName', align: 'center', sortable: true },
  { title: 'Acciones', key: 'actions', align: 'center', sortable: false },
];

// Computed
const filteredDocuments = computed(() => {
  let result = [...documents.value];
  // Filtrar por título
  if (searchTitle.value.trim()) {
    const searchLower = searchTitle.value.toLowerCase().trim();
    result = result.filter(doc => 
      doc.title.toLowerCase().includes(searchLower) ||
      (doc.description && doc.description.toLowerCase().includes(searchLower))
    );
  }
  
  // Filtrar por tipo de documento
  if (selectedDocumentType.value) {
    result = result.filter(doc => 
      doc.type && doc.type.id === selectedDocumentType.value.id
    );
  }
  
  // Filtrar por etiqueta
  if (selectedTag.value) {
    result = result.filter(doc => 
      doc.tags && doc.tags.some(tag => tag.id === selectedTag.value.id)
    );
  }
  
  // Filtrar por autor
  if (searchAuthor.value.trim()) {
    const searchLower = searchAuthor.value.toLowerCase().trim();
    result = result.filter(doc => 
      doc.author.username.toLowerCase().includes(searchLower) ||
      (doc.author.name && doc.author.name.toLowerCase().includes(searchLower)) ||
      (doc.author.surname && doc.author.surname.toLowerCase().includes(searchLower))
    );
  }
  
  // Filtrar por fecha desde
  if (dateFrom.value) {
    const fromDate = new Date(dateFrom.value);
    fromDate.setHours(0, 0, 0, 0); // Establecer al inicio del día
    result = result.filter(doc => 
      new Date(doc.uploadDate) >= fromDate
    );
  }
  
  // Filtrar por fecha hasta
  if (dateTo.value) {
    const toDate = new Date(dateTo.value);
    toDate.setHours(23, 59, 59, 999); // Establecer al final del día
    result = result.filter(doc => 
      new Date(doc.uploadDate) <= toDate
    );
  }
  
  return result;
});

// Lifecycle
onMounted(async () => {
  try {
    loading.value = true;
    
    // Cargar documentos
    await fetchDocuments();
    
    // Cargar tipos de documentos
    await fetchDocumentTypes();
    
    // Cargar etiquetas
    await fetchTags();
    
  } catch (error) {
    showError('Error al cargar los datos iniciales: ' + error.message);
  } finally {
    loading.value = false;
  }
});

// Observar cambios en los filtros para actualizar la búsqueda
watch([selectedDocumentType, selectedTag, dateFrom, dateTo, searchAuthor], () => {
  searchDocuments();
});

// Métodos
async function fetchDocuments() {
  try {
    loading.value = true;
     
    await documentsStore.fetchDocuments();
    documents.value = documentsStore.documents;
    console.log('Documentos cargados:', JSON.stringify(documents.value, null, 2));
  } catch (error) {
    showError('Error al cargar documentos: ' + error.message);
  } finally {
    loading.value = false;
  }
}

async function fetchDocumentTypes() {
  try {
    documentTypes.value = await documentsStore.fetchDocumentTypes();
  } catch (error) {
    showError('Error al cargar tipos de documentos: ' + error.message);
  }
}

async function fetchTags() {
  try {
    tags.value = await documentsStore.fetchTags();
  } catch (error) {
    showError('Error al cargar etiquetas: ' + error.message);
  }
}

async function searchDocuments() {
  try {
    loading.value = true;
    
    // Si hay un título de búsqueda, usar la búsqueda por título
    if (searchTitle.value.trim()) {
      documents.value = await documentsStore.searchDocumentsByTitle(searchTitle.value.trim());
    } 
    // Si hay una etiqueta seleccionada, buscar por etiqueta
    else if (selectedTag.value) {
      documents.value = await documentsStore.fetchDocumentsByTag(selectedTag.value.name);
    }
    // De lo contrario, cargar todos los documentos
    else {
      documents.value = await documentsStore.getDocuments();
    }
    
  } catch (error) {
    showError('Error al buscar documentos: ' + error.message);
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  searchTitle.value = '';
  selectedDocumentType.value = null;
  selectedTag.value = null;
  searchAuthor.value = '';
  dateFrom.value = '';
  dateTo.value = '';
  fetchDocuments();
}

function viewDocumentDetails(id) {
  router.push(`/documents/${id}`);
}

function editDocument(id) {
  router.push(`/documents/${id}/edit`);
}

function navigateToCreateDocument() {
  router.push('/documents/create');
}

async function downloadDocument(id) {
  try {
    await documentsStore.downloadDocument(id);
  } catch (error) {
    showError('Error al descargar el documento: ' + error.message);
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

.v-data-table {
  border-radius: 8px;
  overflow: hidden;
}
</style>
