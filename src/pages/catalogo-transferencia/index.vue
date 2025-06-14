<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between align-center">
        <span>Catálogo de Transferencia (Anexo N° 06)</span>
        <v-btn 
          color="primary" 
          prepend-icon="mdi-plus"
          :to="{ name: 'catalogo-transferencia-create' }"
          class="ml-2"
        >
          Nuevo Catálogo
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <!-- Filtros -->
        <v-row>
          <v-col cols="12" sm="6" md="4" lg="3">
            <v-text-field
              v-model="filters.titulo"
              label="Buscar por título"
              prepend-icon="mdi-magnify"
              hide-details
              variant="outlined"
              density="compact"
              @keyup.enter="buscarCatalogos"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6" md="4" lg="3">
            <v-text-field
              v-model="filters.codigo"
              label="Buscar por código"
              prepend-icon="mdi-barcode"
              hide-details
              variant="outlined"
              density="compact"
              @keyup.enter="buscarCatalogos"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6" md="4" lg="3">
            <v-select
              v-model="filters.estadoConservacion"
              :items="['TODOS', 'BUENO', 'REGULAR', 'MALO']"
              label="Estado de Conservación"
              prepend-icon="mdi-filter-variant"
              hide-details
              variant="outlined"
              density="compact"
              @update:model-value="buscarCatalogos"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="6" md="4" lg="3">
            <v-btn
              color="primary"
              prepend-icon="mdi-refresh"
              @click="buscarCatalogos"
              class="mt-1"
            >
              Actualizar
            </v-btn>
          </v-col>
        </v-row>
        
        <!-- Tabla con catálogos de transferencia -->
        <v-data-table
          :headers="headers"
          :items="catalogosTransferencia"
          :loading="loading"
          :items-per-page="10"
          item-value="id"
          class="elevation-1 mt-4"
          hover
        >
          <!-- Estado de conservación -->
          <template v-slot:item.estadoConservacion="{ item }">
            <v-chip
              :color="getEstadoConservacionColor(item.estadoConservacion)"
              text-color="white"
              size="small"
            >
              {{ item.estadoConservacion }}
            </v-chip>
          </template>
          
          <!-- Acciones -->
          <template v-slot:item.actions="{ item }">
            <v-tooltip text="Ver detalles">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  color="info"
                  v-bind="props"
                  @click="verDetalle(item.id)"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
            
            <v-tooltip text="Editar">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  color="warning"
                  v-bind="props"
                  class="ml-2"
                  @click="editarCatalogo(item.id)"
                >
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
            
            <v-tooltip text="Descargar">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  color="success"
                  v-bind="props"
                  class="ml-2"
                  @click="descargarCatalogo(item.id, 'pdf')"
                >
                  <v-icon>mdi-download</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
            
            <v-tooltip text="Eliminar">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  size="small"
                  color="error"
                  v-bind="props"
                  class="ml-2"
                  @click="eliminarCatalogo(item.id)"
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
              <div class="text-body-1 mt-2">No se encontraron catálogos de transferencia</div>
              <div class="text-body-2 mt-1 grey--text">
                Los catálogos de transferencia aparecerán aquí una vez creados
              </div>
            </div>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/store/auth';
import { useCatalogoTransferenciaStore } from '@/store/catalogo-transferencia';  
import { useRouter } from 'vue-router';

// Stores
const authStore = useAuthStore();
const inventariosStore = useCatalogoTransferenciaStore();
const router = useRouter();

// Estado reactivo
const loading = computed(() => inventariosStore.isLoading());
const catalogosTransferencia = computed(() => inventariosStore.getCatalogosTransferencia());
const filters = ref({
  codigo: '',
  titulo: '',
  estadoConservacion: 'TODOS'
});

// Headers para la tabla según Anexo N° 06
const headers = [
  { title: 'N°', key: 'id', sortable: true, align: 'center', width: '80px' },
  { title: 'Código', key: 'codigoReferencia', sortable: true },
  { title: 'Título', key: 'serieDocumental', sortable: true },
  { title: 'Lugar y Fecha', key: 'lugarFechaElaboracion', sortable: true },
  { title: 'Volumen (M)', key: 'volumenMetrosLineales', sortable: true, align: 'center' },
  { title: 'Soporte', key: 'soporte', sortable: true },
  { title: 'Elaborado por', key: 'inventarioElaboradoPor', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'center' }
];

// Cargar catálogos al montar el componente
onMounted(() => {
  cargarCatalogos();
});

// Métodos
async function cargarCatalogos() {
  await inventariosStore.fetchCatalogosTransferencia();
}

async function buscarCatalogos() {
  // Implementar filtrado avanzado
  // Por ahora, solo recargamos los catálogos TODO
  await cargarCatalogos();
}

function verDetalle(id) {
  router.push({ name: 'catalogo-transferencia-detalle', params: { id } });
}

function editarCatalogo(id) {
  router.push({ name: 'catalogo-transferencia-edit', params: { id } });
}

async function eliminarCatalogo(id) {
  if (confirm('¿Está seguro que desea eliminar este catálogo de transferencia?')) {
    const eliminado = await inventariosStore.deleteCatalogoTransferencia(id);
    if (eliminado) {
      // Recargar la lista después de eliminar
      await cargarCatalogos();
    }
  }
}

async function descargarCatalogo(id, formato = 'pdf') {
  await inventariosStore.downloadInventario(id, formato);
}

// Métodos auxiliares
function getEstadoColor(estado) {
  switch (estado) {
    case 'BUENO':
      return 'success';
    case 'REGULAR':
      return 'warning';
    case 'MALO':
      return 'error';
    default:
      return 'grey';
  }
}

function getEstadoConservacionColor(estadoConservacion) {
  switch (estadoConservacion) {
    case 'BUENO':
      return 'success';
    case 'REGULAR':
      return 'warning';
    case 'MALO':
      return 'error';
    default:
      return 'grey';
  }
}
</script>
