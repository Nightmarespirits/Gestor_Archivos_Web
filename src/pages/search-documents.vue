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
                ></v-text-field>
              </v-col>
              
              <!-- Filtro por tipo de documento -->
              <v-col cols="12" md="3">
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
                ></v-select>
              </v-col>
              
              <!-- Filtro por etiqueta -->
              <v-col cols="12" md="3">
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
                ></v-select>
              </v-col>
              
              <!-- Botones de acción -->
              <v-col cols="12" md="2" class="d-flex align-center">
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
            
            <!-- Filtros adicionales -->
            <v-expand-transition>
              <div v-if="showAdvancedFilters">
                <v-row class="mt-4">
                  <!-- Filtro por fecha desde -->
                  <v-col cols="12" md="3">
                    <v-menu
                      v-model="dateFromMenu"
                      :close-on-content-click="false"
                      location="bottom"
                    >
                      <template v-slot:activator="{ props }">
                        <v-text-field
                          v-model="dateFrom"
                          label="Fecha desde"
                          prepend-inner-icon="mdi-calendar"
                          readonly
                          v-bind="props"
                          variant="outlined"
                          density="comfortable"
                          hide-details
                        ></v-text-field>
                      </template>
                      <v-date-picker
                        v-model="dateFrom"
                        @update:model-value="dateFromMenu = false"
                      ></v-date-picker>
                    </v-menu>
                  </v-col>
                  
                  <!-- Filtro por fecha hasta -->
                  <v-col cols="12" md="3">
                    <v-menu
                      v-model="dateToMenu"
                      :close-on-content-click="false"
                      location="bottom"
                    >
                      <template v-slot:activator="{ props }">
                        <v-text-field
                          v-model="dateTo"
                          label="Fecha hasta"
                          prepend-inner-icon="mdi-calendar"
                          readonly
                          v-bind="props"
                          variant="outlined"
                          density="comfortable"
                          hide-details
                        ></v-text-field>
                      </template>
                      <v-date-picker
                        v-model="dateTo"
                        @update:model-value="dateToMenu = false"
                      ></v-date-picker>
                    </v-menu>
                  </v-col>
                  
                  <!-- Filtro por autor -->
                  <v-col cols="12" md="3">
                    <v-autocomplete
                      v-model="selectedAuthor"
                      :items="authors"
                      item-title="fullName"
                      item-value="id"
                      label="Autor"
                      variant="outlined"
                      density="comfortable"
                      hide-details
                      prepend-inner-icon="mdi-account"
                      return-object
                    ></v-autocomplete>
                  </v-col>

                  <!-- Filtro por departamento -->
                  <v-col cols="12" md="3">
                    <v-select
                      v-model="selectedDepartment"
                      :items="departments"
                      item-title="title"
                      item-value="value"
                      label="Departamento"
                      variant="outlined"
                      density="comfortable"
                      hide-details
                      prepend-inner-icon="mdi-office-building"
                    ></v-select>
                  </v-col>

                  <!-- Filtro por estado de expiración -->
                  <v-col cols="12" md="3">
                    <v-select
                      v-model="selectedExpirationStatus"
                      :items="expirationStatusOptions"
                      item-title="title"
                      item-value="value"
                      label="Estado de expiración"
                      variant="outlined"
                      density="comfortable"
                      hide-details
                      prepend-inner-icon="mdi-calendar-alert"
                    ></v-select>
                  </v-col>
                </v-row>
              </div>
            </v-expand-transition>
            
            <!-- Toggle para mostrar/ocultar filtros avanzados -->
            <div class="d-flex justify-end mt-2">
              <v-btn
                variant="text"
                size="small"
                @click="showAdvancedFilters = !showAdvancedFilters"
              >
                {{ showAdvancedFilters ? 'Ocultar filtros avanzados' : 'Mostrar filtros avanzados' }}
                <v-icon right>
                  {{ showAdvancedFilters ? 'mdi-chevron-up' : 'mdi-chevron-down' }}
                </v-icon>
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
        
        <!-- Resultados de la búsqueda -->
        <v-card>
          <v-card-title class="d-flex justify-space-between align-center">
            <span>Resultados ({{ filteredDocuments.length }})</span>
            <v-btn
              color="success"
              prepend-icon="mdi-plus"
              @click="navigateToCreateDocument"
            >
              Nuevo Documento
            </v-btn>
          </v-card-title>
          
          <v-card-text>
            <v-data-table
              :headers="headers"
              :items="filteredDocuments"
              :search="searchTitle"
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

              <template v-slot:item.uploadDate="{ item }">
                {{ new Date(item.uploadDate).toLocaleDateString() }}
              </template>
              
              <!-- Columna de acciones -->
              <template v-slot:item.actions="{ item }">
                <v-tooltip text="Ver documento" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      icon
                      variant="text"
                      color="info"
                      v-bind="props"
                      @click="viewDocumentDetails(item.id)"
                      :disabled="loading"
                    >
                      <v-icon>mdi-eye</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
                
                <v-tooltip text="Descargar documento" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      icon
                      variant="text"
                      color="success"
                      v-bind="props"
                      @click="downloadDocument(item.id)"
                      :disabled="loading"
                    >
                      <v-icon>mdi-download</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
                
                <v-tooltip text="Editar documento" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      icon
                      variant="text"
                      color="primary"
                      v-bind="props"
                      @click="editDocument(item.id)"
                      :disabled="loading"
                    >
                      <v-icon>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
              </template>
            </v-data-table>
            
            <!-- Estado de carga -->
            <div v-if="loading" class="d-flex justify-center align-center pa-4">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
              <span class="ml-2">Cargando documentos...</span>
            </div>
            
            <!-- Mensaje cuando no hay resultados -->
            <v-alert
              v-if="!loading && filteredDocuments.length === 0"
              type="info"
              text="No se encontraron documentos con los criterios de búsqueda especificados."
              class="mt-4"
            ></v-alert>
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
        <v-btn
          variant="text"
          @click="snackbar.show = false"
        >
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
import { useAuthStore } from '@/store/auth';

