<template>
  <v-avatar :color="backgroundColor" :size="size" rounded>
    <span class="text-h6 font-weight-medium">{{ initials }}</span>
  </v-avatar>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  fullName: {
    type: String,
    default: 'Usuario'
  },
  size: {
    type: [Number, String],
    default: 40
  }
});

// Obtener las iniciales del nombre completo
const initials = computed(() => {
  if (!props.fullName || props.fullName === 'Usuario') return 'U';
  
  // Dividir el nombre en palabras
  const nameParts = props.fullName.split(' ');
  
  // Si solo hay una palabra, tomar las primeras dos letras
  if (nameParts.length === 1) {
    return nameParts[0].substring(0, 2).toUpperCase();
  }
  
  // De lo contrario, tomar la primera letra de cada palabra (hasta 2)
  return nameParts
    .filter(part => part.length > 0)
    .slice(0, 2)
    .map(part => part[0].toUpperCase())
    .join('');
});

// Generar un color de fondo basado en el nombre
const backgroundColor = computed(() => {
  // Una lista de colores de Vuetify para usar
  const colors = [
    'primary',
    'secondary',
    'success',
    'info',
    'warning',
    'error',
    'purple',
    'indigo',
    'blue-grey',
    'teal'
  ];
  
  // Usar el nombre para seleccionar un color consistente
  if (!props.fullName || props.fullName === 'Usuario') {
    return colors[0]; // Color por defecto para 'Usuario'
  }
  
  // Obtener un índice basado en la suma de los códigos de caracteres
  let sum = 0;
  for (let i = 0; i < props.fullName.length; i++) {
    sum += props.fullName.charCodeAt(i);
  }
  
  // Usar el módulo para obtener un índice dentro del rango de colores
  const colorIndex = sum % colors.length;
  return colors[colorIndex];
});
</script>

<style scoped>
.v-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  text-transform: uppercase;
  color: white;
}
</style>