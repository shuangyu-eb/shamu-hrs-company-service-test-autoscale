package shamu.company.job;

import lombok.Data;

@Data
public class JobUserDTO {

    private Long id;

    private String imageUrl;

    private String firstName;

    private String lastName;

    private String email;

    private String jobTitle;

    private String cityName;
}
