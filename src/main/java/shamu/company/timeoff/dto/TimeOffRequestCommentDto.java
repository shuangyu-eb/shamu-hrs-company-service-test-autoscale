package shamu.company.timeoff.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.entity.TimeOffRequestComment;

@Data
@NoArgsConstructor
public class TimeOffRequestCommentDto {

  private String id;

  private String userId;

  private String imageUrl;

  private String comment;

  private String name;

  public TimeOffRequestCommentDto(final TimeOffRequestComment requestComment) {
    this.id = requestComment.getId();
    this.comment = requestComment.getComment();
    this.imageUrl = requestComment.getUser().getImageUrl();
    this.userId = requestComment.getUser().getId();
    this.name = requestComment.getUser().getUserPersonalInformation().getName();
  }
}
