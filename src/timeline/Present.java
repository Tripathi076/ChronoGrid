package timeline;

import map.GridMap;
import map.Node;

/**
 * PRESENT Timeline - The Current Era
 * 
 * Features:
 * - Balanced gameplay (1.0x all stats)
 * - Standard energy costs
 * - Normal visibility
 * - Actions here affect both PAST and FUTURE
 * - Green tinted visuals
 * 
 * Special Ability: TEMPORAL ANCHOR
 * - Creates a save point you can return to
 * - Stabilizes the timeline (increases stability)
 * - Reduces paradox risk
 */
public class Present extends Timeline {
    
    // Present-specific modifiers (balanced)
    public static final double ENEMY_SPEED_MULT = 1.0;
    public static final double PLAYER_DAMAGE_MULT = 1.0;
    public static final double PLAYER_SPEED_MULT = 1.0;
    public static final double ENERGY_REGEN_MULT = 1.0;
    public static final String AMBIENT_COLOR = "#10b981";
    public static final String AMBIENT_COLOR_DARK = "#064e3b";
    
    // Temporal Anchor system
    private boolean hasAnchor = false;
    private double anchorX = 0;
    private double anchorY = 0;
    private int anchorHealth = 0;
    private long anchorTime = 0;
    private static final long ANCHOR_DURATION = 30000; // 30 seconds

    public Present(GridMap map) {
        super(map);
        this.energyCostMultiplier = 1.0;
        this.visibilityRange = 1.0;
        this.hasSpecialVision = false;
    }

    @Override
    public void applyChange(int x, int y) {
        // In present, clear obstacles (modern clearing)
        Node node = map.getNode(x, y);
        if (node != null) {
            map.modifyTile(x, y, true);
        }
    }
    
    @Override
    public String getAmbientColor() {
        return AMBIENT_COLOR;
    }
    
    @Override
    public String getName() {
        return "PRESENT";
    }
    
    @Override
    public String getEffectDescription() {
        return "Temporal Balance: Stable timeline, create anchors";
    }
    
    /**
     * Create a temporal anchor at the current position.
     * Can return to this point within 30 seconds.
     */
    public boolean createAnchor(double x, double y, int health) {
        hasAnchor = true;
        anchorX = x;
        anchorY = y;
        anchorHealth = health;
        anchorTime = System.currentTimeMillis();
        return true;
    }
    
    /**
     * Return to the temporal anchor.
     * Returns the anchor data or null if no valid anchor.
     */
    public AnchorReturn returnToAnchor() {
        if (!hasAnchor) return null;
        
        if (System.currentTimeMillis() - anchorTime > ANCHOR_DURATION) {
            hasAnchor = false;
            return null; // Anchor expired
        }
        
        AnchorReturn result = new AnchorReturn(anchorX, anchorY, anchorHealth);
        hasAnchor = false; // Consume the anchor
        return result;
    }
    
    public void update() {
        // Check if anchor expired
        if (hasAnchor && System.currentTimeMillis() - anchorTime > ANCHOR_DURATION) {
            hasAnchor = false;
        }
    }
    
    public boolean hasActiveAnchor() {
        return hasAnchor && (System.currentTimeMillis() - anchorTime <= ANCHOR_DURATION);
    }
    
    public long getAnchorTimeRemaining() {
        if (!hasAnchor) return 0;
        return Math.max(0, ANCHOR_DURATION - (System.currentTimeMillis() - anchorTime));
    }
    
    public double getAnchorX() { return anchorX; }
    public double getAnchorY() { return anchorY; }
    
    public static class AnchorReturn {
        public final double x;
        public final double y;
        public final int health;
        
        public AnchorReturn(double x, double y, int health) {
            this.x = x;
            this.y = y;
            this.health = health;
        }
    }
}
