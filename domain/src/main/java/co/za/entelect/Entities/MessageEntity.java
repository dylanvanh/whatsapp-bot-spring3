package co.za.entelect.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "id"))
@Table(name = "message", schema = "public")
@Entity
public class MessageEntity extends IIdentifiableEntity {

    private String message;

    private LocalDateTime messageDateTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

}
