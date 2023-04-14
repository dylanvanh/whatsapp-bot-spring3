package co.za.entelect.Entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "id"))
@Table(name = "user", schema = "public")
@Entity
public class UserEntity extends IIdentifiableEntity {

    private String name;

    private String phone;

    @Column(name = "phone_number_id")
    private String phoneNumberId;
}
