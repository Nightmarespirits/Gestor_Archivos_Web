<template>
  <v-container fluid>
    <v-card v-if="loading && !currentInventario" class="pa-5 text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <div class="text-body-1 mt-4">Cargando inventario...</div>
    </v-card>
    
    <template v-else-if="currentInventario">
      <!-- Formulario de edición de inventario -->
      <v-card class="mb-4">
        <v-card-title class="d-flex justify-space-between align-center">
          <span>Editar Inventario</span>
          <v-btn 
            color="secondary" 
            prepend-icon="mdi-arrow-left"
            :to="{ name: 'inventario-view', params: { id: currentInventario.id } }"
          >
            Volver
          </v-btn>
        </v-card-title>

        <v-card-text>
          <v-form ref="form" v-model="isFormValid" @submit.prevent="updateInventario">
            <!-- Información general del inventario -->
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.titulo"
                  label="Título del inventario"
                  :rules="[v => !!v || 'El título es requerido']"
                  required
                  variant="outlined"
                  density="comfortable"
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.dependencia"
                  label="Dependencia"
                  :rules="[v => !!v || 'La dependencia es requerida']"
                  required
                  variant="outlined"
                  density="comfortable"
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
            </v-row>

            <!-- Información basada en Anexo N° 04 -->
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.numeroIdentificacion"
                  label="Número de Identificación"
                  hint="Número único de identificación del inventario"
                  persistent-hint
                  variant="outlined"
                  density="comfortable"
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.organoGenerador"
                  label="Órgano Generador"
                  hint="Entidad o dependencia que genera el inventario"
                  persistent-hint
                  variant="outlined"
                  density="comfortable"
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.unidadOrganizacion"
                  label="Unidad de Organización"
                  variant="outlined"
                  density="comfortable"
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.anio"
                  label="Año"
                  type="number"
                  variant="outlined"
                  density="comfortable"
                  :rules="[
                    v => !!v || 'El año es requerido',
                    v => (v && v.toString().length === 4) || 'Debe ser un año válido'
                  ]"
                  required
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" sm="6">
                <v-menu
                  ref="menuFechaInicio"
                  v-model="menuFechaInicio"
                  :close-on-content-click="false"
                  transition="scale-transition"
                  offset-y
                  min-width="auto"
                  :disabled="!puedeEditarInfo"
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="editedInventario.fechaInicio"
                      label="Fecha de Inicio"
                      prepend-inner-icon="mdi-calendar"
                      readonly
                      variant="outlined"
                      density="comfortable"
                      v-bind="props"
                      :rules="[v => !!v || 'La fecha de inicio es requerida']"
                      required
                      :disabled="!puedeEditarInfo"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="editedInventario.fechaInicio"
                    @update:model-value="menuFechaInicio = false"
                  ></v-date-picker>
                </v-menu>
              </v-col>
              <v-col cols="12" sm="6">
                <v-menu
                  ref="menuFechaFin"
                  v-model="menuFechaFin"
                  :close-on-content-click="false"
                  transition="scale-transition"
                  offset-y
                  min-width="auto"
                  :disabled="!puedeEditarInfo"
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="editedInventario.fechaFin"
                      label="Fecha de Fin"
                      prepend-inner-icon="mdi-calendar"
                      readonly
                      variant="outlined"
                      density="comfortable"
                      v-bind="props"
                      :rules="[
                        v => !!v || 'La fecha de fin es requerida',
                        v => !editedInventario.fechaInicio || v >= editedInventario.fechaInicio || 'La fecha de fin debe ser posterior a la fecha de inicio'
                      ]"
                      required
                      :disabled="!puedeEditarInfo"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="editedInventario.fechaFin"
                    @update:model-value="menuFechaFin = false"
                  ></v-date-picker>
                </v-menu>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.responsable"
                  label="Responsable"
                  hint="Nombre del responsable del inventario"
                  persistent-hint
                  variant="outlined"
                  density="comfortable"
                  :rules="[v => !!v || 'El responsable es requerido']"
                  required
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedInventario.cargoResponsable"
                  label="Cargo del Responsable"
                  variant="outlined"
                  density="comfortable"
                  :disabled="!puedeEditarInfo"
                ></v-text-field>
              </v-col>
            </v-row>

            <!-- Información adicional -->
            <v-row>
              <v-col cols="12">
                <v-textarea
                  v-model="editedInventario.observaciones"
                  label="Observaciones"
                  variant="outlined"
                  auto-grow
                  rows="3"
                  :disabled="!puedeEditarInfo"
                ></v-textarea>
              </v-col>
            </v-row>

            <!-- Botones de acción -->
            <v-row class="mt-4" v-if="puedeEditarInfo">
              <v-col cols="12" class="d-flex justify-end">
                <v-btn
                  color="secondary"
                  class="mr-4"
                  :to="{ name: 'inventario-view', params: { id: currentInventario.id } }"
                >
                  Cancelar
                </v-btn>
                <v-btn
                  color="primary"
                  type="submit"
                  :loading="loading"
                  :disabled="!isFormValid"
                >
                  Guardar Cambios
                </v-btn>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
      </v-card>
      
      <!-- Detalles del inventario -->
      <v-card>
        <v-card-title class="d-flex justify-space-between align-center">
          <span class="text-h6">Detalles del Inventario</span>
          <v-btn 
            v-if="puedeEditarDetalles"
            color="primary" 
            prepend-icon="mdi-plus"
            @click="openDetalleDialog()"
          >
            Agregar Elemento
          </v-btn>
        </v-card-title>
        
        <v-card-text>
          <v-data-table
            :headers="headers"
            :items="editedInventario.detalles || []"
            :loading="loading"
            class="elevation-1"
          >
            <!-- Columna de conservación -->
            <template v-slot:item.estadoConservacion="{ item }">
              <v-chip
                :color="getEstadoConservacionColor(item.estadoConservacion)"
                text-color="white"
                size="small"
              >
                {{ item.estadoConservacion }}
              </v-chip>
            </template>
            
            <!-- Columna de acciones -->
            <template v-slot:item.actions="{ item }">
              <v-tooltip text="Editar" v-if="puedeEditarDetalles">
                <template v-slot:activator="{ props }">
                  <v-btn
                    icon
                    size="small"
                    color="warning"
                    v-bind="props"
                    @click="openDetalleDialog(item)"
                  >
                    <v-icon>mdi-pencil</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip text="Eliminar" v-if="puedeEditarDetalles">
                <template v-slot:activator="{ props }">
                  <v-btn
                    icon
                    size="small"
                    color="error"
                    v-bind="props"
                    class="ml-2"
                    @click="confirmDeleteDetalle(item)"
                  >
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>
            </template>
            
            <!-- Sin resultados -->
            <template v-slot:no-data>
              <div class="text-center pa-5">
                <v-icon size="large" icon="mdi-folder-outline" color="grey"></v-icon>
                <div class="text-body-1 mt-2">Este inventario aún no tiene elementos</div>
                <v-btn
                  v-if="puedeEditarDetalles"
                  color="primary"
                  class="mt-4"
                  @click="openDetalleDialog()"
                >
                  Agregar primer elemento
                </v-btn>
              </div>
            </template>
          </v-data-table>
        </v-card-text>
      </v-card>
    </template>
    
    <v-card v-else class="pa-5 text-center">
      <v-icon size="large" icon="mdi-alert-circle" color="error"></v-icon>
      <div class="text-h6 mt-2">Inventario no encontrado</div>
      <div class="text-body-1 mt-2">El inventario solicitado no existe o no tienes permiso para editarlo.</div>
      <v-btn
        color="primary"
        class="mt-4"
        :to="{ name: 'inventario-list' }"
      >
        Volver al listado
      </v-btn>
    </v-card>
    
    <!-- Diálogo para agregar/editar detalle -->
    <v-dialog v-model="detalleDialog" max-width="800px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ editingDetalleId ? 'Editar' : 'Agregar' }} Elemento</span>
        </v-card-title>
        
        <v-card-text>
          <v-form ref="detalleForm" v-model="isDetalleFormValid">
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedDetalle.codigo"
                  label="Código"
                  variant="outlined"
                  density="comfortable"
                  :rules="[v => !!v || 'El código es requerido']"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedDetalle.serieDocumental"
                  label="Serie Documental"
                  variant="outlined"
                  density="comfortable"
                  :rules="[v => !!v || 'La serie documental es requerida']"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="editedDetalle.titulo"
                  label="Título"
                  variant="outlined"
                  density="comfortable"
                  :rules="[v => !!v || 'El título es requerido']"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedDetalle.anosExtremos"
                  label="Años Extremos"
                  hint="Ej: 2010-2015"
                  persistent-hint
                  variant="outlined"
                  density="comfortable"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="editedDetalle.estadoConservacion"
                  :items="estadosConservacion"
                  label="Estado de Conservación"
                  variant="outlined"
                  density="comfortable"
                  :rules="[v => !!v || 'El estado de conservación es requerido']"
                  required
                ></v-select>
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedDetalle.ubicacion"
                  label="Ubicación"
                  variant="outlined"
                  density="comfortable"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedDetalle.soporte"
                  label="Soporte"
                  hint="Ej: Papel, Digital, etc."
                  persistent-hint
                  variant="outlined"
                  density="comfortable"
                ></v-text-field>
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12">
                <v-textarea
                  v-model="editedDetalle.observaciones"
                  label="Observaciones"
                  variant="outlined"
                  auto-grow
                  rows="2"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="secondary" @click="detalleDialog = false">
            Cancelar
          </v-btn>
          <v-btn 
            color="primary" 
            @click="saveDetalle" 
            :disabled="!isDetalleFormValid"
            :loading="loadingSaveDetalle"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Diálogo de confirmación para eliminar detalle -->
    <v-dialog v-model="deleteDetalleDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h5">
          Confirmar eliminación
        </v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar el elemento "{{ selectedDetalle?.titulo }}"?
          <div class="red--text mt-2">Esta acción no se puede deshacer.</div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="secondary" @click="deleteDetalleDialog = false">
            Cancelar
          </v-btn>
          <v-btn color="error" @click="deleteDetalle" :loading="loadingDeleteDetalle">
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useInventariosStore } from '@/store/inventarios';
import { useAuthStore } from '@/store/auth';

