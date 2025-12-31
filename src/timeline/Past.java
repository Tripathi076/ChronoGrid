package timeline;

import map.GridMap;

public class Past extends Timeline {

    public Past(GridMap map) {
        super(map);
    }

    @Override
    public void applyChange(int x, int y) {
        map.modifyTile(x, y, false);
    }
}
