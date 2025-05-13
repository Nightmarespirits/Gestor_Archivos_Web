<template>
  <v-container fluid>
    <v-row v-if="loading">
      <v-col cols="12" class="d-flex justify-center align-center pa-8">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <span class="ml-4 text-h6">Cargando documento...</span>
      </v-col>
    </v-row>
    <template v-else-if="document">
      <v-row>
        <v-col cols="12">
          <v-card class="mb-4">
            <v-card-text>
              <div class="d-flex align-center">
                <v-btn variant="text" prepend-icon="mdi-arrow-left" @click="goBack">Volver</v-btn>
                <v-spacer></v-spacer>
                <PermissionButton 
                  :permissions="['FILE_DOWNLOAD']"
                  color="primary" 
                  prepend-icon="mdi-download" 
                  class="mr-2" 
                  @click="downloadDocument"
                >
                  Descargar
                </PermissionButton>
                <PermissionButton 
                  :permissions="['DOCUMENT_UPDATE']"
                  color="warning" 
                  prepend-icon="mdi-pencil" 
                  class="mr-2" 
                  @click="editDocument"
                >
                  Editar
                </PermissionButton>
                <PermissionButton 
                  :permissions="['DOCUMENT_DELETE']"
                  color="error" 
                  prepend-icon="mdi-delete" 
                  @click="confirmDeleteDocument"
                >
                  Eliminar
                </PermissionButton>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

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
                      <v-list-item-subtitle>{{ document.documentType ? document.documentType : 'Sin tipo' }}</v-list-item-subtitle>
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
                      <v-list-item-subtitle>{{ document.authorName ? document.authorName : 'Desconocido' }}</v-list-item-subtitle>
                    </v-list-item>
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-tag-multiple-outline</v-icon>
                      </template>
                      <v-list-item-title>Etiquetas</v-list-item-title>
                      <v-list-item-subtitle>
                        <div class="d-flex flex-wrap gap-1 mt-1">
                          <v-chip v-for="tag in document.tags" size="small" color="secondary" class="mr-1 mb-1">{{ tag }}</v-chip>
                          <span v-if="!document.tags || document.tags.length === 0">Sin etiquetas</span>
                        </div>
                      </v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                </v-col>
              </v-row>
              
              <!-- Metadatos del documento -->
              <v-divider class="my-4"></v-divider>
              <v-row v-if="metadata">
                <v-col cols="12">
                  <div class="text-subtitle-1 font-weight-bold mb-2">Metadatos</div>
                  <v-list density="compact">
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-key-outline</v-icon>
                      </template>
                      <v-list-item-title>Palabras clave</v-list-item-title>
                      <v-list-item-subtitle>{{ metadata.keywords || 'Sin palabras clave' }}</v-list-item-subtitle>
                    </v-list-item>
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-office-building-outline</v-icon>
                      </template>
                      <v-list-item-title>Departamento</v-list-item-title>
                      <v-list-item-subtitle>{{ metadata.department || 'General' }}</v-list-item-subtitle>
                    </v-list-item>
                    <v-list-item v-if="metadata.expirationDate">
                      <template v-slot:prepend>
                        <v-icon color="primary">mdi-calendar-clock</v-icon>
                      </template>
                      <v-list-item-title>Fecha de expiración</v-list-item-title>
                      <v-list-item-subtitle>{{ formatDate(metadata.expirationDate) }}</v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card height="100%">
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-eye</v-icon>
              Vista previa
            </v-card-title>
            <v-card-text class="d-flex flex-column align-center justify-center" style="height: calc(100% - 64px);">
              <template v-if="isPDF">
                <v-img src="@/assets/pdf-preview.png" max-height="200" contain class="mb-4"></v-img>
                <PermissionButton :permissions="['DOCUMENT_READ']" color="primary" prepend-icon="mdi-eye" @click="showPreviewDialog = true" class="mr-2">Ver PDF</PermissionButton>
                <PermissionButton :permissions="['FILE_DOWNLOAD']" color="secondary" prepend-icon="mdi-download" @click="downloadDocument">Descargar</PermissionButton>
              </template>
              <template v-else-if="isImage">
                <v-img :src="getDocumentPreviewUrl" max-height="300" contain class="mb-4" @click="showPreviewDialog = true" style="cursor: pointer;"></v-img>
                <PermissionButton :permissions="['DOCUMENT_READ']" color="primary" prepend-icon="mdi-fullscreen" @click="showPreviewDialog = true" class="mr-2">Maximizar</PermissionButton>
                <PermissionButton :permissions="['FILE_DOWNLOAD']" color="secondary" prepend-icon="mdi-download" @click="downloadDocument">Descargar</PermissionButton>
              </template>
              <template v-else>
                <v-icon size="100" color="grey lighten-1" class="mb-4">mdi-file-document-outline</v-icon>
                <div class="text-body-1 text-center">No hay vista previa disponible para este tipo de archivo.</div>
                <PermissionButton :permissions="['FILE_DOWNLOAD']" color="primary" prepend-icon="mdi-download" class="mt-4" @click="downloadDocument">Descargar archivo</PermissionButton>
              </template>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
    <v-row v-else>
      <v-col cols="12">
        <v-alert type="error" title="Error" text="No se pudo encontrar el documento solicitado." class="mb-4"></v-alert>
        <div class="text-center">
          <v-btn color="primary" prepend-icon="mdi-arrow-left" @click="goBack">Volver a la lista de documentos</v-btn>
        </div>
      </v-col>
    </v-row>

    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Confirmar eliminación</v-card-title>
        <v-card-text>
          <p class="mb-4">¿Está seguro que desea eliminar el documento "{{ document?.title }}"?</p>
          <v-alert type="warning" variant="tonal" class="mb-4">
            Esta acción no se puede deshacer.
          </v-alert>
          <v-form ref="deleteForm" v-model="deleteFormValid">
            <v-text-field
              v-model="adminPassword"
              label="Contraseña de administrador"
              type="password"
              :rules="[v => !!v || 'La contraseña es requerida']"
              required
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="deleteDialog = false">
            Cancelar
          </v-btn>
          <v-btn 
            color="error" 
            variant="elevated" 
            @click="deleteDocument"
            :loading="loading"
            :disabled="!deleteFormValid"
          >
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="showPreviewDialog" fullscreen @update:modelValue="val => { if (!val) beforeDialogClose(); }">
      <v-card>
        <v-toolbar color="primary">
          <v-btn icon @click="showPreviewDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>Vista previa: {{ document?.title }}</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn 
            prepend-icon="mdi-download" 
            @click="downloadDocument"
            color="white"
            variant="text"
          >
            Descargar
          </v-btn>
        </v-toolbar>
        <v-card-text class="d-flex justify-center align-center pa-0" style="height: calc(100vh - 64px);">
          <template v-if="isPDF">
            <iframe 
              :src="getDocumentPreviewUrl" 
              width="100%" 
              height="100%" 
              style="border: none;"
              type="application/pdf"
            ></iframe>
          </template>
          <template v-else-if="isImage">
            <v-img 
              :src="getDocumentPreviewUrl" 
              max-height="90vh" 
              contain 
              class="mx-auto"
            ></v-img>
          </template>
        </v-card-text>
      </v-card>
    </v-dialog>
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="snackbar.timeout">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar.show = false">Cerrar</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDocumentsStore } from '@/store/documents';
import { useUserPermissionsStore } from '@/store/userPermissions';
import PermissionButton from '@/components/common/PermissionButton.vue';