// Stores
const documentsStore = useDocumentsStore();
const router = useRouter();

// Estado del componente
const searchTitle = ref('');
const selectedDocumentType = ref(null);
const selectedTag = ref(null);
const selectedAuthor = ref(null);
const dateFrom = ref(null);
const dateTo = ref(null);
const dateFromMenu = ref(false);
const dateToMenu = ref(false);
const showAdvancedFilters = ref(false);
const documents = ref([]);
const documentTypes = ref([]);
const tags = ref([]);
const authors = ref([]);
const loading = ref(false);
const deleteDialog = ref(false);
const documentToDelete = ref(null);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});
const selectedDepartment = ref(null);
const selectedExpirationStatus = ref(null);
const departments = ref([]);
const adminPassword = ref('');
const deleteFormValid = ref(false);

// Columnas de la tabla
const headers = [
  { title: 'ID', key: 'id', align: 'start', sortable: true },
  { title: 'Título', key: 'title', align: 'start', sortable: true, maxWidth: 200 },
  { title: 'Tipo', key: 'type', align: 'center', sortable: true },
  { title: 'Versión', key: 'versionNumber', align: 'center', sortable: true },
  { title: 'Fecha de Subida', key: 'uploadDate', align: 'center', sortable: true },
  { title: 'Autor', key: 'author.username', align: 'center', sortable: true },
  { title: 'Acciones', key: 'actions', align: 'center', sortable: false },
];

// Opciones de estado de expiración
const expirationStatusOptions = [
  { title: 'Expirados', value: 'expired' },
  { title: 'Por expirar', value: 'expiring-soon' },
  { title: 'Vigentes', value: 'valid' },
  { title: 'Sin expiración', value: 'no-expiration' }
];

