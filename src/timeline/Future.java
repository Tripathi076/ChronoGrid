package timeline;

import map.GridMap;

public class Future extends Timeline {

    public Future(GridMap map) {
        super(map);
    }

    @Override
    public void applyChange(int x, int y) {
        map.modifyTile(x, y, true);
    }
}