const documentsStore = useDocumentsStore();
const userPermissionsStore = useUserPermissionsStore();
const route = useRoute();
const router = useRouter();

const document = ref(null);
const loading = ref(true);
const deleteDialog = ref(false);
const showPreviewDialog = ref(false);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});
const adminPassword = ref('');
const deleteFormValid = ref(false);
const previewData = ref({ url: '', mimeType: '' });

const documentId = computed(() => route.params.id);

const isPDF = computed(() => {
  if (!document.value) return false;
  return (document.value.format || '').toLowerCase().includes('pdf');
});

const isImage = computed(() => {
  if (!document.value) return false;
  const mime = (document.value.format || '').toLowerCase();
  return mime.startsWith('image/');
});

const getDocumentPreviewUrl = computed(() => previewData.value.url);

const metadata = computed(() => document.value?.metadata || null);

onMounted(async () => {
  await loadDocument();
});

async function loadDocument() {
  try {
    loading.value = true;
    document.value = await documentsStore.fetchDocumentById(documentId.value);
    previewData.value = await documentsStore.getFilePreview(documentId.value);
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

async function downloadDocument() {
  try {
    loading.value = true;
    await documentsStore.downloadDocument(documentId.value);
    showSuccess('Documento descargado correctamente');
  } catch (error) {
    showError('Error al descargar el documento: ' + error.message);
  } finally {
    loading.value = false;
  }
}

function confirmDeleteDocument() {
  deleteDialog.value = true;
}

async function deleteDocument() {
  try {
    loading.value = true;
    await documentsStore.deleteDocument(documentId.value, adminPassword.value);
    showSuccess(`Documento "${document.value.title}" eliminado correctamente.`);
    deleteDialog.value = false;
    setTimeout(() => {
      router.push('/search-documents');
    }, 1500);
  } catch (error) {
    showError('Error al eliminar el documento: ' + error.message);
  } finally {
    loading.value = false;
  }
}

function beforeDialogClose() {
  documentsStore.revokePreviewUrl(documentId.value);
}

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
