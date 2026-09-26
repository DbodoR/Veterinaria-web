package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.MascotaRepository;
import com.dbodor.veterinariaweb.service.HistoriaClinicaService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {

    private static final Color GRIS = new Color(217, 217, 217);
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font PEQUENA = FontFactory.getFont(FontFactory.HELVETICA, 8);

    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;

    public HistoriaClinicaServiceImpl(MascotaRepository mascotaRepository, CitaRepository citaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> buscarMascotas(String texto) {
        return mascotaRepository.buscar(texto == null ? "" : texto.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Mascota> mascota(Long idMascota) {
        return mascotaRepository.conDueno(idMascota);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> atenciones(Long idMascota) {
        return citaRepository.historialDeMascota(idMascota);
    }

    @Override
    public byte[] generarPdf(Mascota mascota, List<Cita> atenciones) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER, 50, 50, 50, 50);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        documento.add(new Paragraph("Historia clínica", TITULO));
        documento.add(new Paragraph("Generada el "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), PEQUENA));
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("Datos de la mascota", SUBTITULO));
        PdfPTable datos = new PdfPTable(new float[]{1.2f, 2.8f});
        datos.setWidthPercentage(100);
        datos.setSpacingBefore(6);
        fila(datos, "Nombre", mascota.getNombre());
        fila(datos, "Especie", mascota.getEspecie());
        fila(datos, "Raza", mascota.getRaza());
        fila(datos, "Sexo", "H".equals(mascota.getSexo()) ? "Hembra" : "Macho");
        fila(datos, "Fecha de nacimiento", mascota.getFechaNacimiento() == null ? null : mascota.getFechaNacimiento().toString());
        fila(datos, "Edad", mascota.getEdad());
        fila(datos, "Peso", mascota.getPesoKg() == null ? null : mascota.getPesoKg() + " kg");
        fila(datos, "Estado", mascota.getEstado());
        documento.add(datos);
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("Datos del propietario", SUBTITULO));
        PdfPTable dueno = new PdfPTable(new float[]{1.2f, 2.8f});
        dueno.setWidthPercentage(100);
        dueno.setSpacingBefore(6);
        fila(dueno, "Nombre", mascota.getUsuario().getNombre());
        fila(dueno, "Documento", mascota.getUsuario().getDocumento());
        fila(dueno, "Teléfono", mascota.getUsuario().getTelefono());
        fila(dueno, "Correo", mascota.getUsuario().getCorreo());
        documento.add(dueno);
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("Atenciones", SUBTITULO));
        if (atenciones.isEmpty()) {
            documento.add(new Paragraph("La mascota no tiene atenciones registradas.", NORMAL));
        } else {
            PdfPTable tabla = new PdfPTable(new float[]{1.2f, 0.7f, 2.2f, 1.8f, 1.2f, 1.1f});
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(6);
            tabla.setHeaderRows(1);
            for (String t : new String[]{"Fecha", "Hora", "Servicio", "Veterinario", "Estado", "Valor"}) {
                PdfPCell c = new PdfPCell(new Phrase(t, NEGRITA));
                c.setBackgroundColor(GRIS);
                c.setPadding(4);
                tabla.addCell(c);
            }
            for (Cita a : atenciones) {
                celda(tabla, a.getFechaCita().toString());
                celda(tabla, a.getHoraCita().toString());
                celda(tabla, a.getServicio().getNombre());
                celda(tabla, a.getVeterinario().getNombre());
                celda(tabla, a.getEstado());
                celda(tabla, moneda(a.getCostoTotal()));
            }
            documento.add(tabla);
        }

        documento.close();
        return salida.toByteArray();
    }

    private void fila(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell c = new PdfPCell(new Phrase(etiqueta, NEGRITA));
        c.setBackgroundColor(GRIS);
        c.setPadding(4);
        tabla.addCell(c);
        celda(tabla, valor);
    }

    private void celda(PdfPTable tabla, String valor) {
        PdfPCell c = new PdfPCell(new Phrase(valor == null || valor.isBlank() ? "-" : valor, NORMAL));
        c.setPadding(4);
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabla.addCell(c);
    }

    private String moneda(Double valor) {
        if (valor == null) {
            return "-";
        }
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.of("es", "CO"));
        simbolos.setGroupingSeparator('.');
        return "$" + new DecimalFormat("#,##0", simbolos).format(valor);
    }
}