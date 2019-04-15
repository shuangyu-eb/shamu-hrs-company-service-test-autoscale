package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.user.entity.UserContactInformation;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContactInformationDTO {
    private Long id;

    private String phoneWork;

    private String phoneHome;

    private String emailWork;

    private String emailHome;

    public UserContactInformationDTO (UserContactInformation userContactInformation) {
        BeanUtils.copyProperties(userContactInformation,this);
    }
}
