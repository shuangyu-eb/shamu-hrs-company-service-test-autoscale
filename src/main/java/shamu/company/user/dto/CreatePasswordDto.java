package shamu.company.user.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatePasswordDto extends UpdatePasswordDto {

  @NotNull
  private String emailWork;
}
