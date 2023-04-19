package co.za.entelect.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

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
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "day_count")
    private Integer dayCount;

    @Column(name = "request_created_date")
    private LocalDateTime requestCreatedDate;

    @Column(name = "request_approved_status")
    private Boolean requestApprovedStatus;

    @Column(name = "request_journey_completed_status")
    private Boolean requestJourneyCompletedStatus;

}
