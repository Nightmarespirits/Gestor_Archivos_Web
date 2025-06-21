package com.ns.iestpffaaarchives.application.service.export;

import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleInventarioGeneral;
import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Exporta un Inventario General de Transferencia (Anexo N.º 04) a Excel
 * empleando una plantilla con:
 *   • Rangos con nombre para la cabecera (ver enum NamedCell).<br>
 *   • Fila plantilla de detalles marcada por el rango «detalleTemplate».
 */
@Service
public class InventarioGeneralExportService {

    private static final Logger log = LoggerFactory.getLogger(InventarioGeneralExportService.class);

    private static final String TEMPLATE_LOCATION      = "classpath:templates/Plantilla_Inventario_General.xlsx";
    private static final String DETAILS_TEMPLATE_RANGE = "detalleTemplate";

    private final ResourceLoader resourceLoader;

    private static final DateTimeFormatter DATE_FMT      = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());

    public InventarioGeneralExportService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /* ============================== API ============================== */

    public byte[] exportar(InventarioGeneral inv) throws IOException {
        if (inv == null) throw new IllegalArgumentException("El inventario no puede ser nulo");
        log.info("Exportando Inventario General (id={}) a Excel", inv.getId());

        Resource tpl = resourceLoader.getResource(TEMPLATE_LOCATION);
        try (InputStream in = tpl.getInputStream();
             Workbook wb    = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            completarCabecera(wb, inv);
            completarDetalles(wb, inv.getDetalles());

            wb.write(out);
            return out.toByteArray();
        }
    }

    /* =========================== Cabecera ============================ */

    private void completarCabecera(Workbook wb, InventarioGeneral g) {
        setValue(wb, NamedCell.TITULO,                     g.getTitulo());
        setValue(wb, NamedCell.UNIDAD_ADMINISTRATIVA,      g.getUnidadAdministrativa());
        setValue(wb, NamedCell.SECCION,                    g.getSeccion());
        setValue(wb, NamedCell.NUMERO_ANIO_REMISION,       g.getNumeroAnioRemision());
        setValue(wb, NamedCell.FECHA_TRANSFERENCIA,        format(g.getFechaTransferencia()));
        setValue(wb, NamedCell.TOTAL_VOLUMEN,              format(g.getTotalVolumen()));
        setValue(wb, NamedCell.LUGAR_FECHA_ENTREGA,        g.getLugarFechaEntrega());
        setValue(wb, NamedCell.LUGAR_FECHA_RECEPCION,      g.getLugarFechaRecepcion());
        setValue(wb, NamedCell.FIRMA_AUTORIDAD_ENTREGA,    g.getFirmaSelloAutoridadEntrega());
        setValue(wb, NamedCell.FIRMA_AUTORIDAD_RECIBE,     g.getFirmaSelloAutoridadRecibe());
        setValue(wb, NamedCell.FECHA_INICIAL,              format(g.getFechaInicial()));
        setValue(wb, NamedCell.FECHA_FINAL,                format(g.getFechaFinal()));
        setValue(wb, NamedCell.ESTADO_CONSERVACION,        g.getEstadoConservacion());
        setValue(wb, NamedCell.OBSERVACIONES,              g.getObservaciones());

        // Metadatos
        setValue(wb, NamedCell.FECHA_CREACION,             format(g.getFechaCreacion()));
        setValue(wb, NamedCell.FECHA_MODIFICACION,         format(g.getFechaModificacion()));
        setValue(wb, NamedCell.ESTADO,                     g.getEstado() != null ? g.getEstado().name() : null);
        setValue(wb, NamedCell.VERSION,                    g.getVersion() != null ? String.valueOf(g.getVersion()) : null);
        setValue(wb, NamedCell.RUTA_ARCHIVO,               g.getRutaArchivo());
    }

    /* =========================== Detalles ============================ */

    private void completarDetalles(Workbook wb, List<DetalleInventarioGeneral> detalles) {
        Name tplName = wb.getName(DETAILS_TEMPLATE_RANGE);
        if (tplName == null) {
            log.warn("Plantilla sin rango «{}»: se omiten detalles.", DETAILS_TEMPLATE_RANGE);
            return;
        }
        CellReference tplRef = firstCellOf(tplName.getRefersToFormula());
        Sheet sheet          = wb.getSheet(tplRef.getSheetName());
        Row templateRow      = sheet.getRow(tplRef.getRow());
        if (templateRow == null) {
            log.warn("Fila plantilla de detalles inexistente (fila {}).", tplRef.getRow());
            return;
        }

        int rowIdx = templateRow.getRowNum();
        for (DetalleInventarioGeneral d : detalles) {
            Row row = (rowIdx == templateRow.getRowNum())
                      ? templateRow
                      : cloneRow(sheet, templateRow, rowIdx);
            popularDetalle(row, d);
            rowIdx++;
        }
        if (detalles.isEmpty()) limpiarFila(templateRow);
    }

    private void popularDetalle(Row row, DetalleInventarioGeneral d) {
        int c = 0;
        set(row, c++, d.getNumeroItem());
        set(row, c++, d.getSerieDocumental());
        set(row, c++, d.getFechaExtremaDel());
        set(row, c++, d.getFechaExtremaAl());
        set(row, c++, d.getTipoUnidadArchivamiento());
        set(row, c++, d.getCantidadUnidadArchivamiento());
        set(row, c++, d.getVolumenMetrosLineales());
        set(row, c++, d.getSoporte());
        set(row, c++, d.getObservaciones());
        set(row, c,   d.getDescripcion());
    }

    /* ====================== Utilidades comunes ======================= */

    private CellReference firstCellOf(String formula) {
        if (formula.contains(":")) {
            AreaReference area = new AreaReference(formula, SpreadsheetVersion.EXCEL2007);
            return area.getFirstCell();
        }
        return new CellReference(formula);
    }

    private void setValue(Workbook wb, NamedCell named, String value) {
        Name name = wb.getName(named.range);
        if (name == null) { log.debug("Rango «{}» inexistente.", named.range); return; }
        CellReference ref = firstCellOf(name.getRefersToFormula());
        Sheet sheet = wb.getSheet(ref.getSheetName());
        Row row     = sheet.getRow(ref.getRow());
        Cell cell   = row.getCell(ref.getCol(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null && !value.isBlank()) cell.setCellValue(value); else cell.setBlank();
    }

    /* --------- set(...) sobrecargados (null‑safe y por tipo) ---------- */

    private void set(Row row, int col, String value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null && !value.isBlank()) cell.setCellValue(value); else cell.setBlank();
    }
    private void set(Row row, int col, Integer value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null) cell.setCellValue(value); else cell.setBlank();
    }
    private void set(Row row, int col, BigDecimal value) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value != null) cell.setCellValue(value.doubleValue()); else cell.setBlank();
    }
    private void set(Row row, int col, LocalDate date) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (date != null) cell.setCellValue(DATE_FMT.format(date)); else cell.setBlank();
    }
    private void set(Row row, int col, LocalDateTime dateTime) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (dateTime != null) cell.setCellValue(DATE_TIME_FMT.format(dateTime)); else cell.setBlank();
    }

    /* ---------------- Clonado / limpieza de fila ---------------------- */

    private Row cloneRow(Sheet sh, Row template, int newRowIdx) {
        Row newRow = sh.createRow(newRowIdx);
        for (int i = template.getFirstCellNum(); i < template.getLastCellNum(); i++) {
            Cell src = template.getCell(i);
            if (src == null) continue;
            Cell tgt = newRow.createCell(i);
            tgt.setCellStyle(src.getCellStyle());
        }
        return newRow;
    }
    private void limpiarFila(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) cell.setBlank();
        }
    }

    /* ===================== Rangos con nombre ========================= */

    private enum NamedCell {
        TITULO("titulo"),
        UNIDAD_ADMINISTRATIVA("unidadAdministrativa"),
        SECCION("seccion"),
        NUMERO_ANIO_REMISION("numeroAnioRemision"),
        FECHA_TRANSFERENCIA("fechaTransferencia"),
        TOTAL_VOLUMEN("totalVolumen"),
        LUGAR_FECHA_ENTREGA("lugarFechaEntrega"),
        LUGAR_FECHA_RECEPCION("lugarFechaRecepcion"),
        FIRMA_AUTORIDAD_ENTREGA("firmaAutoridadEntrega"),
        FIRMA_AUTORIDAD_RECIBE("firmaAutoridadRecibe"),
        FECHA_INICIAL("fechaInicial"),
        FECHA_FINAL("fechaFinal"),
        ESTADO_CONSERVACION("estadoConservacion"),
        OBSERVACIONES("observaciones"),
        FECHA_CREACION("fechaCreacion"),
        FECHA_MODIFICACION("fechaModificacion"),
        ESTADO("estado"),
        VERSION("version"),
        RUTA_ARCHIVO("rutaArchivo");

        private final String range;
        NamedCell(String range) { this.range = range; }
    }

    /* ---------------------- formateo genérico ------------------------ */

    private String format(BigDecimal bd) {
        return bd != null ? bd.stripTrailingZeros().toPlainString() : null;
    }
    private String format(LocalDate ld) {
        return ld != null ? DATE_FMT.format(ld) : null;
    }
    private String format(LocalDateTime ldt) {
        return ldt != null ? DATE_TIME_FMT.format(ldt) : null;
    }
}