// Stores
const inventariosStore = useInventariosStore();
const authStore = useAuthStore();

// snackbar
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});
function showSnackbar(text, color = 'success', timeout = 3000) {
  snackbar.value = { show: true, text, color, timeout };
}

// Router
const route = useRoute();
const router = useRouter();

// Estado
const loading = ref(false);
const loadingSaveDetalle = ref(false);
const loadingDeleteDetalle = ref(false);
const inventarioId = computed(() => Number(route.params.id));
const currentInventario = computed(() => inventariosStore.getCurrentInventario());
const isFormValid = ref(false);
const form = ref(null);
const menuFechaInicio = ref(false);
const menuFechaFin = ref(false);

// Estado para detalles
const detalleDialog = ref(false);
const deleteDetalleDialog = ref(false);
const isDetalleFormValid = ref(false);
const detalleForm = ref(null);
const editingDetalleId = ref(null);
const selectedDetalle = ref(null);

// Estados de conservación predefinidos
const estadosConservacion = ['BUENO', 'REGULAR', 'MALO'];

// Datos editados del inventario
const editedInventario = reactive({
  titulo: '',
  dependencia: '',
  numeroIdentificacion: '',
  organoGenerador: '',
  unidadOrganizacion: '',
  anio: '',
  fechaInicio: '',
  fechaFin: '',
  responsable: '',
  cargoResponsable: '',
  observaciones: '',
  estado: '',
  detalles: []
});

