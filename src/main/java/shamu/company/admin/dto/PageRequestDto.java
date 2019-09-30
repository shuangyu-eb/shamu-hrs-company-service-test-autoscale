package shamu.company.admin.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Data
public class PageRequestDto {

  private int page = 0;

  private int size = 20;

  private String keyword = "";

  private Sort.Direction direction = Direction.ASC;

  private Field field = Field.NAME;

  public Pageable getPageable() {
    return PageRequest.of(page, size, direction, field.getValue());
  }

  public String getKeyword() {
    return this.keyword.trim();
  }

  public enum Field {
    NAME("userPersonalInformation.firstName"),
    COMPANY("company.name"),
    ROLE("userRole.name"),
    EMAIL("userContactInformation.emailWork");
    private final String value;

    Field(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
