package tics.uide.gestionuide.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.model.DetalleFactura;
import tics.uide.gestionuide.model.Factura;

@Service
public class FacturaPdfService {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private DetalleFacturaService detalleFacturaService;

    private static final BaseColor BRAND = new BaseColor(145, 0, 72);
    private static final BaseColor GOLD = new BaseColor(234, 170, 0);
    private static final BaseColor NAVY = new BaseColor(0, 45, 114);
    private static final BaseColor GRAY_LIGHT = new BaseColor(245, 245, 245);

    public byte[] generarPdf(Long facturaId) throws Exception {
        Factura f = facturaService.buscarPorId(facturaId);
        List<DetalleFactura> detalles = detalleFacturaService.listarPorFactura(facturaId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BRAND);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
        Font bigBold = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BRAND);

        // Header
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{60, 40});

        PdfPCell brandCell = new PdfPCell();
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setPadding(10);
        Paragraph brand = new Paragraph();
        brand.add(new Chunk("☕ Cafetería UIDE\n", titleFont));
        brand.add(new Chunk("Universidad Internacional del Ecuador\n", subtitleFont));
        brand.add(new Chunk("RUC: 1792456789001\n", subtitleFont));
        brand.add(new Chunk("Av. Simón Bolívar, Quito - Ecuador", subtitleFont));
        brandCell.addElement(brand);
        header.addCell(brandCell);

        PdfPCell numCell = new PdfPCell();
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        numCell.setPadding(10);
        numCell.setBackgroundColor(GRAY_LIGHT);
        Paragraph numPara = new Paragraph();
        numPara.setAlignment(Element.ALIGN_RIGHT);
        numPara.add(new Chunk("FACTURA\n", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, NAVY)));
        numPara.add(new Chunk(f.getNumeroFactura() + "\n\n", bigBold));
        numPara.add(new Chunk("Fecha: " + (f.getFechaEmision() != null ?
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(f.getFechaEmision()) : "—") + "\n", subtitleFont));
        numPara.add(new Chunk("Estado: " + f.getEstado(), subtitleFont));
        numCell.addElement(numPara);
        header.addCell(numCell);
        doc.add(header);

        // Separator
        LineSeparator sep = new LineSeparator(1, 100, GOLD, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(sep));
        doc.add(Chunk.NEWLINE);

        // Client/Cashier info
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{50, 50});

        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorder(Rectangle.BOX);
        clientCell.setBorderColor(BaseColor.LIGHT_GRAY);
        clientCell.setPadding(10);
        Paragraph cp = new Paragraph();
        cp.add(new Chunk("CLIENTE\n", new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BRAND)));
        if (f.getCliente() != null) {
            cp.add(new Chunk(f.getCliente().getNombre() + " " + (f.getCliente().getApellido() != null ? f.getCliente().getApellido() : "") + "\n", boldFont));
            cp.add(new Chunk(f.getCliente().getEmail() + "\n", subtitleFont));
            if (f.getCliente().getTelefono() != null) cp.add(new Chunk(f.getCliente().getTelefono(), subtitleFont));
        } else { cp.add(new Chunk("Consumidor Final", cellFont)); }
        clientCell.addElement(cp);
        info.addCell(clientCell);

        PdfPCell cajeroCell = new PdfPCell();
        cajeroCell.setBorder(Rectangle.BOX);
        cajeroCell.setBorderColor(BaseColor.LIGHT_GRAY);
        cajeroCell.setPadding(10);
        Paragraph cjp = new Paragraph();
        cjp.add(new Chunk("CAJERO\n", new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BRAND)));
        if (f.getCajero() != null) {
            cjp.add(new Chunk(f.getCajero().getNombre() + " " + (f.getCajero().getApellido() != null ? f.getCajero().getApellido() : ""), boldFont));
        }
        cajeroCell.addElement(cjp);
        info.addCell(cajeroCell);
        doc.add(info);
        doc.add(Chunk.NEWLINE);

        // Products table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8, 40, 15, 17, 20});

        String[] headers2 = {"#", "Producto", "Cant.", "P. Unit.", "Subtotal"};
        for (String h : headers2) {
            PdfPCell hc = new PdfPCell(new Phrase(h, headerFont));
            hc.setBackgroundColor(NAVY);
            hc.setPadding(8);
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hc);
        }

        int idx = 1;
        for (DetalleFactura d : detalles) {
            BaseColor rowBg = idx % 2 == 0 ? GRAY_LIGHT : BaseColor.WHITE;
            addCell(table, String.valueOf(idx), cellFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, d.getProducto() != null ? d.getProducto().getNombre() : "—", cellFont, rowBg, Element.ALIGN_LEFT);
            addCell(table, d.getCantidad() != null ? String.format("%.0f", d.getCantidad()) : "0", cellFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, d.getPrecioUnitario() != null ? String.format("$%.2f", d.getPrecioUnitario()) : "$0.00", cellFont, rowBg, Element.ALIGN_RIGHT);
            addCell(table, d.getSubtotal() != null ? String.format("$%.2f", d.getSubtotal()) : "$0.00", cellFont, rowBg, Element.ALIGN_RIGHT);
            idx++;
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);

        // Totals
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(40);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        addTotalRow(totals, "Subtotal:", String.format("$%.2f", f.getSubtotal()), cellFont);
        addTotalRow(totals, "IVA:", String.format("$%.2f", f.getIva()), cellFont);
        if (f.getDescuento() != null && f.getDescuento().compareTo(java.math.BigDecimal.ZERO) > 0)
            addTotalRow(totals, "Descuento:", String.format("-$%.2f", f.getDescuento()), cellFont);

        PdfPCell tl = new PdfPCell(new Phrase("TOTAL:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, NAVY)));
        tl.setBorder(Rectangle.TOP); tl.setBorderColor(GOLD); tl.setBorderWidthTop(2);
        tl.setPadding(8); tl.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(tl);
        PdfPCell tv = new PdfPCell(new Phrase(String.format("$%.2f", f.getTotal()), bigBold));
        tv.setBorder(Rectangle.TOP); tv.setBorderColor(GOLD); tv.setBorderWidthTop(2);
        tv.setPadding(8); tv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(tv);
        doc.add(totals);

        // Footer
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("¡Gracias por su compra!\nCafetería UIDE — Universidad Internacional del Ecuador",
            new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    public File generarPdfFile(Long facturaId) throws Exception {
        byte[] bytes = generarPdf(facturaId);
        Factura f = facturaService.buscarPorId(facturaId);
        File tempFile = File.createTempFile(f.getNumeroFactura() + "_", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bytes);
        }
        return tempFile;
    }

    private void addCell(PdfPTable t, String text, Font font, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg); c.setPadding(6); c.setHorizontalAlignment(align);
        c.setBorder(Rectangle.BOTTOM); c.setBorderColor(BaseColor.LIGHT_GRAY);
        t.addCell(c);
    }

    private void addTotalRow(PdfPTable t, String label, String value, Font font) {
        PdfPCell l = new PdfPCell(new Phrase(label, font));
        l.setBorder(Rectangle.NO_BORDER); l.setPadding(4); l.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value, font));
        v.setBorder(Rectangle.NO_BORDER); v.setPadding(4); v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(v);
    }
}
