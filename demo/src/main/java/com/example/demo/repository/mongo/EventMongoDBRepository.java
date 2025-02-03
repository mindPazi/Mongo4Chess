package com.example.demo.repository.mongo;

import com.example.demo.model.EventMongoDB;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMongoDBRepository extends MongoRepository<EventMongoDB, String> {
}
