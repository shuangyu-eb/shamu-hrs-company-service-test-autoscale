package shamu.company.email.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.email.entity.Email;

import java.util.List;

public interface EmailRepository extends BaseRepository<Email, String> {

  Email findFirstByToAndSubjectOrderBySendDateDesc(String to, String subject);

  Email findByMessageId(String messageId);

  List<Email> findByMessageIdIn(List<String> messageIds);
}
