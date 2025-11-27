package raisetech.student.management.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import raisetech.student.management.data.ApplicationStatus;
import raisetech.student.management.data.Course;
import raisetech.student.management.data.Student;
import raisetech.student.management.domain.CourseDetail;
import raisetech.student.management.domain.StudentDetail;
import raisetech.student.management.service.MainService;

@SuppressWarnings("removal")
@WebMvcTest(MainController.class)
class MainControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private MainService service;

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /**
   * /students 200 test
   *
   * @throws Exception
   */
  @Test
  void 受講生詳細の一覧検索が実行できて空のリストが返ってくること() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/students"))
        .andExpect(status().isOk());

    verify(service, times(1)).searchStudentList(
        null, null, null, null, null);
  }

  /**
   * /students 500 test
   */
  @Test
  void 受講生詳細の一覧検索が内部エラー発生すること() throws Exception {
    when(service.searchStudentList(null, null, null, null, null))
        .thenThrow(new RuntimeException("内部サーバー エラーが発生しました。"));

    mockMvc.perform(MockMvcRequestBuilders.get("/students"))
        .andExpect(status().isInternalServerError());

    verify(service, times(1)).searchStudentList(
        null, null, null, null, null);
  }

  /**
   * /student/{id} correct input test 200
   */
  @Test
  void 受講生詳細の受講生で適切な値を入力したとき入力チェックに異常が発生しないこと() {
    Student student = new Student();
    student.setId(1);
    student.setName("TestName");
    student.setEmailAddress("test@example.com");
    student.setAge(20);

    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    assertThat(violations.size()).isEqualTo(0);

  }

  /**
   * /student/{id} invalid ID input test 400
   */
  @Test
  void 受講生詳細の検索をIDで検索する時_IDに数字以外を用いた時_400エラーが発生すること() throws Exception {
    String invalidId = "Test";

    mockMvc.perform(MockMvcRequestBuilders.get("/students/{id}", invalidId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.validationErrors").exists());

    verify(service, times(0)).searchStudentId(Mockito.any(Integer.class));


  }

  /**
   * student/{id} 404 no student found
   */
  @Test
  void 受講生詳細の検索をIDで検索する時_受講生詳細がないこと() throws Exception {
    String idString = "436";
    Integer id = Integer.parseInt(idString);
    when(service.searchStudentId(id))
        .thenReturn(null);

    mockMvc.perform(MockMvcRequestBuilders.get("/students/{id}", idString))
        .andExpect(status().isNotFound());

    verify(service, times(1)).searchStudentId(id);

  }

  /**
   * /student/{id} 500
   */
  @Test
  void 受講生詳細の検索をIDで検索する時_内部エラー発生すること() throws Exception {
    String idString = "999";
    Integer id = Integer.parseInt(idString);
    when(service.searchStudentId(id))
        .thenThrow(new RuntimeException("内部サーバー エラーが発生しました。"));

    mockMvc.perform(MockMvcRequestBuilders.get("/students/999"))
        .andExpect(status().isInternalServerError());

    verify(service, times(1)).searchStudentId(id);

  }

  /**
   * registerStudent 200 test returns student detail
   *
   * @throws Exception
   */
  @Test
  void 受講生詳細を適切に登録した時_受講生詳細情報を返ってくること() throws Exception {
    Student student = new Student();
    student.setId(999);
    student.setEmailAddress("test@example.com");
    student.setAge(20);

    Course course = new Course();
    course.setCourseName("Crash Test Course");
    List<Course> courseList = List.of(course);

    ApplicationStatus status = new ApplicationStatus();

    CourseDetail courseDetail = new CourseDetail(course, status);
    List<CourseDetail> courseDetailList = List.of(courseDetail);

    StudentDetail expectedDetail = new StudentDetail(student, courseDetailList);

    String jsonBody = objectMapper.writeValueAsString(expectedDetail);

    when(service.registerStudent(Mockito.any(StudentDetail.class)))
        .thenReturn(expectedDetail);

    mockMvc.perform(MockMvcRequestBuilders.post("/students")
            .contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonBody));
    verify(service, times(1))
        .registerStudent(Mockito.any(StudentDetail.class));

  }

  /**
   * registerStudent throws 400 bad input (ID) test
   *
   * @throws Exception
   */
  @Test
  void 受講生詳細の登録した時_不正なメールアドレスを用いた時にバリデーションエラーが発生する() throws Exception {
    Student student = new Student();
    student.setId(999);
    student.setEmailAddress("testexamplecom");
    student.setAge(20);

    Course course = new Course();
    course.setCourseName("Crash Test Course");
    List<Course> courseList = List.of(course);

    StudentDetail expectedDetail = new StudentDetail();
    expectedDetail.setStudent(student);

    String jsonBody = objectMapper.writeValueAsString(expectedDetail);

    mockMvc.perform(MockMvcRequestBuilders.post("/students")
            .contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().isBadRequest())
        .andDo(result ->
            verify(service, times(0))
                .registerStudent(Mockito.any(StudentDetail.class)))
        .andExpect(jsonPath("$.validationErrors['student.emailAddress']").exists())
        .andExpect(jsonPath("$.validationErrors['student.emailAddress']")
            .value("有効なメールアドレスを入力してください。"));

  }

  /**
   * /registerStudent 500 test
   * @throws Exception
   */
  @Test
  void 受講生詳細の登録した時_内部エラーが発生すること() throws Exception {
    Student student = new Student();
    student.setId(1);
    student.setEmailAddress("test@example.com");
    student.setAge(20);
    Course course = new Course();
    course.setCourseName("Crash Test Course");
    List<Course> courseList = List.of(course);
    StudentDetail expectedDetail = new StudentDetail();
    String jsonBody = objectMapper.writeValueAsString(expectedDetail);

    when(service.registerStudent(Mockito.any(StudentDetail.class)))
        .thenThrow(new RuntimeException("内部サーバー エラーが発生しました。"));

    mockMvc.perform(MockMvcRequestBuilders.post("/students")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonBody))
        .andExpect(status().isInternalServerError())
        .andDo(result ->
            verify(service, times(1))
                .registerStudent(Mockito.any(StudentDetail.class)))
        .andExpect(jsonPath("$.errorResponse")
            .value("内部サーバー エラーが発生しました。"));

  }

  /**
   * /updateStudent 200 response ok
   */
  @Test
  void 受講生詳細を適切に更新した時_受講生詳細情報を返ってくること() throws Exception {
    Student student = new Student();
    student.setId(999);
    student.setEmailAddress("test@example.com");
    student.setAge(20);

    Course course = new Course();
    course.setCourseName("Crash Test Course");

    CourseDetail courseDetail = new CourseDetail();
    courseDetail.setCourse(course);
    List<CourseDetail> courseDetailList = List.of(courseDetail);


    StudentDetail expectedDetail = new StudentDetail();
    expectedDetail.setStudent(student);
    expectedDetail.setCourseDetailList(courseDetailList);


    String jsonBody = objectMapper.writeValueAsString(expectedDetail);

    mockMvc.perform(MockMvcRequestBuilders.put("/students/{id}", 999)
            .contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().isOk())
        .andExpect(content().string("更新処理が成功しました。"));
    verify(service, times(1))
        .updateStudent(Mockito.any(StudentDetail.class));

  }


  /**
   * updateStudent 400 bad email input
   * @throws Exception
   */
  @Test
  void 受講生詳細を更新した時_不正なメールアドレスを入力した時にバリデーションエラーが発生すること() throws Exception {
    Student student = new Student();
    student.setId(999);
    student.setEmailAddress("testexample.com");
    student.setAge(20);

    Course course = new Course();
    course.setCourseName("Crash Test Course");
    List<Course> courseList = List.of(course);

    StudentDetail expectedDetail = new StudentDetail();
    expectedDetail.setStudent(student);

    String jsonBody = objectMapper.writeValueAsString(expectedDetail);

    mockMvc.perform(MockMvcRequestBuilders.put("/students/{id}", 999)
            .contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().isBadRequest())
        .andDo(result ->
            verify(service, times(0))
                .updateStudent(Mockito.any(StudentDetail.class)))
        .andExpect(jsonPath("$.validationErrors['student.emailAddress']")
            .exists())
        .andExpect(jsonPath("$.validationErrors['student.emailAddress']")
            .value("有効なメールアドレスを入力してください。"));

  }

  /**
   * /updateStudent 500 test
   * @throws Exception
   */
  @Test
  void 受講生詳細の更新した時_内部エラーが発生すること() throws Exception {
    Student student = new Student();
    student.setId(1);
    student.setEmailAddress("test@example.com");
    student.setAge(20);

    Course course = new Course();
    course.setCourseName("Crash Test Course");

    CourseDetail courseDetail = new CourseDetail();
    courseDetail.setCourse(course);
    List<CourseDetail> courseDetailList = List.of(courseDetail);

    StudentDetail expectedDetail = new StudentDetail();
    expectedDetail.setStudent(student);
    expectedDetail.setCourseDetailList(courseDetailList);

    String jsonBody = objectMapper.writeValueAsString(expectedDetail);

    doThrow(new RuntimeException("内部サーバー エラーが発生しました。"))
    .when(service)
        .updateStudent(Mockito.any(StudentDetail.class));

    mockMvc.perform(MockMvcRequestBuilders.put("/students/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody))
        .andExpect(status().isInternalServerError())
        .andDo(result ->
            verify(service, times(1))
                .updateStudent(Mockito.any(StudentDetail.class)))
        .andExpect(jsonPath("$.errorResponse")
            .value("内部サーバー エラーが発生しました。"));

  }

  @Test
  void 氏名で受講生詳細を検索した時一致する受講生が返されること() throws Exception {
    String expectedName = "Test";
    List<StudentDetail> expectedDetails = List.of(new StudentDetail());

    when(service.searchStudentList(expectedName, null, null,null,null))
        .thenReturn(expectedDetails);

    mockMvc.perform(MockMvcRequestBuilders.get("/students?name=Test"))
        .andExpect(status().isOk());

    verify(service, times(1))
        .searchStudentList(expectedName, null, null, null, null);

  }

  @Test
  void 全ての条件で受講生詳細を検索条件した時一致する受講生が返されること() throws Exception {
    String name = "Tommy";
    String emailAddress = "bommytums@example.com";
    String gender = "Male";
    String courseName = "Show";
    String applicationStatus = "Expelled";
    List<StudentDetail> expectedDetails = List.of(new StudentDetail());

    when(service.searchStudentList(name, emailAddress, gender, courseName, applicationStatus))
        .thenReturn(expectedDetails);

    mockMvc.perform(MockMvcRequestBuilders.get(
        "/students?name=Tommy&emailAddress=bommytums@example.com&gender=Male&courseName=Show&applicationStatus=Expelled"))
        .andExpect(status().isOk());

    verify(service, times(1))
        .searchStudentList(name, emailAddress, gender, courseName, applicationStatus);

  }
}