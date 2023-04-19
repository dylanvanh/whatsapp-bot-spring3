package co.za.entelect.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "id"))
@Table(name = "user", schema = "public")
@Entity
public class UserEntity extends IdentifiableEntity {

    private String name;

    private String phone;

    @Column(name = "phone_number_id")
    private String phoneNumberId;

    @ManyToOne()
    @JoinColumn(name = "conversation_state_id")
    private ConversationStateEntity conversationState;

    private String email;
}
