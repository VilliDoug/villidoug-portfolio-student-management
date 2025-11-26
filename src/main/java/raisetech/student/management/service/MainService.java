package raisetech.student.management.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.student.management.controller.converter.MainConverter;
import raisetech.student.management.data.ApplicationStatus;
import raisetech.student.management.data.Student;
import raisetech.student.management.data.Course;
import raisetech.student.management.domain.StudentDetail;
import raisetech.student.management.repository.MainRepository;

/**
 * 受講生情報を取り扱うServiceです。
 * 受講生の検索や登録、更新処理を行います。
 */
@Service
public class MainService {

  private MainRepository repository;

  @Autowired
  private MainConverter converter;

  @Autowired
  public MainService(MainRepository repository, MainConverter converter) {
    this.repository = repository;
    this.converter = converter;
  }

  /**
   * 受講生詳細の一覧検索を行います。
   * 全件検索を行うので、条件指定は行いません。
   *
   * @return　受講生詳細一覧（全件）
   */
  public List<StudentDetail> searchStudentList(
      String name,
      String emailAddress,
      String gender,
      String courseName,
      String applicationStatus) {
    List<Student> studentList = repository.searchStudentByCriteria(
        name, emailAddress, gender, courseName, applicationStatus);
    if (studentList == null || studentList.isEmpty()) {
      return converter.convertDetails(studentList, Collections.emptyList(), Collections.emptyList());
    }
    List<Integer> studentIdList = studentList.stream()
        .map(student -> student.getId())
        .toList();
    List<Course> courseList = repository.searchCoursesByStudentId(studentIdList);
    List<ApplicationStatus> statusList = repository.searchStatusByStudentId(studentIdList);
    return converter.convertDetails(studentList, courseList, statusList);
  }

  /**
   * 受講生詳細検索です。
   * IDに紐づく受講生情報を取得したあと、その受講生に紐づく受講生コース情報を取得して設定します。
   *
   * @param id　受講生ID
   * @return　受講生詳細
   */
  public StudentDetail searchStudentId(Integer id) {
    Student student = repository.fetchById(id);
    if (student == null) {
      return null;
    }
    List<Student> studentList = List.of(student);
    List<Course> fetchedCourseList = repository.fetchCourseById(student.getId());
    List<Course> courseList = (fetchedCourseList == null)
        ? Collections.emptyList()
        : fetchedCourseList;

    List<Integer> courseIdList =  courseList.stream()
        .map(course -> course.getId())
        .collect(Collectors.toList());
    List<ApplicationStatus> statusList = (courseIdList.isEmpty())
        ? Collections.emptyList()
        : repository.fetchStatusByCourseIds(courseIdList);
    List<StudentDetail> detailList = converter.convertDetails(studentList, courseList, statusList);
    if (detailList.isEmpty()) {
      return null;
    } else {
      return detailList.get(0);
    }

  }

  /**
   * 受講生詳細の登録を行います。
   * 受講生と受講生コース情報を個別に登録し、受講生コース情報には受講生情報を紐づける値や日付情報（コース開始日）を設定します。
   *
   * @param studentDetail　受講生詳細
   * @return　登録情報を付与した受講生詳細
   */
  @Transactional
  public StudentDetail registerStudent(StudentDetail studentDetail) {
    Student student = studentDetail.getStudent();
    repository.registerStudent(student);

    studentDetail.getCourseDetailList().forEach(courseDetail -> {
      Course course = courseDetail.getCourse();
      ApplicationStatus status = Objects.requireNonNullElse(
          courseDetail.getApplicationStatus(), new ApplicationStatus());
      status.setApplicationStatus("仮申込");

      initStudentCourse(course, student);
      repository.registerCourse(course);
      status.setCourseId(course.getId());
      repository.registerStatus(status);
    });
    return studentDetail;
  }

  /**
   * 受講生詳細の更新を行います。受講生と受講生コース情報をそれぞれ更新します。
   *
   * @param studentDetail　受講生詳細
   */
  @Transactional
  public void updateStudent(StudentDetail studentDetail) {
    studentDetail.getCourseDetailList().forEach(courseDetail -> {
      repository.updateCourseName(courseDetail.getCourse());
      Course course = courseDetail.getCourse();

      ApplicationStatus status = Objects.requireNonNullElse(
          courseDetail.getApplicationStatus(), new ApplicationStatus());
      status.setCourseId(course.getId());

      if (status.getId() == null) {
        status.setApplicationStatus("仮申込");
        repository.registerStatus(status);
      } else {
        repository.updateStatus(status);
      }
    });
    repository.updateStudent(studentDetail.getStudent());
  }

  /**
   * 受講生コース情報を登録する際の初期情報を設定する
   *
   * @param course　受講生コース情報
   * @param student　受講生
   */
  void initStudentCourse(Course course, Student student) {
    course.setStudentId(student.getId());
    course.setCourseStartAt(LocalDate.now());
  }
}


