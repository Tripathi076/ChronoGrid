package ai;

import java.util.*;

/**
 * Behavior Tree for AI decision making.
 * Supports Selector, Sequence, and Action nodes.
 */
public class BehaviorTree {
    private BehaviorNode root;

    public enum NodeStatus {
        SUCCESS, FAILURE, RUNNING
    }

    public abstract static class BehaviorNode {
        protected String name;
        protected List<BehaviorNode> children;

        protected BehaviorNode(String name) {
            this.name = name;
            this.children = new ArrayList<>();
        }

        public abstract NodeStatus execute();

        public void addChild(BehaviorNode child) {
            children.add(child);
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Selector: Tries children until one succeeds (OR logic)
     */
    public static class Selector extends BehaviorNode {
        public Selector(String name) {
            super(name);
        }

        @Override
        public NodeStatus execute() {
            for (BehaviorNode child : children) {
                NodeStatus status = child.execute();
                if (status == NodeStatus.SUCCESS) {
                    return NodeStatus.SUCCESS;
                }
                if (status == NodeStatus.RUNNING) {
                    return NodeStatus.RUNNING;
                }
            }
            return NodeStatus.FAILURE;
        }
    }

    /**
     * Sequence: Runs all children in order, fails if any fail (AND logic)
     */
    public static class Sequence extends BehaviorNode {
        public Sequence(String name) {
            super(name);
        }

        @Override
        public NodeStatus execute() {
            for (BehaviorNode child : children) {
                NodeStatus status = child.execute();
                if (status == NodeStatus.FAILURE) {
                    return NodeStatus.FAILURE;
                }
                if (status == NodeStatus.RUNNING) {
                    return NodeStatus.RUNNING;
                }
            }
            return NodeStatus.SUCCESS;
        }
    }

    /**
     * Action: Leaf node that performs an action
     */
    public static class ActionNode extends BehaviorNode {
        private Runnable action;
        private java.util.function.Supplier<NodeStatus> condition;

        public ActionNode(String name, Runnable action) {
            super(name);
            this.action = action;
            this.condition = () -> NodeStatus.SUCCESS;
        }

        public ActionNode(String name, java.util.function.Supplier<NodeStatus> condition) {
            super(name);
            this.condition = condition;
        }

        @Override
        public NodeStatus execute() {
            if (action != null) {
                action.run();
                return NodeStatus.SUCCESS;
            }
            return condition.get();
        }
    }

    /**
     * Condition: Checks a condition without side effects
     */
    public static class Condition extends BehaviorNode {
        private java.util.function.Supplier<Boolean> predicate;

        public Condition(String name, java.util.function.Supplier<Boolean> predicate) {
            super(name);
            this.predicate = predicate;
        }

        @Override
        public NodeStatus execute() {
            boolean result = predicate.get();
            return result ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
        }
    }

    /**
     * Inverter: Decorator that inverts child result
     */
    public static class Inverter extends BehaviorNode {
        public Inverter(String name) {
            super(name);
        }

        @Override
        public NodeStatus execute() {
            if (children.isEmpty()) return NodeStatus.FAILURE;
            
            NodeStatus status = children.get(0).execute();
            if (status == NodeStatus.SUCCESS) return NodeStatus.FAILURE;
            if (status == NodeStatus.FAILURE) return NodeStatus.SUCCESS;
            return NodeStatus.RUNNING;
        }
    }

    public BehaviorTree() {
        this.root = null;
    }

    public void setRoot(BehaviorNode root) {
        this.root = root;
    }

    public NodeStatus tick() {
        if (root == null) {
            return NodeStatus.FAILURE;
        }
        return root.execute();
    }

    /**
     * Builder for creating behavior trees fluently
     */
    public static class Builder {
        private Deque<BehaviorNode> nodeStack = new ArrayDeque<>();
        private BehaviorNode root;

        public Builder selector(String name) {
            BehaviorNode node = new Selector(name);
            addNode(node);
            nodeStack.push(node);
            return this;
        }

        public Builder sequence(String name) {
            BehaviorNode node = new Sequence(name);
            addNode(node);
            nodeStack.push(node);
            return this;
        }

        public Builder action(String name, Runnable action) {
            addNode(new ActionNode(name, action));
            return this;
        }

        public Builder condition(String name, java.util.function.Supplier<Boolean> predicate) {
            addNode(new Condition(name, predicate));
            return this;
        }

        public Builder end() {
            if (!nodeStack.isEmpty()) {
                nodeStack.pop();
            }
            return this;
        }

        private void addNode(BehaviorNode node) {
            if (root == null) {
                root = node;
            } else if (!nodeStack.isEmpty()) {
                nodeStack.peek().addChild(node);
            }
        }

        public BehaviorTree build() {
            BehaviorTree tree = new BehaviorTree();
            tree.setRoot(root);
            return tree;
        }
    }
}
