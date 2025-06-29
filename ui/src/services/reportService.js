import ExcelJS from 'exceljs';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

/**
 * Servicio para generar reportes en formato Excel
 */
export const reportService = {
  /**
   * Genera un reporte Excel de documentos
   * @param {Array} documents - Lista de documentos para el reporte
   * @param {Object} options - Opciones de configuración del reporte
   * @returns {Promise<Blob>} - Blob del archivo Excel generado
   */
  async generateDocumentsExcelReport(documents, options = {}) {
    // Asegurarnos de que estamos trabajando con un array normal (no reactivo)
    try {
      if (documents && !Array.isArray(documents)) {
        console.error('Error: documents no es un array:', documents);
        // Intentar convertir un objeto reactivo a un array normal
        if (documents && typeof documents === 'object') {
          // Si tiene Symbol.iterator o es iterable
          if (documents[Symbol.iterator]) {
            documents = Array.from(documents);
            console.log('Convertido de iterable a array:', documents);
          } else {
            // Si es un objeto con propiedades numéricas como índices
            const keys = Object.keys(documents).filter(k => !isNaN(parseInt(k)));
            if (keys.length > 0) {
              documents = keys.map(k => documents[k]);
              console.log('Convertido de objeto con índices a array:', documents);
            }
          }
        }
      }
      
      // Última verificación - asegurar que sea un array
      if (!Array.isArray(documents)) {
        documents = [];
        console.warn('No se pudo convertir a un array. Usando array vacío.');
      }
    } catch (err) {
      console.error('Error al procesar documents:', err);
      documents = [];
    }
    const {
      title = 'Reporte de Documentos',
      includeFields = ['id', 'title', 'documentType', 'versionNumber', 'uploadDate', 'authorName', 'security'],
      dateRange = null,
    } = options;

    // Preparar los documentos para filtrar
    console.log('Antes de filtrar, documentos:', documents);
    let filteredDocuments = [...documents];
    
    // Analizar estructura de documentos para debug
    if (documents.length > 0) {
      console.log('Estructura del primer documento:', JSON.stringify(documents[0]));
      console.log('Propiedades del primer documento:', Object.keys(documents[0]));
      console.log('Valor de uploadDate:', documents[0].uploadDate);
      if (documents[0].uploadDate) {
        console.log('Fecha parseada:', new Date(documents[0].uploadDate));
      }
    }
    
    // Filtrar por fechas solo si dateRange no es null y tiene valores
    if (dateRange && dateRange.start && dateRange.end) {
      console.log(`Filtrando por fechas: ${dateRange.start} a ${dateRange.end}`);
      try {
        const startDate = new Date(dateRange.start);
        const endDate = new Date(dateRange.end);
        endDate.setHours(23, 59, 59, 999); // Incluir todo el día final
        
        console.log(`Fechas de filtro: ${startDate.toISOString()} a ${endDate.toISOString()}`);
        
        // Verificar si las fechas son válidas
        if (!isNaN(startDate.getTime()) && !isNaN(endDate.getTime())) {
          filteredDocuments = documents.filter(doc => {
            // Si no hay fecha de subida, incluir el documento
            if (!doc.uploadDate) {
              console.log(`Documento ${doc.id} sin fecha, se incluye por defecto`);
              return true;
            }
            
            try {
              const uploadDate = new Date(doc.uploadDate);
              console.log(`Documento ${doc.id}, fecha: ${uploadDate.toISOString()}, incluido: ${uploadDate >= startDate && uploadDate <= endDate}`);
              return uploadDate >= startDate && uploadDate <= endDate;
            } catch (err) {
              console.error(`Error al filtrar documento ${doc.id} por fecha:`, err);
              return true; // Si hay error, incluir el documento
            }
          });
        } else {
          console.warn('Fechas de filtrado inválidas, mostrando todos los documentos');
        }
      } catch (err) {
        console.error('Error al procesar filtro de fechas:', err);
      }
    } else {
      console.log('Usando todos los documentos sin filtrar por fecha');
    }
    
    console.log('Después de filtrar, documentos:', filteredDocuments);

    // Crear un nuevo libro de Excel
    const workbook = new ExcelJS.Workbook();
    workbook.creator = 'User';
    workbook.created = new Date();
    
    // Agregar una hoja de trabajo
    const worksheet = workbook.addWorksheet('Documentos');
    
    // Definir los encabezados según los campos incluidos
    const headers = [];
    
    if (includeFields.includes('id')) headers.push({ header: 'ID', key: 'id', width: 10 });
    if (includeFields.includes('title')) headers.push({ header: 'Título', key: 'title', width: 40 });
    if (includeFields.includes('documentType')) headers.push({ header: 'Tipo', key: 'documentType', width: 20 });
    if (includeFields.includes('description')) headers.push({ header: 'Descripción', key: 'description', width: 50 });
    if (includeFields.includes('versionNumber')) headers.push({ header: 'Versión', key: 'versionNumber', width: 10 });
    if (includeFields.includes('uploadDate')) headers.push({ header: 'Fecha de Subida', key: 'uploadDate', width: 20 });
    if (includeFields.includes('authorName')) headers.push({ header: 'Autor', key: 'authorName', width: 25 });
    if (includeFields.includes('security')) headers.push({ header: 'Nivel de Acceso', key: 'security', width: 15 });
    
    worksheet.columns = headers;
    
    // Agregar título del reporte
    worksheet.mergeCells('A1:H1');
    const titleRow = worksheet.getRow(1);
    titleRow.getCell(1).value = title;
    titleRow.getCell(1).font = { size: 16, bold: true };
    titleRow.getCell(1).alignment = { horizontal: 'center' };
    titleRow.height = 30;
    
    // Agregar fecha de generación
    worksheet.mergeCells('A2:H2');
    const dateRow = worksheet.getRow(2);
    dateRow.getCell(1).value = `Generado: ${format(new Date(), 'PPP', { locale: es })}`;
    dateRow.getCell(1).font = { size: 12 };
    dateRow.getCell(1).alignment = { horizontal: 'center' };
    
    // Dejar una fila en blanco
    const headerRow = worksheet.getRow(4);
    
    // Dar formato a la fila de encabezados
    headerRow.eachCell((cell) => {
      cell.font = { bold: true };
      cell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: 'FFCCCCCC' }
      };
      cell.alignment = { horizontal: 'center' };
    });
    
    // Debug - Mostrar estructura de documentos
    console.log('Documentos a exportar:', filteredDocuments);
    console.log('Número de documentos:', filteredDocuments.length);
    
    // Agregar los datos
    filteredDocuments.forEach((doc) => {
      const row = {};
      
      // Añadir logging para debug
      if (doc === undefined || doc === null) {
        console.error('Documento indefinido o nulo encontrado');
        return;
      }
      
      // Asegurar que tenemos un objeto plano, no uno reactivo
      try {
        if (doc && typeof doc === 'object') {
          // Extraer propiedades para eliminar cualquier proxy
          const plainDoc = {};
          Object.keys(doc).forEach(key => {
            plainDoc[key] = doc[key];
          });
          doc = plainDoc;
        }
      } catch (err) {
        console.error('Error al convertir documento a objeto plano:', err);
      }
      
      console.log('Procesando documento:', doc);
      
      if (includeFields.includes('id')) row.id = doc.id || '';
      if (includeFields.includes('title')) row.title = doc.title || '';
      if (includeFields.includes('documentType')) {
        // Usar la propiedad correcta según la estructura real
        if (doc.type && typeof doc.type === 'object' && doc.type.name) {
          row.documentType = doc.type.name;
        } else if (doc.documentType) {
          row.documentType = doc.documentType;
        } else {
          row.documentType = 'Sin tipo';
        }
      }
      if (includeFields.includes('description')) row.description = doc.description || '';      
      if (includeFields.includes('versionNumber')) row.versionNumber = doc.versionNumber || '1.0';
      
      if (includeFields.includes('uploadDate')) {
        row.uploadDate = doc.uploadDate ? format(new Date(doc.uploadDate), 'dd/MM/yyyy', { locale: es }) : '';
      }
      
      if (includeFields.includes('authorName')) {
        // Usar la propiedad correcta para el autor
        row.authorName = doc.authorName || doc.author || '';
      }
      if (includeFields.includes('security')) {
        // Manejar diferentes estructuras posibles para el nivel de seguridad
        row.security = doc.security?.accessLevel || doc.accessLevel || 'Privado';
      }
      
      worksheet.addRow(row);
    });
    
    // Aplicar bordes a todas las celdas con datos
    for (let i = 4; i <= filteredDocuments.length + 4; i++) {
      worksheet.getRow(i).eachCell({ includeEmpty: true }, (cell) => {
        cell.border = {
          top: { style: 'thin' },
          left: { style: 'thin' },
          bottom: { style: 'thin' },
          right: { style: 'thin' }
        };
      });
    }
    
    // Generar el archivo como Blob
    const buffer = await workbook.xlsx.writeBuffer();
    return new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  },
  
  /**
   * Descarga un blob como un archivo
   * @param {Blob} blob - El blob a descargar
   * @param {String} fileName - Nombre del archivo
   */
  downloadExcel(blob, fileName) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
};

export default reportService;
