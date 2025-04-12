<template>
  <v-container fluid>
    <v-row v-if="loading">
      <v-col cols="12" class="d-flex justify-center align-center pa-8">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <span class="ml-4 text-h6">Cargando documento...</span>
      </v-col>
    </v-row>

    <template v-else-if="document">
      <!-- Barra de navegación y acciones -->
      <v-row>
        <v-col cols="12">
          <v-card class="mb-4">
            <v-card-text>
              <div class="d-flex align-center">
                <v-btn
                  variant="text"
                  prepend-icon="mdi-arrow-left"
                  @click="goBack"
                >
                  Volver
                </v-btn>
                <v-spacer></v-spacer>
                <v-btn
                  color="primary"
                  prepend-icon="mdi-download"
                  class="mr-2"
                  @click="downloadDocument"
                >
                  Descargar
                </v-btn>
                <v-btn
                  color="warning"
                  prepend-icon="mdi-pencil"
                  class="mr-2"
                  @click="editDocument"
                >
                  Editar
                </v-btn>
                <v-btn
                  color="error"
                  prepend-icon="mdi-delete"
                  @click="confirmDeleteDocument"
                >
                  Eliminar
                </v-btn>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Información del documento -->
      <v-row>
        <v-col cols="12" md="8">
          <v-card>
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-file-document-outline</v-icon>
              {{ document.title }}
            </v-card-title>
            
            <v-card-text>
              <v-row>
                <v-col cols="12">
                  <div class="text-subtitle-1 font-weight-bold mb-2">Descripción</div>
                  <p class="text-body-1">{{ document.description || 'Sin descripción' }}</p>
                </v-col>
              </v-row>
              
              <v-divider class="my-4"></v-divider>
              
              <v-row>
                <v-col cols="12" md="6">
                  <div class="text-subtitle-1 font-weight-bold mb-2">Información del Documento</div>
                  <v-list density="compact">
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-calendar</v-icon>
                      </template>
                      <v-list-item-title>Fecha de subida</v-list-item-title>
                      <v-list-item-subtitle>{{ formatDate(document.uploadDate) }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-file-outline</v-icon>
                      </template>
                      <v-list-item-title>Formato</v-list-item-title>
                      <v-list-item-subtitle>{{ document.format || 'Desconocido' }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-tag-outline</v-icon>
                      </template>
                      <v-list-item-title>Tipo de documento</v-list-item-title>
                      <v-list-item-subtitle>{{ document.type ? document.type.name : 'Sin tipo' }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-numeric</v-icon>
                      </template>
                      <v-list-item-title>Versión</v-list-item-title>
                      <v-list-item-subtitle>{{ document.versionNumber || '1' }}</v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                </v-col>
                
                <v-col cols="12" md="6">
                  <div class="text-subtitle-1 font-weight-bold mb-2">Información Adicional</div>
                  <v-list density="compact">
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-account</v-icon>
                      </template>
                      <v-list-item-title>Autor</v-list-item-title>
                      <v-list-item-subtitle>{{ document.author ? document.author.username : 'Desconocido' }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-tag-multiple-outline</v-icon>
                      </template>
                      <v-list-item-title>Etiquetas</v-list-item-title>
                      <v-list-item-subtitle>
                        <div class="d-flex flex-wrap gap-1 mt-1">
                          <v-chip
                            v-for="tag in document.tags"
                            :key="tag.id"
                            size="small"
                            color="secondary"
                            class="mr-1 mb-1"
                          >
                            {{ tag.name }}
                          </v-chip>
                          <span v-if="!document.tags || document.tags.length === 0">
                            Sin etiquetas
                          </span>
                        </div>
                      </v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
        
        <!-- Vista previa del documento -->
        <v-col cols="12" md="4">
          <v-card height="100%">
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-eye</v-icon>
              Vista previa
            </v-card-title>
            
            <v-card-text class="d-flex flex-column align-center justify-center" style="height: calc(100% - 64px);">
              <!-- Mostrar vista previa según el tipo de archivo -->
              <template v-if="isPDF">
                <v-img
                  src="@/assets/pdf-preview.png"
                  max-height="200"
                  contain
                  class="mb-4"
                ></v-img>
                <v-btn
                  color="primary"
                  prepend-icon="mdi-eye"
                  @click="openPreview"
                >
                  Ver PDF
                </v-btn>
              </template>
              
              <template v-else-if="isImage">
                <v-img
                  :src="getDocumentDownloadUrl"
                  max-height="300"
                  contain
                  class="mb-4"
                ></v-img>
              </template>
              
              <template v-else>
                <v-icon size="100" color="grey lighten-1" class="mb-4">mdi-file-document-outline</v-icon>
                <div class="text-body-1 text-center">
                  No hay vista previa disponible para este tipo de archivo.
                </div>
                <v-btn
                  color="primary"
                  prepend-icon="mdi-download"
                  class="mt-4"
                  @click="downloadDocument"
                >
                  Descargar archivo
                </v-btn>
              </template>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
    
    <!-- Mensaje de error si no se encuentra el documento -->
    <v-row v-else>
      <v-col cols="12">
        <v-alert
          type="error"
          title="Error"
          text="No se pudo encontrar el documento solicitado."
          class="mb-4"
        ></v-alert>
        <div class="text-center">
          <v-btn
            color="primary"
            prepend-icon="mdi-arrow-left"
            @click="goBack"
          >
            Volver a la lista de documentos
          </v-btn>
        </div>
      </v-col>
    </v-row>
    
    <!-- Diálogo de confirmación para eliminar documento -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Confirmar eliminación</v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar el documento "{{ document?.title }}"?
          <v-alert
            type="warning"
            variant="tonal"
            class="mt-3"
          >
            Esta acción no se puede deshacer.
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            variant="text"
            @click="deleteDialog = false"
          >
            Cancelar
          </v-btn>
          <v-btn
            color="error"
            variant="elevated"
            @click="deleteDocument"
          >
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
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
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDocumentsStore } from '@/store/documents';

// Stores y router
const documentsStore = useDocumentsStore();
const route = useRoute();
const router = useRouter();

// Estado del componente
const document = ref(null);
const loading = ref(true);
const deleteDialog = ref(false);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Obtener el ID del documento de la URL
const documentId = computed(() => route.params.id);

// Verificar si el documento es un PDF
const isPDF = computed(() => {
  if (!document.value || !document.value.format) return false;
  return document.value.format.toLowerCase() === 'pdf';
});

// Verificar si el documento es una imagen
const isImage = computed(() => {
  if (!document.value || !document.value.format) return false;
  const imageFormats = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
  return imageFormats.includes(document.value.format.toLowerCase());
});

// URL de descarga del documento
const getDocumentDownloadUrl = computed(() => {
  if (!document.value) return '';
  return documentsStore.getDocumentDownloadUrl(document.value.id);
});

// Cargar el documento al montar el componente
onMounted(async () => {
  await loadDocument();
});

// Métodos
async function loadDocument() {
  try {
    loading.value = true;
    document.value = await documentsStore.fetchDocumentById(documentId.value);
  } catch (error) {
    showError('Error al cargar el documento: ' + error.message);
    document.value = null;
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.back();
}

function editDocument() {
  router.push(`/documents/${documentId.value}/edit`);
}

function downloadDocument() {
  window.open(getDocumentDownloadUrl.value, '_blank');
}

function openPreview() {
  window.open(getDocumentDownloadUrl.value, '_blank');
}

function confirmDeleteDocument() {
  deleteDialog.value = true;
}

async function deleteDocument() {
  try {
    loading.value = true;
    await documentsStore.deleteDocument(documentId.value);
    showSuccess(`Documento "${document.value.title}" eliminado correctamente.`);
    deleteDialog.value = false;
    
    // Redirigir a la lista de documentos después de eliminar
    setTimeout(() => {
      router.push('/search-documents');
    }, 1500);
  } catch (error) {
    showError('Error al eliminar el documento: ' + error.message);
  } finally {
    loading.value = false;
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
</style>
