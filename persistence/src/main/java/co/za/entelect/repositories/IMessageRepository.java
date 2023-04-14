package co.za.entelect.repositories;

import co.za.entelect.Entities.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMessageRepository extends JpaRepository<MessageEntity, Long> {
}
