package com.elcom.metacen.content.schedule;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

public class ScanedImage {

    public static void
    processImg(BufferedImage ipimage,
               float scaleFactor,
               float offset)
            throws IOException, TesseractException
    {
        // Making an empty image buffer
        // to store image later
        // ipimage is an image buffer
        // of input image
        BufferedImage opimage
                = new BufferedImage(1050,
                1024,
                ipimage.getType());

        // creating a 2D platform
        // on the buffer image
        // for drawing the new image
        Graphics2D graphic
                = opimage.createGraphics();

        // drawing new image starting from 0 0
        // of size 1050 x 1024 (zoomed images)
        // null is the ImageObserver class object
        graphic.drawImage(ipimage, 0, 0,
                1050, 1024, null);
        graphic.dispose();

        // rescale OP object
        // for gray scaling images
        RescaleOp rescale
                = new RescaleOp(scaleFactor, offset, null);

        // performing scaling
        // and writing on a .png file
        BufferedImage fopimage
                = rescale.filter(opimage, null);
        ImageIO
                .write(fopimage,
                        "jpg",
                        new File("config/output.png"));

        // Instantiating the Tesseract class
        // which is used to perform OCR
        Tesseract it = new Tesseract();

        it.setDatapath("config/tess4j-4.3.0.jar/tessdata");

        // doing OCR on the image
        // and storing result in string str
        String str = it.doOCR(fopimage);
        System.out.println(str);

    }
    public static void main(String[] args)
    {
        Long start = System.currentTimeMillis();
        Tesseract tesseract = new Tesseract();
        try {
//            tesseract.setDatapath("config/tess4j-4.3.0.jar/tessdata");

            tesseract.setDatapath("src/main/java/com/elcom/metacen/content/tessdata");
//            tesseract.setLanguage("vie");

            // the path of your tess data folder
            // inside the extracted file
            String text
                    = tesseract.doOCR(new File( "config/testJpg.jpg"));


//            String text
//                    = tesseract.doOCR(new File("config/anhtest.png"));

            // path of your image file
            System.out.print(text);
            Long end = System.currentTimeMillis();
            System.out.println(end-start);
        }
        catch (TesseractException e) {
            e.printStackTrace();
        }
    }


//    public static void main(String args[]) throws Exception
//    {
//
//        Long start = System.currentTimeMillis();
//        File f
//                = new File(
//                "config/testchu.jpg");
//
//        BufferedImage ipimage = ImageIO.read(f);
//
//        // getting RGB content of the whole image file
//        double d
//                = ipimage
//                .getRGB(ipimage.getTileWidth() / 2,
//                        ipimage.getTileHeight() / 2);
//
//        // comparing the values
//        // and setting new scaling values
//        // that are later on used by RescaleOP
//        if (d >= -1.4211511E7 && d < -7254228) {
//            processImg(ipimage, 3f, -10f);
//        }
//        else if (d >= -7254228 && d < -2171170) {
//            processImg(ipimage, 1.455f, -47f);
//        }
//        else if (d >= -2171170 && d < -1907998) {
//            processImg(ipimage, 1.35f, -10f);
//        }
//        else if (d >= -1907998 && d < -257) {
//            processImg(ipimage, 1.19f, 0.5f);
//        }
//        else if (d >= -257 && d < -1) {
//            processImg(ipimage, 1f, 0.5f);
//        }
//        else if (d >= -1 && d < 2) {
//            processImg(ipimage, 1f, 0.35f);
//        }
//        Long end = System.currentTimeMillis();
//        System.out.println(end-start);
//    }
}