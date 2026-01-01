package util;

/**
 * Global game settings that persist across the game session.
 * 
 * Responsibilities:
 * - Store the player's selected difficulty
 * - Store the current game level/wave
 * - Provide a central point for global game state
 * 
 * This class is the single source of truth for difficulty selection.
 * Scaling logic is delegated to DifficultyConfig and EnemyStats.
 */
public class GameSettings {
    
    /**
     * Available difficulty levels.
     * Each level affects enemy stats and spawn counts via DifficultyConfig.
     */
    public enum Difficulty { 
        EASY,    // Reduced enemy stats, fewer spawns
        MEDIUM,  // Baseline experience
        HARD     // Increased enemy stats, more spawns
    }
    
    // Current difficulty setting
    private static Difficulty selectedDifficulty = Difficulty.MEDIUM;
    
    // Current game level (can be used for level-based scaling across systems)
    private static int currentLevel = 1;

    /**
     * Sets the game difficulty.
     * Should be called from the main menu or settings screen.
     * 
     * @param difficulty The difficulty to set
     */
    public static void setDifficulty(Difficulty difficulty) {
        selectedDifficulty = difficulty;
    }

    /**
     * Gets the currently selected difficulty.
     * 
     * @return The current difficulty setting
     */
    public static Difficulty getDifficulty() {
        return selectedDifficulty;
    }
    
    /**
     * Sets the current game level.
     * Can be used by systems that need level information without
     * having direct access to GameEngine.
     * 
     * @param level The current level/wave number
     */
    public static void setCurrentLevel(int level) {
        currentLevel = Math.max(1, level);
    }
    
    /**
     * Gets the current game level.
     * 
     * @return The current level/wave number
     */
    public static int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Resets game settings to defaults.
     * Useful when starting a new game.
     */
    public static void reset() {
        selectedDifficulty = Difficulty.MEDIUM;
        currentLevel = 1;
    }
    
    /**
     * Returns a human-readable description of the current difficulty.
     * 
     * @return Description string for UI display
     */
    public static String getDifficultyDescription() {
        switch (selectedDifficulty) {
            case EASY:
                return "Easy - Enemies have reduced health, damage, and speed. Fewer enemies spawn.";
            case HARD:
                return "Hard - Enemies have increased health, damage, and speed. More enemies spawn.";
            case MEDIUM:
            default:
                return "Medium - Balanced gameplay experience.";
        }
    }
}
