package raisetech.student.management.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import raisetech.student.management.data.ApplicationStatus;
import raisetech.student.management.data.Course;
import raisetech.student.management.data.Student;

@MybatisTest
@Transactional
@Rollback(true)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class MainRepositoryTest {

  @Autowired
  private MainRepository sut;

  @Test
  void 受講生の全件検索が行えること() {
    List<Student> actual = sut.searchAllStudents();
    assertThat(actual.size()).isEqualTo(5);
  }

  @Test
  void 受講生情報をのIDで取得処理が行えること() {
    Student actual = sut.fetchById(1);
    assertThat(actual).isNotNull();
    assertThat(actual).extracting(
            Student::getId,
            Student::getName,
            Student::getKanaName,
            Student::getNickname,
            Student::getEmailAddress,
            Student::getResidence,
            Student::getAge,
            Student::getGender,
            Student::getRemark,
            Student::isWasDeleted)
        .containsExactly(
            1,
            "山田太郎",
            "ヤマダタロウ",
            "タロウ",
            "taro@example.com",
            "東京",
            25,
            "男性",
            "最初の登録者",
            false);
  }

  @Test
  void コースリストの全件検索が行えること() {
    List<Course> actual = sut.searchAllCourses();
    assertThat(actual.size()).isEqualTo(10);
  }

  @Test
  void コース情報をのIDで取得処理が行えること() {
    List<Course> actual = sut.fetchCourseById(5);
    LocalDate expectedStartDate = LocalDate.of(2023,12,1);
    LocalDate expectedEndDate = LocalDate.of(2024,4,1);
    assertThat(actual).isNotNull();
    assertThat(actual).extracting(
        Course::getId,
        Course::getStudentId,
        Course::getCourseName,
        Course::getCourseStartAt,
        Course::getCourseEndAt)
        .containsExactly(tuple(
        9,
        5,
        "AWSコース",
        expectedStartDate,
        expectedEndDate));
  }

  @Test
  void 受講生の登録が行えること() {
    Student student = new Student();
    student.setName("TestName");
    student.setKanaName("テストネーム");
    student.setNickname("テスト");
    student.setEmailAddress("test@example.com");
    student.setResidence("Area 51");
    student.setAge(20);
    student.setGender("その他");
    student.setRemark("テスト");
    student.setWasDeleted(false);

    sut.registerStudent(student);

    List<Student> actual = sut.searchAllStudents();

    assertThat(actual.size()).isEqualTo(6);

  }

  @Test
  void コースの新規登録が行えること() {
    Course course = new Course();
    course.setStudentId(1);
    course.setCourseName("Test Course");
    course.setCourseStartAt(LocalDate.now());
    course.setCourseEndAt(LocalDate.now().plusYears(1));

    sut.registerCourse(course);

    List<Course> actual = sut.searchAllCourses();

    assertThat(actual.size()).isEqualTo(11);
  }

  @Test
  void 受講生情報の更新を適切に行うこと() {
    Student actual = sut.fetchById(1);
    assertThat(actual).isNotNull();
    assertThat(actual).extracting(
            Student::getName,
            Student::getEmailAddress)
        .containsExactly(
            "山田太郎",
            "taro@example.com");

    String nameToUpdate = "Test Name";
    actual.setName(nameToUpdate);
    String emailAddressToUpdate = "test@example.com";
    actual.setEmailAddress(emailAddressToUpdate);

    sut.updateStudent(actual);

    Student expected = sut.fetchById(1);
    assertThat(expected).extracting(
            Student::getName,
            Student::getEmailAddress)
        .containsExactly(
            "Test Name",
            "test@example.com");

  }

  @Test
  void コース名の更新を適切に行うこと() {
    List<Course> preUpdateCourse = sut.fetchCourseById(5);
    Course courseToUpdate = preUpdateCourse.get(0);
    assertThat(preUpdateCourse).isNotNull();
    assertThat(courseToUpdate).extracting(
            Course::getId,
            Course::getStudentId,
            Course::getCourseName)
        .containsExactly(
            9,
            5,
            "AWSコース");

    String expectedName = "Crash Test Course";
    courseToUpdate.setCourseName(expectedName);

    sut.updateCourseName(courseToUpdate);
    sut.fetchCourseById(5);

    List<Course> actualUpdateCourse = sut.fetchCourseById(5);
    Course actualCourse = actualUpdateCourse.get(0);

    assertThat(actualCourse).extracting(
        Course::getId,
        Course::getStudentId,
        Course::getCourseName)
        .containsExactly(
            9,
            5,
            "Crash Test Course");
  }

  @Test
  void 申込状況情報の全件検索が行えること() {
    List<ApplicationStatus> actual = sut.searchAllStatus();
    assertThat(actual.size()).isEqualTo(10);
  }

  @Test
  void 申込状況をコースIDで取得処理が行えること() {
    Integer courseId = 2;
    List<Integer> courseIdList = List.of(courseId);

    List<ApplicationStatus> actualStatus = sut.fetchStatusByCourseIds(courseIdList);
    assertThat(actualStatus).isNotNull();
    assertThat(actualStatus.get(0).getCourseId()).isEqualTo(2);
    assertThat(actualStatus).extracting(
        ApplicationStatus::getId,
        ApplicationStatus::getCourseId,
        ApplicationStatus::getApplicationStatus)
        .containsExactly(tuple(
            2, 2,"受講中"
        ));

  }

  @Test
  void 申込状況の登録が適切に行えること() {
    ApplicationStatus status = new ApplicationStatus();
    status.setId(1);
    status.setCourseId(1);
    status.setApplicationStatus("本申込");

    sut.registerStatus(status);

    List<ApplicationStatus> actual = sut.searchAllStatus();

    assertThat(actual.size()).isEqualTo(11);

  }

  @Test
  void 申込状況の更新が適切に行えること() {
    ApplicationStatus expected = new ApplicationStatus();
    expected.setCourseId(1);
    expected.setApplicationStatus("仮申込");

    sut.registerStatus(expected);

    assertThat(expected).extracting(
        ApplicationStatus::getCourseId,
        ApplicationStatus::getApplicationStatus)
        .containsExactly(1,"仮申込");

    ApplicationStatus actual = new ApplicationStatus();

    actual.setCourseId(expected.getCourseId());
    actual.setApplicationStatus("本申込");

    sut.updateStatus(actual);

    assertThat(actual).extracting(
        ApplicationStatus::getCourseId,
        ApplicationStatus::getApplicationStatus)
        .containsExactly(1,"本申込");

  }

  @Test
  void 新規コース登録時にIDが自動採番されること() {
    Course course = new Course();
    course.setStudentId(322);
    course.setCourseName("Test");

    sut.registerCourse(course);

    assertThat(course.getId()).isNotNull();
    assertThat(course.getId()).isGreaterThan(0);

  }

}






