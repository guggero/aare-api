package ch.illubits.api;

import org.hibernate.Session;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.math.BigInteger;
import java.util.*;

public abstract class BaseRepository<T> {

    public static final String GRAPH_TYPE_ADDITIONAL_GRAPHS = "javax.persistence.loadgraph";
    public static final String GRAPH_TYPE_EXCLUSIVE_GRAPHS = "javax.persistence.fetchgraph";

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<T> entityType;

    protected BaseRepository() {
        entityType = Objects.requireNonNull(ReflectionUtil.<T>getActualTypeArguments(getClass(), 0));
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected Session getSession() {
        return (Session) entityManager.getDelegate();
    }

    public void flush() {
        this.entityManager.flush();
    }

    public T getReference(Long id) {
        return entityManager.getReference(entityType, id);
    }

    /**
     * Creates an entity on the database. The object is updated with the ID that it is created with.
     *
     * @param entity The entity to create
     */
    public void persist(T entity) {
        prePersist(entity);

        entityManager.persist(entity);
    }

    /**
     * Can be overridden in a subclass to check/change the entity before persist.
     */
    protected void prePersist(T entity) {
    }

    /**
     * Persists an entity without performing the normal user level access check. Should only be used by subclasses
     * in special cases where you know exactly what you are doing.
     *
     * @param entity The entity to update without access check
     */
    protected void persistWithoutAccessCheck(T entity) {
        prePersist(entity);
        entityManager.persist(entity);
    }

    /**
     * Updates an entity on the database. A new reference to the updated entity is returned, the old one should be
     * discarded.
     *
     * @param entity The entity to update
     * @return A new reference to the updated object that should be used for further actions.
     */
    public T merge(T entity) {
        preMerge(entity);

        return entityManager.merge(entity);
    }

    /**
     * Can be overridden in a subclass to check/change the entity before merge.
     */
    protected void preMerge(T entity) {
    }

    /**
     * Deletes the entity with the provided id.
     *
     * @param id id of the entity to delete.
     * @return true if the entity to delete was found - otherwise false.
     */
    public boolean remove(long id) {
        T entity = find(id);
        if (entity == null) {
            return false;
        }
        remove(entity);
        return true;
    }

    /**
     * Removes an entity from the database.
     *
     * @param entity The entity to remove.
     */
    public void remove(T entity) {
        preRemove(entity);

        entityManager.remove(entity);
    }

    /**
     * Can be overridden in a subclass to check/change the entity before remove.
     */
    protected void preRemove(T entity) {
    }

    /**
     * Finds an entity by its ID.
     *
     * @param id The ID of the entity to look for
     * @return The entity from the database or null if it was not found
     */
    public T find(Object id) {
        T entity = entityManager.find(entityType, id);
        return entity;
    }

    /**
     * Returns a type-safe result list which contains all entities of the provided type.
     *
     * @return the type-safe result list.
     */
    public List<T> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityType);
        CriteriaQuery<T> all = query.select(query.from(entityType));
        return resultList(entityManager.createQuery(all));
    }

    /**
     * Returns a type-safe result list that was eager fetched with the given graph.
     *
     * @param graph The graph to use
     * @return the type-safe result list
     */
    protected List<T> findAllWithGraph(EntityGraph<T> graph) {
        return findAllWithGraph(graph, GRAPH_TYPE_ADDITIONAL_GRAPHS);
    }

    /**
     * Returns a type-safe result list that was eager fetched with the given graph.
     *
     * @param graph     The graph to use
     * @param graphType The type of graph to use, additional or exclusive (loadgraph/fetchgraph in hibernate terms)
     * @return the type-safe result list
     */
    protected List<T> findAllWithGraph(EntityGraph<T> graph, String graphType) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityType);
        CriteriaQuery<T> all = query.select(query.from(entityType)).distinct(true);
        return resultList(entityManager.createQuery(all).setHint(graphType, graph));
    }

    /**
     * Returns a type-safe result list of the given query.
     *
     * @param query the query
     * @return the result list
     */
    protected List<T> resultList(TypedQuery<T> query) {
        List<T> entityList = query.getResultList();
        return entityList;
    }

    public int executeNativeQuery(String query) {
        return executeNativeQuery(query, null);
    }

    public int executeNativeQuery(String query, Map<String, Object> params) {
        Query nativeQuery = entityManager.createNativeQuery(query);
        if (params != null) {
            params.forEach(nativeQuery::setParameter);
        }
        return nativeQuery.executeUpdate();
    }

    /**
     * Returns a type-safe single result of the given query or null.
     *
     * @param query Typed query to get result from
     * @return the result or null
     * @throws NonUniqueResultException if more than one result
     */
    protected T singleResult(TypedQuery<T> query) {
        List<T> resultList = resultList(query);

        if (resultList.isEmpty()) {
            return null;
        }

        if (resultList.size() > 1) {
            // maybe the result is a join, so make it distinct.
            Set<T> distinctResult = new HashSet<>(resultList);
            if (distinctResult.size() > 1) {
                throw new NonUniqueResultException("Result for query '" + query + "' must contain exactly one item");
            }
        }

        return resultList.get(0);
    }

    protected Long countResult(TypedQuery<Long> query) {
        return query.getSingleResult();
    }

    protected EntityGraph<T> getGraph() {
        return entityManager.createEntityGraph(entityType);
    }

    protected T findWithGraph(EntityGraph<T> graph, long id) {
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.loadgraph", graph);

        T entity = entityManager.find(entityType, id, hints);
        return entity;
    }

    protected TypedQuery<T> createNamedQuery(String queryName) {
        return entityManager.createNamedQuery(queryName, entityType);
    }

    protected TypedQuery<T> createNamedQueryWithGraph(String queryName, EntityGraph<T> graph) {
        TypedQuery<T> query = createNamedQuery(queryName);
        query.setHint("javax.persistence.loadgraph", graph);
        return query;
    }

    protected Query createNoTypedNamedQuery(String queryName) {
        return entityManager.createNamedQuery(queryName);
    }

    protected TypedQuery<T> createQuery(String query) {
        return entityManager.createQuery(query, entityType);
    }

    protected TypedQuery<Long> createNamedCountQuery(String query) {
        return entityManager.createNamedQuery(query, Long.class);
    }

    protected long nextSequenceValue(String sequenceName) {
        return ((BigInteger) entityManager.createNativeQuery("select nextval('" + sequenceName + "');").getSingleResult()).longValue();
    }
}