// Computed property para documentos filtrados
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
  if (selectedAuthor.value) {
    result = result.filter(doc => 
      doc.author && doc.author.id === selectedAuthor.value.id
    );
  }
  
  // Filtrar por rango de fechas
  if (dateFrom.value) {
    const fromDate = new Date(dateFrom.value);
    result = result.filter(doc => new Date(doc.uploadDate) >= fromDate);
  }
  
  if (dateTo.value) {
    const toDate = new Date(dateTo.value);
    toDate.setHours(23, 59, 59, 999);
    result = result.filter(doc => new Date(doc.uploadDate) <= toDate);
  }
  
  // Filtrar por metadatos
  result = result.filter(doc => {
    if (!doc.metadata) return true;
    
    // Filtrar por departamento
    if (selectedDepartment.value && 
        doc.metadata.department !== selectedDepartment.value) {
      return false;
    }
    
    // Filtrar por palabras clave
    if (searchTitle.value.trim() && doc.metadata.keywords) {
      const keywords = doc.metadata.keywords.toLowerCase();
      if (!keywords.includes(searchTitle.value.toLowerCase().trim())) {
        return false;
      }
    }
    
    // Filtrar por estado de expiración
    if (selectedExpirationStatus.value) {
      const today = new Date();
      const expirationDate = doc.metadata.expirationDate ? 
        new Date(doc.metadata.expirationDate) : null;
      
      switch (selectedExpirationStatus.value) {
        case 'expired':
          if (!expirationDate || expirationDate > today) return false;
          break;
        case 'expiring-soon':
          if (!expirationDate) return false;
          const thirtyDaysFromNow = new Date();
          thirtyDaysFromNow.setDate(today.getDate() + 30);
          if (expirationDate > thirtyDaysFromNow || expirationDate < today) return false;
          break;
        case 'valid':
          if (!expirationDate || expirationDate <= today) return false;
          break;
        case 'no-expiration':
          if (expirationDate) return false;
          break;
      }
    }
    
    return true;
  });
  
  return result;
});

// Cargar datos iniciales
onMounted(async () => {
  try {
    loading.value = true;
    
    // Cargar documentos
    await fetchDocuments();
    
    // Cargar tipos de documentos
    await fetchDocumentTypes();
    
    // Cargar etiquetas
    await fetchTags();
    
    // Extraer departamentos únicos de los documentos
    const deptSet = new Set();
    documents.value.forEach(doc => {
      if (doc.metadata && doc.metadata.department) {
        deptSet.add(doc.metadata.department);
      }
    });
    departments.value = Array.from(deptSet).map(dept => ({
      title: dept,
      value: dept
    }));
    
  } catch (error) {
    showError('Error al cargar los datos iniciales: ' + error.message);
  } finally {
    loading.value = false;
  }
});

// Observar cambios en los filtros para actualizar la búsqueda
watch([selectedDocumentType, selectedTag, selectedAuthor, dateFrom, dateTo, selectedDepartment, selectedExpirationStatus], () => {
  searchDocuments();
});

// Métodos
async function fetchDocuments() {
  try {
    loading.value = true;
    documents.value = await documentsStore.fetchAllDocuments();
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
    // Si hay un autor seleccionado, buscar por autor
    else if (selectedAuthor.value) {
      documents.value = await documentsStore.fetchDocumentsByAuthor(selectedAuthor.value.id);
    }
    // De lo contrario, cargar todos los documentos
    else {
      documents.value = await documentsStore.fetchAllDocuments();
    }
    
    // Filtrar resultados por tipo de documento si está seleccionado
    if (selectedDocumentType.value) {
      documents.value = documents.value.filter(doc => 
        doc.type && doc.type.id === selectedDocumentType.value.id
      );
    }
    
    // Filtrar por fecha desde si está seleccionada
    if (dateFrom.value) {
      const fromDate = new Date(dateFrom.value);
      documents.value = documents.value.filter(doc => 
        new Date(doc.uploadDate) >= fromDate
      );
    }
    
    // Filtrar por fecha hasta si está seleccionada
    if (dateTo.value) {
      const toDate = new Date(dateTo.value);
      toDate.setHours(23, 59, 59, 999); // Establecer al final del día
      documents.value = documents.value.filter(doc => 
        new Date(doc.uploadDate) <= toDate
      );
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
  selectedAuthor.value = null;
  dateFrom.value = null;
  dateTo.value = null;
  selectedDepartment.value = null;
  selectedExpirationStatus.value = null;
  fetchDocuments();
}

function viewDocumentDetails(id) {
  router.push(`/documents/${id}`);
}

function editDocument(id) {
  router.push(`/documents/${id}/edit`);
}

async function downloadDocument(id) {
  try {
    const downloadUrl = documentsStore.getDocumentDownloadUrl(id);
    const response = await fetch(downloadUrl);
    if (!response.ok) {
      throw new Error(`Error al descargar: ${response.statusText}`);
    }
    window.open(downloadUrl, '_blank');
  } catch (error) {
    showError('Error al descargar el documento: ' + error.message);
  }
}

function navigateToCreateDocument() {
  router.push('/documents/create');
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
