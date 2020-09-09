package shamu.company.email.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.email.entity.Email;

public interface EmailRepository extends BaseRepository<Email, String> {

  Email findFirstByToAndSubjectOrderBySendDateDesc(String to, String subject);

  Email findByMessageId(String messageId);

  List<Email> findByMessageIdIn(List<String> messageIds);
}
