<template>
  <PermissionWrapper :permissions="permissions" :roles="roles" :requireAllPermissions="requireAllPermissions">
    <v-btn
      :color="color"
      :variant="variant"
      :size="size"
      :icon="icon"
      :disabled="disabled"
      @click="handleClick"
      :class="buttonClass"
    >
      <v-icon v-if="prependIcon" :icon="prependIcon" class="mr-1"></v-icon>
      <slot>{{ text }}</slot>
    </v-btn>
  </PermissionWrapper>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import PermissionWrapper from './PermissionWrapper.vue';

const props = defineProps({
  // Permisos y roles (heredados de PermissionWrapper)
  permissions: {
    type: [String, Array],
    default: () => []
  },
  requireAllPermissions: {
    type: [String, Array],
    default: () => []
  },
  roles: {
    type: [String, Array],
    default: () => []
  },
  
  // Propiedades del botón
  text: {
    type: String,
    default: ''
  },
  color: {
    type: String,
    default: 'primary'
  },
  variant: {
    type: String,
    default: 'elevated'
  },
  size: {
    type: String,
    default: 'default'
  },
  icon: {
    type: String,
    default: null
  },
  prependIcon: {
    type: String,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  buttonClass: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['click']);

const handleClick = (event) => {
  emit('click', event);
};
</script>
