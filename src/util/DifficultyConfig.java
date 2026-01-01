package util;

/**
 * Centralized configuration for difficulty settings.
 * Defines base multipliers and scaling factors for each difficulty level.
 * 
 * Responsibilities:
 * - Store base stat multipliers per difficulty (health, damage, speed, spawn count)
 * - Store level-based scaling factors
 * - Provide a clean API to retrieve scaled values
 * 
 * This class separates difficulty DATA from scaling LOGIC.
 */
public final class DifficultyConfig {

    private DifficultyConfig() {
        // Prevent instantiation - utility class
    }

    // ==================== BASE ENEMY STATS PER DIFFICULTY ====================

    /**
     * Health multipliers per difficulty.
     * These multiply the base enemy health defined in EnemyType.
     */
    public static double getHealthMultiplier(GameSettings.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:   return 0.7;
            case HARD:   return 1.4;
            case MEDIUM:
            default:     return 1.0;
        }
    }

    /**
     * Damage multipliers per difficulty.
     * These multiply the base enemy damage defined in EnemyType.
     */
    public static double getDamageMultiplier(GameSettings.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:   return 0.6;
            case HARD:   return 1.5;
            case MEDIUM:
            default:     return 1.0;
        }
    }

    /**
     * Speed multipliers per difficulty.
     * These multiply the base enemy speed defined in EnemyType.
     */
    public static double getSpeedMultiplier(GameSettings.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:   return 0.85;
            case HARD:   return 1.2;
            case MEDIUM:
            default:     return 1.0;
        }
    }

    /**
     * Base enemy spawn count per difficulty.
     * This is the starting number of enemies before level scaling.
     */
    public static int getBaseSpawnCount(GameSettings.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:   return 3;
            case HARD:   return 8;
            case MEDIUM:
            default:     return 5;
        }
    }

    // ==================== LEVEL-BASED SCALING FACTORS ====================
    // These values determine how much stats increase per level.

    /** Health increases by this percentage per level (e.g., 0.12 = 12% per level) */
    public static final double LEVEL_HEALTH_SCALE = 0.12;

    /** Damage increases by this percentage per level */
    public static final double LEVEL_DAMAGE_SCALE = 0.08;

    /** Speed increases by this percentage per level */
    public static final double LEVEL_SPEED_SCALE = 0.04;

    /** Additional enemies spawned per level */
    public static final int LEVEL_SPAWN_INCREMENT = 1;

    // ==================== LEVEL UPGRADE THRESHOLDS ====================
    // Score/XP required to reach each level (index 0 = Level 1, index 14 = Level 15)
    
    private static final int[] LEVEL_THRESHOLDS = {
        10_000,      // Level 1
        25_000,      // Level 2
        45_000,      // Level 3
        80_000,      // Level 4
        130_000,     // Level 5
        195_000,     // Level 6
        280_000,     // Level 7
        390_000,     // Level 8
        530_000,     // Level 9
        710_000,     // Level 10
        940_000,     // Level 11
        1_230_000,   // Level 12
        1_590_000,   // Level 13
        2_040_000,   // Level 14
        2_600_000    // Level 15
    };

    /** Maximum level a player can reach */
    public static final int MAX_LEVEL = 15;

    /**
     * Gets the score threshold required to reach a specific level.
     * 
     * @param level The target level (1-15)
     * @return Score required to reach that level
     */
    public static int getLevelThreshold(int level) {
        if (level < 1) return 0;
        if (level > MAX_LEVEL) return LEVEL_THRESHOLDS[MAX_LEVEL - 1];
        return LEVEL_THRESHOLDS[level - 1];
    }

    /**
     * Gets the level threshold adjusted by difficulty.
     * EASY requires less score, HARD requires more.
     * 
     * @param level The target level (1-15)
     * @param difficulty The current difficulty
     * @return Adjusted score required to reach that level
     */
    public static int getLevelThreshold(int level, GameSettings.Difficulty difficulty) {
        int baseThreshold = getLevelThreshold(level);
        double multiplier;
        switch (difficulty) {
            case EASY:   multiplier = 0.75; break;  // 25% less score needed
            case HARD:   multiplier = 1.5;  break;  // 50% more score needed
            case MEDIUM:
            default:     multiplier = 1.0;  break;
        }
        return (int)(baseThreshold * multiplier);
    }

    /**
     * Calculates the player's level based on their current score.
     * Uses the current difficulty from GameSettings.
     * 
     * @param score The player's current score
     * @return The player's level (1 to MAX_LEVEL)
     */
    public static int calculateLevelFromScore(int score) {
        return calculateLevelFromScore(score, GameSettings.getDifficulty());
    }

    /**
     * Calculates the player's level based on their current score and difficulty.
     * 
     * @param score The player's current score
     * @param difficulty The difficulty setting
     * @return The player's level (1 to MAX_LEVEL)
     */
    public static int calculateLevelFromScore(int score, GameSettings.Difficulty difficulty) {
        for (int level = MAX_LEVEL; level >= 1; level--) {
            if (score >= getLevelThreshold(level, difficulty)) {
                return level;
            }
        }
        return 1; // Minimum level is 1
    }

    /**
     * Gets the score needed for the next level.
     * 
     * @param currentLevel The player's current level
     * @return Score needed for next level, or -1 if at max level
     */
    public static int getNextLevelThreshold(int currentLevel) {
        return getNextLevelThreshold(currentLevel, GameSettings.getDifficulty());
    }

    /**
     * Gets the score needed for the next level with difficulty adjustment.
     * 
     * @param currentLevel The player's current level
     * @param difficulty The difficulty setting
     * @return Score needed for next level, or -1 if at max level
     */
    public static int getNextLevelThreshold(int currentLevel, GameSettings.Difficulty difficulty) {
        if (currentLevel >= MAX_LEVEL) return -1;
        return getLevelThreshold(currentLevel + 1, difficulty);
    }

    /**
     * Calculates progress percentage towards the next level.
     * 
     * @param score Current score
     * @param currentLevel Current level
     * @return Progress percentage (0.0 to 1.0)
     */
    public static double getLevelProgress(int score, int currentLevel) {
        return getLevelProgress(score, currentLevel, GameSettings.getDifficulty());
    }

    /**
     * Calculates progress percentage towards the next level.
     * 
     * @param score Current score
     * @param currentLevel Current level
     * @param difficulty The difficulty setting
     * @return Progress percentage (0.0 to 1.0)
     */
    public static double getLevelProgress(int score, int currentLevel, GameSettings.Difficulty difficulty) {
        if (currentLevel >= MAX_LEVEL) return 1.0;
        
        int currentThreshold = currentLevel > 1 ? getLevelThreshold(currentLevel, difficulty) : 0;
        int nextThreshold = getLevelThreshold(currentLevel + 1, difficulty);
        int range = nextThreshold - currentThreshold;
        
        // Prevent division by zero
        if (range <= 0) return 1.0;
        
        int progress = score - currentThreshold;
        double ratio = (double) progress / range;
        
        // Clamp between 0.0 and 1.0
        if (ratio < 0.0) return 0.0;
        if (ratio > 1.0) return 1.0;
        return ratio;
    }

    // ==================== LEVEL SCALING MULTIPLIER METHODS ====================

    /**
     * Returns the level-based health multiplier.
     * Formula: 1.0 + (level - 1) * LEVEL_HEALTH_SCALE
     * 
     * @param level Current game level/wave (1-indexed)
     * @return Multiplier for health based on level
     */
    public static double getLevelHealthMultiplier(int level) {
        return 1.0 + Math.max(0, level - 1) * LEVEL_HEALTH_SCALE;
    }

    /**
     * Returns the level-based damage multiplier.
     * 
     * @param level Current game level/wave (1-indexed)
     * @return Multiplier for damage based on level
     */
    public static double getLevelDamageMultiplier(int level) {
        return 1.0 + Math.max(0, level - 1) * LEVEL_DAMAGE_SCALE;
    }

    /**
     * Returns the level-based speed multiplier.
     * 
     * @param level Current game level/wave (1-indexed)
     * @return Multiplier for speed based on level
     */
    public static double getLevelSpeedMultiplier(int level) {
        return 1.0 + Math.max(0, level - 1) * LEVEL_SPEED_SCALE;
    }

    /**
     * Returns the total enemy spawn count for a given level.
     * Combines base spawn count (from difficulty) with level-based increment.
     * 
     * @param difficulty The selected difficulty
     * @param level Current game level/wave (1-indexed)
     * @return Total number of enemies to spawn
     */
    public static int getSpawnCount(GameSettings.Difficulty difficulty, int level) {
        int baseCount = getBaseSpawnCount(difficulty);
        int levelBonus = Math.max(0, level - 1) * LEVEL_SPAWN_INCREMENT;
        return baseCount + levelBonus;
    }

    // ==================== COMBINED SCALING (CONVENIENCE) ====================

    /**
     * Returns the combined health multiplier (difficulty × level).
     * 
     * @param difficulty The selected difficulty
     * @param level Current game level/wave
     * @return Combined multiplier for enemy health
     */
    public static double getCombinedHealthMultiplier(GameSettings.Difficulty difficulty, int level) {
        return getHealthMultiplier(difficulty) * getLevelHealthMultiplier(level);
    }

    /**
     * Returns the combined damage multiplier (difficulty × level).
     * 
     * @param difficulty The selected difficulty
     * @param level Current game level/wave
     * @return Combined multiplier for enemy damage
     */
    public static double getCombinedDamageMultiplier(GameSettings.Difficulty difficulty, int level) {
        return getDamageMultiplier(difficulty) * getLevelDamageMultiplier(level);
    }

    /**
     * Returns the combined speed multiplier (difficulty × level).
     * 
     * @param difficulty The selected difficulty
     * @param level Current game level/wave
     * @return Combined multiplier for enemy speed
     */
    public static double getCombinedSpeedMultiplier(GameSettings.Difficulty difficulty, int level) {
        return getSpeedMultiplier(difficulty) * getLevelSpeedMultiplier(level);
    }
}
