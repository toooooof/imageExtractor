package net.toooooof;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Image {

    private int width;
    private int height;
    private int[] pix;
    private BufferedImage bufferedImage;

    private int[] averageBackground;

    private static final int BLACK_BOOL = 1;
    private static final int WHITE_BOOL = 0;

    private final Extractor.Conf conf;

    public Image(Extractor.Conf conf) {
        this.conf = conf;
        String fileName = conf.getInputFileName();

        try {
            this.bufferedImage = ImageIO.read(new File(fileName));
            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();

            System.out.println("\n  Reading file " + fileName + " [" + width + "x" + height + "]");

            this.pix = new int[this.width * this.height];

            List<Integer> colors = new ArrayList<>();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < conf.getNbLinesReference(); y++) {

                    int color = bufferedImage.getRGB(x, y);
                    colors.add(color);

                }
            }

            this.averageBackground = toRGB(averageBackgroudColor(colors));

            // compute if each pixel is background or not
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {

                    int color = bufferedImage.getRGB(x, y);

                    if (included(color)) {
                        pix[x + y * width] = WHITE_BOOL;
                    } else {
                        pix[x + y * width] = BLACK_BOOL;
                    }

                }
            }

            // extract each separated black (= non-background) zone
            List<Set<Integer>> simpleZones = extractBlackZones();
            List<Zone> zones = simpleZones.stream().map(z -> new Zone(z, width, conf)).collect(Collectors.toList());

            // convert zones to pix array
            pix = buildPixelsFromZones(simpleZones);

            // save each zone as a new image
            saveSubImage(zones);

            zones.forEach(zone -> {
                System.out.println(String.format("convert %s -rotate %f %s", zone.getFileName(), Math.toDegrees(-zone.getAngle()), "rotated-" + zone.getFileName()));
            });

        } catch (IOException e) {
            System.err.println("Error while reading file : " + fileName);
            e.printStackTrace();
        }
    }

    private int averageBackgroudColor(List<Integer> colors) {
        List<Integer> reds = new ArrayList<>();
        List<Integer> greens = new ArrayList<>();
        List<Integer> blues = new ArrayList<>();

        colors.forEach(c -> {
            reds.add( (c >> 16) & 0xFF);
            greens.add( (c >> 8) & 0xFF);
            blues.add( c & 0xFF);
        });

        int averageRed = reds.stream().mapToInt(Integer::intValue).sum() / reds.size();
        int averageGreen = greens.stream().mapToInt(Integer::intValue).sum() / greens.size();
        int averageBlue = blues.stream().mapToInt(Integer::intValue).sum() / blues.size();

        return (255 << 24) | (averageRed << 16) | (averageGreen << 8) | averageBlue;
    }

    private void saveSubImage(List<Zone> zones) {
        int cpt = 0;
        for (Zone zone : zones) {
            BufferedImage img = new BufferedImage(zone.getWidth(), zone.getHeight(), BufferedImage.TYPE_INT_ARGB);

           // fonctionne sans rotation
            for (int i = zone.getX() ; i < zone.getX() + zone.getWidth() ; i++) {
                for (int j = zone.getY() ; j < zone.getY() + zone.getHeight() ; j++) {
                    boolean belong = zone.belong(i,j);
                    if (belong) {
                        int color = bufferedImage.getRGB(i, j);
                        int alpha = 255;
                        color = (alpha << 24) | (color & 0x00FFFFFF);
                        img.setRGB(i - zone.getX(), j - zone.getY(), color);

                    }
                }
            }


            try {
                zone.setFileName("output-image" + (cpt++) + ".png");
                ImageIO.write(img, "png", new File(zone.getFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private List<Set<Integer>> extractBlackZones() {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> toExplore = new HashSet<>();
        List<Set<Integer>> zones = new ArrayList<>();

        for(int i = 0 ; i < Math.floor(width / 10) ; i++) {
            for(int j = 0 ; j < Math.floor(height / 10) ; j++) {
                toExplore.add(j * 10 * width + i * 10);
            }
        }

        while (!toExplore.isEmpty()) {
            //Set<Integer> temp = new HashSet<>();
            toExplore.forEach(i -> {
                if (!visited.contains(i)) {
                    Set<Integer> temp = blackZone(i);
                    if (!temp.isEmpty()) {
                        zones.add(temp);
                        visited.addAll(temp);
                    }
                    visited.add(i);
                }
            });

            toExplore = toExplore.stream().filter(i -> !visited.contains(i)).collect(Collectors.toSet());
        }
        return zones.stream().filter(zone -> zone.size() > conf.getMinZoneSize()).collect(Collectors.toList());
    }

    private int[] buildPixelsFromZones(List<Set<Integer>> zones) {
        int[] result = new int[width * height];
        Arrays.fill(result, WHITE_BOOL);

        zones.forEach(zone -> {
            zone.forEach(coord -> {
                result[coord] = BLACK_BOOL;
            });
        });
        return result;
    }

    private boolean included(int color) {

        int[] colors = toRGB(color);
        int diff = Math.abs(colors[0] - averageBackground[0]);
        diff += Math.abs(colors[1] - averageBackground[1]);
        diff += Math.abs(colors[2] - averageBackground[2]);
        diff += Math.abs(colors[3] - averageBackground[3]);

        return diff < conf.getTolerance();

    }

    private static int[] toRGB(int rgb) {
        int[] result = new int[4];
        result[0] = (rgb >> 24) & 0xFF;
        result[1] = (rgb >> 16) & 0xFF;
        result[2] = (rgb >> 8) & 0xFF;
        result[3] = rgb & 0xFF;

        return result;
    }

    private static int compressRgb(int alpha, int rouge, int vert, int bleu) {
        return (alpha << 24) + (rouge << 16) + (vert << 8) + bleu;
    }

    private Set<Integer> blackZone(int coord) {
        if (pix[coord] != BLACK_BOOL) {
            return Collections.emptySet();
        }

        Set<Integer> result = new HashSet<>();

        Set<Integer> visited = new HashSet<>();
        Set<Integer> toExplore = new HashSet<>();

        toExplore.add(coord);

        while (!toExplore.isEmpty()) {
            Set<Integer> temp = new HashSet<>();
            toExplore.forEach(i -> {
                if (pix[i] == BLACK_BOOL) {
                    result.add(i);
                    temp.addAll(neighbours(i));
                }

                visited.add(i);
            });
            toExplore = temp.stream().filter(i -> !visited.contains(i)).collect(Collectors.toSet());
        }

        return result;
    }

    private Set<Integer> neighbours(int coord) {
        Set<Integer> result = new HashSet<>();
        int x = coord % width;
        int y = (int) Math.floor(coord / width);
        if (y > 0) {
            result.add(coord - width);
        }
        if (y < (height-1)) {
            result.add(coord + width);
        }
        if (x > 0) {
            result.add(coord - 1);
        }
        if (x < (width-1)) {
            result.add(coord + 1);
        }

        return result;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return Arrays.stream(pix).map(i -> i == BLACK_BOOL ? Color.BLACK.getRGB() : Color.WHITE.getRGB()).toArray();
    }
}
