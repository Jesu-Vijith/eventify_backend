package com.eventmanagement.EventManagement.utils;

import com.eventmanagement.EventManagement.model.entity.Ticket;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QrCodeGenerator {
    public static byte[] generateQRCode(Ticket ticket){
        String qrCodeName="";
        var qrCodeWriter=new QRCodeWriter();
        try {
            BitMatrix bitMatrix=qrCodeWriter.encode("ticket ID: "+ticket.getTicketId()
                    +"\n"+"event ID: "+ticket.getEventId()
                    +"\n"+"seats: "+ticket.getSeatNames(),
                    BarcodeFormat.QR_CODE,400,400);
            BufferedImage image = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < bitMatrix.getWidth(); x++) {
                for (int y = 0; y < bitMatrix.getHeight(); y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (WriterException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
