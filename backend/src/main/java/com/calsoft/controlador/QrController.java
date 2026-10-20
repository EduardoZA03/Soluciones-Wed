package com.calsoft.controlador;

import com.calsoft.error.ValidacionException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Genera el código QR de una orden de compra como PNG (HU-08).
 *   GET /api/qr?texto=OC-20261019-7KQ2XD
 * Es pública porque solo dibuja el texto recibido (sin datos del sistema).
 */
@RestController
public class QrController {

    private static final int TAMANO = 220;

    @GetMapping(value = "/api/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@RequestParam String texto) throws IOException {
        if (texto.isBlank() || texto.length() > 100) {
            throw new ValidacionException("Texto inválido");
        }
        BitMatrix matriz;
        try {
            matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, TAMANO, TAMANO,
                    Map.of(EncodeHintType.MARGIN, 1));
        } catch (WriterException e) {
            throw new ValidacionException("No se pudo generar el QR");
        }
        BufferedImage imagen = new BufferedImage(matriz.getWidth(), matriz.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < matriz.getWidth(); x++) {
            for (int y = 0; y < matriz.getHeight(); y++) {
                imagen.setRGB(x, y, matriz.get(x, y) ? 0x1A1A3D : 0xFFFFFF);
            }
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", png);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS)).body(png.toByteArray());
    }
}
