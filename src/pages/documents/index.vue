<template>
  <div>
    <v-row class="mb-4">
      <v-col cols="8">
        <h1 class="text-h4">Gestión de Documentos</h1>
      </v-col>
      <v-col cols="4" class="d-flex justify-end">
        <v-btn 
          color="primary" 
          prepend-icon="mdi-file-plus" 
          @click="navigateToCreateDocument"
        >
          Nuevo Documento
        </v-btn>
      </v-col>
    </v-row>

    <!-- Tabla de documentos -->
    <v-card>
      <v-card-text>
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="Buscar documento"
          single-line
          hide-details
          clearable
          variant="outlined"
          density="compact"
          class="mb-4"
        ></v-text-field>

        <v-data-table
          :headers="headers"
          :items="documentsStore.documents"
          :search="search"
          :loading="documentsStore.loading"
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
              @click.prevent="viewDocument(item)"
            >
              {{ item.title }}
            </a>
          </template>

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
          
          <template v-slot:item.actions="{ item }">
            <v-tooltip text="Ver documento" location="top">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  variant="text"
                  color="info"
                  v-bind="props"
                  @click="viewDocument(item)"
                  :disabled="documentsStore.loading"
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
                  @click="downloadDocument(item)"
                  :disabled="documentsStore.loading"
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
                  @click="navigateToEditDocument(item)"
                  :disabled="documentsStore.loading"
                >
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- Snackbar para notificaciones -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="snackbar.timeout">
      {{ snackbar.text }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useDocumentsStore } from '@/store/documents';
import { useRouter } from 'vue-router';

// Store
const documentsStore = useDocumentsStore();
const router = useRouter();

// Estado
const search = ref('');
const deleteDialog = ref(false);
const documentToDelete = ref(null);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000,
});

// Columnas de la tabla
const headers = [
  { title: 'ID', key: 'id', align: 'start', sortable: true },
  { title: 'Título', key: 'title', align: 'start', sortable: true , maxWidth: 200 },
  { title: 'Tipo', key: 'type', align: 'center', sortable: true },
  { title: 'Versión', key: 'versionNumber', align: 'center', sortable: true },
  { title: 'Fecha de Subida', key: 'uploadDate', align: 'center', sortable: true },
  { title: 'Autor', key: 'author.username', align: 'center', sortable: true },
  { title: 'Acciones', key: 'actions', align: 'center', sortable: false },
];

// Cargar documentos al montar el componente
onMounted(async () => {
  try {
    await documentsStore.fetchAllDocuments();
  } catch (error) {
    showSnackbar(`Error al cargar documentos: ${error.message}`, 'error');
  }
});

// Navegar a la página de creación de documentos
function navigateToCreateDocument() {
  router.push({ name: 'CreateDocument' });
}

// Navegar a la página de edición de documentos
function navigateToEditDocument(document) {
  router.push({ 
    name: 'EditDocument', 
    params: { id: document.id }
  });
}

// Ver detalles del documento
function viewDocument(document) {
  router.push({ 
    name: 'DocumentDetail',
    params: { id: document.id }
  });
}

// Descargar documento
async function downloadDocument(document) {
  try {
    const downloadUrl = documentsStore.getDocumentDownloadUrl(document.id);
    window.open(downloadUrl, '_blank');
  } catch (error) {
    showSnackbar(`Error al descargar documento: ${error.message}`, 'error');
  }
}

// Mostrar snackbar
function showSnackbar(text, color = 'success', timeout = 3000) {
  snackbar.value = { show: true, text, color, timeout };
}
</script>

<style scoped>
.v-data-table {
  border-radius: 8px;
  overflow: hidden;
}
</style>
