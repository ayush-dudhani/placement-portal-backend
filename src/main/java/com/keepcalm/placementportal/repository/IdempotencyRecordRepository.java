package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord,Long>{Optional<IdempotencyRecord> findByInstitutionIdAndActorIdAndOperationAndIdempotencyKey(Long institutionId,Long actorId,String operation,String key);}
