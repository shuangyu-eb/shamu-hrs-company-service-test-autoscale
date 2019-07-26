package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequestComment;

@Data
public class TimeOffRequestCommentDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  private String imageUrl;

  private String comment;

  private String name;

  public TimeOffRequestCommentDto(TimeOffRequestComment requestComment) {
    this.id = requestComment.getId();
    this.comment = requestComment.getComment();
    this.imageUrl = requestComment.getUser().getImageUrl();
    this.userId = requestComment.getUser().getId();
    this.name = requestComment.getUser().getUserPersonalInformation().getName();
  }
}
