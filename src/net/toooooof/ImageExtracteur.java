package net.toooooof;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class ImageExtracteur {
    //http://java.developpez.com/faq/gui/?page=graphique_general_images

    private int tolerance;
    private int couleurFond;
    private int couleurForme;
    private String inputImageFile;
    private String outputDirectory;
    private String outputFileName;
    private int[] image;
    private int width;
    private BufferedImage bufferedImage;

    // scanBlue : 22a0d2 (34, 160, 210)


    public static void main(String[] args) {

        printShortHelp();

        String file = "./input.jpg";
        String sortie = "output";
        String outDir = ".";
        int coulFond = compressRgb(255, 255, 255, 255);
        int coulForme = compressRgb(255, 0, 0, 0);
        int tol = 35;

        ImageExtracteur extracteur = new ImageExtracteur();
        extracteur.setCouleurFond(coulFond);
        extracteur.setCouleurForme(coulForme);
        extracteur.setTolerance(tol);
        extracteur.setInputImageFile(file);
        extracteur.setOutputDirectory(outDir);
        extracteur.setOutputFileName(sortie);

        boolean go = false;
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if ("-h".equals(args[i])) {
                    printLongHelp();
                } else if ("-o".equals(args[i])) {
                    extracteur.setOutputDirectory(args[i + 1]);
                    go = true;
                } else if ("-s".equals(args[i])) {
                    extracteur.setOutputFileName(args[i + 1]);
                    go = true;
                } else if ("-i".equals(args[i])) {
                    extracteur.setInputImageFile(args[i + 1]);
                    go = true;
                } else if ("-t".equals(args[i])) {
                    extracteur.setTolerance(Integer.parseInt(args[i + 1]));
                    go = true;
                } else if ("-b".equals(args[i])) {
                    extracteur.setCouleurFond(colorStringToInt(args[i + 1]));
                    go = true;
                } else if ("-f".equals(args[i])) {
                    extracteur.setCouleurForme(colorStringToInt(args[i + 1]));
                    go = true;
                }
            }
        }

        if (go) extracteur.process();

    }

    private ImageExtracteur() {
    }

    private static void printShortHelp() {
        String help = "Voici la liste des paramètres possibles : \n";
        help += "  -o outputDirectory (valeur par défaut : .)\n";
        help += "  -s outputFileName (valeur par défaut : output)\n";
        help += "  -i inputFileName (valeur par défaut : ./input.jpg)\n";
        help += "  -t tolerance (valeur par défaut : 35)\n";
        help += "  -b backgroundColor (valeur par défaut : ffffff\n";
        help += "  -f shapeColor (valeur par défaut : 000000\n";
        help += "  -h : affiche l'aide\n";

        System.out.println(help);
    }

    private static void printLongHelp() {
        printShortHelp();
    }

    private void process() {
        try {
            bufferedImage = readImage(new File(inputImageFile));
            width = bufferedImage.getWidth();

            int w = width;
            int h = bufferedImage.getHeight();
            image = new int[w * h];
            bufferedImage.getRGB(0, 0, w, h, image, 0, w);

            int nb = 0;
            while (getFirstSaut() > -1) {
                scanAndExtractFirstImageAndClean(nb++);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int getFirstSaut() {
        for (int i = 0; i < image.length; i++) {
            if (((i % width) < width - 1) && saut(image[i], image[i + 1])) {
                return i;
            }
        }
        return -1;
    }
/*
    public int getFirstVerticalSaut(int startIndex) {
        int obj = startIndex;

        while (!(obj > image.length) && !(saut(image[startIndex], image[obj]))) {
            obj += width;
        }

        if (obj != startIndex) return obj;

        return -1;
    }*/

    private void scanAndExtractFirstImageAndClean(int ImageNumber) throws IOException {

        int start = getFirstSaut();
        int[] bornes = contour(start + 1, start);
        int boxStart = bornes[0];
        int last = bornes[1];
        int x = boxStart % width;
        int y = boxStart / width;
        int w = last % width - x;
        int h = (last / width) - y;

        if (w > 0 && h > 0) {
            writeImage(extractSubImage(x, y, w, h), "jpg", new File(outputDirectory, outputFileName + ImageNumber + ".jpg"));


            // rotation
			/*System.out.println("flat ? " + (start/width==boxStart/width));
			int deb = boxStart+w-1;
			int coin = getFirstVerticalSaut(deb);
			System.out.println((coin/width) + "/" + (y+h));
			if ((coin/width) < (y+h/2)) {

			}
			if (!(start/width==boxStart/width)) {
				float l1 = longueur(start, coin, width);
				float l2 = longueur(deb, coin, width);
				float l3 = longueur(start, deb, width);
				float asin = l2/l1;
				float acos = l3/l1;
				double angle = Math.toDegrees(Math.asin(asin));
				double angle2 = Math.toDegrees(Math.acos(acos));
				System.out.println(l1 + " " + l2 + " "+ asin + " " + angle + " " + angle2);
			}*/


        }
        clean(x, y, w, h);

    }

    /**
     * http://fr.wikipedia.org/wiki/Rotation_vectorielle
     * <p>
     * x' = x*cos(a) - y*sin(a)
     * y' = x*sin(a) + y*cos(a)
     */
   /* public static int[] rotate(int[] image, double angle, int width) {
        int[] result = new int[image.length];
        Arrays.fill(result, -1);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        for (int i = 0; i < image.length; i++) {
            int x = i % width;
            int y = i / width;

            double xp = x * cos - y * sin;
            double yp = x * sin + y * cos;

            int neo = ((int) yp) * width + ((int) xp);

            if (neo < image.length && neo > 0) {
                result[neo] = image[i];
            }
        }

        return result;
    }*/

    /*public static float longueur(int pointA, int pointB, int width) {
        return longueur(pointA % width, pointA / width, pointB % width, pointB / width);
    }*/

   /* public static float longueur(int xA, int yA, int xB, int yB) {
        System.out.println("[" + xA + "," + yA + "]-[" + xB + "," + yB + "]");
        int a = xB - xA;
        int b = yB - yA;
        a *= a;
        b *= b;
        float result = Math.round(Math.sqrt(a + b));
        return result;
    }*/
    private void clean(int x, int y, int w, int h) {
        for (int i = 0; i < image.length; i++) {
            int curX = i % width;
            int curY = (int) i / width;

            if (curX >= x && curX <= (x + w) && curY >= y && curY <= y + h) {
                image[i] = couleurFond;
            }
        }
    }

    private static void writeImage(BufferedImage image, String type, File file) throws IOException {
        ImageIO.write(image, type, file);
    }

    private static BufferedImage readImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    private BufferedImage extractSubImage(int startX, int startY, int wid, int hei) {
        int[] tab = new int[wid * hei];
        bufferedImage.getRGB(startX, startY, wid, hei, tab, 0, wid);

        BufferedImage nouvelleImage = new BufferedImage(
                wid,
                hei,
                bufferedImage.getType());
        nouvelleImage.setRGB(0, 0, wid, hei, tab, 0, wid);

        return nouvelleImage;
    }

    private static int[] toRGB(int rgb) {
        int[] result = new int[4];
        result[0] = (rgb >> 24) & 0xFF;
        result[1] = (rgb >> 16) & 0xFF;
        result[2] = (rgb >> 8) & 0xFF;
        result[3] = rgb & 0xFF;

        return result;
    }

    /*public static int compressRgb(int[] rgb) {
        return (rgb[1] << 24) + (rgb[1] << 16) + (rgb[1] << 8) + rgb[1];
    }*/

    private static int compressRgb(int alpha, int rouge, int vert, int bleu) {
        return (alpha << 24) + (rouge << 16) + (vert << 8) + bleu;
    }

   /* public int getPix(int x, int y) {
        return image[y * width + x];
    }*/

    private boolean saut(int[] a, int[] b) {
        if (Math.abs(a[0] - b[0]) > tolerance) return true;
        if (Math.abs(a[1] - b[1]) > tolerance) return true;
        if (Math.abs(a[2] - b[2]) > tolerance) return true;
        return Math.abs(a[3] - b[3]) > tolerance;

    }

    /**
     * une fois le cardre noir trouvé, on cherche le coin haut-droite du cadre
     */
    /*public int bordDroit(int startIndex) {
        int index = startIndex;

        int maxHaut = index;
        int maxBas = index;

        while (true) {
            if (index % width == width - 1) break;

            if (saut(image[index], image[index + 1])) {
                if (saut(image[index], image[index - width])) {
                    break;
                } else {
                    index -= width;
                    maxHaut = index;
                }
            } else {
                index++;
            }
        }


        while (true) {
            if (index % width == width - 1) break;

            if (saut(image[index], image[index + 1])) {
                if (saut(image[index], image[index + width])) {
                    break;
                } else {
                    index += width;
                    maxBas = index;
                }
            } else {
                index++;
            }
        }

        if (maxBas % width > maxHaut % width) return maxBas;
        else return maxHaut;

    }*/
    private int[] contour(int startIndex, int lastWhite) {
        int maxX = startIndex % width;
        int maxY = (int) startIndex / width;
        int minX = maxX;
        int minY = maxY;

        int current = startIndex;
        int last = current;
        int[] voi = voisinsFromLastBlack(current, lastWhite);
        do {
            for (int i = 0; i < voi.length; i++) {
                if (!saut(image[current], image[voi[i]])) {
                    last = current;
                    current = voi[i];
                    voi = voisinsFromLastBlack(current, last);

                    if (current % width > maxX) maxX = current % width;
                    if (((int) current / width) > maxY) maxY = (int) current / width;
                    if (current % width < minX) minX = current % width;
                    if (((int) current / width) < minY) minY = (int) current / width;

                    break;
                }
            }
        } while (current != startIndex);

        int[] result = new int[2];
        result[0] = minY * width + minX;
        result[1] = maxY * width + maxX;
        return result;
    }

    private int[] voisinsFromLastBlack(int startIndex, int lastBlack) {
        return reorderTab(orderedNeighbours(startIndex, width), lastBlack);
    }

    private static int[] orderedNeighbours(int startIndex, int width) {
        int[] result = new int[8];

        result[0] = startIndex + width;
        result[1] = startIndex + width - 1;
        result[2] = startIndex - 1;
        result[3] = startIndex - width - 1;
        result[4] = startIndex - width;
        result[5] = startIndex - width + 1;
        result[6] = startIndex + 1;
        result[7] = startIndex + width + 1;

        return result;
    }

    private static int[] reorderTab(int[] tab, int start) {
        int[] result = new int[tab.length];
        int startIndex = -1;
        int l = tab.length;

        for (int i = 0; i < l; i++) {
            if (tab[i] == start) {
                startIndex = (i + 1) % l;
                break;
            }
        }

        if (startIndex > -1) {
            for (int i = 0; i < l; i++) {
                int j = (i + startIndex) % l;
                result[i] = tab[j];
            }
        }

        return result;
    }

    private boolean saut(int a, int b) {
        return saut(toRGB(a), toRGB(b));
    }

    private static int colorStringToInt(String color) {
        int result = -1;

        try {
            if (color.length() == 6) {
                int a = Integer.parseInt(color.substring(0, 2), 16);
                int b = Integer.parseInt(color.substring(2, 4), 16);
                int c = Integer.parseInt(color.substring(4, 6), 16);
                result = compressRgb(255, a, b, c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    private void setCouleurFond(int couleurFond) {
        this.couleurFond = couleurFond;
    }

    private void setCouleurForme(int couleurForme) {
        this.couleurForme = couleurForme;
    }

    private void setInputImageFile(String inputImageFile) {
        this.inputImageFile = inputImageFile;
    }

    private void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    private void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

}