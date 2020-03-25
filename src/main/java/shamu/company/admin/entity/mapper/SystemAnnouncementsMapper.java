package shamu.company.admin.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.admin.dto.SystemAnnouncementDto;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface SystemAnnouncementsMapper {

  SystemAnnouncementDto convertSystemAnnouncementDto(SystemAnnouncement systemAnnouncement);
}
