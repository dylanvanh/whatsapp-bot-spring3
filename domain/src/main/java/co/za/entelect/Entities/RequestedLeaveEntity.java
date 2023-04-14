package co.za.entelect.Entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "id"))
@Table(name = "requested_leave", schema = "public")
@Entity
public class RequestedLeaveEntity extends IdentifiableEntity {

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "leave_type_id")
    private LeaveTypeEntity leaveType;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name="day_count")
    private Integer dayCount;

    @Column(name="request_created_date")
    private String requestCreatedDate;

    @Column(name="request_status")
    private Boolean requestStatus;

}
