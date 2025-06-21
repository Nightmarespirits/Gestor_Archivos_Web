<template>
  <v-container fluid>
    <v-card v-if="loading" class="pa-5 text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <div class="text-body-1 mt-4">Cargando inventario...</div>
    </v-card>
    
    <template v-else-if="currentInventario">
      <!-- Cabecera del inventario -->
      <v-card class="mb-4">
        <v-card-title class="d-flex justify-space-between align-center">
          <div>
            <span class="text-h5">{{ currentInventario.titulo }}</span>
            <v-chip
              :color="getInventarioStateColor(currentInventario.estado)"
              text-color="white"
              class="ml-2"
              size="small"
            >
              {{ currentInventario.estado }}
            </v-chip>
          </div>
          <v-btn 
            color="secondary" 
            prepend-icon="mdi-arrow-left"
            :to="{ name: 'inventario-list' }"
          >
            Volver
          </v-btn>
        </v-card-title>

        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <v-list>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-office-building"></v-icon>
                  </template>
                  <v-list-item-title>Dependencia</v-list-item-title>
                  <v-list-item-subtitle>{{ currentInventario.dependencia }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-identifier"></v-icon>
                  </template>
                  <v-list-item-title>Número de Identificación</v-list-item-title>
                  <v-list-item-subtitle>{{ currentInventario.numeroIdentificacion || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-account"></v-icon>
                  </template>
                  <v-list-item-title>Responsable</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ currentInventario.responsable }} 
                    <span v-if="currentInventario.cargoResponsable">({{ currentInventario.cargoResponsable }})</span>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-list>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-calendar-range"></v-icon>
                  </template>
                  <v-list-item-title>Período</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ formatDate(currentInventario.fechaInicio) }} - {{ formatDate(currentInventario.fechaFin) }}
                  </v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-calendar"></v-icon>
                  </template>
                  <v-list-item-title>Año</v-list-item-title>
                  <v-list-item-subtitle>{{ currentInventario.anio }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-calendar-clock"></v-icon>
                  </template>
                  <v-list-item-title>Fecha de creación</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDateTime(currentInventario.fechaCreacion) }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
          
          <v-row v-if="currentInventario.observaciones">
            <v-col cols="12">
              <v-list>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-note-text"></v-icon>
                  </template>
                  <v-list-item-title>Observaciones</v-list-item-title>
                  <v-list-item-subtitle>{{ currentInventario.observaciones }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
          
          <v-row class="mt-2">
            <v-col cols="12" class="d-flex justify-end">
              <v-btn 
                v-if="canEditInventario"
                color="warning" 
                class="mx-2"
                :to="{ name: 'inventario-edit', params: { id: currentInventario.id } }"
                prepend-icon="mdi-pencil"
              >
                Editar
              </v-btn>
              
              <v-menu>
                <template v-slot:activator="{ props }">
                  <v-btn
                    color="primary"
                    prepend-icon="mdi-download"
                    v-bind="props"
                    class="mx-2"
                  >
                    Descargar
                  </v-btn>
                </template>
                <v-list>
                  <v-list-item
                    @click="downloadInventario('excel')"
                    prepend-icon="mdi-file-excel"
                    title="Descargar Excel"
                  ></v-list-item>
                  <v-list-item
                    @click="downloadInventario('pdf')"
                    prepend-icon="mdi-file-pdf-box"
                    title="Descargar PDF"
                  ></v-list-item>
                </v-list>
              </v-menu>
              
              <v-menu v-if="canChangeState">
                <template v-slot:activator="{ props }">
                  <v-btn
                    color="success"
                    prepend-icon="mdi-check-circle"
                    v-bind="props"
                    class="mx-2"
                  >
                    Cambiar Estado
                  </v-btn>
                </template>
                <v-list>
                  <v-list-item
                    v-if="currentInventario.estado === 'BORRADOR'"
                    @click="changeState('FINALIZADO')"
                    prepend-icon="mdi-check-circle"
                    title="Finalizar inventario"
                  ></v-list-item>
                  <v-list-item
                    v-if="currentInventario.estado === 'FINALIZADO'"
                    @click="changeState('APROBADO')"
                    prepend-icon="mdi-check-circle-outline"
                    title="Aprobar inventario"
                  ></v-list-item>
                </v-list>
              </v-menu>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
      
      <!-- Detalles del inventario -->
      <v-card>
        <v-card-title>
          <span class="text-h6">Detalles del Inventario</span>
        </v-card-title>
        
        <v-card-text>
          <v-data-table
            :headers="headers"
            :items="currentInventario.detalles || []"
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
            
            <!-- Sin resultados -->
            <template v-slot:no-data>
              <div class="text-center pa-5">
                <v-icon size="large" icon="mdi-folder-outline" color="grey"></v-icon>
                <div class="text-body-1 mt-2">Este inventario aún no tiene elementos</div>
                <v-btn
                  v-if="canEditInventario"
                  color="primary"
                  class="mt-4"
                  :to="{ name: 'inventario-edit', params: { id: currentInventario.id } }"
                >
                  Agregar elementos
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
      <div class="text-body-1 mt-2">El inventario solicitado no existe o no tienes permiso para verlo.</div>
      <v-btn
        color="primary"
        class="mt-4"
        :to="{ name: 'inventario-list' }"
      >
        Volver al listado
      </v-btn>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useInventariosStore } from '@/store/inventarios';
