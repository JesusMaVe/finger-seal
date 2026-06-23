package com.dataforge.query;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QueryHistoryRepository extends CrudRepository<QueryHistory, Long> {
    List<QueryHistory> findByConnectionIdOrderByCreatedAtDesc(Long connectionId);

    default void deleteByConnectionId(Long connectionId) {
        deleteAll(findByConnectionIdOrderByCreatedAtDesc(connectionId));
    }
}
