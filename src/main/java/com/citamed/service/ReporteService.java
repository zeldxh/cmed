package com.citamed.service;

import com.citamed.domain.Cita;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class ReporteService {

    // Genera el PDF del reporte de citas por rango de fechas (HU-14).
    private static final Color AZUL_MARCA = new Color(0x0D, 0x6E, 0xFD);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MessageSource messageSource;

    public ReporteService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public byte[] generarPdfCitas(List<Cita> citas, LocalDate inicio, LocalDate fin, Locale locale) {
        try {
            Document documento = new Document(PageSize.LETTER, 40, 40, 50, 40);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, AZUL_MARCA);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font fuenteEncabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            Paragraph titulo = new Paragraph(mensaje("reporte.titulo", locale), fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            String rango = mensaje("reporte.fechaInicio", locale) + ": " + inicio.format(FORMATO_FECHA)
                    + "    " + mensaje("reporte.fechaFin", locale) + ": " + fin.format(FORMATO_FECHA);
            Paragraph subtitulo = new Paragraph(rango, fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            documento.add(subtitulo);

            PdfPTable tabla = new PdfPTable(new float[] { 2.4f, 2.2f, 1.6f, 1.3f, 1.8f, 2.7f });
            tabla.setWidthPercentage(100);

            String[] encabezados = {
                    mensaje("campo.paciente", locale), mensaje("campo.medico", locale),
                    mensaje("campo.fecha", locale), mensaje("campo.hora", locale),
                    mensaje("campo.estado", locale), mensaje("campo.motivo", locale),
            };
            for (String encabezado : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuenteEncabezadoTabla));
                celda.setBackgroundColor(AZUL_MARCA);
                celda.setPadding(6);
                tabla.addCell(celda);
            }

            for (Cita cita : citas) {
                tabla.addCell(celda(cita.getPaciente().getUsuario().getNombre(), fuenteCelda));
                tabla.addCell(celda(cita.getDoctor().getUsuario().getNombre(), fuenteCelda));
                tabla.addCell(celda(cita.getFecha().format(FORMATO_FECHA), fuenteCelda));
                tabla.addCell(celda(cita.getHora().toString(), fuenteCelda));
                tabla.addCell(celda(mensaje("estado." + cita.getEstado(), locale), fuenteCelda));
                tabla.addCell(celda(cita.getMotivo(), fuenteCelda));
            }
            documento.add(tabla);

            Paragraph total = new Paragraph(
                    mensaje("reporte.totalCitas", locale) + ": " + citas.size(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK));
            total.setSpacingBefore(15);
            documento.add(total);

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el reporte en PDF.", e);
        }
    }

    private PdfPCell celda(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        celda.setPadding(5);
        return celda;
    }

    private String mensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, clave, locale);
    }
}