// Datos del detalle en edición
const editedDetalle = reactive({
  codigo: '',
  serieDocumental: '',
  titulo: '',
  anosExtremos: '',
  estadoConservacion: 'BUENO',
  ubicacion: '',
  soporte: '',
  observaciones: ''
});

// Headers para la tabla de detalles
const headers = [
  { title: 'Código', key: 'codigo', sortable: true },
  { title: 'Serie Documental', key: 'serieDocumental', sortable: true },
  { title: 'Título', key: 'titulo', sortable: true },
  { title: 'Años Extremos', key: 'anosExtremos', sortable: true },
  { title: 'Ubicación', key: 'ubicacion', sortable: true },
  { title: 'Conservación', key: 'estadoConservacion', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false }
];

// Permisos
const puedeEditarInfo = computed(() => {
  const inv = currentInventario.value;
  if (!inv) return false;
  
  return authStore.hasPermission('inventarios:edit') && 
         (inv.estado === 'BORRADOR' || authStore.hasPermission('inventarios:edit:any'));
});

const puedeEditarDetalles = computed(() => {
  const inv = currentInventario.value;
  if (!inv) return false;
  
  return authStore.hasPermission('inventarios:edit') && 
         (inv.estado === 'BORRADOR' || authStore.hasPermission('inventarios:edit:any'));
});

// Métodos auxiliares
function getEstadoConservacionColor(estado) {
  switch (estado) {
    case 'BUENO':
      return 'green';
    case 'REGULAR':
      return 'orange';
    case 'MALO':
      return 'red';
    default:
      return 'grey';
  }
}

