package org.amplafi.flow;

/**
 * Interface that defines access to the transaction object currently active.
 *
 * @author Patrick Moore
 *
 */
public interface FlowTx {

    public <T> boolean flushIfNeeded(T... entities);

    public <T, K> T load(Class<? extends T> clazz, K entityId, boolean nullIdReturnsNull);

    public <T, K> T get(Class<? extends T> clazz, K entityId, boolean nullIdReturnsNull);

    public void delete(Object entity);

    /**
     * @param entity
     */
    public void saveOrUpdate(Object entity);
}
