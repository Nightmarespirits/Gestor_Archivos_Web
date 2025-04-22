<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card class="mb-4">
          <v-card-title class="d-flex align-center">
            <v-btn
              variant="text"
              prepend-icon="mdi-arrow-left"
              @click="goBack"
              class="mr-4"
            >
              Volver
            </v-btn>
            <v-icon class="mr-2">mdi-file-edit-outline</v-icon>
            Editar Documento
          </v-card-title>
        </v-card>
      </v-col>
    </v-row>

    <v-row v-if="loading && !document">
      <v-col cols="12" class="d-flex justify-center align-center pa-8">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <span class="ml-4 text-h6">Cargando documento...</span>
      </v-col>
    </v-row>

    <v-row v-else-if="document">
      <v-col cols="12">
        <v-card>
          <v-card-text>
            <v-form ref="form" @submit.prevent="submitForm">
              <v-row>
                <!-- Título del documento -->
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.title"
                    label="Título del documento *"
                    item-title="name"
                    item-value="name"
                    variant="outlined"
                    :rules="[v => !!v || 'El título es obligatorio']"
                    required
                  ></v-text-field>
                </v-col>
                
                <!-- Tipo de documento -->
                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.type"
                    item-title="name"
                    item-value="name"
                    :items="documentTypes"
                    label="Tipo de documento *"

                    variant="outlined"
                    :rules="[v => !!v || 'El tipo de documento es obligatorio']"
                    required
                  ></v-select>
                </v-col>
                
                <!-- Descripción del documento -->
                <v-col cols="12">
                  <v-textarea
                    v-model="formData.description"
                    label="Descripción"
                    variant="outlined"
                    rows="3"
                    auto-grow
                  ></v-textarea>
                </v-col>
                
                <!-- Etiquetas -->
                <v-col cols="12">
                  <v-autocomplete
                    v-model="formData.tags"
                    :items="availableTags"
                    label="Etiquetas"
                    variant="outlined"
                    chips
                    multiple
                    closable-chips
                    return-object
                    item-title="name"
                    item-value="id"
                  ></v-autocomplete>
                </v-col>
                
                <!-- Información del archivo actual -->
                <v-col cols="12">
                  <v-alert
                    type="info"
                    variant="tonal"
                    class="mb-4"
                  >
                    <div class="d-flex align-center">
                      <v-icon class="mr-2">{{ getFileIcon(document.format) }}</v-icon>
                      <div>
                        <div class="text-subtitle-1">Archivo actual: {{ document.filePath.split('/').pop() }}</div>
                        <div class="text-caption">Formato: {{ document.format }}</div>
                      </div>
                    </div>
                  </v-alert>
                  
                  <!-- Opción para reemplazar el archivo -->
                  <v-expansion-panels variant="accordion">
                    <v-expansion-panel>
                      <v-expansion-panel-title>
                        <div class="d-flex align-center">
                          <v-icon class="mr-2">mdi-file-replace-outline</v-icon>
                          Reemplazar archivo
                        </div>
                      </v-expansion-panel-title>
                      <v-expansion-panel-text>
                        <v-file-input
                          v-model="formData.file"
                          label="Nuevo archivo"
                          variant="outlined"
                          accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.txt"
                          show-size
                          prepend-icon="mdi-file-upload-outline"
                          @change="handleFileChange"
                        ></v-file-input>
                        
                        <v-alert
                          v-if="formData.file"
                          type="info"
                          variant="tonal"
                          class="mt-2"
                        >
                          <div class="d-flex align-center">
                            <v-icon class="mr-2">{{ getFileIcon(formData.file) }}</v-icon>
                            <div>
                              <div>{{ formData.file.name }}</div>
                              <div class="text-caption">{{ formatFileSize(formData.file.size) }}</div>
                            </div>
                          </div>
                        </v-alert>
                      </v-expansion-panel-text>
                    </v-expansion-panel>
                  </v-expansion-panels>
                </v-col>
              </v-row>
              
              <v-divider class="my-4"></v-divider>
              
              <!-- Botones de acción -->
              <div class="d-flex justify-end">
                <v-btn
                  v-if="authStore.user?.role?.name === 'ADMIN'"
                  variant="text"
                  color="error"
                  @click="confirmDeleteDocument"
                  class="mr-auto"
                >
                  Eliminar Documento
                </v-btn>
                
                <v-btn
                  variant="text"
                  @click="resetForm"
                  class="mr-2"
                >
                  Cancelar
                </v-btn>
                
                <v-btn
                  color="primary"
                  type="submit"
                  :loading="saving"
                  :disabled="saving"
                >
                  Guardar Cambios
                </v-btn>
              </div>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
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
          <p class="mb-4">¿Está seguro que desea eliminar el documento "{{ document?.title }}"?</p>
          
          <v-alert
            type="warning"
            variant="tonal"
            class="mb-4"
          >
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
            :loading="loading"
            :disabled="!deleteFormValid"
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
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDocumentsStore } from '@/store/documents';
import { useAuthStore } from '@/store/auth';

// Stores y router
const documentsStore = useDocumentsStore();
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// Referencias
const form = ref(null);
const deleteForm = ref(null);

// Estado del componente
const document = ref(null);
const documentTypes = ref([]);
const availableTags = ref([]);
const loading = ref(true);
const saving = ref(false);
const deleteDialog = ref(false);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});
const deleteFormValid = ref(false);
const adminPassword = ref('');

