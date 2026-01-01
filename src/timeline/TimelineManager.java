package timeline;

import map.GridMap;
import java.util.*;

/**
 * Advanced Timeline Manager - Controls all timeline mechanics and effects.
 * 
 * Features:
 * - Timeline-specific abilities and buffs
 * - Temporal Echo system (ghost recordings of player movements)
 * - Time Paradox mechanics
 * - Timeline Stability system
 * - Causality Chain reactions
 * - Temporal Rewind
 */
public class TimelineManager {
    
    private Past past;
    private Present present;
    private Future future;
    private GridMap map;
    
    // Current state
    private TimelineType currentTimeline = TimelineType.PRESENT;
    private double stability = 100.0; // 0-100, low stability = paradox risk
    private int paradoxCount = 0;
    
    // Temporal Echo system - records player positions for ghost replay
    private List<TemporalEcho> pastEchoes = new ArrayList<>();
    private List<TemporalEcho> futureEchoes = new ArrayList<>();
    private List<EchoPoint> currentRecording = new ArrayList<>();
    private long recordingStartTime = 0;
    private static final int MAX_ECHO_DURATION = 5000; // 5 seconds of recording
    private static final int MAX_ECHOES = 3;
    
    // Timeline shift cooldown
    private long lastShiftTime = 0;
    private static final long SHIFT_COOLDOWN = 500; // ms
    
    // Ability cooldowns
    private long lastRewindTime = 0;
    private long lastTimeStopTime = 0;
    private long lastParadoxBlastTime = 0;
    private static final long REWIND_COOLDOWN = 10000; // 10 seconds
    private static final long TIME_STOP_COOLDOWN = 15000; // 15 seconds
    private static final long PARADOX_BLAST_COOLDOWN = 20000; // 20 seconds
    
    // Active effects
    private boolean timeSlowed = false;
    private boolean timeStopped = false;
    private long timeStopEnd = 0;
    private long timeSlowEnd = 0;
    private double timeScale = 1.0; // 1.0 = normal, 0.5 = slow, 0 = stopped
    
    // Causality chains
    private List<CausalityChain> activeChains = new ArrayList<>();
    
    // Rewind buffer - stores recent positions for rewind ability
    private Deque<RewindState> rewindBuffer = new ArrayDeque<>();
    private static final int REWIND_BUFFER_SIZE = 180; // ~3 seconds at 60fps
    
    public TimelineManager(GridMap map) {
        this.map = map;
        this.past = new Past(map);
        this.present = new Present(map);
        this.future = new Future(map);
    }
    
    // ==================== TIMELINE SWITCHING ====================
    
    public enum TimelineType {
        PAST("PAST", "#4a90d9", -1),      // Blue - slower enemies, more obstacles
        PRESENT("PRESENT", "#10b981", 0),  // Green - balanced
        FUTURE("FUTURE", "#a855f7", 1);    // Purple - faster enemies, more tech
        
        public final String name;
        public final String color;
        public final int modifier;
        
        TimelineType(String name, String color, int modifier) {
            this.name = name;
            this.color = color;
            this.modifier = modifier;
        }
    }
    
    /**
     * Switch to a different timeline with effects.
     * Returns true if switch was successful.
     */
    public boolean switchTimeline(TimelineType newTimeline, double playerX, double playerY) {
        if (System.currentTimeMillis() - lastShiftTime < SHIFT_COOLDOWN) {
            return false; // On cooldown
        }
        
        if (newTimeline == currentTimeline) {
            return false; // Already in this timeline
        }
        
        // Save echo of current position before switching
        if (currentRecording.size() > 0) {
            saveCurrentEcho();
        }
        
        // Apply timeline change to map
        Timeline targetTimeline = getTimeline(newTimeline);
        targetTimeline.applyChange((int) playerX, (int) playerY);
        
        // Reduce stability when switching rapidly
        stability = Math.max(0, stability - 2);
        
        // Check for paradox
        if (stability < 20 && Math.random() < 0.1) {
            triggerParadox(playerX, playerY);
        }
        
        // Start new recording
        currentRecording.clear();
        recordingStartTime = System.currentTimeMillis();
        
        TimelineType oldTimeline = currentTimeline;
        currentTimeline = newTimeline;
        lastShiftTime = System.currentTimeMillis();
        
        // Trigger causality chain if applicable
        checkCausalityTriggers(oldTimeline, newTimeline, playerX, playerY);
        
        return true;
    }
    
    private Timeline getTimeline(TimelineType type) {
        switch (type) {
            case PAST: return past;
            case FUTURE: return future;
            default: return present;
        }
    }
    
    // ==================== TIMELINE-SPECIFIC BUFFS ====================
    
