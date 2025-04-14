import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';
import Layouts from 'vite-plugin-vue-layouts';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),

    // Docs: https://github.com/JohnCampionJr/vite-plugin-vue-layouts
    Layouts({
      layoutsDirs: 'src/layouts', // Directorio de tus layouts
      defaultLayout: 'default', // Layout por defecto si no se especifica
    }),

    // Docs: https://github.com/vuetifyjs/vuetify-loader/tree/master/packages/vite-plugin#readme
    vuetify({
      autoImport: true,
      styles: {
        configFile: 'src/styles/settings.scss',
      }
    }),

    // Docs: https://github.com/unplugin/unplugin-vue-components
    Components({
      dirs: ['src/components'], // Directorio de componentes auto-importables
      dts: 'src/components.d.ts', // Opcional: para type safety
      // Vuetify Resolver si es necesario, aunque vite-plugin-vuetify ya lo maneja
    }),

    // Docs: https://github.com/unplugin/unplugin-auto-import
    AutoImport({
      imports: [
        'vue',
        {
          'vue-router': ['useRouter', 'useRoute', 'useLink'],
        },
        'pinia', // Importación automática para Pinia
      ],
      dts: 'src/auto-imports.d.ts', // Opcional: para type safety
      dirs: [
        'src/composables',
        'src/store',
      ],
      vueTemplate: true,
    }),
  ],
  define: { 
    'process.env': {},
    // Añadir cualquier variable de entorno que necesites aquí
    '__VUE_OPTIONS_API__': true,
    '__VUE_PROD_DEVTOOLS__': false,
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
    extensions: [
      '.js',
      '.json',
      '.jsx',
      '.mjs',
      '.ts',
      '.tsx',
      '.vue',
    ],
  },
  server: {
    port: 3000, // Asegúrate de que el puerto sea 3000
  },
});
