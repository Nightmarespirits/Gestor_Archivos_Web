<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between align-center">
        <span>Registro de Transferencia (Anexo N° 05)</span>
        <v-btn 
          color="primary" 
          prepend-icon="mdi-plus"
          :to="{ name: 'registro-transferencia-create' }"
          class="ml-2"
        >
          Nuevo Registro
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <!-- Filtros -->
        <v-row>
          <v-col cols="12" sm="6" md="4" lg="3">
            <v-text-field
              v-model="filters.codigo"
              label="Buscar por código"
              prepend-icon="mdi-magnify"
              hide-details
              variant="outlined"
              density="compact"
              @keyup.enter="buscarRegistros"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6" md="4" lg="3">
            <v-select
              v-model="filters.estado"
              :items="['TODOS', 'BORRADOR', 'FINALIZADO', 'APROBADO']"
              label="Estado"
              prepend-icon="mdi-filter-variant"
              hide-details
              variant="outlined"
              density="compact"
              @update:model-value="buscarRegistros"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="6" md="4" lg="3">
            <v-btn
              color="primary"
              prepend-icon="mdi-refresh"
              @click="buscarRegistros"
              class="mt-1"
            >
              Actualizar
            </v-btn>
          </v-col>
        </v-row>
        
        <!-- Tabla con registros de transferencia -->
        <v-data-table
          :headers="headers"
          :items="registrosTransferencia"
          :loading="loading"
          :items-per-page="10"
          item-value="id"
          class="elevation-1 mt-4"
          hover
        >
          <!-- Estado -->
          <template v-slot:item.estado="{ item }">
            <v-chip
              :color="getEstadoColor(item.estado)"
              text-color="white"
              size="small"
            >
              {{ item.estado }}
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
                  @click="editarRegistro(item.id)"
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
                  @click="descargarRegistro(item.id, 'pdf')"
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
                  @click="eliminarRegistro(item.id)"
                >
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
          </template>
          
          <!-- Sin resultados -->
          <template v-slot:no-data>
            <div class="text-center pa-5">
              <v-icon size="large" icon="mdi-file-document-outline" color="grey"></v-icon>
              <div class="text-body-1 mt-2">No se encontraron registros de transferencia</div>
              <div class="text-body-2 mt-1 grey--text">
                Los registros de transferencia aparecerán aquí una vez creados
              </div>
            </div>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/store/auth';
import { useRegistroTransferenciaStore } from '@/store/registro-transferencia';
import { useRouter } from 'vue-router';

// Stores
const authStore = useAuthStore();
const inventariosStore = useRegistroTransferenciaStore();
const router = useRouter();

// Estado reactivo
const loading = computed(() => inventariosStore.isLoading());
const registrosTransferencia = computed(() => inventariosStore.getRegistrosTransferencia());
const filters = ref({
  codigo: '',
  estado: 'TODOS',
  fechaDesde: '',
  fechaHasta: ''
});

// Headers para la tabla según Anexo N° 05
const headers = [
  { title: 'N°', key: 'numero', sortable: true, align: 'center', width: '80px' },
  { title: 'Código', key: 'codigo', sortable: true },
  { title: 'Fecha', key: 'fecha', sortable: true },
  { title: 'Dependencia Origen', key: 'dependenciaOrigen', sortable: true },
  { title: 'Dependencia Destino', key: 'dependenciaDestino', sortable: true },
  { title: 'Total Documentos', key: 'totalDocumentos', sortable: true, align: 'center' },
  { title: 'Responsable', key: 'responsable', sortable: true },
  { title: 'Estado', key: 'estado', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'center' }
];

// Cargar registros al montar el componente
onMounted(() => {
  cargarRegistrosTransferencia();
});

// Métodos
async function cargarRegistrosTransferencia() {
  await inventariosStore.fetchRegistrosTransferencia();
}

async function buscarRegistros() {
  // Aquí podríamos implementar filtrado más avanzado
  // Por ahora, solo recargamos los registros
  await cargarRegistrosTransferencia();
}

function verDetalle(id) {
  router.push({ name: 'registro-transferencia-detalle', params: { id } });
}

function editarRegistro(id) {
  router.push({ name: 'registro-transferencia-edit', params: { id } });
}

async function eliminarRegistro(id) {
  if (confirm('¿Está seguro que desea eliminar este registro de transferencia?')) {
    const eliminado = await inventariosStore.deleteRegistroTransferencia(id);
    if (eliminado) {
      // Recargar la lista después de eliminar
      await cargarRegistrosTransferencia();
    }
  }
}

async function descargarRegistro(id, formato = 'pdf') {
  await inventariosStore.downloadInventario(id, formato);
}

// Métodos auxiliares
function getEstadoColor(estado) {
  switch (estado) {
    case 'BORRADOR':
      return 'grey';
    case 'FINALIZADO':
      return 'primary';
    case 'APROBADO':
      return 'success';
    default:
      return 'grey';
  }
}
</script>
