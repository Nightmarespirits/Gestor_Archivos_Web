<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Detalle de Registro de Transferencia</span>
        <div>
          <v-btn
            color="warning"
            prepend-icon="mdi-pencil"
            :to="{ name: 'registro-transferencia-edit', params: { id } }"
            class="mr-2"
            v-if="puedeManejarRegistro"
          >
            Editar
          </v-btn>
          
          <v-btn
            color="primary"
            prepend-icon="mdi-arrow-left"
            :to="{ name: 'registro-transferencia-list' }"
          >
            Volver
          </v-btn>
        </div>
      </v-card-title>
      
      <v-card-text>
        <v-alert
          v-if="error"
          type="error"
          title="Error"
          :text="error"
          class="mb-4"
        ></v-alert>
        
        <v-skeleton-loader
          v-if="loading && !registro"
          type="card-heading, list-item-three-line, list-item-three-line, list-item-three-line, list-item-three-line"
        ></v-skeleton-loader>
        
        <template v-else-if="registro">
          <div class="d-flex justify-space-between align-center mb-4">
            <h2 class="text-h5">{{ registro.codigo }} - {{ registro.numero }}</h2>
            <v-chip
              :color="getEstadoColor(registro.estado)"
              text-color="white"
            >
              {{ registro.estado }}
            </v-chip>
          </div>
          
          <v-divider class="mb-4"></v-divider>
          
          <v-row>
            <v-col cols="12" md="6">
              <v-list>
                <v-list-subheader>Información de Registro</v-list-subheader>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-calendar"></v-icon>
                  </template>
                  <v-list-item-title>Fecha</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(registro.fecha) }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-office-building"></v-icon>
                  </template>
                  <v-list-item-title>Dependencia de Origen</v-list-item-title>
                  <v-list-item-subtitle>{{ registro.dependenciaOrigen }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-office-building-outline"></v-icon>
                  </template>
                  <v-list-item-title>Dependencia de Destino</v-list-item-title>
                  <v-list-item-subtitle>{{ registro.dependenciaDestino }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-list>
                <v-list-subheader>Responsables y Detalles</v-list-subheader>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-account"></v-icon>
                  </template>
                  <v-list-item-title>Responsable</v-list-item-title>
                  <v-list-item-subtitle>{{ registro.responsable }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-file-document-multiple"></v-icon>
                  </template>
                  <v-list-item-title>Total de Documentos</v-list-item-title>
                  <v-list-item-subtitle>{{ registro.totalDocumentos || 0 }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-calendar-clock"></v-icon>
                  </template>
                  <v-list-item-title>Fecha Creación</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(registro.createdAt) }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
          
          <v-row v-if="registro.observaciones">
            <v-col cols="12">
              <v-card variant="outlined" color="background">
                <v-card-title class="text-subtitle-1">
                  <v-icon start icon="mdi-information-outline"></v-icon>
                  Observaciones
                </v-card-title>
                <v-card-text class="text-body-1">
                  {{ registro.observaciones }}
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
          
          <v-divider class="my-4"></v-divider>
          
          <!-- Botones de acción según el estado -->
          <div class="d-flex flex-wrap gap-3">
            <v-btn
              v-if="registro.estado === 'BORRADOR' && puedeManejarRegistro"
              color="success"
              prepend-icon="mdi-check"
              @click="cambiarEstado('FINALIZADO')"
            >
              Finalizar
            </v-btn>
            
            <v-btn
              v-if="registro.estado === 'FINALIZADO' && esAprobador"
              color="success"
              prepend-icon="mdi-check-all"
              @click="cambiarEstado('APROBADO')"
            >
              Aprobar
            </v-btn>
            
            <v-btn
              color="primary"
              prepend-icon="mdi-printer"
              @click="descargarRegistro('pdf')"
            >
              Imprimir PDF
            </v-btn>
            
            <v-btn
              color="success"
              prepend-icon="mdi-microsoft-excel"
              @click="descargarRegistro('excel')"
            >
              Descargar Excel
            </v-btn>
            
            <v-btn
              v-if="puedeManejarRegistro"
              color="error"
              prepend-icon="mdi-delete"
              @click="eliminarRegistro"
            >
              Eliminar
            </v-btn>
          </div>
        </template>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useRegistroTransferenciaStore } from '@/store/registro-transferencia';
import { useAuthStore } from '@/store/auth';

// Stores
const route = useRoute();
const router = useRouter();
const inventariosStore = useRegistroTransferenciaStore();
const authStore = useAuthStore();

// Estado reactivo
const id = computed(() => route.params.id);
const loading = computed(() => inventariosStore.isLoading());
const error = ref(null);
const registro = computed(() => inventariosStore.getCurrentRegistroTransferencia());

// Permisos
const esAprobador = computed(() => authStore.hasPermission('APROBAR_REGISTROS'));
const puedeManejarRegistro = computed(() => 
  authStore.hasPermission('EDITAR_REGISTROS') || 
  (registro.value && registro.value.createdBy === authStore.getUserId())
);

// Cargar el registro al montar el componente
onMounted(async () => {
  try {
    await inventariosStore.fetchRegistroTransferenciaById(id.value);
    if (!registro.value) {
      error.value = 'No se encontró el registro de transferencia solicitado';
    }
  } catch (err) {
    error.value = `Error al cargar el registro: ${err.message}`;
    console.error('Error:', err);
  }
});

// Métodos
async function cambiarEstado(nuevoEstado) {
  try {
    if (!registro.value) return;
    
    const registroActualizado = { ...registro.value, estado: nuevoEstado };
    await inventariosStore.updateRegistroTransferencia(id.value, registroActualizado);
    
    // Mostrar mensaje de éxito
    alert(`Estado actualizado a ${nuevoEstado}`);
  } catch (err) {
    error.value = `Error al cambiar estado: ${err.message}`;
    console.error('Error:', err);
  }
}

async function descargarRegistro(formato) {
  try {
    await inventariosStore.downloadInventario(id.value, formato);
  } catch (err) {
    error.value = `Error al descargar: ${err.message}`;
    console.error('Error:', err);
  }
}

async function eliminarRegistro() {
  if (!registro.value) return;
  
  if (confirm('¿Está seguro que desea eliminar este registro de transferencia?')) {
    try {
      const eliminado = await inventariosStore.deleteRegistroTransferencia(id.value);
      if (eliminado) {
        alert('Registro eliminado correctamente');
        router.push({ name: 'registro-transferencia-list' });
      }
    } catch (err) {
      error.value = `Error al eliminar: ${err.message}`;
      console.error('Error:', err);
    }
  }
}

function formatDate(dateString) {
  if (!dateString) return 'N/A';
  
  try {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('es-ES', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    }).format(date);
  } catch (e) {
    return dateString;
  }
}

// Método auxiliar
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
