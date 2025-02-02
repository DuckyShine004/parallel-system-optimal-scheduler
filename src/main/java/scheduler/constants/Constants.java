package scheduler.constants;

/**
 * This class defines constants that are used throughout the project
 */
public class Constants {
    public static final String TEST_INPUT_DOT_FILE_PATH = "src/test/resources/dotfiles/input/";
    public static final String TEST_OUTPUT_DOT_FILE_PATH = "src/test/resources/dotfiles/output/";
    public static final String RANDOM_OUTPUT_DOT_FILE_PATH = "src/main/resources/dotfiles/random/";
    public static final String TEST_CRAWLED_DOT_FILE_PATH = "src/test/resources/dotfiles/crawled/";

    public static final byte NUMBER_OF_REQUIRED_ARGUMENTS = 2;

    public static final int INFINITY_32 = Integer.MAX_VALUE;
    public static final int NUMBER_OF_NODES_LOWER_BOUND = 10;
    public static final int NUMBER_OF_NODES_UPPER_BOUND = 20;
    public static final int WEIGHT_LOWER_BOUND = 100;
    public static final int WEIGHT_UPPER_BOUND = 1000;
    public static final int CPU_AND_RAM_UPDATE_INTERVAL = 100;
    public static final int GANTT_CHART_UPDATE_INTERVAL = 10;
    public static final int GANTT_CHART_WIDTH = 270;
    public static final int GANTT_CHART_HEIGHT = 1180;
    public static final int MAXIMUM_NUMBER_OF_DATA_POINTS = 10;
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;

    public static final double EDGE_RATIO_LOWER_BOUND = 0.6;
    public static final double EDGE_RATIO_UPPER_BOUND = 1.0;
    public static final double SOURCE_NODE_RATIO_LOWER_BOUND = 0.2;
    public static final double SOURCE_NODE_RATIO_UPPER_BOUND = 0.4;
    public static final double LAYER_RATIO_LOWER_BOUND = 0.1;
    public static final double LAYER_RATIO_UPPER_BOUND = 0.3;
}
