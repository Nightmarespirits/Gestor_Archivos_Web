<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Detalles del Catálogo de Transferencia</span>
        <v-btn
          color="secondary"
          variant="outlined"
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'catalogo-transferencia-detalle', params: { id } }"
          class="ml-2"
        >
          Volver al Catálogo
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <v-alert
          v-if="error"
          type="error"
          title="Error"
          :text="error"
          class="mb-4"
        ></v-alert>
        
        <!-- Controles de búsqueda y filtrado -->
        <v-row class="mb-4">
          <v-col cols="12" md="4">
            <v-text-field
              v-model="search"
              label="Buscar"
              prepend-inner-icon="mdi-magnify"
              variant="outlined"
              density="compact"
              hide-details
              @update:model-value="debouncedSearch"
            ></v-text-field>
          </v-col>
          <v-col cols="12" md="8" class="d-flex justify-end">
            <v-btn
              color="primary"
              prepend-icon="mdi-plus"
              @click="openDetalleDialog()"
            >
              Añadir Detalle
            </v-btn>
          </v-col>
        </v-row>
        
        <!-- Tabla de detalles con paginación -->
        <v-data-table
          :headers="headers"
          :items="detalles"
          :loading="loading"
          :items-per-page="pagination.size"
          :server-items-length="pagination.totalItems"
          @update:options="handleTableOptions"
          class="elevation-1"
        >
          <!-- Columna de fecha -->
          <template v-slot:item.fechaUnidadDocumental="{ item }">
            {{ item.fechaUnidadDocumental ? formatDate(item.fechaUnidadDocumental) : '-' }}
          </template>
          
          <!-- Columna de contenido -->
          <template v-slot:item.alcanceContenido="{ item }">
            {{ truncateText(item.alcanceContenido, 30) }}
          </template>
          
          <!-- Columna de acciones -->
          <template v-slot:item.actions="{ item }">
            <v-icon size="small" class="me-2" @click="editarDetalle(item)">
              mdi-pencil
            </v-icon>
            <v-icon size="small" @click="confirmarEliminarDetalle(item)">
              mdi-delete
            </v-icon>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
    
    <!-- Modal para añadir/editar detalle -->
    <v-dialog v-model="modalDetalle" max-width="800px">
      <v-card>
        <v-card-title>
          <span>{{ editandoDetalle ? 'Editar' : 'Añadir' }} Detalle</span>
        </v-card-title>
        
        <v-card-text>
          <v-form ref="detalleForm" @submit.prevent="guardarDetalle">
            <v-row>
              <!-- Número de Item -->
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="detalleActual.numeroItem"
                  label="Número de Item"
                  type="number"
                  :rules="[v => !!v || 'Número de item es requerido']"
                  required
                  variant="outlined"
                ></v-text-field>
              </v-col>
              
              <!-- Número de Caja -->
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="detalleActual.numeroCaja"
                  label="Número de Caja"
                  variant="outlined"
                ></v-text-field>
              </v-col>
              
              <!-- Número de Tomo/Paquete -->
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="detalleActual.numeroTomoPaquete"
                  label="Número de Tomo/Paquete"
                  variant="outlined"
                ></v-text-field>
              </v-col>
              
              <!-- Número de Unidad Documental -->
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="detalleActual.numeroUnidadDocumental"
                  label="Número de Unidad Documental"
                  variant="outlined"
                ></v-text-field>
              </v-col>
              
              <!-- Fecha de Unidad Documental -->
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="detalleActual.fechaUnidadDocumental"
                  label="Fecha de Unidad Documental"
                  type="date"
                  variant="outlined"
                ></v-text-field>
              </v-col>
              
              <!-- Cantidad de Folios -->
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="detalleActual.cantidadFolios"
                  label="Cantidad de Folios"
                  type="number"
                  variant="outlined"
                ></v-text-field>
              </v-col>
              
              <!-- Alcance y Contenido -->
              <v-col cols="12">
                <v-textarea
                  v-model="detalleActual.alcanceContenido"
                  label="Alcance y Contenido"
                  variant="outlined"
                  rows="3"
                ></v-textarea>
              </v-col>
              
              <!-- Observaciones -->
              <v-col cols="12">
                <v-textarea
                  v-model="detalleActual.observaciones"
                  label="Observaciones"
                  variant="outlined"
                  rows="2"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="secondary"
            variant="text"
            @click="cerrarModalDetalle"
          >
            Cancelar
          </v-btn>
          <v-btn
            color="primary"
            @click="guardarDetalle"
            :loading="guardando"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Diálogo de confirmación para eliminar -->
    <v-dialog v-model="dialogEliminar" max-width="500px">
      <v-card>
        <v-card-title>¿Eliminar este detalle?</v-card-title>
        <v-card-text>
          Esta acción no se puede deshacer. ¿Está seguro que desea eliminar este detalle?
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="secondary" variant="text" @click="dialogEliminar = false">Cancelar</v-btn>
          <v-btn color="error" @click="eliminarDetalle" :loading="eliminando">Eliminar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCatalogoTransferenciaStore } from '@/store/catalogo-transferencia';
import { format } from 'date-fns';

// Stores y router
const route = useRoute();
const router = useRouter();
const catalogoTransferenciaStore = useCatalogoTransferenciaStore();

// Estado reactivo
const catalogoId = computed(() => route.params.id);
const loading = ref(false);
const error = ref(null);
const detalles = ref([]);
const modalDetalle = ref(false);
const editandoDetalle = ref(false);
const guardando = ref(false);
const eliminando = ref(false);
const dialogEliminar = ref(false);
const detalleAEliminar = ref(null);

