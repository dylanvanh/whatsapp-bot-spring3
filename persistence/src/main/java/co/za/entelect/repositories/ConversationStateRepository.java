package co.za.entelect.repositories;

import co.za.entelect.Entities.ConversationStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationStateRepository extends JpaRepository<ConversationStateEntity, Long> {
}
