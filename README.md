
<img width="1024" height="572" alt="smsystem" src="https://github.com/user-attachments/assets/0032070f-c8eb-4cc9-8449-762bb96e53fd" />

<details open>
<summary>受講生募集システム</summary>

# 受講生募集システム・Student Management System

<details open>
<summary>サービス概要</summary>

## サービス概要

このプロジェクトは、Javaコース学習プログラムの一環として、受講生管理システムを実装し  
【受講生・コース・申請状況　を検索・登録・更新・論理削除】できるように、REST APIを使用して開発されました。

現在、アプリケーションはバックエンド開発のみを考慮して開発されています。  
フロントエンドの開発は、学習カリキュラムに沿って行われます。
</details>

<details open>
<summary>技術スタック</summary>

## 技術スタック

#### 開発言語・フレームワーク
![badge](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=ED8B00)
![badge](https://img.shields.io/badge/SpringBoot-3.5.6-%236DB33F?logo=spring)


#### データベース・O/Rマッパー
![badge](https://img.shields.io/badge/MySQL-%234479A1?logo=mysql&logoColor=white)
![badge](https://img.shields.io/badge/MyBatis-%23DC382D?logoColor=white)
![badge](https://img.shields.io/badge/H2%20Database-09476B?logo=h2database&logoColor=white)

#### ドキュメンテーション・ツール
![badge](https://img.shields.io/badge/Gradle-8.14.3-02303A?logo=gradle&logoColor=white)
![badge](https://img.shields.io/badge/Junit5-%2325A162?logo=junit5&logoColor=white)
![badge](https://img.shields.io/badge/Postman-%23FF6C37?logo=postman&logoColor=white)
![badge](https://img.shields.io/badge/Swagger-%2385EA2D?logo=swagger&logoColor=white)
![badge](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white)
![badge](https://img.shields.io/badge/GitHub-%23181717?logo=github&logoColor=white)
![badge](https://img.shields.io/badge/-IntelliJ%20IDEA-000.svg?logo=intellij-idea&style=flat)

</details>
<details open>
<summary>開発プロセス</summary>

## 開発プロセス

Java・Spring BootをプログラミンスクールのWebアプリケーション開発講座に沿って作成したプロジェクトになります。

#### →開発の流れ

1. 講座を視聴し、技術やツールの知識を得る。
2. 課題に基づいた新機能を実装する。
3. **プルリクエスト形式で提出し、講師に確認してもらう。**
4. レビューを基にした修正・追加を行う。
5. 問題なければ、次の講座へ進む。

</details>
<details open>
<summary>機能一覧</summary>

## 機能一覧

| 機能        | 詳細                         | 
|:---------|----------------------------|
| 新規登録 | 受講生情報、コース情報、申込状況を含む受講生詳細を新規登録 | 
| 条件検索| 氏名、メールアドレス、性別、コース名、申込状況の@RequestParamによる条件検索 | 
|単一取得 | IDに基づき、一意の受講生詳細を取得| 
|情報更新| 既存の受講生詳細データ（年齢、氏名など）を上書き更新 | 
| 論理削除 | wasDeletedフラグをtrueに更新し、データベースから物理削除せずに論理的に削除。 | 

※ 定義は以下のとおりです。

- 受講生詳細: 受講生、コース詳細をもつオブジェクト<br/>
  - 受講生: 氏名、メールアドレス、性別、年齢などをもつオブジェクト<br/>
  - コース詳細: コース、申込状況<br/>
    - コース: コース名、開始日、終了日をもつオブジェクト<br/>
    - 申込状況: 仮申込、本申込、受講中、受講終了をもつオブジェクト<br/>

</details>
<details open>
<summary>設計書</summary>

## 設計書

<details open>
<summary>ER図・API仕様書</summary>

### ER図

```mermaid 
erDiagram
    STUDENT ||--|{ STUDENT_COURSES: "enrolls in"
    STUDENT_COURSES ||--|| APPLICATION_STATUS: "has"
    STUDENT {
        int id PK "auto_increment"
        varchar(50) name
        varchar(50) kana_name
        varchar(50) nickname
        varchar(50) email_address
        varchar(50) residence
        int age
        varchar(50) gender
        varchar(50) remark
        tinyint was_deleted "0"
    }    
    STUDENT_COURSES {
        int id PK "auto_increment"
        int student_id "Students→id"
        varchar(50) course_name
        date course_start_at "登録する時、現在時刻を設定"
        date course_end_at
    }
    APPLICATION_STATUS {
        varchar(36) id PK
        varchar(36) course_id "Course→id"
        enum application_status "'仮申込', '本申込', '受講中', '受講終了'"
    }
```

### API仕様書
SwaggerによるAPI仕様書

1. Index
![Swagger UI Index](assets/images/swagger_index.png)
2. GET /students/{id} 受講生ID検索
![Swagger UI GET /students/{id} 受講生ID検索](assets/images/swagger_get_id.png)
3. PUT /students/{id} 受講生詳細情報更新
![Swagger UI PUT /students/{id} 受講生詳細情報更新](assets/images/swagger_put_id.png)
4. GET /students 一覧検索
![Swagger UI GET /students 一覧検索](assets/images/swagger_get_filter.png)
5. POST /students 受講生登録
![Swagger UI POST /students 受講生登録](assets/images/swagger_post.png)
6. Schema
![Swagger UI Schema](assets/images/swagger_schemas.png)

</details>
<details open>
<summary>シーケンス図</summary>

### シーケンス図 

#### 受講生詳細の新規登録フロー
```mermaid
sequenceDiagram
    actor C as Client
    participant Ctrl as MainController
    participant S as MainService
    participant R as MainRepository
    participant DB as MySQL Database

    C->>Ctrl: POST /students (StudentDetail DTO)
    activate Ctrl
    Note right of Ctrl: 1. 入力値検証 (@Valid)

    Ctrl->>S: registerStudent(studentDetail)
    activate S
    Note right of S: 2. トランザクション開始 (@Transactional)

    S->>R: registerStudent(student)
    activate R
    R->>DB: INSERT INTO students (受講生登録)
    deactivate R

    loop For each Course Detail in List
        Note right of S: 3. コース/ステータス情報設定 (initStudentCourse)
        S->>R: registerCourse(course)
        activate R
        R->>DB: INSERT INTO courses
        deactivate R

        S->>R: registerStatus(status)
        activate R
        R->>DB: INSERT INTO application_status
        deactivate R
    end

    Note right of S: 4. トランザクションコミット (自動)
    S-->>Ctrl: StudentDetail レスポンス
    deactivate S

    Ctrl-->>C: 200 OK (StudentDetail レスポンス)
    deactivate Ctrl
```
#### 受講生詳細の更新フロー・論理削除フロー
```mermaid

sequenceDiagram
actor C as Client
participant Ctrl as MainController
participant S as MainService
participant R as MainRepository
participant DB as MySQL Database

    C->>Ctrl: PUT /students/{id} (StudentDetail DTO)
    activate Ctrl
    
    Note right of Ctrl: 1. Path変数検証 (@Pattern)<br/>2. Path IDをDTOに設定
    
    Ctrl->>S: updateStudent(studentDetail)
    activate S
    Note right of S: 3. トランザクション開始 (@Transactional)

    loop For each Course Detail in List
        S->>R: updateCourseName(course)
        activate R
        R->>DB: UPDATE student_courses (コース名更新)
        deactivate R
        
        alt Status IDがnullの場合
            Note right of S: 4-a. 仮申込として新規登録 (status.getId() == null)
            S->>R: registerStatus(status)
            activate R
            R->>DB: INSERT INTO application_status
            deactivate R
        else Status IDが存在する場合
            Note right of S: 4-b. 既存の申込状況を更新
            S->>R: updateStatus(status)
            activate R
            R->>DB: UPDATE application_status
            deactivate R
        end
    end
    
    S->>R: updateStudent(student)
    activate R
    R->>DB: UPDATE students<br/>(受講生情報と論理削除フラグ更新)
    deactivate R

    Note right of S: 5. トランザクションコミット (自動)
    S-->>Ctrl: 更新処理完了
    deactivate S
    
    Ctrl-->>C: 200 OK ("更新処理が成功しました。")
    deactivate Ctrl
```
#### 受講生詳細の条件検索フロー
```mermaid
sequenceDiagram
    actor  C as Client
    participant Ctrl as MainController
    participant S as MainService
    participant R as MainRepository
    participant Conv as MainConverter
    participant DB as MySQL Database

    C->>Ctrl: GET /students?name=...&emailAddress=...
    activate Ctrl
    
    Note right of Ctrl: 1. パラメータ抽出 (@RequestParam)
    
    Ctrl->>S: searchStudentList(name, emailAddress, ...)
    activate S
    
    Note right of S: 2. 検索条件のService側での準備
    
    S->>R: searchStudentByCriteria(...)
    activate R
    R->>DB: SELECT students WHERE... (条件に合致する受講生のみ取得)
    DB-->>R: studentList
    deactivate R
    
    alt studentListがnullまたは空の場合
        Note right of S: 3-a. 結果を空リストに変換し返却
        S->>Conv: convertDetails(empty)
        Conv-->>S: emptyList
    else studentListが存在する場合
        Note right of S: 3-b. 関連情報取得のためのIDリスト作成
        S->>R: searchCoursesByStudentId(studentIdList)
        activate R
        R->>DB: SELECT courses (紐づくコース一覧取得)
        DB-->>R: courseList
        deactivate R
        
        S->>R: searchStatusByStudentId(studentIdList)
        activate R
        R->>DB: SELECT status (紐づく申込状況一覧取得)
        DB-->>R: statusList
        deactivate R
        
        Note right of S: 4. 取得した全データのマッピング
        S->>Conv: convertDetails(studentList, courseList, statusList)
        activate Conv
        Note right of Conv: 5. StudentDetail DTOへの組み立て処理
        Conv-->>S: List<StudentDetail>
        deactivate Conv
    end
    
    S-->>Ctrl: List<StudentDetail>
    deactivate S
    
    Ctrl-->>C: 200 OK (受講生詳細リスト)
    deactivate Ctrl
```
#### 一意の受講生詳細を検索
```mermaid
sequenceDiagram
    actor C as Client
participant Ctrl as MainController
participant S as MainService
participant R as MainRepository
participant Conv as MainConverter
participant DB as MySQL Database

C->>Ctrl: GET /students/{id}
activate Ctrl

Note right of Ctrl: 1. Path変数検証 (@Pattern)

Ctrl->>S: searchStudentId(id)
activate S

S->>R: fetchById(id)
activate R
R->>DB: SELECT * FROM students WHERE id = ?
deactivate R

alt Studentが見つからない場合
DB-->>R: empty
R-->>S: null
Note right of S: 2-a. nullを返却
S-->>Ctrl: null
Ctrl-->>C: 404 Not Found
else Studentが見つかった場合
DB-->>R: Student Entity
R-->>S: Student Entity

Note right of S: 2-b. 関連情報取得

S->>R: fetchCourseById(student.getId())
activate R
R->>DB: SELECT courses (コース一覧取得)
DB-->>R: courseList
deactivate R

S->>R: fetchStatusByCourseIds(courseIdList)
activate R
R->>DB: SELECT status (申込状況一覧取得)
DB-->>R: statusList
deactivate R

Note right of S: 3. DTOへのマッピング (convertDetails)
S->>Conv: convertDetails(studentList, courseList, statusList)
activate Conv
Conv-->>S: List<StudentDetail>
deactivate Conv

Note right of S: 4. リストから単一要素を抽出
S-->>Ctrl: StudentDetail DTO

Ctrl-->>C: 200 OK (StudentDetail DTO)
end

deactivate S
deactivate Ctrl
```

</details>
<details open>
<summary>APIのURI設計・Postman・リスポンス確認</summary>

### APIのURI設計

| HTTPメソッド | URIパス (URI Path)                     | 処理内容                  |
|:---------|------------------------------|-----------------------|
| POST     |/students  | 受講生詳細の新規登録            |
| GET     |/students  | 受講生詳細の条件検索            |
| GET   | /students/{id} | IDに基づき、一意の受講生詳細を検索    |
| PUT     | /students/{id}| IDに基づき、一意の受講生詳細を更新    |
| PUT    |/students/{id} | IDに基づき、受講生情報を取得し、論理削除 |


### Postman・リスポンス確認

#### GET　受講生の一覧検索
![man_get_all.png](assets/images/man_get_all.png)

#### GET
![man_get_email.png](assets/images/man_get_email.png)

#### GET
![man_get_name.png](assets/images/man_get_name.png)

#### GET
![man_get_id.png](assets/images/man_get_id.png)

#### GET id - PUT before
![man_pre_put.png](assets/images/man_pre_put.png)

#### PUT 200 ok
![man_put_200.png](assets/images/man_put_200.png)

#### GET id - PUT after
![man_put_result.png](assets/images/man_put_result.png)

#### POST body
![man_post_data.png](assets/images/man_post_data.png)

#### POST result
![man_post_result.png](assets/images/man_post_result.png)

</details>
</details>
<details open>
<summary>テスト</summary>

## テスト

下記のテストは・Junit5・Mockito・を使用したテストです。

### MainControllerテスト
![controller_tests.png](assets/images/controller_tests.png)

### MainConverterテスト
![converter_tests.png](assets/images/converter_tests.png)

### MainRepositoryテスト
![repository_tests.png](assets/images/repository_tests.png)

### MainServiceテスト
![service_tests.png](assets/images/service_tests.png)

</details>
<details open>
<summary>力を入れたところ</summary>

## 力を入れたところ

・WEBアプリ開発はゼロからの学習でしたので、オブジェクト指向、命名規則、開発の流れを中心にしてきました。
最終課題に「申込状況」の追加と言うことで、全体的にリファクタリングし、メソッド名やテストケースを修正しました。

- StudentDetail
  - Student
  - Course
から
- StudentDetail
  - Student
  - CourseDetail
    - Course
    - ApplicationStatus
への変更。

【以前のデータ設計】 受講生 1 : 多 コース　<br/>
【リファクタリング後の設計】 受講生 1 : 多 コース、コース 1 : 1 申込状況 <br/>

・URI Path を REST APIエンドポイントの命名に関するベストプラクティスを考慮しました。

→/registerStudent・/updateStudent・などを　/students・/students/{id}　に変更。

・クライアント側でユーザビリティを大切にして、自動採番処理を実装してきました。

- 受講を新規登録 → 氏名、メールアドレス、年齢、コース名などを入力(入力値検証あり)
- 登録のリクエスト　→　コース開始日、申込状況を自動採番
- 論理削除をしたい場合は、更新処理で簡単にできるようにしています(削除フラグを変更)

【AWS環境でのデプロイと運用】 
・ EC2、RDS、VPC、セキュリティグループ、そしてNACLを使用して、アプリケーションをAWS上にデプロイしました。

【最大の課題：ネットワーク接続】
外部からのWebアクセス (Port 8080) や、EC2からRDSへのDB接続の際、NACLのアウトバウンド・インバウンドルール設定で苦戦しました。特に、応答パケット用のエフェメラルポートの必要性を深く理解しました。  

</details>
<details open>
<summary>今後の展望</summary>

## 今後の展望

・現在、デプロイを学習しており(CI:CD, GitHubActions, Docker)、
早いうちにデプロイをできるように進んでいきます。

・デプロイ後は、フロントエンド(JavaScript → React → Next.js)の学習をして、実装するつもりです。<br/>
ユーザビリティを中心にしてプロジェクトを作成していきたいです。

・Homeページ<br/>
・ページネーション<br/>
・DBの外部キー設定<br/>

</details>
</details>


