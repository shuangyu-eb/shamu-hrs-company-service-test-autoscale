package shamu.company.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.dto.UserPersonalInformationDTO;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserPersonalInformation extends BaseEntity {

    private String firstName;

    private String middleName;

    private String lastName;

    private String preferredName;

    private Timestamp birthDate;

    private String ssn;

    @ManyToOne
    private Gender gender;

    @ManyToOne
    private MaritalStatus maritalStatus;

    @ManyToOne
    private Ethnicity ethnicity;

    @ManyToOne
    private CitizenshipStatus citizenshipStatus;

    public UserPersonalInformation(UserPersonalInformationDTO userPersonalInformationDTO){
        BeanUtils.copyProperties(userPersonalInformationDTO,this);
        this.setGender(new Gender(userPersonalInformationDTO.getGenderId()));
        this.setMaritalStatus(new MaritalStatus(userPersonalInformationDTO.getMaritalStatusId()));
    }

}
