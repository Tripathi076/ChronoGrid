package timeline;

import map.GridMap;
import java.util.List;
import java.util.ArrayList;

public abstract class Timeline {
    protected GridMap map;
    protected List<TimelineEvent> events = new ArrayList<>();

    protected Timeline(GridMap map) {
        this.map = map;
    }

    public abstract void applyChange(int x, int y);
    
    public void addEvent(TimelineEvent event) {
        events.add(event);
    }
    
    public List<TimelineEvent> getEvents() {
        return events;
    }
    
    public void propagateToOtherTimelines(Timeline past, Timeline present, Timeline future) {
        // Butterfly effect: changes in one timeline affect others
        for (TimelineEvent event : events) {
            if (event.type == EventType.ENEMY_KILLED) {
                // Killing an enemy in the past prevents it from appearing in the future
                if (this instanceof Past) {
                    future.addEvent(new TimelineEvent(EventType.ENEMY_PREVENTED, event.x, event.y));
                }
            } else if (event.type == EventType.COLLECTIBLE_TAKEN) {
                // Taking a collectible in present might create echoes in past/future
                if (this instanceof Present) {
                    past.addEvent(new TimelineEvent(EventType.ECHO_CREATED, event.x, event.y));
                    future.addEvent(new TimelineEvent(EventType.ECHO_CREATED, event.x, event.y));
                }
            }
        }
        events.clear(); // Clear processed events
    }
    
    public static class TimelineEvent {
        public EventType type;
        public int x, y;
        public long timestamp;
        
        public TimelineEvent(EventType type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public enum EventType {
        ENEMY_KILLED, COLLECTIBLE_TAKEN, ENEMY_PREVENTED, ECHO_CREATED, TIMELINE_SHIFT
    }
}
