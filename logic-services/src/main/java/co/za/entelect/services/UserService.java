package co.za.entelect.services;

import co.za.entelect.Entities.UserEntity;
import co.za.entelect.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final IUserRepository userRepository;

    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserEntity> getUser(long id) {
        return Optional.ofNullable(userRepository.findByPhoneNumberId("ASD"));
    }

}
