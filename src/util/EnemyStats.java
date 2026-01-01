package util;

/**
 * Immutable data class representing the final scaled stats for an enemy.
 * This class holds the computed values after applying both difficulty 
 * and level-based scaling.
 * 
 * Responsibilities:
 * - Store final computed enemy stats (health, damage, speed)
 * - Provide a clean way to pass scaled stats to Enemy instances
 * - Keep scaling logic results separate from raw stat definitions
 * 
 * Usage:
 *   EnemyStats stats = EnemyStats.calculate(baseHealth, baseDamage, baseSpeed, level);
 *   enemy.initialize(stats);
 */
public final class EnemyStats {

    private final int maxHealth;
    private final int damage;
    private final double speed;

    /**
     * Private constructor - use the static factory method calculate() instead.
     */
    private EnemyStats(int maxHealth, int damage, double speed) {
        this.maxHealth = maxHealth;
        this.damage = damage;
        this.speed = speed;
    }

    // ==================== GETTERS ====================

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getDamage() {
        return damage;
    }

    public double getSpeed() {
        return speed;
    }

    // ==================== STATIC FACTORY METHOD ====================

    /**
     * Calculates and returns the final scaled enemy stats.
     * 
     * This method:
     * 1. Retrieves the current difficulty from GameSettings
     * 2. Applies difficulty multipliers from DifficultyConfig
     * 3. Applies level-based scaling from DifficultyConfig
     * 4. Returns an immutable EnemyStats object with the final values
     * 
     * @param baseHealth Base health from EnemyType
     * @param baseDamage Base damage from EnemyType
     * @param baseSpeed Base speed from EnemyType
     * @param level Current game level/wave (1-indexed)
     * @return EnemyStats containing the final scaled values
     */
    public static EnemyStats calculate(int baseHealth, int baseDamage, double baseSpeed, int level) {
        GameSettings.Difficulty difficulty = GameSettings.getDifficulty();

        // Get combined multipliers (difficulty × level scaling)
        double healthMult = DifficultyConfig.getCombinedHealthMultiplier(difficulty, level);
        double damageMult = DifficultyConfig.getCombinedDamageMultiplier(difficulty, level);
        double speedMult = DifficultyConfig.getCombinedSpeedMultiplier(difficulty, level);

        // Apply multipliers to base stats
        int scaledHealth = (int) Math.max(1, baseHealth * healthMult);
        int scaledDamage = (int) Math.max(1, baseDamage * damageMult);
        double scaledSpeed = baseSpeed * speedMult;

        return new EnemyStats(scaledHealth, scaledDamage, scaledSpeed);
    }

    /**
     * Alternative factory method that accepts a difficulty parameter directly.
     * Useful for previewing stats at different difficulties without changing global state.
     * 
     * @param baseHealth Base health from EnemyType
     * @param baseDamage Base damage from EnemyType
     * @param baseSpeed Base speed from EnemyType
     * @param level Current game level/wave
     * @param difficulty The difficulty to use for calculation
     * @return EnemyStats containing the final scaled values
     */
    public static EnemyStats calculate(int baseHealth, int baseDamage, double baseSpeed, 
                                       int level, GameSettings.Difficulty difficulty) {
        double healthMult = DifficultyConfig.getCombinedHealthMultiplier(difficulty, level);
        double damageMult = DifficultyConfig.getCombinedDamageMultiplier(difficulty, level);
        double speedMult = DifficultyConfig.getCombinedSpeedMultiplier(difficulty, level);

        int scaledHealth = (int) Math.max(1, baseHealth * healthMult);
        int scaledDamage = (int) Math.max(1, baseDamage * damageMult);
        double scaledSpeed = baseSpeed * speedMult;

        return new EnemyStats(scaledHealth, scaledDamage, scaledSpeed);
    }

    @Override
    public String toString() {
        return String.format("EnemyStats[health=%d, damage=%d, speed=%.4f]", 
                             maxHealth, damage, speed);
    }
}
