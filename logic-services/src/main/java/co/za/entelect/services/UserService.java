package co.za.entelect.services;

import co.za.entelect.Entities.UserEntity;
import co.za.entelect.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserEntity> getUser(long id) {
        return Optional.ofNullable(userRepository.findByPhoneNumberId("ASD"));
    }

}
