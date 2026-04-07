package com.proyecto.nexus.pago.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.proyecto.nexus.pago.model.Pago;

@Service
public class FacturaService {

public byte[] generarFactura(Pago pago) {

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ==================== LOGO ====================
        InputStream logoStream = getClass().getResourceAsStream("/static/imagenes/Nexus.png");

        if (logoStream != null) {
            ImageData imageData = ImageDataFactory.create(logoStream.readAllBytes());
            Image logo = new Image(imageData)
                    .scaleToFit(80, 80)
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

            document.add(logo);
        }

        // ==================== NUMERO FACTURA ====================
        String numeroFactura = "FAC-" + System.currentTimeMillis();

        document.add(new Paragraph("Factura N°: " + numeroFactura)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10));

        // ==================== TITULO ====================
        document.add(new Paragraph("FACTURA DE PAGO")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" "));

        // ==================== DATOS SEGUROS ====================
        String nombre = "N/A";
        String apellido = "";
        String cedula = "N/A";
        String plan = "N/A";
        String fecha = "N/A";
        String metodo = "N/A";

        if (pago != null) {

            if (pago.getUsuario() != null) {
                nombre = pago.getUsuario().getNombre() != null ? pago.getUsuario().getNombre() : "";
                apellido = pago.getUsuario().getApellido() != null ? pago.getUsuario().getApellido() : "";
                cedula = pago.getUsuario().getCedula() != null 
                        ? String.valueOf(pago.getUsuario().getCedula()) 
                        : "N/A";
            }

            if (pago.getPaquete() != null) {
                plan = pago.getPaquete().getIdPlanes() != null 
                        ? "Plan ID: " + pago.getPaquete().getIdPlanes() 
                        : "N/A";
            }

            if (pago.getFechaPago() != null) {
                fecha = pago.getFechaPago()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }

            if (pago.getMetodoPago() != null) {
                metodo = pago.getMetodoPago();
            }
        }

        // ==================== INFO CLIENTE ====================
        document.add(new Paragraph("Cliente: " + nombre + " " + apellido));
        document.add(new Paragraph("Cédula: " + cedula));
        document.add(new Paragraph("Fecha: " + fecha));
        document.add(new Paragraph("Método de pago: " + metodo));

        document.add(new Paragraph(" "));

        // ==================== TABLA ====================
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2}))
                .useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Precio").setBold()));

        String precio = "$0";
        int cantidad = 0;

        if (pago != null) {
            if (pago.getMonto() != null) {
                precio = String.format("$%,.0f", pago.getMonto());
            }

            if (pago.getPaquete() != null && pago.getPaquete().getTotalClases() != null) {
                cantidad = pago.getPaquete().getTotalClases();
            }
        }

        table.addCell(plan);
        table.addCell(String.valueOf(cantidad));
        table.addCell(precio);

        document.add(table);

        document.add(new Paragraph(" "));

        // ==================== TOTAL ====================
        Paragraph total = new Paragraph("TOTAL: " + precio)
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT);

        document.add(total);

        document.add(new Paragraph(" "));

        // ==================== FOOTER ====================
        document.add(new Paragraph("Gracias por confiar en NEXUS GYM")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("Factura generada automáticamente")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8));

        document.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return out.toByteArray();
}

}