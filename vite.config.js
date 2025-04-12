import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';
import VueRouter from 'unplugin-vue-router/vite';
import Layouts from 'vite-plugin-vue-layouts';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { VueRouterAutoImports } from 'unplugin-vue-router';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    // Docs: https://github.com/posva/unplugin-vue-router
    VueRouter({
      routesFolder: 'src/pages', // Directorio donde están tus páginas
      extensions: ['.vue'],
      dts: 'src/typed-router.d.ts', // Opcional: para type safety
    }),

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
        VueRouterAutoImports, // Importaciones automáticas para vue-router
        {
          'vue-router/auto': ['useLink'],
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
  define: { 'process.env': {} },
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
