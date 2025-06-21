<template>
  <v-container>
    <h1 class="text-h4 mb-6">Gestión de Etiquetas y Tipos de Documentos</h1>

    <v-row>
      <!-- Panel de Etiquetas -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-tag-multiple</v-icon>
            Etiquetas
            <v-spacer></v-spacer>
            <v-dialog v-model="tagDialog" max-width="500px">
              <template v-slot:activator="{ props }">
                <PermissionButton 
                  :permissions="['SYSTEM_CONFIG']"
                  prependIcon="mdi-plus"
                  color="primary" 
                  v-bind="props" 
                  size="small"
                >
                  Nueva
                </PermissionButton>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h6">{{ formTitle('tag') }}</span>
                </v-card-title>
                <v-card-text>
                  <v-form ref="tagForm" v-model="tagFormValid">
                    <v-text-field
                      v-model="editedTag.name"
                      label="Nombre de etiqueta"
                      :rules="[v => !!v || 'El nombre es requerido']"
                      required
                    ></v-text-field>
                    <v-textarea
                      v-model="editedTag.description"
                      label="Descripción"
                      rows="3"
                      auto-grow
                    ></v-textarea>
                  </v-form>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="grey" variant="text" @click="closeDialog('tag')">
                    Cancelar
                  </v-btn>
                  <v-btn color="primary" variant="text" @click="saveTag" :disabled="!tagFormValid">
                    Guardar
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-card-title>
          <v-card-text>
            <v-data-table
              :headers="tagHeaders"
              :items="tags"
              :loading="loading.tags"
              :items-per-page="10"
              class="elevation-1"
            >
              <template v-slot:item.actions="{ item }">
                <PermissionButton
                  :permissions="['SYSTEM_CONFIG']"
                  prependIcon="mdi-pencil"
                  :iconButton="true"
                  color="primary"
                  size="small"
                  @click="editItem('tag', item)"
                  tooltip="Editar"
                  variant="plain"
                />
                <PermissionButton
                  :permissions="['SYSTEM_CONFIG']"
                  prependIcon="mdi-delete"
                  :iconButton="true"
                  color="error"
                  size="small"
                  @click="deleteItem('tag', item)"
                  tooltip="Eliminar"
                  variant="plain"
                />
              </template>
            </v-data-table>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Panel de Tipos de Documentos -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-file-document-outline</v-icon>
            Tipos de Documentos
            <v-spacer></v-spacer>
            <v-dialog v-model="typeDialog" max-width="500px">
              <template v-slot:activator="{ props }">
                <PermissionButton 
                  :permissions="['SYSTEM_CONFIG']"
                  prependIcon="mdi-plus"
                  color="primary" 
                  v-bind="props" 
                  size="small"
                >
                  Nuevo
                </PermissionButton>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h6">{{ formTitle('type') }}</span>
                </v-card-title>
                <v-card-text>
                  <v-form ref="typeForm" v-model="typeFormValid">
                    <v-text-field
                      v-model="editedType.name"
                      label="Nombre del tipo"
                      :rules="[v => !!v || 'El nombre es requerido']"
                      required
                    ></v-text-field>
                    <v-textarea
                      v-model="editedType.description"
                      label="Descripción"
                      rows="3"
                      auto-grow
                    ></v-textarea>
                  </v-form>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="grey" variant="text" @click="closeDialog('type')">
                    Cancelar
                  </v-btn>
                  <v-btn color="primary" variant="text" @click="saveType" :disabled="!typeFormValid">
                    Guardar
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-card-title>
          <v-card-text>
            <v-data-table
              :headers="typeHeaders"
              :items="documentTypes"
              :loading="loading.types"
              :items-per-page="10"
              class="elevation-1"
            >
              <template v-slot:item.description="{ item }">
                <v-tooltip location="bottom" :text="item.description">
                  <template v-slot:activator="{ props }">
                    <span v-bind="props" class="text-truncate d-inline-block" style="max-width: 200px;">
                      {{ item.description || 'Sin descripción' }}
                    </span>
                  </template>
                </v-tooltip>
              </template>
              <template v-slot:item.actions="{ item }">
                <PermissionButton
                  :permissions="['SYSTEM_CONFIG']"
                  prependIcon="mdi-pencil"
                  :iconButton="true"
                  color="primary"
                  size="small"
                  @click="editItem('type', item)"
                  tooltip="Editar"
                  variant="plain"
                />
                <PermissionButton
                  :permissions="['SYSTEM_CONFIG']"
                  prependIcon="mdi-delete"
                  :iconButton="true"
                  color="error"
                  size="small"
                  @click="deleteItem('type', item)"
                  tooltip="Eliminar"
                  variant="plain"
                />
              </template>
            </v-data-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Confirmación de eliminación -->
    <v-dialog v-model="deleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h6">
          ¿Está seguro que desea eliminar este elemento?
        </v-card-title>
        <v-card-text>
          Esta acción no se puede revertir.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="deleteDialog = false">Cancelar</v-btn>
          <v-btn color="error" variant="text" @click="confirmDelete">Confirmar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Alertas -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar.show = false">Cerrar</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useUserPermissionsStore } from '@/store/userPermissions';
