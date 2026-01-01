package timeline;

import map.GridMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Base class for all timeline types.
 * Each timeline has unique properties and effects on the game world.
 */
public abstract class Timeline {
    protected GridMap map;
    protected List<TimelineEvent> events = new ArrayList<>();
    protected double energyCostMultiplier = 1.0;
    protected double visibilityRange = 1.0;
    protected boolean hasSpecialVision = false;

    protected Timeline(GridMap map) {
        this.map = map;
    }

    /**
     * Apply a change to the map at the given coordinates.
     * Different timelines affect the map differently.
     */
    public abstract void applyChange(int x, int y);
    
    /**
     * Get the ambient color tint for this timeline.
     */
    public abstract String getAmbientColor();
    
    /**
     * Get the name of this timeline.
     */
    public abstract String getName();
    
    /**
     * Get special effect description for this timeline.
     */
    public abstract String getEffectDescription();
    
    public void addEvent(TimelineEvent event) {
        events.add(event);
    }
    
    public List<TimelineEvent> getEvents() {
        return events;
    }
    
    public double getEnergyCostMultiplier() { return energyCostMultiplier; }
    public double getVisibilityRange() { return visibilityRange; }
    public boolean hasSpecialVision() { return hasSpecialVision; }
    
    public void propagateToOtherTimelines(Timeline past, Timeline present, Timeline future) {
        // Butterfly effect: changes in one timeline affect others
        for (TimelineEvent event : events) {
            if (event.type == EventType.ENEMY_KILLED) {
                // Killing an enemy in the past prevents it from appearing in the future
                if (this instanceof Past) {
                    future.addEvent(new TimelineEvent(EventType.ENEMY_PREVENTED, event.x, event.y));
                }
                // Killing in future creates echoes in present
                if (this instanceof Future) {
                    present.addEvent(new TimelineEvent(EventType.ECHO_CREATED, event.x, event.y));
                }
            } else if (event.type == EventType.COLLECTIBLE_TAKEN) {
                // Taking a collectible in present might create echoes in past/future
                if (this instanceof Present) {
                    past.addEvent(new TimelineEvent(EventType.ECHO_CREATED, event.x, event.y));
                    future.addEvent(new TimelineEvent(EventType.ECHO_CREATED, event.x, event.y));
                }
            } else if (event.type == EventType.OBSTACLE_DESTROYED) {
                // Destroying obstacle in past removes it from all timelines
                if (this instanceof Past) {
                    present.addEvent(new TimelineEvent(EventType.PATH_OPENED, event.x, event.y));
                    future.addEvent(new TimelineEvent(EventType.PATH_OPENED, event.x, event.y));
                }
            }
        }
        events.clear(); // Clear processed events
    }
    
    public static class TimelineEvent {
        public EventType type;
        public int x;
        public int y;
        public long timestamp;
        public Object data; // Additional data for complex events
        
        public TimelineEvent(EventType type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.timestamp = System.currentTimeMillis();
        }
        
        public TimelineEvent(EventType type, int x, int y, Object data) {
            this(type, x, y);
            this.data = data;
        }
    }
    
    public enum EventType {
        // Combat events
        ENEMY_KILLED,
        ENEMY_SPAWNED,
        ENEMY_PREVENTED,
        PLAYER_DAMAGED,
        
        // Collection events
        COLLECTIBLE_TAKEN,
        COLLECTIBLE_SPAWNED,
        
        // Map events
        OBSTACLE_DESTROYED,
        PATH_OPENED,
        PATH_BLOCKED,
        
        // Timeline events
        TIMELINE_SHIFT,
        ECHO_CREATED,
        ECHO_TRIGGERED,
        PARADOX_OCCURRED,
        TIME_RIFT_OPENED,
        
        // Ability events
        REWIND_ACTIVATED,
        TIME_STOP_ACTIVATED,
        TIME_SLOW_ACTIVATED
    }
}
