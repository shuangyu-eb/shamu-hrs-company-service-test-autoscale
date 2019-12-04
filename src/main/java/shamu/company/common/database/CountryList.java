package shamu.company.common.database;

import java.util.List;
import lombok.Data;

@Data
public class CountryList {

  private List<CountryItem> countries;
}
