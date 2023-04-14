package co.za.entelect.repositories;

import co.za.entelect.Entities.LeaveTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILeaveTypeRepository extends JpaRepository<LeaveTypeEntity, Long> {
}
