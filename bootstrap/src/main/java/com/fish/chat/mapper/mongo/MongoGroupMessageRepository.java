package com.fish.chat.mapper.mongo;

import com.fish.chat.entity.MongoGroupMessage;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoGroupMessageRepository extends MongoRepository<MongoGroupMessage, String> {

    List<MongoGroupMessage> findByGroupIdOrderByTimestamp(String groupId);

    List<MongoGroupMessage> findByFromAndGroupIdOrderByTimestamp(String from, String groupId);

    Page<MongoGroupMessage> findByGroupIdOrderByTimestampDesc(String groupId, Pageable pageable);
}