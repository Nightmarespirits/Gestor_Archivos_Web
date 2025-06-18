package com.ns.iestpffaaarchives.application.service.export;

import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Exporta un Catálogo de Transferencia a Excel empleando una plantilla con:
 *  • rangos nombrados para la cabecera
 *  • rango «detalleTemplate» para la fila plantilla de detalles (misma hoja)
 */
@Service
public class CatalogoTransferenciaExportService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoTransferenciaExportService.class);

    private static final String TEMPLATE_LOCATION = "classpath:templates/Plantilla_Catalogo_Transferencia.xlsx";
    /** Rango con nombre que marca la fila plantilla de detalles */
    private static final String DETAILS_TEMPLATE_RANGE = "detalleTemplate";

    private final ResourceLoader resourceLoader;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());

    public CatalogoTransferenciaExportService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public byte[] exportar(final CatalogoTransferencia catalogo) throws IOException {
        if (catalogo == null) throw new IllegalArgumentException("El catálogo no puede ser nulo");

        log.info("Exportando Catálogo de Transferencia (id={}) a Excel", catalogo.getId());

        Resource template = resourceLoader.getResource(TEMPLATE_LOCATION);
        try (InputStream in = template.getInputStream();
             Workbook wb = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            completarInformacionGeneral(wb, catalogo);
            completarDetalles(wb, catalogo.getDetalles());

            wb.write(out);
            return out.toByteArray();
        }
    }

    /* --------------------------- Cabecera -------------------------------- */

    private void completarInformacionGeneral(Workbook wb, CatalogoTransferencia c) {
        setValue(wb, NamedCell.NOMBRE_ENTIDAD, c.getNombreEntidad());
        setValue(wb, NamedCell.UNIDAD_ORGANIZACION, c.getUnidadOrganizacion());
        setValue(wb, NamedCell.SECCION, c.getSeccion());
        setValue(wb, NamedCell.NIVEL_DESCRIPCION, c.getNivelDescripcion());
        setValue(wb, NamedCell.SERIE_DOCUMENTAL, c.getSerieDocumental());
        setValue(wb, NamedCell.CODIGO_REFERENCIA, c.getCodigoReferencia());
        setValue(wb, NamedCell.SOPORTE, c.getSoporte());
        setValue(wb, NamedCell.VOLUMEN_ML, format(c.getVolumenMetrosLineales()));
        setValue(wb, NamedCell.RESPONSABLE_SECCION, c.getResponsableSeccion());
        setValue(wb, NamedCell.INVENTARIO_ELABORADO_POR, c.getInventarioElaboradoPor());
        setValue(wb, NamedCell.NUMERO_ANIO_REMISION, c.getNumeroAnioRemision());
        setValue(wb, NamedCell.LUGAR_FECHA_ELABORACION, c.getLugarFechaElaboracion());

        setValue(wb, NamedCell.VISTO_BUENO_RESP, c.getVistoBuenoResponsable());
        setValue(wb, NamedCell.FECHA_CREACION, format(c.getFechaCreacion()));
        setValue(wb, NamedCell.FECHA_MODIFICACION, format(c.getFechaModificacion()));
        setValue(wb, NamedCell.ESTADO, c.getEstado() != null ? c.getEstado().name() : null);
        setValue(wb, NamedCell.VERSION, c.getVersion() != null ? String.valueOf(c.getVersion()) : null);
    }

    /* --------------------------- Detalles -------------------------------- */

    private void completarDetalles(Workbook wb, List<DetalleCatalogoTransferencia> detalles) {
        Name tplName = wb.getName(DETAILS_TEMPLATE_RANGE);
        if (tplName == null) {
            log.warn("La plantilla no contiene el rango «{}»; se omiten los detalles.", DETAILS_TEMPLATE_RANGE);
            return;
        }

        CellReference tplRef = firstCellOf(tplName.getRefersToFormula());
        Sheet sheet = wb.getSheet(tplRef.getSheetName());
        Row templateRow = sheet.getRow(tplRef.getRow());
        if (templateRow == null) {
            log.warn("La fila plantilla para detalles no existe (fila {}).", tplRef.getRow());
            return;
        }

        int rowIdx = templateRow.getRowNum();
        for (DetalleCatalogoTransferencia d : detalles) {
            Row row = (rowIdx == templateRow.getRowNum()) ? templateRow : cloneRow(sheet, templateRow, rowIdx);
            popularDetalle(row, d);
            rowIdx++;
        }

        // Si no hay detalles, limpia la fila plantilla
        if (detalles.isEmpty()) limpiarDetalle(templateRow);
    }

    private void popularDetalle(Row row, DetalleCatalogoTransferencia d) {
        int col = 0;
        set(row, col++, d.getNumeroItem());
        set(row, col++, d.getNumeroCaja());
        set(row, col++, d.getNumeroTomoPaquete());
        set(row, col++, d.getNumeroUnidadDocumental());
        set(row, col++, format(d.getFechaUnidadDocumental()));
        set(row, col++, d.getAlcanceContenido());
        set(row, col++, d.getInformacionAdicional());
        set(row, col++, d.getCantidadFolios());
        set(row, col,   d.getObservaciones());
    }

    /* ---------------------- Utilidades plantilla ------------------------- */

    /** Permite rangos que sean una celda o un área «A1:B2»: se toma la primera. */
    private CellReference firstCellOf(String formula) {
        if (formula.contains(":")) {
            AreaReference area = new AreaReference(formula, SpreadsheetVersion.EXCEL2007);
            return area.getFirstCell();
        }
        return new CellReference(formula);
    }

    private void setValue(Workbook wb, NamedCell namedCell, String value) {
        Name name = wb.getName(namedCell.rangeName);
        if (name == null) {
            log.debug("Rango con nombre «{}» no encontrado en la plantilla", namedCell.rangeName);
            return;
        }
        CellReference ref = firstCellOf(name.getRefersToFormula());
        Sheet sheet = wb.getSheet(ref.getSheetName());
        Row row = sheet.getRow(ref.getRow());
        Cell cell = row.getCell(ref.getCol(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value != null ? value : "");
    }

    private Row cloneRow(Sheet sh, Row template, int newRowIdx) {
        Row newRow = sh.createRow(newRowIdx);
        for (int i = template.getFirstCellNum(); i < template.getLastCellNum(); i++) {
            Cell src = template.getCell(i);
            Cell tgt = newRow.createCell(i);
            if (src == null) continue;
            tgt.setCellStyle(src.getCellStyle());
        }
        return newRow;
    }

    private void limpiarDetalle(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null) cell.setBlank();
        }
    }

    /* -------------------- Helpers de escritura de fila ------------------- */

    private void set(Row row, int col, Integer value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null) cell.setCellValue(value);
        else cell.setBlank();
    }

    private void set(Row row, int col, BigDecimal value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null) cell.setCellValue(value.doubleValue());
        else cell.setBlank();
    }

    private void set(Row row, int col, String value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value != null ? value : "");
    }

    private void set(Row row, int col, LocalDate date) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (date != null) cell.setCellValue(DATE_FMT.format(date));
        else cell.setBlank();
    }

    private String format(java.time.temporal.TemporalAccessor t) {
        if (t == null) return null;
        if (t instanceof java.time.LocalDateTime dt) return DATE_TIME_FMT.format(dt);
        if (t instanceof java.time.LocalDate d) return DATE_FMT.format(d);
        return t.toString();
    }

    private String format(BigDecimal bd) {
        return bd != null ? bd.stripTrailingZeros().toPlainString() : null;
    }

    /* ------------------ Enumeración de rangos cabecera ------------------- */

    private enum NamedCell {
        NOMBRE_ENTIDAD("nombreEntidad"),
        UNIDAD_ORGANIZACION("unidadOrganizacion"),
        SECCION("seccion"),
        NIVEL_DESCRIPCION("nivelDescripcion"),
        SERIE_DOCUMENTAL("serieDocumental"),
        CODIGO_REFERENCIA("codigoReferencia"),
        SOPORTE("soporte"),
        VOLUMEN_ML("volumenMetrosLineales"),
        RESPONSABLE_SECCION("responsableSeccion"),
        INVENTARIO_ELABORADO_POR("inventarioElaboradoPor"),
        NUMERO_ANIO_REMISION("numeroAnioRemision"),
        LUGAR_FECHA_ELABORACION("lugarFechaElaboracion"),
        VISTO_BUENO_RESP("vistoBuenoResponsable"),
        FECHA_CREACION("fechaCreacion"),
        FECHA_MODIFICACION("fechaModificacion"),
        ESTADO("estado"),
        VERSION("version");

        private final String rangeName;
        NamedCell(String rangeName) { this.rangeName = rangeName; }
    }
}
