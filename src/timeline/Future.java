package timeline;

import map.GridMap;
import map.Node;
import java.util.*;

/**
 * FUTURE Timeline - The Tech Era
 * 
 * Features:
 * - Enemies move FASTER (1.3x speed)
 * - Player moves FASTER (1.2x)
 * - Energy regenerates SLOWER (0.8x)
 * - Can use TECH ABILITIES
 * - Fewer obstacles, more open areas
 * - Purple/Neon tinted visuals
 * 
 * Special Abilities:
 * - TIME STOP: Freeze all enemies for 3 seconds
 * - TECH SCAN: Reveal all enemies on map
 * - QUANTUM DASH: Teleport short distance
 */
public class Future extends Timeline {
    
    // Future-specific modifiers
    public static final double ENEMY_SPEED_MULT = 1.3;
    public static final double PLAYER_DAMAGE_MULT = 1.0;
    public static final double PLAYER_SPEED_MULT = 1.2;
    public static final double ENERGY_REGEN_MULT = 0.8;
    public static final String AMBIENT_COLOR = "#a855f7";
    public static final String AMBIENT_COLOR_DARK = "#4c1d95";
    
    // Tech abilities
    private boolean techScanActive = false;
    private long techScanEnd = 0;
    private static final long TECH_SCAN_DURATION = 8000; // 8 seconds
    
    private long lastQuantumDashTime = 0;
    private static final long QUANTUM_DASH_COOLDOWN = 5000; // 5 seconds
    private static final double QUANTUM_DASH_DISTANCE = 4.0; // tiles
    
    // Tracked tech nodes for special interactions
    private List<TechNode> techNodes = new ArrayList<>();

    public Future(GridMap map) {
        super(map);
        this.energyCostMultiplier = 1.2; // Abilities cost more in future
        this.visibilityRange = 1.2; // Enhanced visibility (tech)
        this.hasSpecialVision = false;
        
        // Initialize some tech nodes
        initializeTechNodes();
    }
    
    private void initializeTechNodes() {
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            techNodes.add(new TechNode(
                rand.nextInt(20) + 2,
                rand.nextInt(20) + 2,
                TechNodeType.values()[rand.nextInt(TechNodeType.values().length)]
            ));
        }
    }

    @Override
    public void applyChange(int x, int y) {
        // In future, paths are clear (advanced construction)
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
        return "FUTURE";
    }
    
    @Override
    public String getEffectDescription() {
        return "Tech Surge: Move faster, use tech abilities";
    }
    
    /**
     * Activate Tech Scan - reveals all enemies for 8 seconds.
     */
    public boolean activateTechScan() {
        if (techScanActive) return false;
        
        techScanActive = true;
        techScanEnd = System.currentTimeMillis() + TECH_SCAN_DURATION;
        return true;
    }
    
    /**
     * Perform Quantum Dash - teleport in the aim direction.
     * Returns the new position or null if on cooldown.
     */
    public QuantumDashResult quantumDash(double fromX, double fromY, double angle) {
        if (System.currentTimeMillis() - lastQuantumDashTime < QUANTUM_DASH_COOLDOWN) {
            return new QuantumDashResult(false, fromX, fromY, "Quantum Dash on cooldown");
        }
        
        double newX = fromX + Math.cos(angle) * QUANTUM_DASH_DISTANCE;
        double newY = fromY + Math.sin(angle) * QUANTUM_DASH_DISTANCE;
        
        // Clamp to map bounds
        newX = Math.max(1, Math.min(23, newX));
        newY = Math.max(1, Math.min(23, newY));
        
        lastQuantumDashTime = System.currentTimeMillis();
        
        return new QuantumDashResult(true, newX, newY, "Quantum Dash!");
    }
    
    public void update() {
        if (techScanActive && System.currentTimeMillis() > techScanEnd) {
            techScanActive = false;
        }
    }
    
    public boolean isTechScanActive() {
        return techScanActive;
    }
    
    public long getQuantumDashCooldown() {
        return Math.max(0, QUANTUM_DASH_COOLDOWN - (System.currentTimeMillis() - lastQuantumDashTime));
    }
    
    public List<TechNode> getTechNodes() {
        return techNodes;
    }
    
    /**
     * Check if player is near a tech node and can interact.
     */
    public TechNode getNearbyTechNode(double playerX, double playerY) {
        for (TechNode node : techNodes) {
            if (!node.activated) {
                double dist = Math.sqrt(Math.pow(node.x - playerX, 2) + Math.pow(node.y - playerY, 2));
                if (dist < 1.5) {
                    return node;
                }
            }
        }
        return null;
    }
    
    public static class QuantumDashResult {
        public final boolean success;
        public final double newX;
        public final double newY;
        public final String message;
        
        public QuantumDashResult(boolean success, double x, double y, String message) {
            this.success = success;
            this.newX = x;
            this.newY = y;
            this.message = message;
        }
    }
    
    public enum TechNodeType {
        HEALING,      // Restores health
        ENERGY,       // Restores energy
        DAMAGE_BOOST, // Temporary damage boost
        SHIELD,       // Temporary shield
        REVEAL        // Reveals map
    }
    
    public static class TechNode {
        public final int x;
        public final int y;
        public final TechNodeType type;
        public boolean activated = false;
        
        public TechNode(int x, int y, TechNodeType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
        
        public void activate() {
            activated = true;
        }
    }
}
