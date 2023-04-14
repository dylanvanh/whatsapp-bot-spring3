package co.za.entelect;

import co.za.entelect.Entities.UserEntity;
import co.za.entelect.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {


    private final UserService _userService;

    public TestController(UserService _userService) {
        this._userService = _userService;
    }

    @GetMapping
    public UserEntity test() {
        return _userService.getUser(1).orElseThrow(() -> new RuntimeException("User not found"));
    }


}
