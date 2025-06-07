<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Crear Catálogo de Transferencia</span>
        <v-btn
          color="error"
          variant="outlined"
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'catalogo-transferencia-list' }"
          class="ml-2"
        >
          Cancelar
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <v-form ref="form" @submit.prevent="guardarCatalogo">
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
              Guardar Catálogo
            </v-btn>
            
            <v-btn
              color="secondary"
              variant="outlined"
              size="large"
              :loading="loading"
              :to="{ name: 'catalogo-transferencia-list' }"
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCatalogoTransferenciaStore } from '@/store/catalogo-transferencia';  
import { useAuthStore } from '@/store/auth';

// Stores
const router = useRouter();
const inventariosStore = useCatalogoTransferenciaStore();
const authStore = useAuthStore();

// Estado reactivo
const loading = computed(() => inventariosStore.isLoading());
const form = ref(null);

// Datos para los selectores
const soportes = ['PAPEL', 'DIGITAL', 'AUDIOVISUAL', 'MICROFILM', 'OTRO'];
const estadosConservacion = ['BUENO', 'REGULAR', 'MALO'];

// Datos del formulario
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

// Métodos
async function guardarCatalogo() {
  if (!form.value.validate()) {
    return;
  }

  try {
    const resultado = await inventariosStore.createCatalogoTransferencia(catalogo.value);
    
    if (resultado && resultado.id) {
      // Mostrar notificación de éxito
      alert('Catálogo de transferencia creado con éxito');
      
      // Redireccionar a la lista de catálogos
      router.push({ name: 'catalogo-transferencia-list' });
    }
  } catch (error) {
    console.error('Error al guardar el catálogo:', error);
    alert('Error al guardar el catálogo: ' + error.message);
  }
}
</script>