import PermissionButton from '@/components/common/PermissionButton.vue';

// Utilidades para API con fetch
// Obtenemos la URL base desde las variables de entorno
const API_BASE_URL = import.meta.env.VITE_BASE_URL || 'http://localhost:8080/api';

// Función para obtener el token JWT del localStorage
const getAuthToken = () => {
  const token = localStorage.getItem('authToken');
  if (!token) {
    console.warn('No se encontró token de autenticación');
  }
  return token;
};

// Función para crear headers de autenticación
const getAuthHeaders = (contentType = true) => {
  const token = getAuthToken();
  const headers = {
    'Authorization': token ? `Bearer ${token}` : ''
  };
  
  if (contentType) {
    headers['Content-Type'] = 'application/json';
    headers['Accept'] = 'application/json';
  }
  
  return headers;
};

// Función utilitaria para manejar respuestas fetch con mejor manejo de errores
const handleFetchResponse = async (response) => {
  if (!response.ok) {
    // Intentamos leer el cuerpo de error como JSON primero
    try {
      const errorData = await response.json();
      throw { status: response.status, data: errorData };
    } catch (parseError) {
      // Si no es JSON, obtenemos el texto
      try {
        const errorText = await response.text();
        // Verificar si es una respuesta HTML (indicador de que estamos apuntando al lugar equivocado)
        if (errorText.includes('<!DOCTYPE html>')) {
          console.error('Recibiendo HTML en lugar de JSON. Verificar URL de API:', API_BASE_URL);
          throw { status: response.status, data: 'Error de conexión con la API. Posible URL incorrecta.' };
        }
        throw { status: response.status, data: errorText || response.statusText };
      } catch (textError) {
        // Si todo falla, usamos el statusText
        throw { status: response.status, data: response.statusText };
      }
    }
  }
  return response.json();
};

// Funciones utilitarias para peticiones API
const apiService = {
  async get(endpoint) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        headers: getAuthHeaders(false)
      });
      return handleFetchResponse(response);
    } catch (error) {
      console.error(`Error en GET ${endpoint}:`, error);
      throw error;
    }
  },
  
  async post(endpoint, data) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(data),
      });
      return handleFetchResponse(response);
    } catch (error) {
      console.error(`Error en POST ${endpoint}:`, error);
      throw error;
    }
  },
  
  async put(endpoint, data) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(data),
      });
      return handleFetchResponse(response);
    } catch (error) {
      console.error(`Error en PUT ${endpoint}:`, error);
      throw error;
    }
  },
  
  async delete(endpoint) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      });
      return handleFetchResponse(response);
    } catch (error) {
      console.error(`Error en DELETE ${endpoint}:`, error);
      throw error;
    }
  },
};

// Estado de las tablas
const tags = ref([]);
const documentTypes = ref([]);
const loading = ref({ tags: false, types: false });

// Estado de los diálogos
const tagDialog = ref(false);
const typeDialog = ref(false);
const deleteDialog = ref(false);

// Estado de los formularios
const tagForm = ref(null);
const typeForm = ref(null);
const tagFormValid = ref(false);
const typeFormValid = ref(false);

// Elementos editados
const defaultTag = { id: null, name: '' };
const defaultType = { id: null, name: '', description: '' };
const editedTag = ref({ ...defaultTag });
const editedType = ref({ ...defaultType });
const editedIndex = ref(-1);
const itemToDelete = ref({ type: '', item: null });

// Cabeceras de tablas
const tagHeaders = [
  { title: 'ID', key: 'id', width: '20%' },
  { title: 'Nombre', key: 'name', width: '60%' },
  { title: 'Descripción', key: 'description', width: '35%' },
  { title: 'Acciones', key: 'actions', width: '20%', sortable: false },
];

const typeHeaders = [
  { title: 'ID', key: 'id', width: '15%' },
  { title: 'Nombre', key: 'name', width: '35%' },
  { title: 'Descripción', key: 'description', width: '35%' },
  { title: 'Acciones', key: 'actions', width: '15%', sortable: false },
];

// Notificaciones
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
});

// Store de permisos
const userPermissionsStore = useUserPermissionsStore();

// Funciones para los títulos de formularios
const formTitle = (type) => {
  if (type === 'tag') {
    return editedIndex.value === -1 ? 'Nueva Etiqueta' : 'Editar Etiqueta';
  } else {
    return editedIndex.value === -1 ? 'Nuevo Tipo de Documento' : 'Editar Tipo de Documento';
  }
};

