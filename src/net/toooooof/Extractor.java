package net.toooooof;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Extractor {

    public static final int DEFAULT_TOLERANCE = 70;
    public static final int DEFAULT_MIN_ZONE_SIZE = 5000;
    public static final int DEFAULT_NB_LINES_REFERENCE = 10;
    public static final double DEFAULT_SEARCH_BOUNDARIES_PERCENTAGE = 0.80;
    public static final String DEFAULT_OUTPUT_FILENAME = "output";

    public static void main(String[] args) {

        System.out.println("Starting image parsing at " + new Date());
        long now =  System.currentTimeMillis();

        try {
            Conf conf = parseOptions(args);
            System.out.println(conf);
            new Image(conf);

        } catch (IllegalArgumentException e) {
            System.err.println("Error in input parameters: " + e.getMessage());

            printHelp();

            System.exit(1);
        }

        System.out.println("Duration : " + (System.currentTimeMillis() - now) + "ms");

    }

    public static void printHelp() {
        // add output directory

        System.out.println("Usage : \n\n      Extractor <input_file> [--option value]{n}");
        System.out.println("\n\n  Options:\n  ========\n");
        System.out.println("    --tolerance <int> : Tolerance toward the background color (infered or provided). The higher the more tolerant. Default: " + DEFAULT_TOLERANCE);
        System.out.println("\n    --min-zone-size <int> : if a detected zone is less than this size (number of pixels), it is discarded. Default: " + DEFAULT_MIN_ZONE_SIZE);
        System.out.println("\n    --nb-lines-reference <int> : Number of top lines scanned in order to detected average background color. Default: " + DEFAULT_NB_LINES_REFERENCE);
        System.out.println("\n    --background-color <RRGGBB> : Expected background color. Overrides top lines scanning. NOT IMPLEMENTED");
        System.out.println("\n    --search-boundaries-percentage <double> : each border of each zone is scanned in order to determine the rotation. It is not 100%, so rounded corder are ignored. Default: " + DEFAULT_SEARCH_BOUNDARIES_PERCENTAGE);
        System.out.println("\n    --expected-zones <int> : the number of elements on the source image. Throw an error if different from what is found. Ignored if not provided");
        System.out.println("\n    --output-files <string,string> : Comma separated strings for output files. Must match --expected-zones if provided. Overrides --ouput-file-schema . Ignored if not provided");
        System.out.println("\n    --output-file-pattern <string> : Output file base name (incremented). Do not add file extension (png implied). Default: " + DEFAULT_OUTPUT_FILENAME);
    }

    public static Conf parseOptions(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No argument provided");
        }

        if (args.length % 2 == 0) {
            throw new IllegalArgumentException("Wrong number of arguments");
        }

        Conf conf = new Conf();
        conf.setInputFileName(args[0]);

        for (int i = 0 ; i < (args.length - 1) / 2 ; i++) {
            String k = args[i*2 + 1];
            String v = args[i*2 + 2];

            switch (k) {
                case "--tolerance":
                    conf.setTolerance(Integer.parseInt(v));
                    break;
                case "--min-zone-size":
                    conf.setMinZoneSize(Integer.parseInt(v));
                    break;
                case "--nb-lines-reference":
                    conf.setNbLinesReference(Integer.parseInt(v));
                    break;
                case "--search-boundaries-percentage":
                    conf.setSearchBoundariesPercentage(Double.parseDouble(v));
                    break;
                case "--output-file-pattern":
                    conf.setOutputFilePattern(v);
                    break;
                case "--output-files":
                    conf.setOutputFiles(Arrays.asList(v.split(",")));
                    if (conf.getExpectedElements() > -1 && conf.getExpectedElements() != conf.getOutputFiles().size()) {
                        throw new IllegalArgumentException("Expecting " + conf.getExpectedElements() + " elements, but having " + conf.getOutputFiles().size() + " output file names");
                    }
                    break;
                case "--expected-zones":
                    conf.setExpectedElements(Integer.parseInt(v));
                    if (conf.getExpectedElements() > -1 && conf.getOutputFiles() != null && conf.getExpectedElements() != conf.getOutputFiles().size()) {
                        throw new IllegalArgumentException("Expecting " + conf.getExpectedElements() + " elements, but having " + conf.getOutputFiles().size() + " output file names");
                    }
                    break;
                default: throw new IllegalArgumentException("Argument unknown: " + k);
            }
        }

        return conf;
    }

    public static class Conf {

        private String inputFileName;
        private int tolerance = DEFAULT_TOLERANCE;
        private int minZoneSize = DEFAULT_MIN_ZONE_SIZE;
        private int nbLinesReference = DEFAULT_NB_LINES_REFERENCE;
        private double searchBoundariesPercentage = DEFAULT_SEARCH_BOUNDARIES_PERCENTAGE;
        private String outputFilePattern = DEFAULT_OUTPUT_FILENAME;
        private List<String> outputFiles;
        private int expectedElements = -1;

        public String getInputFileName() {
            return inputFileName;
        }

        public void setInputFileName(String inputFileName) {
            this.inputFileName = inputFileName;
        }

        public int getTolerance() {
            return tolerance;
        }

        public void setTolerance(int tolerance) {
            this.tolerance = tolerance;
        }

        public int getMinZoneSize() {
            return minZoneSize;
        }

        public void setMinZoneSize(int minZoneSize) {
            this.minZoneSize = minZoneSize;
        }

        public int getNbLinesReference() {
            return nbLinesReference;
        }

        public void setNbLinesReference(int nbLinesReference) {
            this.nbLinesReference = nbLinesReference;
        }

        public double getSearchBoundariesPercentage() {
            return searchBoundariesPercentage;
        }

        public void setSearchBoundariesPercentage(double searchBoundariesPercentage) {
            this.searchBoundariesPercentage = searchBoundariesPercentage;
        }

        public String getOutputFilePattern() {
            return outputFilePattern;
        }

        public void setOutputFilePattern(String outputFilePattern) {
            this.outputFilePattern = outputFilePattern;
        }

        public List<String> getOutputFiles() {
            return outputFiles;
        }

        public void setOutputFiles(List<String> outputFiles) {
            this.outputFiles = outputFiles;
        }

        public int getExpectedElements() {
            return expectedElements;
        }

        public void setExpectedElements(int expectedElements) {
            this.expectedElements = expectedElements;
        }

        @Override
        public String toString() {
            return "Conf{" +
                    "inputFileName='" + inputFileName + '\'' +
                    ", tolerance=" + tolerance +
                    ", minZoneSize=" + minZoneSize +
                    ", nbLinesReference=" + nbLinesReference +
                    ", searchBoundariesPercentage=" + searchBoundariesPercentage +
                    ", outputFilePattern='" + outputFilePattern + '\'' +
                    '}';
        }
    }
}
