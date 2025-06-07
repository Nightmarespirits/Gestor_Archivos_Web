<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Detalle de Catálogo de Transferencia</span>
        <div>
          <v-btn
            color="warning"
            prepend-icon="mdi-pencil"
            :to="{ name: 'catalogo-transferencia-edit', params: { id } }"
            class="mr-2"
            v-if="puedeManejarCatalogo"
          >
            Editar
          </v-btn>
          
          <v-btn
            color="primary"
            prepend-icon="mdi-arrow-left"
            :to="{ name: 'catalogo-transferencia-list' }"
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
          v-if="loading && !catalogo"
          type="card-heading, list-item-three-line, list-item-three-line, list-item-three-line, list-item-three-line"
        ></v-skeleton-loader>
        
        <template v-else-if="catalogo">
          <div class="d-flex justify-space-between align-center mb-4">
            <h2 class="text-h5">{{ catalogo.codigo }} - {{ catalogo.titulo }}</h2>
            <v-chip
              :color="getEstadoColor(catalogo.estadoConservacion)"
              text-color="white"
            >
              {{ catalogo.estadoConservacion }}
            </v-chip>
          </div>
          
          <v-divider class="mb-4"></v-divider>
          
          <v-row>
            <v-col cols="12" md="6">
              <v-list>
                <v-list-subheader>Información de Catálogo</v-list-subheader>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-file-document"></v-icon>
                  </template>
                  <v-list-item-title>Número</v-list-item-title>
                  <v-list-item-subtitle>{{ catalogo.numero }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-calendar-range"></v-icon>
                  </template>
                  <v-list-item-title>Años Extremos</v-list-item-title>
                  <v-list-item-subtitle>{{ catalogo.anosExtremos || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-file-document-multiple"></v-icon>
                  </template>
                  <v-list-item-title>Número de Folios</v-list-item-title>
                  <v-list-item-subtitle>{{ catalogo.numeroFolios || '0' }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-list>
                <v-list-subheader>Características</v-list-subheader>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-file"></v-icon>
                  </template>
                  <v-list-item-title>Soporte</v-list-item-title>
                  <v-list-item-subtitle>{{ catalogo.soporte || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-shield"></v-icon>
                  </template>
                  <v-list-item-title>Estado de Conservación</v-list-item-title>
                  <v-list-item-subtitle>{{ catalogo.estadoConservacion }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="mdi-calendar-clock"></v-icon>
                  </template>
                  <v-list-item-title>Fecha Creación</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(catalogo.createdAt) }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
          
          <v-row v-if="catalogo.observaciones">
            <v-col cols="12">
              <v-card variant="outlined" color="background">
                <v-card-title class="text-subtitle-1">
                  <v-icon start icon="mdi-information-outline"></v-icon>
                  Observaciones
                </v-card-title>
                <v-card-text class="text-body-1">
                  {{ catalogo.observaciones }}
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
          
          <v-divider class="my-4"></v-divider>
          
          <!-- Botones de acción -->
          <div class="d-flex flex-wrap gap-3">
            <v-btn
              color="primary"
              prepend-icon="mdi-printer"
              @click="descargarCatalogo('pdf')"
            >
              Imprimir PDF
            </v-btn>
            
            <v-btn
              color="success"
              prepend-icon="mdi-microsoft-excel"
              @click="descargarCatalogo('excel')"
            >
              Descargar Excel
            </v-btn>
            
            <v-btn
              v-if="puedeManejarCatalogo"
              color="error"
              prepend-icon="mdi-delete"
              @click="eliminarCatalogo"
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
import { useCatalogoTransferenciaStore } from '@/store/catalogo-transferencia';  
import { useAuthStore } from '@/store/auth';

// Stores
const route = useRoute();
const router = useRouter();
const inventariosStore = useCatalogoTransferenciaStore();
const authStore = useAuthStore();

// Estado reactivo
const id = computed(() => route.params.id);
const loading = computed(() => inventariosStore.isLoading());
const error = ref(null);
const catalogo = computed(() => inventariosStore.getCurrentCatalogoTransferencia());

// Permisos
const puedeManejarCatalogo = computed(() => 
  authStore.hasPermission('EDITAR_CATALOGOS') || 
  (catalogo.value && catalogo.value.createdBy === authStore.getUserId())
);

// Cargar el catálogo al montar el componente
onMounted(async () => {
  try {
    await inventariosStore.fetchCatalogoTransferenciaById(id.value);
    if (!catalogo.value) {
      error.value = 'No se encontró el catálogo de transferencia solicitado';
    }
  } catch (err) {
    error.value = `Error al cargar el catálogo: ${err.message}`;
    console.error('Error:', err);
  }
});

// Métodos
async function descargarCatalogo(formato) {
  try {
    await inventariosStore.downloadCatalogoTransferencia(id.value, formato);
  } catch (err) {
    error.value = `Error al descargar: ${err.message}`;
    console.error('Error:', err);
  }
}

async function eliminarCatalogo() {
  if (!catalogo.value) return;
  
  if (confirm('¿Está seguro que desea eliminar este catálogo de transferencia?')) {
    try {
      const eliminado = await inventariosStore.deleteCatalogoTransferencia(id.value);
      if (eliminado) {
        alert('Catálogo eliminado correctamente');
        router.push({ name: 'catalogo-transferencia-list' });
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
