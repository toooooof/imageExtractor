package net.toooooof;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Extractor {

    public static final int tolerance = 70;
    //public static int[] reference = {255, 34, 160, 210};
    public static int min_zone_size = 5000;
    public static int nb_lines_reference = 10;
    public static double search_boundaries_percentage = 0.75;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting image parsing at " + new Date());
        long now =  System.currentTimeMillis();

        Image image = new Image("T7C0007.jpg");

        //new Extractor().writeImage("output3.jpg", image.getWidth(), image.getHeight(), image.getPixels());

        System.out.println("Temps : " + (System.currentTimeMillis() - now) + "ms");



    }

    /*public Extractor() {

    }

    public void writeImage(String fileName, int width, int height, int[] pixels) {
        BufferedImage result2 = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        for (int i = 0 ; i < pixels.length ; i++) {
            int h = (int) Math.floor(i / width);
            int w = i%width;
            result2.setRGB(w, h, pixels[i]);
        }

        try {
            ImageIO.write(result2, "jpg", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

}