    /**
     * Get the current timeline's buff effects.
     */
    public TimelineBuff getCurrentBuff() {
        switch (currentTimeline) {
            case PAST:
                return new TimelineBuff(
                    "Ancient Power",
                    0.8,   // Enemy speed multiplier (slower)
                    1.2,   // Player damage multiplier (stronger)
                    0.9,   // Player speed multiplier (slightly slower)
                    1.5,   // Energy regen multiplier (higher)
                    true,  // Can see hidden paths
                    false, // Cannot use tech abilities
                    "#4a90d9"
                );
            case FUTURE:
                return new TimelineBuff(
                    "Tech Surge",
                    1.3,   // Enemy speed multiplier (faster)
                    1.0,   // Player damage multiplier (normal)
                    1.2,   // Player speed multiplier (faster)
                    0.8,   // Energy regen multiplier (lower)
                    false, // Cannot see hidden paths
                    true,  // Can use tech abilities
                    "#a855f7"
                );
            default: // PRESENT
                return new TimelineBuff(
                    "Temporal Balance",
                    1.0, 1.0, 1.0, 1.0,
                    false, false,
                    "#10b981"
                );
        }
    }
    
    public static class TimelineBuff {
        public final String name;
        public final double enemySpeedMult;
        public final double playerDamageMult;
        public final double playerSpeedMult;
        public final double energyRegenMult;
        public final boolean canSeeHiddenPaths;
        public final boolean canUseTechAbilities;
        public final String color;
        
        public TimelineBuff(String name, double enemySpeed, double playerDamage, 
                           double playerSpeed, double energyRegen,
                           boolean hiddenPaths, boolean techAbilities, String color) {
            this.name = name;
            this.enemySpeedMult = enemySpeed;
            this.playerDamageMult = playerDamage;
            this.playerSpeedMult = playerSpeed;
            this.energyRegenMult = energyRegen;
            this.canSeeHiddenPaths = hiddenPaths;
            this.canUseTechAbilities = techAbilities;
            this.color = color;
        }
    }
    
    // ==================== TEMPORAL ECHO SYSTEM ====================
    
    /**
     * Record player position for echo replay.
     * Call this every frame while in a timeline.
     */
    public void recordEchoPoint(double x, double y, double angle, String action) {
        if (System.currentTimeMillis() - recordingStartTime > MAX_ECHO_DURATION) {
            return; // Stop recording after max duration
        }
        
        currentRecording.add(new EchoPoint(
            x, y, angle, action,
            System.currentTimeMillis() - recordingStartTime
        ));
    }
    
    /**
     * Save the current recording as a temporal echo.
     */
    private void saveCurrentEcho() {
        if (currentRecording.isEmpty()) return;
        
        List<TemporalEcho> targetList = (currentTimeline == TimelineType.PAST) 
            ? pastEchoes : futureEchoes;
        
        if (targetList.size() >= MAX_ECHOES) {
            targetList.remove(0); // Remove oldest echo
        }
        
        targetList.add(new TemporalEcho(new ArrayList<>(currentRecording), currentTimeline));
    }
    
    /**
     * Get active echoes for the current timeline.
     * Echoes from PAST appear in FUTURE and vice versa.
     */
    public List<TemporalEcho> getActiveEchoes() {
        switch (currentTimeline) {
            case PAST:
                return futureEchoes; // See echoes from future
            case FUTURE:
                return pastEchoes; // See echoes from past
            default:
                // In present, see both as faint ghosts
                List<TemporalEcho> combined = new ArrayList<>();
                combined.addAll(pastEchoes);
                combined.addAll(futureEchoes);
                return combined;
        }
    }
    
    public static class EchoPoint {
        public final double x;
        public final double y;
        public final double angle;
        public final String action; // "idle", "moving", "shooting", "dash"
        public final long timeOffset; // ms from start
        
