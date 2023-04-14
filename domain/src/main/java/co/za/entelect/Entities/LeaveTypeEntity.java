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
@Table(name = "leave_type", schema = "public")
@Entity
public class LeaveTypeEntity extends IdentifiableEntity {

    private String name;
}
