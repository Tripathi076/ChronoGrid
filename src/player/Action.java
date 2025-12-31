package player;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Represents an action that a player can perform.
 */
public class Action implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Action.class.getName());

    public enum ActionType {
        MOVE_UP(1),
        MOVE_DOWN(1),
        MOVE_LEFT(1),
        MOVE_RIGHT(1),
        ATTACK(10),
        DEFEND(5),
        USE_ABILITY(20),
        SHIFT_TIMELINE(15),
        INTERACT(2),
        WAIT(0);

        private final int energyCost;

        ActionType(int energyCost) {
            this.energyCost = energyCost;
        }

        public int getEnergyCost() {
            return energyCost;
        }
    }

    private ActionType type;
    private int targetX;
    private int targetY;
    private long timestamp;
    private String parameter;

    public Action(ActionType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public Action(ActionType type, int targetX, int targetY) {
        this(type);
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public Action(ActionType type, String parameter) {
        this(type);
        this.parameter = parameter;
    }

    public void execute(Player player) {
        switch (type) {
            case MOVE_UP:
                player.move(0, -1);
                break;
            case MOVE_DOWN:
                player.move(0, 1);
                break;
            case MOVE_LEFT:
                player.move(-1, 0);
                break;
            case MOVE_RIGHT:
                player.move(1, 0);
                break;
            case ATTACK:
                executeAttack(player);
                break;
            case DEFEND:
                executeDefend(player);
                break;
            case USE_ABILITY:
                executeAbility(player);
                break;
            case SHIFT_TIMELINE:
                executeTimelineShift(player);
                break;
            case INTERACT:
                executeInteract(player);
                break;
            case WAIT:
                player.addEnergy(5);
                break;
        }
    }

    private void executeAttack(Player player) {
        logger.info(String.format("%s attacks at (%d, %d)", player.getName(), targetX, targetY));
    }

    private void executeDefend(Player player) {
        logger.info(String.format("%s is defending.", player.getName()));
    }

    private void executeAbility(Player player) {
        logger.info(String.format("%s uses ability: %s", player.getName(), parameter));
    }

    private void executeTimelineShift(Player player) {
        logger.info(String.format("%s attempts to shift timeline.", player.getName()));
    }

    private void executeInteract(Player player) {
        logger.info(String.format("%s interacts at (%d, %d)", player.getName(), targetX, targetY));
    }

    public ActionType getType() {
        return type;
    }

    public int getEnergyCost() {
        return type.getEnergyCost();
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getParameter() {
        return parameter;
    }
}
