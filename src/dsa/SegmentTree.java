package dsa;

public class SegmentTree {
    private int[] tree;
    private int[] data;
    private int n;

    public SegmentTree(int[] arr) {
        this.n = arr.length;
        this.data = arr.clone();
        this.tree = new int[4 * n];
        build(1, 0, n - 1);
    }

    private void build(int node, int start, int end) {
        if (start == end) {
            tree[node] = data[start];
        } else {
            int mid = (start + end) / 2;
            int leftChild = 2 * node;
            int rightChild = 2 * node + 1;
            
            build(leftChild, start, mid);
            build(rightChild, mid + 1, end);
            
            tree[node] = merge(tree[leftChild], tree[rightChild]);
        }
    }

    private int merge(int left, int right) {
        return left + right; // Sum query - can be modified for min/max
    }

    public void update(int index, int value) {
        update(1, 0, n - 1, index, value);
    }

    private void update(int node, int start, int end, int index, int value) {
        if (start == end) {
            data[index] = value;
            tree[node] = value;
        } else {
            int mid = (start + end) / 2;
            int leftChild = 2 * node;
            int rightChild = 2 * node + 1;
            
            if (index <= mid) {
                update(leftChild, start, mid, index, value);
            } else {
                update(rightChild, mid + 1, end, index, value);
            }
            
            tree[node] = merge(tree[leftChild], tree[rightChild]);
        }
    }

    public int query(int left, int right) {
        return query(1, 0, n - 1, left, right);
    }

    private int query(int node, int start, int end, int left, int right) {
        if (right < start || end < left) {
            return 0; // Identity element for sum
        }
        
        if (left <= start && end <= right) {
            return tree[node];
        }
        
        int mid = (start + end) / 2;
        int leftChild = 2 * node;
        int rightChild = 2 * node + 1;
        
        int leftResult = query(leftChild, start, mid, left, right);
        int rightResult = query(rightChild, mid + 1, end, left, right);
        
        return merge(leftResult, rightResult);
    }

    public int getSize() {
        return n;
    }
}