        public EchoPoint(double x, double y, double angle, String action, long timeOffset) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.action = action;
            this.timeOffset = timeOffset;
        }
    }
    
    public static class TemporalEcho {
        public final List<EchoPoint> points;
        public final TimelineType sourceTimeline;
        public final long createdAt;
        public int currentIndex = 0;
        public boolean isPlaying = false;
        public long playStartTime = 0;
        
        public TemporalEcho(List<EchoPoint> points, TimelineType source) {
            this.points = points;
            this.sourceTimeline = source;
            this.createdAt = System.currentTimeMillis();
        }
        
        public void startPlayback() {
            isPlaying = true;
            playStartTime = System.currentTimeMillis();
            currentIndex = 0;
        }
        
        public EchoPoint getCurrentPoint() {
            if (!isPlaying || points.isEmpty()) return null;
            
            long elapsed = System.currentTimeMillis() - playStartTime;
            
            // Find the point closest to current time
            for (int i = currentIndex; i < points.size(); i++) {
                if (points.get(i).timeOffset >= elapsed) {
                    currentIndex = i;
                    return points.get(i);
                }
            }
            
            // Playback finished
            isPlaying = false;
            return null;
        }
        
        public boolean isFinished() {
            return !isPlaying && currentIndex >= points.size() - 1;
        }
    }
    
    // ==================== SPECIAL ABILITIES ====================
    
    /**
     * REWIND - Go back in time 3 seconds.
     * Restores health, position, and removes recent enemies.
     */
    public RewindResult activateRewind() {
        if (System.currentTimeMillis() - lastRewindTime < REWIND_COOLDOWN) {
            return new RewindResult(false, "Rewind on cooldown", 0, 0, 0);
        }
        
        if (rewindBuffer.isEmpty()) {
            return new RewindResult(false, "No rewind data", 0, 0, 0);
        }
        
        // Get state from 3 seconds ago (or oldest available)
        RewindState targetState = null;
        int stepsBack = Math.min(rewindBuffer.size(), REWIND_BUFFER_SIZE);
        
        Iterator<RewindState> it = rewindBuffer.descendingIterator();
        for (int i = 0; i < stepsBack && it.hasNext(); i++) {
            targetState = it.next();
        }
        
        if (targetState == null) {
            return new RewindResult(false, "Rewind failed", 0, 0, 0);
        }
        
        lastRewindTime = System.currentTimeMillis();
        stability = Math.max(0, stability - 15); // Costs stability
        
        return new RewindResult(true, "Time Rewound!", 
            targetState.x, targetState.y, targetState.health);
    }
    
    /**
     * Save current state to rewind buffer.
     * Call every frame.
     */
    public void saveRewindState(double x, double y, int health, int energy) {
        if (rewindBuffer.size() >= REWIND_BUFFER_SIZE) {
            rewindBuffer.removeFirst();
        }
        rewindBuffer.addLast(new RewindState(x, y, health, energy, System.currentTimeMillis()));
    }
    
    public static class RewindState {
        public final double x;
        public final double y;
        public final int health;
        public final int energy;
        public final long timestamp;
        
        public RewindState(double x, double y, int health, int energy, long timestamp) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.energy = energy;
            this.timestamp = timestamp;
        }
    }
    
    public static class RewindResult {
        public final boolean success;
        public final String message;
        public final double newX;
        public final double newY;
        public final int newHealth;
        
        public RewindResult(boolean success, String message, double x, double y, int health) {
            this.success = success;
            this.message = message;
            this.newX = x;
            this.newY = y;
            this.newHealth = health;
        }
    }
    
    /**
     * TIME STOP - Freeze all enemies for 3 seconds.
     * Only available in FUTURE timeline.
     */
    public boolean activateTimeStop() {
        if (currentTimeline != TimelineType.FUTURE) {
            return false; // Only in future
        }
        
        if (System.currentTimeMillis() - lastTimeStopTime < TIME_STOP_COOLDOWN) {
            return false; // On cooldown
        }
        
        timeStopped = true;
        timeStopEnd = System.currentTimeMillis() + 3000; // 3 seconds
        timeScale = 0.0;
        lastTimeStopTime = System.currentTimeMillis();
        stability = Math.max(0, stability - 10);
        
        return true;
    }
    
    /**
     * PARADOX BLAST - Damages all enemies in range.
     * Requires low stability (risky but powerful).
     */
    public ParadoxBlastResult activateParadoxBlast(double playerX, double playerY) {
        if (System.currentTimeMillis() - lastParadoxBlastTime < PARADOX_BLAST_COOLDOWN) {
            return new ParadoxBlastResult(false, "On cooldown", 0, 0);
        }
        
        if (stability > 50) {
            return new ParadoxBlastResult(false, "Stability too high (need < 50)", 0, 0);
        }
        
        // Power scales inversely with stability
        double power = (50 - stability) / 50.0; // 0 to 1
        int damage = (int)(50 + power * 100); // 50-150 damage
        double radius = 3 + power * 4; // 3-7 tile radius
        
        lastParadoxBlastTime = System.currentTimeMillis();
        paradoxCount++;
        stability = Math.min(100, stability + 30); // Restores some stability
        
        return new ParadoxBlastResult(true, "PARADOX BLAST!", damage, radius);
    }
    
    public static class ParadoxBlastResult {
        public final boolean success;
        public final String message;
        public final int damage;
        public final double radius;
        
        public ParadoxBlastResult(boolean success, String message, int damage, double radius) {
            this.success = success;
            this.message = message;
            this.damage = damage;
            this.radius = radius;
        }
    }
    
    /**
     * TIME SLOW - Slow down time to 50% for 5 seconds.
     * Available in any timeline but costs energy.
     */
    public boolean activateTimeSlow() {
        if (timeSlowed || timeStopped) {
            return false;
        }
        
        timeSlowed = true;
        timeSlowEnd = System.currentTimeMillis() + 5000;
        timeScale = 0.5;
        
        return true;
    }
    
    // ==================== PARADOX SYSTEM ====================
    
    /**
     * Trigger a time paradox - spawns paradox enemies or effects.
     */
    private void triggerParadox(double playerX, double playerY) {
        paradoxCount++;
        
        // Paradox effects based on count
        if (paradoxCount >= 5) {
            // Major paradox - timeline instability wave
            activeChains.add(new CausalityChain(
                CausalityType.PARADOX_WAVE,
                playerX, playerY,
                System.currentTimeMillis()
            ));
        }
    }
    
    // ==================== CAUSALITY CHAINS ====================
    
    public enum CausalityType {
        PARADOX_WAVE,      // Damages everything nearby
        ECHO_RESONANCE,    // Echoes become solid and help player
        TIME_RIFT,         // Creates a portal between timelines
        TEMPORAL_ANCHOR    // Stabilizes the timeline
    }
    
    public static class CausalityChain {
        public final CausalityType type;
        public final double originX;
        public final double originY;
        public final long startTime;
        public double radius = 0;
        public boolean active = true;
        
        public CausalityChain(CausalityType type, double x, double y, long startTime) {
            this.type = type;
            this.originX = x;
            this.originY = y;
            this.startTime = startTime;
        }
        
        public void update() {
            long elapsed = System.currentTimeMillis() - startTime;
            radius = elapsed / 100.0; // Expands over time
            
            if (elapsed > 3000) {
                active = false;
            }
        }
    }
    
    private void checkCausalityTriggers(TimelineType from, TimelineType to, double x, double y) {
        // Rapid switching creates time rifts
        if (System.currentTimeMillis() - lastShiftTime < 1000) {
            activeChains.add(new CausalityChain(
                CausalityType.TIME_RIFT, x, y, System.currentTimeMillis()
            ));
        }
    }
    
    // ==================== UPDATE ====================
    
    /**
     * Update timeline effects. Call every frame.
     */
    public void update() {
        // Update time effects
        if (timeStopped && System.currentTimeMillis() > timeStopEnd) {
            timeStopped = false;
            timeScale = timeSlowed ? 0.5 : 1.0;
        }
        
        if (timeSlowed && System.currentTimeMillis() > timeSlowEnd) {
            timeSlowed = false;
            if (!timeStopped) {
                timeScale = 1.0;
            }
        }
        
        // Gradually restore stability
        if (stability < 100) {
            stability = Math.min(100, stability + 0.02);
        }
        
        // Update causality chains
        Iterator<CausalityChain> chainIt = activeChains.iterator();
        while (chainIt.hasNext()) {
            CausalityChain chain = chainIt.next();
            chain.update();
            if (!chain.active) {
                chainIt.remove();
            }
        }
        
        // Update echo playback
        for (TemporalEcho echo : getActiveEchoes()) {
            if (echo.isPlaying) {
                echo.getCurrentPoint(); // Advances the echo
            }
        }
    }
    
    /**
     * Start playing all echoes for current timeline.
     */
    public void playEchoes() {
        for (TemporalEcho echo : getActiveEchoes()) {
            if (!echo.isFinished()) {
                echo.startPlayback();
            }
        }
    }
    
    // ==================== GETTERS ====================
    
    public TimelineType getCurrentTimeline() { return currentTimeline; }
    public double getStability() { return stability; }
    public int getParadoxCount() { return paradoxCount; }
    public double getTimeScale() { return timeScale; }
    public boolean isTimeStopped() { return timeStopped; }
    public boolean isTimeSlowed() { return timeSlowed; }
    public List<CausalityChain> getActiveChains() { return activeChains; }
    
    public long getRewindCooldownRemaining() {
        return Math.max(0, REWIND_COOLDOWN - (System.currentTimeMillis() - lastRewindTime));
    }
    
    public long getTimeStopCooldownRemaining() {
        return Math.max(0, TIME_STOP_COOLDOWN - (System.currentTimeMillis() - lastTimeStopTime));
    }
    
    public long getParadoxBlastCooldownRemaining() {
        return Math.max(0, PARADOX_BLAST_COOLDOWN - (System.currentTimeMillis() - lastParadoxBlastTime));
    }
    
    public Past getPast() { return past; }
    public Present getPresent() { return present; }
    public Future getFuture() { return future; }
}
