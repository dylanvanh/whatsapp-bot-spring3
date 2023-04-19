package co.za.entelect.repositories;

import co.za.entelect.Entities.RequestedLeaveEntity;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRequestedLeaveRepository extends JpaRepository<RequestedLeaveEntity, Long> {
    RequestedLeaveEntity findTopByUserIdOrderByIdDesc(Long userId);

    RequestedLeaveEntity findTopByUserIdAndRequestJourneyCompletedStatusOrderByRequestCreatedDateDesc(
            Long userId, Boolean requestJourneyCompletedStatus);

    List<RequestedLeaveEntity> findAllByUserId(Long id);
}
