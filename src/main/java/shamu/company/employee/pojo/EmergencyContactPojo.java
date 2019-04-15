package shamu.company.employee.pojo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmergencyContactPojo {
    private String emergencyContactFirstName;
    private String emergencyContactLastName;
    private String relationship;
    private String emergencyContactPhone;
    private String emergencyContactEmail;
    private String emergencyContactStreet1;
    private String emergencyContactStreet2;
    private String emergencyContactCity;
    private String emergencyContactState;
    private String emergencyContactZip;
    private String emergencyContactIsPrimary;

    public static List<EmergencyContactPojo> getEmergencyContactPojoList(String json){
        Gson gson = new Gson();
        List<EmergencyContactPojo> list = gson.fromJson(json, new TypeToken<ArrayList<EmergencyContactPojo>>(){}.getType());
        return list;
    }
}
