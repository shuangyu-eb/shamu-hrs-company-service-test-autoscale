package shamu.company.timeoff.service;

import shamu.company.timeoff.entity.TimeOffRequest;

public interface TimeOffRequestEmailService {


  void sendEmail(TimeOffRequest timeOffRequest);

  void sendApprovedEmail(TimeOffRequest timeOffRequest);

  void sendDeniedEmail(TimeOffRequest timeOffRequest);

  void sendPendingEmail(TimeOffRequest timeOffRequest);

}
