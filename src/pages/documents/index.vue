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
        >
          <template v-slot:item.status="{ item }">
            <v-chip
              :color="item.status ? 'success' : 'error'"
              :text="item.status ? 'Activo' : 'Inactivo'"
              size="small"
            ></v-chip>
          </template>
          
          <template v-slot:item.documentType="{ item }">
            <v-chip
              v-if="item.documentType"
              color="primary"
              size="small"
            >
              {{ item.documentType.name }}
            </v-chip>
            <v-chip
              v-else
              color="grey"
              size="small"
            >
              Sin tipo
            </v-chip>
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
            
            <v-tooltip text="Eliminar documento" location="top">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  variant="text"
                  color="error"
                  v-bind="props"
                  @click="confirmDeleteDocument(item)"
                  :disabled="documentsStore.loading"
                >
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- Diálogo de confirmación para eliminar documento -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Confirmar eliminación</v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar el documento "{{ documentToDelete?.title }}"? Esta acción no se puede deshacer.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text @click="deleteDialog = false">Cancelar</v-btn>
          <v-btn 
            color="error" 
            text 
            @click="deleteDocument"
            :loading="documentsStore.loading"
          >
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
  { title: 'Título', key: 'title', align: 'start', sortable: true },
  { title: 'Descripción', key: 'description', align: 'start', sortable: true },
  { title: 'Tipo de Documento', key: 'documentType', align: 'center' },
  { title: 'Fecha Creación', key: 'createdAt', align: 'center', sortable: true },
  { title: 'Estado', key: 'status', align: 'center', sortable: true },
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
  router.push({ name: 'documents-create' });
}

// Navegar a la página de edición de documentos
function navigateToEditDocument(document) {
  router.push({ 
    name: 'documents-edit', 
    params: { id: document.id }
  });
}

// Ver detalles del documento
function viewDocument(document) {
  router.push({ 
    name: 'documents-view',
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

// Confirmar eliminación de documento
function confirmDeleteDocument(document) {
  documentToDelete.value = document;
  deleteDialog.value = true;
}

// Eliminar documento
async function deleteDocument() {
  if (!documentToDelete.value) return;
  
  try {
    await documentsStore.deleteDocument(documentToDelete.value.id);
    showSnackbar('Documento eliminado correctamente', 'success');
    deleteDialog.value = false;
    documentToDelete.value = null;
  } catch (error) {
    showSnackbar(`Error al eliminar documento: ${error.message}`, 'error');
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
