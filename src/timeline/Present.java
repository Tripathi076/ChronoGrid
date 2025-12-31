package timeline;

import map.GridMap;

public class Present extends Timeline {

    public Present(GridMap map) {
        super(map);
    }

    @Override
    public void applyChange(int x, int y) {
        map.modifyTile(x, y, true);
    }
}