// Métodos de acción
async function updateInventario() {
  if (!form.value.validate()) return;
  
  try {
    loading.value = true;
    
    // Actualizar el inventario
    const updatedInventario = await inventariosStore.updateInventario(inventarioId.value, editedInventario);
    
    if (updatedInventario) {
      showSnackbar('Inventario actualizado correctamente', 'success');
    } else {
      showSnackbar('Error al actualizar el inventario', 'error');
    }
  } catch (error) {
    showSnackbar('Error: ' + (error.message || 'No se pudo actualizar el inventario'), 'error');
    console.error('Error al actualizar inventario:', error);
  } finally {
    loading.value = false;
  }
}

function openDetalleDialog(detalle = null) {
  if (detalle) {
    // Editar detalle existente
    editingDetalleId.value = detalle.id;
    Object.keys(editedDetalle).forEach(key => {
      if (key in detalle) {
        editedDetalle[key] = detalle[key];
      }
    });
  } else {
    // Nuevo detalle
    editingDetalleId.value = null;
    Object.keys(editedDetalle).forEach(key => {
      editedDetalle[key] = key === 'estadoConservacion' ? 'BUENO' : '';
    });
  }
  
  detalleDialog.value = true;
  // Dar tiempo al DOM para actualizar antes de resetear validación
  setTimeout(() => {
    if (detalleForm.value) {
      detalleForm.value.resetValidation();
    }
  }, 100);
}

async function saveDetalle() {
  if (!detalleForm.value.validate()) return;
  
  try {
    loadingSaveDetalle.value = true;
    
    if (editingDetalleId.value) {
      // Actualizar detalle existente
      await inventariosStore.updateInventarioDetalle(
        inventarioId.value, 
        editingDetalleId.value, 
        editedDetalle
      );
      showSnackbar('Elemento actualizado correctamente', 'success');
    } else {
      // Agregar nuevo detalle
      await inventariosStore.addInventarioDetalle(
        inventarioId.value, 
        editedDetalle
      );
      showSnackbar('Elemento agregado correctamente', 'success');
    }
    
    // Recargar el inventario para mostrar los cambios
    await loadInventario();
    detalleDialog.value = false;
  } catch (error) {
    showSnackbar('Error: ' + (error.message || 'No se pudo guardar el elemento'), 'error');
    console.error('Error al guardar detalle:', error);
  } finally {
    loadingSaveDetalle.value = false;
  }
}

function confirmDeleteDetalle(detalle) {
  selectedDetalle.value = detalle;
  deleteDetalleDialog.value = true;
}

async function deleteDetalle() {
  if (!selectedDetalle.value) return;
  
  try {
    loadingDeleteDetalle.value = true;
    
    const success = await inventariosStore.deleteInventarioDetalle(
      inventarioId.value, 
      selectedDetalle.value.id
    );
    
    if (success) {
      showSnackbar('Elemento eliminado correctamente', 'success');
      deleteDetalleDialog.value = false;
      selectedDetalle.value = null;
      
      // Recargar el inventario para mostrar los cambios
      await loadInventario();
    } else {
      showSnackbar('Error al eliminar el elemento', 'error');
    }
  } catch (error) {
    showSnackbar('Error: ' + (error.message || 'No se pudo eliminar el elemento'), 'error');
    console.error('Error al eliminar detalle:', error);
  } finally {
    loadingDeleteDetalle.value = false;
  }
}

// Cargar el inventario
async function loadInventario() {
  try {
    loading.value = true;
    const inventario = await inventariosStore.fetchInventarioById(inventarioId.value);
    
    if (inventario) {
      // Copiar los valores al objeto editado
      Object.keys(editedInventario).forEach(key => {
        if (key in inventario) {
          editedInventario[key] = inventario[key];
        }
      });
    } else {
      showSnackbar('Inventario no encontrado', 'error');
    }
  } catch (error) {
    showSnackbar('Error al cargar el inventario: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

// Vigilar cambios en el inventario actual
watch(() => currentInventario.value, (newValue) => {
  if (newValue) {
    // Actualizar el formulario con los valores actuales
    Object.keys(editedInventario).forEach(key => {
      if (key in newValue) {
        editedInventario[key] = newValue[key];
      }
    });
  }
});

// Cargar datos al montar el componente
onMounted(async () => {
  if (!authStore.hasPermission('inventarios:edit')) {
    showSnackbar('No tienes permiso para editar inventarios', 'error');
    router.push({ name: 'inventario-list' });
    return;
  }
  
  await loadInventario();
});
</script>
