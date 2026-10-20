package com.calsoft.controlador;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Genera el código QR de una orden de compra como imagen PNG (HU-08).
 *   GET /qr?texto=OC-20261019-7KQ2XD
 * La orden impresa (o mostrada en pantalla) se escanea al recibir la mercadería.
 */
@WebServlet("/qr")
public class QrServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final int TAMANO = 220;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String texto = req.getParameter("texto");
        if (texto == null || texto.isBlank() || texto.length() > 100) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Texto inválido");
            return;
        }
        BitMatrix matriz;
        try {
            matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, TAMANO, TAMANO,
                    Map.of(EncodeHintType.MARGIN, 1));
        } catch (WriterException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo generar el QR");
            return;
        }
        BufferedImage imagen = new BufferedImage(matriz.getWidth(), matriz.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < matriz.getWidth(); x++) {
            for (int y = 0; y < matriz.getHeight(); y++) {
                imagen.setRGB(x, y, matriz.get(x, y) ? 0x1A1A3D : 0xFFFFFF);
            }
        }
        resp.setContentType("image/png");
        resp.setHeader("Cache-Control", "private, max-age=86400");
        ImageIO.write(imagen, "png", resp.getOutputStream());
    }
}
