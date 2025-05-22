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
            <v-icon class="mr-2">mdi-file-plus-outline</v-icon>
            Crear Nuevo Documento
          </v-card-title>
        </v-card>
      </v-col>
    </v-row>

    <v-row>
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
                    variant="outlined"
                    :rules="[v => !!v || 'El título es obligatorio']"
                    required
                  ></v-text-field>
                </v-col>
                
                <!-- Tipo de documento -->
                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.type"
                    :items="documentTypes"
                    item-title="name"
                    item-value="name"
                    label="Tipo de documento *"
                    variant="outlined"
                    :rules="[v => !!v || 'El tipo de documento es obligatorio']"
                    required
                  ></v-select>
                </v-col>
                
                <!-- Nivel de acceso -->
                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.security.accessLevel"
                    :items="accessLevels"
                    label="Nivel de acceso *"
                    variant="outlined"
                    :rules="[v => !!v || 'El nivel de acceso es obligatorio']"
                    required
                  >
                    <template v-slot:selection="{ item }">
                      <v-chip
                        :color="getSecurityLevelColor(item.value)"
                        size="small"
                        class="mr-2"
                      >
                        {{ item.value }}
                      </v-chip>
                    </template>
                    <template v-slot:item="{ item, props }">
                      <v-list-item
                        v-bind="props"
                        :title="item.value"
                        :prepend-icon="getSecurityLevelIcon(item.value)"
                        :subtitle="getSecurityLevelDescription(item.value)"
                      >
                      </v-list-item>
                    </template>
                  </v-select>
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
                    item-title="name"
                    item-value="id"
                    label="Etiquetas"
                    variant="outlined"
                    chips
                    multiple
                    closable-chips
                    return-object
                  ></v-autocomplete>
                </v-col>
                
                <!-- Subida de archivo -->
                <v-col cols="12">
                  <v-file-input
                    v-model="formData.file"
                    label="Archivo del documento *"
                    variant="outlined"
                    accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.txt"
                    :rules="[v => !!v || 'El archivo es obligatorio']"
                    required
                    show-size
                    prepend-icon="mdi-file-upload-outline"
                    @change="handleFileChange"
                  ></v-file-input>
                  <!-- previzualizacion del archivo -->
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
                </v-col>
              </v-row>
              
              <v-divider class="my-4"></v-divider>
              
              <!-- Botones de acción -->
              <div class="d-flex justify-end">
                <v-btn
                  variant="text"
                  @click="resetForm"
                  class="mr-2"
                >
                  Limpiar
                </v-btn>
                <PermissionButton
                  :permissions="['DOCUMENT_CREATE']"
                  color="primary"
                  type="submit"
                  :loading="loading"
                  :disabled="loading"
                  class="ml-2"
                >
                  Guardar Documento
                </PermissionButton>
              </div>
            </v-form>
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
import { ref, onMounted, computed} from 'vue';
import { useRouter } from 'vue-router';
import { useDocumentsStore } from '@/store/documents';
import { useAuthStore } from '@/store/auth';
import PermissionButton from '@/components/common/PermissionButton.vue';

// Stores y router
const documentsStore = useDocumentsStore();
const authStore = useAuthStore();
const router = useRouter();

// Referencias
const form = ref(null);

// Estado del componente
const documentTypes = ref([]);
const availableTags = ref([]);
const loading = ref(false);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Datos del formulario
const formData = ref({
  title: '',
  description: '',
  type: null,
  tags: [],
  file: null,
  security: {
    accessLevel: 'Privado' // Valor por defecto
  },
  authorId: null
});

// Definir los niveles de acceso disponibles según el rol del usuario
const accessLevels = computed(() => {
  return documentsStore.getAccessLevels();
});

