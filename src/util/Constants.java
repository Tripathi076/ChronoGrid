package util;

/**
 * Global constants for ChronoGrid game.
 */
public final class Constants {
    
    private Constants() {
        // Prevent instantiation
    }

    // ==================== WINDOW ====================
    public static final String GAME_TITLE = "ChronoGrid";
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;

    // ==================== GRID ====================
    public static final int DEFAULT_GRID_WIDTH = 50;
    public static final int DEFAULT_GRID_HEIGHT = 50;
    public static final int CELL_SIZE = 16;
    public static final int MIN_GRID_SIZE = 10;
    public static final int MAX_GRID_SIZE = 200;

    // ==================== TIMELINE ====================
    public static final int PAST_MODIFIER = -100;
    public static final int PRESENT_MODIFIER = 0;
    public static final int FUTURE_MODIFIER = 100;
    public static final int TIMELINE_SHIFT_COOLDOWN = 3000; // ms

    // ==================== PLAYER ====================
    public static final int MAX_HEALTH = 100;
    public static final int MAX_ENERGY = 100;
    public static final int ENERGY_REGEN_RATE = 5;
    public static final int MOVE_COST = 1;
    public static final int ATTACK_COST = 10;
    public static final int ABILITY_COST = 20;

    // ==================== AI ====================
    public static final int AI_UPDATE_INTERVAL = 100; // ms
    public static final int MAX_PATH_LENGTH = 1000;
    public static final double OBSTACLE_RATIO = 0.2;
    public static final int HEAT_MAP_DECAY_INTERVAL = 5000; // ms
    public static final double HEAT_MAP_DECAY_FACTOR = 0.9;

    // ==================== GAME LOOP ====================
    public static final int TARGET_FPS = 60;
    public static final long FRAME_TIME_NS = 1_000_000_000 / TARGET_FPS;
    public static final int TICK_RATE = 20; // Game logic updates per second
    public static final long TICK_TIME_MS = 1000 / TICK_RATE;

    // ==================== SAVE/LOAD ====================
    public static final String SAVE_DIRECTORY = "saves/";
    public static final String SAVE_EXTENSION = ".chrono";
    public static final String CONFIG_FILE = "config.json";
    public static final int MAX_SAVE_SLOTS = 10;

    // ==================== COLORS (RGB values) ====================
    protected static final int[] COLOR_PAST = {100, 100, 180};      // Blueish
    protected static final int[] COLOR_PRESENT = {100, 180, 100};   // Greenish
    protected static final int[] COLOR_FUTURE = {180, 100, 180};    // Purplish
    protected static final int[] COLOR_PLAYER = {255, 215, 0};      // Gold
    protected static final int[] COLOR_ENEMY = {255, 50, 50};       // Red
    protected static final int[] COLOR_OBSTACLE = {60, 60, 60};     // Dark gray
    protected static final int[] COLOR_WALKABLE = {200, 200, 200};  // Light gray

    // ==================== KEYS ====================
    public static final String KEY_MOVE_UP = "W";
    public static final String KEY_MOVE_DOWN = "S";
    public static final String KEY_MOVE_LEFT = "A";
    public static final String KEY_MOVE_RIGHT = "D";
    public static final String KEY_SHIFT_PAST = "1";
    public static final String KEY_SHIFT_PRESENT = "2";
    public static final String KEY_SHIFT_FUTURE = "3";
    public static final String KEY_PAUSE = "ESCAPE";
    public static final String KEY_INTERACT = "E";

    // ==================== DEBUG ====================
    public static final boolean DEBUG_MODE = true;
    public static final boolean SHOW_PATH_OVERLAY = true;
    public static final boolean SHOW_HEAT_MAP = true;
    public static final boolean SHOW_FPS = true;
}
