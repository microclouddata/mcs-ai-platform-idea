package com.mcs.chat.chat;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
}