// Cargar datos iniciales
onMounted(async () => {
  await fetchTags();
  await fetchDocumentTypes();
});

// Funciones para cargar datos
const fetchTags = async () => {
  loading.value.tags = true;
  try {
    const data = await apiService.get('/tags');
    tags.value = data;
  } catch (error) {
    console.error('Error al cargar etiquetas:', error);
    showSnackbar('Error al cargar etiquetas', 'error');
  } finally {
    loading.value.tags = false;
  }
};

const fetchDocumentTypes = async () => {
  loading.value.types = true;
  try {
    const data = await apiService.get('/document-types');
    documentTypes.value = data;
  } catch (error) {
    console.error('Error al cargar tipos de documentos:', error);
    showSnackbar('Error al cargar tipos de documentos', 'error');
  } finally {
    loading.value.types = false;
  }
};

// Funciones para gestionar etiquetas
const saveTag = async () => {
  if (!tagFormValid.value) return;
  
  try {
    let data;
    if (editedIndex.value === -1) {
      // Crear nueva etiqueta
      data = await apiService.post('/tags', editedTag.value);
      tags.value.push(data);
      showSnackbar('Etiqueta creada correctamente', 'success');
    } else {
      // Actualizar etiqueta existente
      data = await apiService.put(`/tags/${editedTag.value.id}`, editedTag.value);
      Object.assign(tags.value[editedIndex.value], data);
      showSnackbar('Etiqueta actualizada correctamente', 'success');
    }
    closeDialog('tag');
  } catch (error) {
    console.error('Error al guardar etiqueta:', error);
    showSnackbar(error.data || 'Error al guardar etiqueta', 'error');
  }
};

// Funciones para gestionar tipos de documentos
const saveType = async () => {
  if (!typeFormValid.value) return;
  
  try {
    let data;
    if (editedIndex.value === -1) {
      // Crear nuevo tipo de documento
      data = await apiService.post('/document-types', editedType.value);
      documentTypes.value.push(data);
      showSnackbar('Tipo de documento creado correctamente', 'success');
    } else {
      // Actualizar tipo de documento existente
      data = await apiService.put(`/document-types/${editedType.value.id}`, editedType.value);
      Object.assign(documentTypes.value[editedIndex.value], data);
      showSnackbar('Tipo de documento actualizado correctamente', 'success');
    }
    closeDialog('type');
  } catch (error) {
    console.error('Error al guardar tipo de documento:', error);
    showSnackbar(error.data || 'Error al guardar tipo de documento', 'error');
  }
};

// Funciones para editar elementos
const editItem = (type, item) => {
  editedIndex.value = type === 'tag' 
    ? tags.value.findIndex(t => t.id === item.id)
    : documentTypes.value.findIndex(dt => dt.id === item.id);
    
  if (type === 'tag') {
    editedTag.value = Object.assign({}, item);
    tagDialog.value = true;
  } else {
    editedType.value = Object.assign({}, item);
    typeDialog.value = true;
  }
};

// Funciones para eliminar elementos
const deleteItem = (type, item) => {
  itemToDelete.value = { type, item };
  deleteDialog.value = true;
};

const confirmDelete = async () => {
  try {
    const { type, item } = itemToDelete.value;
    
    if (type === 'tag') {
      await apiService.delete(`/tags/${item.id}`);
      const index = tags.value.findIndex(t => t.id === item.id);
      if (index !== -1) tags.value.splice(index, 1);
      showSnackbar('Etiqueta eliminada correctamente', 'success');
    } else {
      await apiService.delete(`/document-types/${item.id}`);
      const index = documentTypes.value.findIndex(dt => dt.id === item.id);
      if (index !== -1) documentTypes.value.splice(index, 1);
      showSnackbar('Tipo de documento eliminado correctamente', 'success');
    }
  } catch (error) {
    console.error('Error al eliminar elemento:', error);
    showSnackbar(error.data || 'Error al eliminar elemento', 'error');
  } finally {
    deleteDialog.value = false;
    itemToDelete.value = { type: '', item: null };
  }
};

// Funciones para cerrar diálogos
const closeDialog = (type) => {
  if (type === 'tag') {
    tagDialog.value = false;
    editedTag.value = Object.assign({}, defaultTag);
  } else {
    typeDialog.value = false;
    editedType.value = Object.assign({}, defaultType);
  }
  editedIndex.value = -1;
  // Dar tiempo al diálogo para cerrarse antes de resetear el formulario
  setTimeout(() => {
    tagForm.value?.reset();
    typeForm.value?.reset();
  }, 300);
};

// Función para mostrar notificaciones
const showSnackbar = (text, color = 'success') => {
  snackbar.value = {
    show: true,
    text,
    color,
  };
};
</script>

<style scoped>
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
