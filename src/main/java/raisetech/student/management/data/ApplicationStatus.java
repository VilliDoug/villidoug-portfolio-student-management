package raisetech.student.management.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "申込状況情報")
@Getter
@Setter

public class ApplicationStatus {

    private Integer id;
    private Integer courseId;
    private String applicationStatus;

}


