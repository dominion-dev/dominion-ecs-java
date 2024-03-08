package dev.dominion.ecs.engine.system;

import dev.dominion.ecs.engine.collections.ChunkedPool;

/**
 * @author biding
 */
public interface IDUpdater {
    IDUpdater ID_UPDATER = new IDUpdater() {
        @Override public int getId(ChunkedPool.Item item) {
            return item.getId();
        }
        @Override public void setId(ChunkedPool.Item item, int id) {
            item.setId(id);
        }
    };

    IDUpdater STATE_ID_UPDATER = new IDUpdater() {
        @Override public int getId(ChunkedPool.Item item) {
            return item.getStateId();
        }
        @Override public void setId(ChunkedPool.Item item, int id) {
            item.setStateId(id);
        }
    };

    int getId(ChunkedPool.Item item);
    void setId(ChunkedPool.Item item,int id);
}