// Cargar datos iniciales
onMounted(async () => {
  try {
    loading.value = true;
    
    // Cargar tipos de documentos
    documentTypes.value = await documentsStore.fetchDocumentTypes();
    
    // Cargar etiquetas disponibles
    availableTags.value = await documentsStore.fetchTags();
    
    // Establecer el autor actual usando authStore
    if (authStore.user && authStore.user.id) {
      formData.value.authorId = authStore.user.id;
    }
    
  } catch (error) {
    showError('Error al cargar los datos iniciales: ' + error.message);
  } finally {
    loading.value = false;
  }
});

// Métodos
async function submitForm() {
  if (!form.value) return;
  
  const { valid } = await form.value.validate();
  
  if (!valid) {
    showError('Por favor, complete todos los campos obligatorios.');
    return;
  }

  if (!formData.value.type) {
    showError('El tipo de documento es obligatorio.');
    return;
  }
  
  try {
    loading.value = true;
    
    // Crear FormData directamente aquí en lugar de pasar el objeto
    const formDataToSend = new FormData();
    
    // Agregar el archivo primero
    if (formData.value.file) {
      formDataToSend.append('file', formData.value.file[0] || formData.value.file);
    }
    
    // Agregar el resto de campos
    formDataToSend.append('title', formData.value.title);
    formDataToSend.append('description', formData.value.description || '');
    formDataToSend.append('authorId', formData.value.authorId);
    formDataToSend.append('type', formData.value.type);
    formDataToSend.append('security', JSON.stringify(formData.value.security));
    
    // Agregar etiquetas si existen
    if (formData.value.tags && formData.value.tags.length > 0) {
      formData.value.tags.forEach(tag => {
        formDataToSend.append('tags', tag.name);
      });
    }
    
    // Enviar datos al servidor
    const newDocument = await documentsStore.createDocument(formDataToSend);
    
    showSuccess('Documento creado correctamente.');
    
    // Redirigir usando name-based routing
    router.push({ 
      name: 'DocumentDetail',
      params: { id: newDocument.id }
    });
    
  } catch (error) {
    showError('Error al crear el documento: ' + error.message);
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  formData.value = {
    title: '',
    description: '',
    type: null,
    tags: [],
    file: null,
    security: {
      accessLevel: 'Privado' // Valor por defecto
    },
    authorId: authStore.user ? authStore.user.id : null
  };
  
  if (form.value) {
    form.value.reset();
  }
}

function handleFileChange(file) {
  // Puedes realizar validaciones adicionales aquí si es necesario
  console.log('Archivo seleccionado:', file);
}

function goBack() {
  router.back();
}

// Utilidades
function getFileIcon(file) {
  if (!file) return 'mdi-file-question-outline';
  
  const fileName = file.name || '';
  const extension = fileName.split('.').pop().toLowerCase();
  
  switch (extension) {
    case 'pdf':
      return 'mdi-file-pdf-box';
    case 'doc':
    case 'docx':
      return 'mdi-file-word-outline';
    case 'xls':
    case 'xlsx':
      return 'mdi-file-excel-outline';
    case 'ppt':
    case 'pptx':
      return 'mdi-file-powerpoint-outline';
    case 'jpg':
    case 'jpeg':
    case 'png':
      return 'mdi-file-image-outline';
    case 'txt':
      return 'mdi-file-document-outline';
    default:
      return 'mdi-file-outline';
  }
}

function formatFileSize(size) {
  if (size === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(size) / Math.log(k));
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function getSecurityLevelColor(level) {
  return documentsStore.getSecurityLevelColor(level);
}

function getSecurityLevelIcon(level) {
  switch (level) {
    case 'Secreto':
      return 'mdi-shield-lock';
    case 'Reservado':
      return 'mdi-shield-alert';
    case 'Privado':
    default:
      return 'mdi-shield-check';
  }
}

function getSecurityLevelDescription(level) {
  switch (level) {
    case 'Secreto':
      return 'Solo visible para administradores';
    case 'Reservado':
      return 'Visible para gestores y administradores';
    case 'Privado':
    default:
      return 'Visible para todos los usuarios';
  }
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
