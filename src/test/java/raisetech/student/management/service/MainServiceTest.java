package raisetech.student.management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.student.management.controller.converter.MainConverter;
import raisetech.student.management.data.ApplicationStatus;
import raisetech.student.management.data.Course;
import raisetech.student.management.data.Student;
import raisetech.student.management.domain.CourseDetail;
import raisetech.student.management.domain.StudentDetail;
import raisetech.student.management.repository.MainRepository;


@ExtendWith(MockitoExtension.class)
class MainServiceTest {

  @Mock
  private MainRepository repository;

  @Mock
  private MainConverter converter;

  private MainService sut;

  @BeforeEach
  void before() {
    sut = new MainService(repository, converter);
  }

  @Test
  void 受講生詳細の一覧検索_リポジトリとコンバーターの処理が適切に呼び出されていること() {
    Student mockStudent = mock(Student.class);
    List<Student> studentList = List.of(mockStudent);
    Course mockCourse = mock(Course.class);
    List<Course> courseList = List.of(mockCourse);
    ApplicationStatus mockStatus = mock(ApplicationStatus.class);
    List<ApplicationStatus> statusList = List.of(mockStatus);
    List<StudentDetail> expectedDetails = new ArrayList<>();

    when(mockStudent.getId()).thenReturn(1);

    when(repository.searchStudentByCriteria(
        null, null, null, null, null))
        .thenReturn(studentList);
    when(repository.searchCoursesByStudentId(anyList())).thenReturn(courseList);
    when(repository.searchStatusByStudentId(anyList())).thenReturn(statusList);
    when(converter.convertDetails(anyList(), anyList(), anyList())).thenReturn(expectedDetails);

    List<StudentDetail> actual = sut.searchStudentList(
        null, null, null, null ,null);

    verify(repository, times(1)).searchStudentByCriteria(
        null, null, null, null, null);
    verify(repository, times(1)).searchCoursesByStudentId(anyList());
    verify(repository, times(1)).searchStatusByStudentId(anyList());
    verify(converter, times(1)).convertDetails(studentList, courseList, statusList);

    assertEquals(expectedDetails, actual);

  }


  @Test
  void 受講生詳細の検索_リポジトリからIDに紐づく検索処理が適切に呼び出されていること() {
    Student student = new Student();
    student.setId(555);
    List<Student> studentList = List.of(student);
    Course course = new Course();
    course.setId(2);
    List<Course> courseList = List.of(course);
    List<ApplicationStatus> statusList = new ArrayList<>();
    when(repository.fetchById(student.getId())).thenReturn(student);
    when(repository.fetchCourseById(student.getId())).thenReturn(courseList);
    when(repository.fetchStatusByCourseIds(anyList())).thenReturn(statusList);

    StudentDetail expectedDetail = new StudentDetail();
    expectedDetail.setStudent(student);
    List<StudentDetail> expectedDetailList = List.of(expectedDetail);
    when(converter.convertDetails(studentList, courseList, statusList)).thenReturn(expectedDetailList);

    StudentDetail actual = sut.searchStudentId(student.getId());


    verify(repository, times(1)).fetchById(student.getId());
    verify(repository, times(1)).fetchCourseById(student.getId());
    verify(repository, times(1)).fetchStatusByCourseIds(anyList());
    verify(converter, times(1)).convertDetails(studentList, courseList, statusList);

    assertEquals(student, actual.getStudent());
    assertEquals(expectedDetail, actual);
  }

  @Test
  void 受講生詳細の登録が適切に実装していること() {
    Student student = new Student();
    student.setId(555);
    student.setName("Gordon");
    List<Student> studentList = List.of(student);

    Course course = new Course();
    course.setCourseName("Testing Unit Course");
    List<Course> courseList = List.of(course);
    ApplicationStatus status = new ApplicationStatus();
    List<ApplicationStatus> statusList = List.of(status);

    CourseDetail courseDetail = new CourseDetail(course, status);
    List<CourseDetail> courseDetailList = List.of(courseDetail);

    StudentDetail expectedRegisterDetail = new StudentDetail(student, courseDetailList);

    StudentDetail actual = sut.registerStudent(expectedRegisterDetail);

    verify(repository, times(1)).registerStudent(student);
    verify(repository, times(1)).registerCourse(course);
    verify(repository, times(1)).registerStatus(status);

    assertEquals(student, actual.getStudent());
    assertEquals(student.getId(), actual.getStudent().getId());
    assertEquals(expectedRegisterDetail, actual);

  }

  @Test
  void 受講生詳細の更新処理が適切に実装していること() {
    Student student = new Student();
    student.setId(777);
    Course course = new Course();
    course.setId(123);
    course.setCourseName("Slot Mechanic");
    ApplicationStatus status = new ApplicationStatus();
    status.setId(444);
    status.setCourseId(123);
    status.setApplicationStatus("仮申込");
    CourseDetail courseDetail = new CourseDetail(course, status);
    List<CourseDetail> courseDetailList = List.of(courseDetail);
    StudentDetail expectedUpdateDetail = new StudentDetail();
    expectedUpdateDetail.setStudent(student);
    expectedUpdateDetail.setCourseDetailList(courseDetailList);

    sut.updateStudent(expectedUpdateDetail);

    verify(repository, times(1)).updateStudent(student);
    verify(repository, times(1)).updateCourseName(course);
    verify(repository, times(1)).updateStatus(status);

  }

  @Test
  void 受講生詳細の登録_初期化処理が行われること() {
    Student student = new Student();
    student.setId(888);
    Course course = new Course();

    sut.initStudentCourse(course, student);

    assertEquals(student.getId(), course.getStudentId());
    assertEquals(LocalDate.now(), course.getCourseStartAt());
  }

}