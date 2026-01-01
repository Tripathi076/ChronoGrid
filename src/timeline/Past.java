package timeline;

import map.GridMap;
import map.Node;

/**
 * PAST Timeline - The Ancient Era
 * 
 * Features:
 * - Enemies move SLOWER (0.8x speed)
 * - Player deals MORE damage (1.2x)
 * - Player moves slightly slower (0.9x)
 * - Energy regenerates FASTER (1.5x)
 * - Can see HIDDEN PATHS (ancient secrets)
 * - More obstacles and ruins
 * - Sepia/Blue tinted visuals
 * 
 * Special Ability: ANCESTRAL SIGHT
 * - Reveals hidden collectibles and secret paths
 * - Shows where enemies will spawn in other timelines
 */
public class Past extends Timeline {
    
    // Past-specific modifiers
    public static final double ENEMY_SPEED_MULT = 0.8;
    public static final double PLAYER_DAMAGE_MULT = 1.2;
    public static final double PLAYER_SPEED_MULT = 0.9;
    public static final double ENERGY_REGEN_MULT = 1.5;
    public static final String AMBIENT_COLOR = "#4a90d9";
    public static final String AMBIENT_COLOR_DARK = "#1a3a5f";
    
    private boolean ancestralSightActive = false;
    private long ancestralSightEnd = 0;

    public Past(GridMap map) {
        super(map);
        this.energyCostMultiplier = 0.8; // Abilities cost less in past
        this.visibilityRange = 0.9; // Slightly reduced visibility
        this.hasSpecialVision = true; // Can see hidden things
    }

    @Override
    public void applyChange(int x, int y) {
        // In the past, some paths are blocked by ancient ruins
        Node node = map.getNode(x, y);
        if (node != null) {
            // Create obstacles (ancient ruins)
            map.modifyTile(x, y, false);
        }
    }
    
    @Override
    public String getAmbientColor() {
        return AMBIENT_COLOR;
    }
    
    @Override
    public String getName() {
        return "PAST";
    }
    
    @Override
    public String getEffectDescription() {
        return "Ancient Power: Enemies slower, you deal more damage";
    }
    
    /**
     * Activate Ancestral Sight - reveals hidden elements for 5 seconds.
     */
    public boolean activateAncestralSight() {
        if (ancestralSightActive) return false;
        
        ancestralSightActive = true;
        ancestralSightEnd = System.currentTimeMillis() + 5000;
        return true;
    }
    
    public void update() {
        if (ancestralSightActive && System.currentTimeMillis() > ancestralSightEnd) {
            ancestralSightActive = false;
        }
    }
    
    public boolean isAncestralSightActive() {
        return ancestralSightActive;
    }
    
    /**
     * Check if a hidden path exists at coordinates.
     * Only visible in PAST timeline with special vision.
     */
    public boolean hasHiddenPath(int x, int y) {
        // Hidden paths exist at specific pattern locations
        return (x + y) % 7 == 0 && ancestralSightActive;
    }
}
