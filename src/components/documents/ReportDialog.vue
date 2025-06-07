<template>
  <v-dialog v-model="dialog" max-width="600px">
    <v-card>
      <v-card-title class="text-h5">Generar Reporte Excel</v-card-title>
      <v-card-text>
        <v-form ref="form">
          <v-text-field
            v-model="reportTitle"
            label="Título del reporte"
            variant="outlined"
            density="compact"
            class="mb-3"
          ></v-text-field>

          <v-switch
            v-model="useDateFilter"
            label="Filtrar por rango de fechas"
            color="primary"
            hide-details
            class="mb-2"
          ></v-switch>
          
          <v-row v-if="useDateFilter">
            <v-col cols="12" md="6">
              <v-menu
                v-model="startDateMenu"
                :close-on-content-click="false"
                transition="scale-transition"
                max-width="290px"
                min-width="auto"
              >
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-model="dateRange.start"
                    label="Fecha inicial"
                    variant="outlined"
                    density="compact"
                    prepend-inner-icon="mdi-calendar"
                    readonly
                    v-bind="props"
                  ></v-text-field>
                </template>
                <v-date-picker
                  v-model="dateRange.start"
                  @update:model-value="startDateMenu = false"
                ></v-date-picker>
              </v-menu>
            </v-col>
            <v-col cols="12" md="6">
              <v-menu
                v-model="endDateMenu"
                :close-on-content-click="false"
                transition="scale-transition"
                max-width="290px"
                min-width="auto"
              >
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-model="dateRange.end"
                    label="Fecha final"
                    variant="outlined"
                    density="compact"
                    prepend-inner-icon="mdi-calendar"
                    readonly
                    v-bind="props"
                  ></v-text-field>
                </template>
                <v-date-picker
                  v-model="dateRange.end"
                  @update:model-value="endDateMenu = false"
                ></v-date-picker>
              </v-menu>
            </v-col>
          </v-row>

          <v-divider class="my-4"></v-divider>
          <p class="text-subtitle-1">Campos a incluir:</p>
          <v-row>
            <v-col cols="6" v-for="field in availableFields" :key="field.key">
              <v-checkbox
                v-model="selectedFields"
                :value="field.key"
                :label="field.label"
                density="compact"
              ></v-checkbox>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="error" variant="text" @click="closeDialog">Cancelar</v-btn>
        <v-btn 
          color="primary" 
          variant="elevated" 
          @click="generateReport" 
          :loading="loading"
          :disabled="loading || selectedFields.length === 0"
        >
          Generar Reporte
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, reactive, watch, defineEmits, defineProps } from 'vue';
import { format } from 'date-fns';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close', 'generate']);

const dialog = ref(false);
const loading = ref(false);
const form = ref(null);
const startDateMenu = ref(false);
const endDateMenu = ref(false);
const reportTitle = ref('Reporte de Documentos');
const useDateFilter = ref(false); // Por defecto, no filtrar por fechas

// Configuración de fechas
const today = new Date();
const dateRange = reactive({
  start: format(new Date(today.getFullYear(), today.getMonth(), 1), 'yyyy-MM-dd'),
  end: format(today, 'yyyy-MM-dd')
});

// Campos disponibles para el reporte
const availableFields = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: 'Título' },
  { key: 'documentType', label: 'Tipo de Documento' },
  { key: 'description', label: 'Descripción' },
  { key: 'versionNumber', label: 'Versión' },
  { key: 'uploadDate', label: 'Fecha de Subida' },
  { key: 'authorName', label: 'Autor' },
  { key: 'security', label: 'Nivel de Acceso' }
];

// Campos seleccionados por defecto
const selectedFields = ref([
  'id', 'title', 'documentType', 'versionNumber', 'uploadDate', 'authorName', 'security'
]);

// Observar cambios en la propiedad show
watch(() => props.show, (newVal) => {
  dialog.value = newVal;
});

// Observar cambios en el diálogo
watch(dialog, (newVal) => {
  if (!newVal) {
    emit('close');
  }
});

// Cerrar el diálogo
const closeDialog = () => {
  dialog.value = false;
};

// Generar reporte
const generateReport = async () => {
  loading.value = true;
  
  try {
    emit('generate', {
      title: reportTitle.value,
      includeFields: selectedFields.value,
      // Solo incluir el rango de fechas si se ha activado el filtro
      dateRange: useDateFilter.value ? {
        start: dateRange.start,
        end: dateRange.end
      } : null
    });
    
    closeDialog();
  } catch (error) {
    console.error('Error al generar el reporte:', error);
  } finally {
    loading.value = false;
  }
};
</script>
