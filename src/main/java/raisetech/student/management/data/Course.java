package raisetech.student.management.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "受講生コース情報")
@Getter
@Setter

public class Course {

  private Integer id;
  private Integer studentId;
  private String courseName;
  private LocalDate courseStartAt;
  private LocalDate courseEndAt;

}