// Obtener el ID del documento de la URL
const documentId = computed(() => route.params.id);

// Datos del formulario
const formData = ref({
  title: '',
  description: '',
  type: '', 
  tags: [],
  file: null
});

// Cargar datos iniciales
onMounted(async () => {
  try {
    loading.value = true;
    
    // Cargar datos en paralelo para mejor rendimiento
    const [documentData, types, tags] = await Promise.all([
      loadDocument(),
      documentsStore.fetchDocumentTypes(),
      documentsStore.fetchTags()
    ]);
    
    documentTypes.value = types;
    availableTags.value = tags;
    
  } catch (error) {
    showError('Error al cargar los datos iniciales: ' + error.message);
  } finally {
    loading.value = false;
  }
});

// Observar cambios en el documento para actualizar el formulario
watch(document, (newDocument) => {
  if (newDocument) {
    formData.value = {
      title: newDocument.title || 'no encontrado',
      description: newDocument.description || '',
      type: newDocument.documentType || null, 
      tags: newDocument.tags || [],
      file: null
    };
  }
}, { immediate: true });

// Métodos
async function loadDocument() {
  try {
    document.value = await documentsStore.fetchDocumentById(documentId.value);
  } catch (error) {
    showError('Error al cargar el documento: ' + error.message);
    document.value = null;
  }
}

async function validateFormData() {
  if (!formData.value.type) {
    throw new Error('El tipo de documento es obligatorio');
  }
  
  // Validar el tamaño del archivo si se proporciona uno nuevo
  if (formData.value.file) {
    const maxSize = 20 * 1024 * 1024; // 20MB
    if (formData.value.file.size > maxSize) {
      throw new Error('El archivo excede el tamaño máximo permitido de 20MB');
    }
  }
  
  // Validar que el tipo de documento exista
  const validType = documentTypes.value.find(t => t.name === formData.value.type);
  if (!validType) {
    throw new Error('El tipo de documento seleccionado no es válido');
  }
  
  return true;
}

async function submitForm() {
  // Validar antes de enviar
  if (!validateFormData()) {
    showError('Por favor, completa los campos obligatorios.');
    return;
  }
  const documentId = route.params.id;
  // Crear objeto plano para enviar al store
  const plainData = {
    title: formData.value.title,
    description: formData.value.description || '',
    // Mapear correctamente el documentTypeId
    documentTypeId: (formData.value.type && typeof formData.value.type === 'object' && formData.value.type.id)
      ? formData.value.type.id
      : formData.value.type,
    tags: formData.value.tags ? formData.value.tags.map(tag => tag.name || tag) : [],
    file: formData.value.file || null
  };
  // Log de los datos enviados
  console.log('Datos enviados al store (plainData):', plainData);
  try {
    const updated = await documentsStore.updateDocument(documentId, plainData);
    console.log('[submitForm] Documento actualizado:', updated);
    showSuccess('Documento actualizado correctamente.');
    router.push({ name: 'DocumentDetail', params: { id: documentId } });
  } catch (error) {
    showError('Error al actualizar el documento. Revisa la consola para más detalles.');
    console.error('[submitForm] Error:', error);
  }
}

function resetForm() {
  if (document.value) {
    formData.value = {
      title: document.value.title || '',
      description: document.value.description || '',
      type: document.value.type ? document.value.type.name : null,
      tags: document.value.tags || [],
      file: null
    };
  }
  
  if (form.value) {
    form.value.resetValidation();
  }
}

function handleFileChange(file) {
  if (!file) return;
  
  // Validación básica de tamaño (20MB máximo)
  const maxSize = 20 * 1024 * 1024; // 20MB en bytes
  if (file.size > maxSize) {
    showError('El archivo es demasiado grande. El tamaño máximo es 20MB.');
    formData.value.file = null;
  }
}

function goBack() {
  router.back();
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
function getFileIcon(fileOrFormat) {
  if (!fileOrFormat) return 'mdi-file-outline';
  
  let extension;
  
  if (typeof fileOrFormat === 'string') {
    // Si es un string (formato)
    extension = fileOrFormat.toLowerCase();
  } else {
    // Si es un objeto File
    extension = fileOrFormat.name.split('.').pop().toLowerCase();
  }
  
  const iconMap = {
    pdf: 'mdi-file-pdf-box',
    doc: 'mdi-file-word-outline',
    docx: 'mdi-file-word-outline',
    xls: 'mdi-file-excel-outline',
    xlsx: 'mdi-file-excel-outline',
    ppt: 'mdi-file-powerpoint-outline',
    pptx: 'mdi-file-powerpoint-outline',
    jpg: 'mdi-file-image-outline',
    jpeg: 'mdi-file-image-outline',
    png: 'mdi-file-image-outline',
    txt: 'mdi-file-document-outline'
  };
  
  return iconMap[extension] || 'mdi-file-outline';
}

function formatFileSize(size) {
  if (!size) return '';
  
  if (size < 1024) {
    return size + ' bytes';
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB';
  } else {
    return (size / (1024 * 1024)).toFixed(2) + ' MB';
  }
}

function showSuccess(text) {
  snackbar.value = {
    show: true,
    text,
    color: 'success',
    timeout: 3000
  };
}

function showError(text) {
  snackbar.value = {
    show: true,
    text,
    color: 'error',
    timeout: 5000
  };
}
</script>