import { useAuthStore } from '@/store/auth';

// Stores
const inventariosStore = useInventariosStore();
const authStore = useAuthStore();
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Router
const route = useRoute();
const router = useRouter();

// Estado
const loading = ref(false);
const inventarioId = computed(() => Number(route.params.id));
const currentInventario = computed(() => inventariosStore.getCurrentInventario());

// Permisos
const canEditInventario = computed(() => {
  const inv = currentInventario.value;
  if (!inv) return false;
  
  return authStore.hasPermission('inventarios:edit') && 
         (inv.estado === 'BORRADOR' || authStore.hasPermission('inventarios:edit:any'));
});

const canChangeState = computed(() => {
  const inv = currentInventario.value;
  if (!inv) return false;
  
  if (inv.estado === 'BORRADOR' && authStore.hasPermission('inventarios:finalize')) {
    return true;
  }
  if (inv.estado === 'FINALIZADO' && authStore.hasPermission('inventarios:approve')) {
    return true;
  }
  return false;
});

// Headers para la tabla de detalles
const headers = [
  { title: 'Código', key: 'codigo', sortable: true },
  { title: 'Serie Documental', key: 'serieDocumental', sortable: true },
  { title: 'Título', key: 'titulo', sortable: true },
  { title: 'Años Extremos', key: 'anosExtremos', sortable: true },
  { title: 'Ubicación', key: 'ubicacion', sortable: true },
  { title: 'Conservación', key: 'estadoConservacion', sortable: true },
];

// Métodos auxiliares
function formatDate(dateString) {
  if (!dateString) return 'No especificado';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date);
}

function formatDateTime(dateString) {
  if (!dateString) return 'No especificado';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

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

function getInventarioStateColor(state) {
  return inventariosStore.getInventarioStateColor(state);
}

// Métodos de acción
async function downloadInventario(format) {
  try {
    loading.value = true;
    const success = await inventariosStore.downloadInventario(inventarioId.value, format);
    if (!success) {
      showSnackbar(`Error al descargar el inventario en formato ${format}`, 'error');
    }
  } catch (error) {
    showSnackbar('Error al descargar: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

async function changeState(newState) {
  try {
    loading.value = true;
    await inventariosStore.changeInventarioState(inventarioId.value, newState);
    showSnackbar(`Estado del inventario actualizado a ${newState}`, 'success');
    // Recargar el inventario para ver los cambios
    await loadInventario();
  } catch (error) {
    showSnackbar('Error al cambiar el estado: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

// Cargar el inventario
async function loadInventario() {
  try {
    loading.value = true;
    const inventario = await inventariosStore.fetchInventarioById(inventarioId.value);
    
    if (!inventario) {
      showSnackbar('Inventario no encontrado', 'error');
    }
  } catch (error) {
    showSnackbar('Error al cargar el inventario: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
}

// Mostrar snackbar
function showSnackbar(text, color = 'success', timeout = 3000) {
  snackbar.value = { show: true, text, color, timeout };
}

// Cargar datos al montar el componente
onMounted(async () => {
  if (!authStore.hasPermission('inventarios:view')) {
    showSnackbar('No tienes permiso para ver inventarios', 'error');
    router.push({ name: 'inventario-list' });
    return;
  }
  
  await loadInventario();
});
</script>
