package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressDTO {
    private Long id;

    private Long userId;

    private String street1;

    private String street2;

    private String city;

    private String countryName;

    private String stateProvinceName;

    private Long stateProvinceId;

    private String postalCode;
}
