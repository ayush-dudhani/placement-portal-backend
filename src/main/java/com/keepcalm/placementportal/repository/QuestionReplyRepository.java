package com.keepcalm.placementportal.repository;
import com.keepcalm.placementportal.entity.QuestionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface QuestionReplyRepository extends JpaRepository<QuestionReply,Long>{ List<QuestionReply> findByQuestionIdOrderByCreatedAtAsc(Long questionId); }
