package co.za.entelect.repositories;

import co.za.entelect.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByPhoneNumberId(String phoneNumberId);

    UserEntity findByEmail(String validEmployeeEmail);
}
