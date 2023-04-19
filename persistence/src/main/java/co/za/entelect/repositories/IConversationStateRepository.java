package co.za.entelect.repositories;

import co.za.entelect.entities.ConversationStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IConversationStateRepository extends JpaRepository<ConversationStateEntity, Long> {
}
