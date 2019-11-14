package shamu.company.utils.database;

import java.util.List;
import lombok.Data;

@Data
public class CountryItem {

  private String name;

  private List<String> cities;
}