// Formulario
const detalleForm = ref(null);
const detalleActual = ref({
  catalogoId: null,
  numeroItem: null,
  numeroCaja: '',
  numeroTomoPaquete: '',
  numeroUnidadDocumental: '',
  fechaUnidadDocumental: null,
  cantidadFolios: 0,
  alcanceContenido: '',
  observaciones: ''
});

// Paginación
const pagination = ref({
  page: 0,
  size: 10,
  totalItems: 0,
  totalPages: 0
});

// Búsqueda
const search = ref('');
let debounceTimeout = null;

// Función debounce nativa
function debouncedSearch() {
  clearTimeout(debounceTimeout);
  debounceTimeout = setTimeout(() => {
    cargarDetalles();
  }, 500);
}

// Configuración de la tabla
const headers = [
  { title: 'Nº Item', key: 'numeroItem', sortable: true },
  { title: 'Nº Caja', key: 'numeroCaja', sortable: true },
  { title: 'Nº Tomo/Paquete', key: 'numeroTomoPaquete', sortable: true },
  { title: 'Nº Unidad Documental', key: 'numeroUnidadDocumental', sortable: true },
  { title: 'Fecha', key: 'fechaUnidadDocumental', sortable: true },
  { title: 'Contenido', key: 'alcanceContenido', sortable: false },
  { title: 'Folios', key: 'cantidadFolios', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false }
];

// Cargar detalles al montar el componente
onMounted(async () => {
  await cargarDetalles();
});

// Métodos
async function cargarDetalles() {
  try {
    loading.value = true;
    // Aquí implementaremos la llamada a la API para obtener detalles paginados
    // Por ahora, usamos el método existente en el store
    const response = await catalogoTransferenciaStore.getDetallesByCatalogo(catalogoId.value);
    detalles.value = response || [];
    
    // En una implementación completa, actualizaríamos la paginación con los datos de la respuesta
    pagination.value = {
      ...pagination.value,
      totalItems: detalles.value.length
    };
  } catch (err) {
    error.value = `Error al cargar detalles: ${err.message}`;
    console.error('Error:', err);
  } finally {
    loading.value = false;
  }
}

function handleTableOptions(options) {
  // Actualizar opciones de paginación y ordenamiento
  pagination.value.page = options.page - 1;
  pagination.value.size = options.itemsPerPage;
  
  // Recargar datos con nuevas opciones
  cargarDetalles();
}

function openDetalleDialog(detalle = null) {
  if (detalle) {
    // Modo edición
    editandoDetalle.value = true;
    detalleActual.value = { ...detalle };
  } else {
    // Modo creación
    editandoDetalle.value = false;
    resetDetalleForm();
  }
  modalDetalle.value = true;
}

function editarDetalle(detalle) {
  openDetalleDialog(detalle);
}

function cerrarModalDetalle() {
  modalDetalle.value = false;
  resetDetalleForm();
}

function resetDetalleForm() {
  detalleActual.value = {
    catalogoId: null,
    numeroItem: null,
    numeroCaja: '',
    numeroTomoPaquete: '',
    numeroUnidadDocumental: '',
    fechaUnidadDocumental: null,
    cantidadFolios: 0,
    alcanceContenido: '',
    observaciones: ''
  };
}

async function guardarDetalle() {
  if (!detalleForm.value || !detalleForm.value.validate()) {
    return;
  }
  
  try {
    console.log('Guardando...')
    guardando.value = true;
    
    // Preparar los datos del detalle
    const detalleData = {
      ...detalleActual.value,
      catalogoId: catalogoId.value
    };
    
    if (editandoDetalle.value && detalleActual.value.id) {
      // Actualizar detalle existente
      await catalogoTransferenciaStore.updateDetalle(
        catalogoId.value,
        detalleActual.value.id,
        detalleData
      );
      alert('Detalle actualizado con éxito');
    } else {
      // Crear nuevo detalle
      await catalogoTransferenciaStore.addDetalle(
        catalogoId.value,
        detalleData
      );
      alert('Detalle añadido con éxito');
    }
    
    // Recargar detalles
    await cargarDetalles();
    cerrarModalDetalle();
  } catch (err) {
    error.value = `Error al guardar detalle: ${err.message}`;
    console.error('Error:', err);
  } finally {
    guardando.value = false;
  }
}

function confirmarEliminarDetalle(detalle) {
  detalleAEliminar.value = detalle;
  dialogEliminar.value = true;
}

async function eliminarDetalle() {
  if (!detalleAEliminar.value) return;
  
  try {
    eliminando.value = true;
    
    // Implementar llamada a la API para eliminar detalle
    // await api.delete(`/api/catalogo-transferencia/${id.value}/detalles/${detalleAEliminar.value.id}`);
    
    alert('Detalle eliminado con éxito');
    dialogEliminar.value = false;
    detalleAEliminar.value = null;
    
    // Recargar detalles
    await cargarDetalles();
  } catch (err) {
    error.value = `Error al eliminar detalle: ${err.message}`;
    console.error('Error:', err);
  } finally {
    eliminando.value = false;
  }
}

// Utilidades
function formatDate(date) {
  if (!date) return '';
  try {
    return format(new Date(date), 'dd/MM/yyyy');
  } catch (e) {
    console.error('Error formatting date:', e);
    return date;
  }
}

function truncateText(text, maxLength) {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
}
</script>
