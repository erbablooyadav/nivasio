package in.nivasio.repository;

import in.nivasio.model.BotSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface BotSessionRepository extends MongoRepository<BotSession, String> {
    Optional<BotSession> findByWhatsappNo(String whatsappNo);

    void deleteByWhatsappNo(String whatsappNo);
}
