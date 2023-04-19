package co.za.entelect.entities;

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
public class MessageEntity extends IdentifiableEntity {

    private String message;

    @Column(name = "received_datetime")
    private LocalDateTime receivedDateTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;


    @Column(name = "message_id")
    private String messageId;

}
