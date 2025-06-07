<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Editar Catálogo de Transferencia</span>
        <v-btn
          color="error"
          variant="outlined"
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'catalogo-transferencia-detalle', params: { id } }"
          class="ml-2"
        >
          Cancelar
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
        
        <v-skeleton-loader
          v-if="loading && !catalogoExistente"
          type="card-heading, form"
        ></v-skeleton-loader>
        
        <v-form v-else ref="form" @submit.prevent="actualizarCatalogo">
          <v-row>
            <!-- Código -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.codigo"
                label="Código"
                :rules="[v => !!v || 'Código es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Número -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.numero"
                label="Número"
                :rules="[v => !!v || 'Número es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Título -->
            <v-col cols="12">
              <v-text-field
                v-model="catalogo.titulo"
                label="Título"
                :rules="[v => !!v || 'Título es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Años Extremos -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.anosExtremos"
                label="Años Extremos"
                hint="Ejemplo: 1995-2005"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Número de Folios -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.numeroFolios"
                label="Número de Folios"
                type="number"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Soporte -->
            <v-col cols="12" md="6">
              <v-select
                v-model="catalogo.soporte"
                :items="soportes"
                label="Soporte"
                variant="outlined"
              ></v-select>
            </v-col>
            
            <!-- Estado de Conservación -->
            <v-col cols="12" md="6">
              <v-select
                v-model="catalogo.estadoConservacion"
                :items="estadosConservacion"
                label="Estado de Conservación"
                :rules="[v => !!v || 'Estado de conservación es requerido']"
                required
                variant="outlined"
              ></v-select>
            </v-col>
            
            <!-- Observaciones -->
            <v-col cols="12">
              <v-textarea
                v-model="catalogo.observaciones"
                label="Observaciones"
                variant="outlined"
                rows="3"
              ></v-textarea>
            </v-col>
          </v-row>
          
          <div class="d-flex flex-column flex-sm-row gap-2 mt-5">
            <v-btn
              color="primary"
              type="submit"
              size="large"
              :loading="loading"
              prepend-icon="mdi-content-save"
            >
              Guardar Cambios
            </v-btn>
            
            <v-btn
              color="secondary"
              variant="outlined"
              size="large"
              :to="{ name: 'catalogo-transferencia-detalle', params: { id } }"
            >
              Cancelar
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
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
const form = ref(null);
const catalogoExistente = computed(() => inventariosStore.getCurrentCatalogoTransferencia());

// Estructura del catálogo para editar
const catalogo = ref({
  codigo: '',
  numero: '',
  titulo: '',
  anosExtremos: '',
  numeroFolios: 0,
  soporte: 'PAPEL',
  estadoConservacion: 'BUENO',
  observaciones: '',
});

// Datos para los selectores
const soportes = ['PAPEL', 'DIGITAL', 'AUDIOVISUAL', 'MICROFILM', 'OTRO'];
const estadosConservacion = ['BUENO', 'REGULAR', 'MALO'];

// Cargar el catálogo al montar el componente
onMounted(async () => {
  try {
    await inventariosStore.fetchCatalogoTransferenciaById(id.value);
    if (!catalogoExistente.value) {
      error.value = 'No se encontró el catálogo de transferencia solicitado';
    }
  } catch (err) {
    error.value = `Error al cargar el catálogo: ${err.message}`;
  }
});

// Observar cambios en el catálogo existente para actualizar el formulario
watch(catalogoExistente, (newVal) => {
  if (newVal) {
    // Copiar propiedades para evitar modificar directamente el store
    catalogo.value = { ...newVal };
  }
}, { immediate: true });

// Métodos
async function actualizarCatalogo() {
  if (!form.value.validate()) {
    return;
  }

  try {
    const resultado = await inventariosStore.updateCatalogoTransferencia(id.value, catalogo.value);
    
    if (resultado) {
      // Mostrar notificación de éxito
      alert('Catálogo de transferencia actualizado con éxito');
      
      // Redireccionar a la vista de detalle
      router.push({ name: 'catalogo-transferencia-detalle', params: { id: id.value } });
    }
  } catch (err) {
    error.value = `Error al actualizar el catálogo: ${err.message}`;
    console.error('Error:', err);
  }
}
</script>
