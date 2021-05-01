# Spring Framework 정리 자료
실전! 스프링 MVC 1편 정리 문서

Table of contents
=================
<!--ts-->
   * [웹 애플리케이션 이해](#웹-애플리케이션-이해)
   * [서블릿](#서블릿)
   * [서블릿, JSP, MVC 패턴](#서블릿-JSP-MVC-패턴)
   * [MVC 프레임워크 만들기](#MVC-프레임워크-만들기)
   * [스프링 MVC 구조 이해](#스프링-MVC-구조-이해)
   * [스프링 MVC 기본 기능](#스프링-MVC-기본-기능)
   * [스프링 MVC 웹 페이지 만들기](#스프링-MVC-웹-페이지-만들기)
<!--te-->

웹 애플리케이션 이해
=======


### 웹 서버, 웹 애플리케이션 서버
##### 웹
- HTTP 기반
- 모든 것이 HTTP: HTTP 메시지에 모든 것을 전송
  - HTML, TEXT
  - IMAGE, 음성, 영상, 파일
  - JSON, XML (API)
  - 거의 모든 형태의 데이터 전송 가능
  - 서버간에 데이터를 주고 받을 때도 대부분 HTTP 사용

##### 웹서버
- HTTP 기반으로 동작
- `정적 리소스 제공`, 기타 부가 기능 제공
  - 정적(파일): HTML, CSS, JS, 이미지, 영상
- nginx, apache

##### 웹 애플리케이션 서버
- HTTP 기반으로 동작
- 프로그램 코드를 실행해서 `애플리케이션 로직 수행`
  - 동적 HTML, HTTP API(JSON)
  - 서블릿, JSP, 스프링 MVC
- Tomcat Jetty, Undertow

##### 웹서버와 웹 애플리케이션 서버 차이
- 기본적으로 웹 서버는 정적 리소스(파일), WAS는 애플리케이션 로직
- 자바는 `서블릿 컨테이너 기능`을 제공하면 WAS
- 웹 서버도 프로그램을 실행하는 기능을 포함하기도 하고 웹 애플리케이션 서버도 웹 서버의 기능을 제공함

##### 웹 시스템의 구성
- 웹 애플리케이션 서버 - DB 만으로도 구성이 가능하나 보통 `웹서버 - 웹 에플리케이션 서버 - DB`으로 구성
- WAS가 너무 많은 역할을 담당하면 서버 과부하 우려하여 웹서버 필요
- WAS 장애시 오류 화면도 노출 불가능하므로 웹서버 필요
- 웹 서버는 애플리케이션 로직같은 동적인 처리가 필요하면 WAS에 요청

### 서블릿

##### HTML Form 데이터 전송 처리 과정
- HTTP 요청 메시지
```
POST /save HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
username=kim&age=20
```
- 서버에서 처리해야하는 일
  - 서버 TCP/IP 연결 대기, 소켓 연결
  - HTTP 요청 메시지를 파싱해서 읽기 
  - POST 방식, /save URL 인지 
  - Content-Type 확인
  - HTTP 메시지 바디 내용 피싱
    - username, age 데이터를 사용할 수 있게 파싱 저장 프로세스 실행
  - `비즈니스 로직 실행`
    - 데이터베이스에 저장 요청 
  - HTTP 응답 메시지 생성 시작
    - HTTP 시작 라인 생성
    - Header 생성
    - 메시지 바디에 HTML 생성에서 입력
  - TCP/IP에 응답 전달, 소켓 종료
- HTTL 응답 메시지
```
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8 Content-Length: 3423
<html> <body>...</body>
</html>
```

##### 서블릿
- 비즈니스 로직 이외의 HTTP 스펙을 처리를 해주는 객체
- 서블릿 코드
  ```java
  @WebServlet(name = "helloServlet", urlPatterns = "/hello")
  public class HelloServlet extends HttpServlet {
      @Override
      protected void service(HttpServletRequest request, HttpServletResponse response){
        //애플리케이션 로직
      } 
  }
  ```
  - urlPatterns(/hello)의 URL이 호출되면 서블릿 코드가 실행
  - HTTP 요청 정보를 편리하게 사용할 수 있는 HttpServletRequest 
  - HTTP 응답 정보를 편리하게 제공할 수 있는 HttpServletResponse 

- 서블릿 컨테이너
  - 톰캣처럼 서블릿을 지원하는 WAS는 `서블릿 컨테이너`를 포함하고 있어야함
  - 서블릿 객체를 생성, 초기화, 호출, 종료하는 생명주기 관리
  - 서블릿 객체는 싱글톤으로 관리 -> `공유 변수 사용 시 조심`
  - JSP도 서블릿으로 변환 되어서 사용
  - 동시 요청에 대한 `멀티 쓰레드 지원`

### 동시 요청 - 멀티 스레드

##### 쓰레드
- 쓰레드
  - 애플리케이션 코드를 하나하나 순차적으로 실행하는 것은 `쓰레드`라고 함
  - 자바 메인 메서드를 처음 실행하면 main 이라는 이름의 쓰레드가 실행
  - 쓰레드가 없다면 자바 애플리케이션 실행이 불가능
  - 쓰레드는 `한번에 하나의 코드만 수행`
  - 동시 처리가 필요하면 쓰레드를 추가로 생성
- 요청마다 쓰레드 생성
  - 장점
    - 동시 요청을 처리
    - 리소스(CPU, 메모리)가 허용할 때 까지 처리
    - 하나의 쓰레드가 지연 되어도, 나머지 쓰레드는 정상 동작
  - 단점
    - 쓰레드는 생성 비용은 매우 비쌈
    - 쓰레드는 컨텍스트 스위칭 비용이 발생
    - 쓰레드 생성에 제한이 없어 서버가 죽을 수 있음

##### 쓰레드 풀
- 동시 요청을 효율적으로 처리하기 위해 쓰레드 풀에 생성 가능한 쓰레드를 관리
- 장점
  - 쓰레드가 필요하면, 이미 생성되어 있는 쓰레드를 쓰레드 풀에서 꺼내서 사용하여 생성 비용이 적음
  - 사용을 종료하면 쓰레드 풀에 해당 쓰레드를 반납하여 종료 비용이 적음
  - 생성 가능한 쓰레드의 최대치가 있으므로 너무 많은 요청이 들어와도 기존 요청은 안전하게 처리
- 단점
  - 구현이 어려움
  - 최대 쓰레드의 수 선정이 필요
- WAS의 주요 튜닝 포인트는 `최대 쓰레드(max thread) 수`
  - 값이 낮으면 -> 서버 리소스는 여유롭지만, 클라이언트는 금방 응답 지연
  - 값이 높으면 -> CPU, 메모리 리소스 임계점 초과로 서버 다운
  - 제이미터, nGrinder 등을 통해서 성능 테스트하여 환경 별로 최대 쓰레드의 적정 수를 체크해야함


##### WAS의 멀티 쓰레드 지원
- 멀티 쓰레드에 대한 부분은 WAS가 처리하여 개발자가 멀티 쓰레드 관련 코드를 신경쓰지 않아도 됨
- 멀티 쓰레드 환경이므로 싱글톤 객체(서블릿, 스프링 빈)의 멤버 변수는 주의해서 사용해야함

### HTML, HTTP API, CSR, SSR

##### 정적 리소스
- 고정된 HTML 파일, CSS, JS, 이미지, 영상 등을 제공
- HTML은 JSP, 타임리프를 통해 동적으로 필요한 HTML 파일을 생성도 가능

##### HTTP API
- HTML이 아니라 `데이터만 전달`
- 주로 JSON 형식 사용
- 앱과 웹 등 다양한 시스템에서 호출이 가능


##### CSR 클라이언트 사이드 렌더링
- HTML 결과를 `자바스크립트를 사용해 웹 브라우저에서 동적으로 생성해서 적용`
- 주로 동적인 화면에 사용, 웹 환경을 마치 앱 처럼 필요한 부분부분 변경할 수 있음
- HTML 요청 -> 자바스크립트 요청 -> HTTP API로 데이터 요청 -> HTML 결과 렌더링
- 관련기술: React, Vue.js -> 웹 프론트엔드 개발자

##### SSR 서버 사이드 렌더링
- `HTML 최종 결과를 서버에서 만들어서 웹 브라우저에 전달`
- 주로 정적인 화면에 사용
- 관련 기술: JSP, 타임리프 -> 백엔드 개발자

서블릿
=======
### HttpServeltRequest 개요

##### HttpServletRequest 역할
- 서블릿은 개발자가 HTTP 요청 메시지를 편리하게 사용할 수 있도록 개발자 대신에 HTTP 요청 메시지를 파싱하고 그 결과를 `HttpServletRequest` 객체에 담아서 제공
- HTTP 요청 메시지
  - 메시지 예시
  ```json
  POST /save HTTP/1.1
  Host: localhost:8080
  Content-Type: application/x-www-form-urlencoded
  username=kim&age=20
  ```
  - START LINE
    - HTTP 메소드
    - URL
    - 쿼리 스트링
    - 스키마, 프로토콜 
  - 헤더
    - 헤더 조회
  - 바디
    - form 파라미터 형식 조회
    - message body 데이터 직접 조회
- 임시 저장소 기능
  - 저장: request.setAttribute(name, value)
  - 조회: request.getAttribute(name)
- 세션 관리 기능: request.getSession(create: true)

### HttpServeltRequest 기본 사용법
- 서블릿 요청 처리 코드
```java
// http://localhost:8080/request-header?username=hello
@WebServlet(name = "requestHeaderServlet", urlPatterns = "/request-header") 
public class RequestHeaderServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException 
    {
        printStartLine(request);
        printHeaders(request);
        printHeaderUtils(request);
        printEtc(request);
        response.getWriter().write("ok"); 
    }
}
```
- 출력 코드
  - start-line
  ```java
  private void printStartLine(HttpServletRequest request) {
    System.out.println("--- REQUEST-LINE - start ---");
    System.out.println("request.getMethod() = " + request.getMethod()); // GET
    System.out.println("request.getProtocal() = " + request.getProtocol()); // HTTP/1.1
    System.out.println("request.getScheme() = " + request.getScheme()); // http
    System.out.println("request.getRequestURL() = " + request.getRequestURL()); // http://localhost:8080/request-header
    System.out.println("request.getRequestURI() = " + request.getRequestURI()); // /request-header
    System.out.println("request.getQueryString() = " + request.getQueryString()); // username=hi
    System.out.println("request.isSecure() = " + request.isSecure()); //https 사용 유무
    System.out.println("--- REQUEST-LINE - end ---");
    System.out.println();
  }
  ```
  - 헤더 
  ```java
  private void printHeaders(HttpServletRequest request) {
    System.out.println("--- Headers - start ---"); 
    request.getHeaderNames().asIterator()
                            .forEachRemaining(headerName -> System.out.println(headerName + ":" + request.getHeader(headerName))); 
    System.out.println("--- Headers - end ---"); 
    System.out.println();
  }
  ```
  ```java
   private void printHeaderUtils(HttpServletRequest request) {
    System.out.println("--- Header 편의 조회 start ---"); 
    System.out.println("[Host 편의 조회]"); 
    System.out.println("request.getServerName() = " + request.getServerName()); //Host 헤더 
    System.out.println("request.getServerPort() = " + request.getServerPort()); //Host 헤더 
    System.out.println();
    
    System.out.println("[Accept-Language 편의 조회]");
    request.getContentLength()); 
    System.out.println("request.getCharacterEncoding() = " + request.getCharacterEncoding());
    
    System.out.println("--- Header 편의 조회 end ---");
    System.out.println(); 

    System.out.println("[cookie 편의 조회]"); 
    if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) { 
            System.out.println(cookie.getName() + ": " + cookie.getValue());
        } 
    }
    System.out.println();
    System.out.println("[Content 편의 조회]");
    
    System.out.println("request.getContentType() = " + request.getContentType());
    System.out.println("request.getContentLength() = " + request.getContentLength()); 
    System.out.println("request.getCharacterEncoding() = " + request.getCharacterEncoding());

    System.out.println("--- Header 편의 조회 end ---");
    System.out.println();

  }
  ```
  - 기타 정보
  ```java
  private void printEtc(HttpServletRequest request) { 
      System.out.println("--- 기타 조회 start ---");
      System.out.println("[Remote 정보]");
      System.out.println("request.getRemoteHost() = " + request.getRemoteHost()); // 0:0:0:0:0:0:0:1
      System.out.println("request.getRemoteAddr() = " + request.getRemoteAddr()); // 0:0:0:0:0:0:0:1
      System.out.println("request.getRemotePort() = " + request.getRemotePort()); // 54305
      System.out.println();
      
      System.out.println("[Local 정보]");
      System.out.println("request.getLocalName() = " + request.getLocalName()); // localhost
      System.out.println("request.getLocalAddr() = " + request.getLocalAddr()); // 0:0:0:0:0:0:0:1
      System.out.println("request.getLocalPort() = " + request.getLocalPort()); // 8080
      System.out.println("--- 기타 조회 end ---");
      System.out.println(); 
  }
  ```

### HTTP 요청 데이터 개요

##### 클라이언트에서 서버로 데이터를 전달하는 방법
- GET - `쿼리 파라미터`
  - /url`?username=hello&age=20`
  - 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달 
  - 검색, 필터, 페이징 등에서 많이 사용하는 방식
  - 쿼리 파라미터는 URL에 ?를 시작으로 보낼 수 있고 추가 파라미터는 &로 구분
- POST - HTML `Form`
  - content-type: application/x-www-form-urlencoded
  - 메시지 바디에 쿼리 파리미터 형식으로 전달 `username=hello&age=20`
  - 회원 가입, 상품 주문, HTML Form 사용
- HTTP message `body`에 데이터를 직접 담아서 요청
  - HTTP API에서 주로 사용, JSON, XML, TEXT
  - 데이터 형식은 주로 JSON 사용 (POST, PUT, PATCH)

### HTTP 요청 데이터 - GET 쿼리 파라미터

##### 쿼리 파라미터 조회 메서드
- 쿼리
  - 메소드: GET
  - 요청 URL: http://localhost:8080/request-param?username=hello&age=20
- 쿼리 파라미터 조회 메서드
```java
// HttpServletRequest인 request
String username = request.getParameter("username"); // 단일 파라미터 조회 
Enumeration<String> parameterNames = request.getParameterNames(); // 파라미터 이름들 모두 조회
Map<String, String[]> parameterMap = request.getParameterMap(); // 파라미터를 Map 으로 조회
String[] usernames = request.getParameterValues("username"); // 복수 파라미터 조회
```
- 같은 파라미터 이름은 하나인데, 값이 중복인 경우 처리 방법
  - request.getParameter() 는 하나의 파라미터 이름에 대해서 단 하나의 값만 있을 때 사용해야하고 중복일 때는 request.getParameterValues() 를 사용
  - 중복인 경우에도 request.getParameter()를 사용하면 getParameterValues()의 첫번째 값을 반환

### HTTP 요청 데이터 - POST HTML Form

##### HTML Form 조회 메소드
- 쿼리
  - 메소드: POST
  - 요청 URL: http://localhost:8080/request-param
  - content-type: application/x-www-form-urlencoded
  - message body: username=hello&age=20
- 폼 데이터 조회 메서드
```java
// HttpServletRequest인 request
String username = request.getParameter("username"); // 단일 파라미터 조회 
Enumeration<String> parameterNames = request.getParameterNames(); // 파라미터 이름들 모두 조회
Map<String, String[]> parameterMap = request.getParameterMap(); // 파라미터를 Map 으로 조회
String[] usernames = request.getParameterValues("username"); // 복수 파라미터 조회
```
- request.getParameter() 는 GET URL 쿼리 파라미터 형식도 지원하고, POST HTML Form 형식도 둘 다 지원

### HTTP 요청 데이터 API 메시지 바디 - 단순 텍스트

##### HTTP 메시지 바디의 텍스트 조회 메소드
- 쿼리
  - 메소드: POST
  - 요청 URL: http://localhost:8080/request-body-string
  - content-type: text/plain
  - message body: hello
- 바디 조회 메소드
```java
@WebServlet(name = "requestBodyStringServlet", urlPatterns = "/request-body- string")
public class RequestBodyStringServlet extends HttpServlet {
       @Override
      protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        System.out.println("messageBody = " + messageBody); response.getWriter().write("ok");
      }
}
```

### HTTP 요청 데이터 API 메시지 바디 - JSON

##### ##### HTTP 메시지 바디의 JSON 조회 메소드
- 쿼리
  - 메소드: POST
  - 요청 URL: http://localhost:8080/request-body-json
  - content-type: application/json
  - message body: {"username": "hello", "age": 20}
- 바디 조회 메소드
```java
@WebServlet(name = "requestBodyJsonServlet", urlPatterns = "/request-body- json")
public class RequestBodyJsonServlet extends HttpServlet {
    
    // JSON 결과를 파싱해서 자바 객체로 변환을 위해서 Jackson, Gson 같은 JSON 변환 라이브러리를 추가해서 사용
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        System.out.println("messageBody = " + messageBody);
        HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
        System.out.println("helloData.username = " + helloData.getUsername()); 
        System.out.println("helloData.age = " + helloData.getAge());
        response.getWriter().write("ok"); 
    }
}
```

### HttpServeltResponse 기본 사용법

- 서블릿 응답 처리 코드
```java
@WebServlet(name = "responseHeaderServlet", urlPatterns = "/response-header") 
public class ResponseHeaderServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        
    //[status-line]
    response.setStatus(HttpServletResponse.SC_OK); //200
    
    //[response-headers]
    response.setHeader("Content-Type", "text/plain;charset=utf-8");
    response.setHeader("Cache-Control", "no-cache, no-store, must- revalidate");
    response.setHeader("Pragma", "no-cache"); response.setHeader("my-header","hello");

    //[Header 편의 메서드] 
    content(response); 
    cookie(response); 
    redirect(response);
    //[message body]
    PrintWriter writer = response.getWriter();
    writer.println("ok");
    }
}
```
- 편의 메서드
  - Content 편의 메서드
  ```java
  private void content(HttpServletResponse response) {
    //Content-Type: text/plain;charset=utf-8
    //Content-Length: 2
    response.setHeader("Content-Type", "text/plain;charset=utf-8"); 
    response.setContentType("text/plain"); response.setCharacterEncoding("utf-8"); 
    response.setContentLength(2); //(생략시 자동 생성)
  }
  ```
  - 쿠키 편의 메서드
  ```java
  private void cookie(HttpServletResponse response) {
     //Set-Cookie: myCookie=good; Max-Age=600; 
     response.setHeader("Set-Cookie", "myCookie=good; Max-Age=600"); 
     Cookie cookie = new Cookie("myCookie", "good"); 
     cookie.setMaxAge(600); //600초
     response.addCookie(cookie); 
  }
  ```
  - redirect 편의 메서드
  ```java
  private void redirect(HttpServletResponse response) throws IOException { 
      //Status Code 302
      //Location: /basic/hello-form.html
      response.setStatus(HttpServletResponse.SC_FOUND); //302 
      response.setHeader("Location", "/basic/hello-form.html"); 
      response.sendRedirect("/basic/hello-form.html");
  }
  ```

### HTTP 응답 데이터 - 단순 텍스트, HTML

##### 단순 텍스트, HTML 응답 메소드
- 단순 텍스트, HTML 응답 메소드
```java
WebServlet(name = "responseHtmlServlet", urlPatterns = "/response-html") public class ResponseHtmlServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //Content-Type: text/html;charset=utf-8
        response.setContentType("text/html"); 
        response.setCharacterEncoding("utf-8");

        PrintWriter writer = response.getWriter(); 
        writer.println("<html>"); 
        writer.println("<body>");
        writer.println(" <div>안녕?</div>"); 
        writer.println("</body>"); 
        writer.println("</html>");
    } 
}
```

### HTTP 응답 데이터 API - JSON

##### JSON 응답 메소드
- JSON 응답 메소드
```java
WebServlet(name = "responseJsonServlet", urlPatterns = "/response-json") public class ResponseJsonServlet extends HttpServlet {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        //Content-Type: application/json
        response.setHeader("content-type", "application/json"); 
        response.setCharacterEncoding("utf-8");
        HelloData data = new HelloData(); data.setUsername("kim");
        data.setAge(20); 
        
        //{"username":"kim","age":20}
        String result = objectMapper.writeValueAsString(data); 
        response.getWriter().write(result);
    }
}
```

서블릿 JSP MVC 패턴
=======

### 회원 관리 웹 애플리케이션 로직
- 회원 도메인 모델
```java
@Getter @Setter
public class Member {
    private Long id;
    private String username;
    private int age;
    public Member() {}

    public Member(String username, int age) { 
        this.username = username;
        this.age = age;
    }
}
```
- 회원 저장소
```java
public class MemberRepository {

    private static Map<Long, Member> store = new HashMap<>(); //static 사용
    private static long sequence = 0L; //static 사용

    private static final MemberRepository instance = new MemberRepository();
    
    public static MemberRepository getInstance() {
          return instance;
    }

    // 싱글톤 패턴을 위해 생성자를 private로 설정
    private MemberRepository() {}
    
    public Member save(Member member) { 
        member.setId(++sequence); 
        store.put(member.getId(), member); 
        return member;
    }

    public Member findById(Long id) { 
        return store.get(id);
    }
 
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore() { 
        store.clear();
    } 
}
```

### 서블릿으로 회원 관리 웹 애플리케이션 만들기
- 회원 등록 폼
```java
@WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new- form")
public class MemberFormServlet extends HttpServlet {
    
    private MemberRepository memberRepository = MemberRepository.getInstance();
    
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            
            PrintWriter w = response.getWriter(); w.write("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + " <meta charset=\"UTF-8\">\n" + "    <title>Title</title>\n" +
            "</head>\n" + "<body>\n" + "<form action=\"/servlet/members/save\" method=\"post\">\n" +
            "    username: <input type=\"text\" name=\"username\" />\n" + "    age:      <input type=\"text\" name=\"age\" />\n" + " <button type=\"submit\">전송</button>\n" + "</form>\n" + "</body>\n" + "</html>\n");
    } 
}
```
- 회원 저장
```java
@WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                
        System.out.println("MemberSaveServlet.service");
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));
        
        Member member = new Member(username, age); System.out.println("member = " + member); memberRepository.save(member);
        
        response.setContentType("text/html"); response.setCharacterEncoding("utf-8");
        
        
        PrintWriter w = response.getWriter(); w.write("<html>\n" + "<head>\n" + " <meta charset=\"UTF-8\">\n" + "</head>\n" + "<body>\n" + "성공\n" + "<ul>\n" + " <li>id="+member.getId()+"</li>\n" + " <li>username="+member.getUsername()+"</li>\n" + " <li>age="+member.getAge()+"</li>\n" + "</ul>\n" + "<a href=\"/index.html\">메인</a>\n" + "</body>\n" + "</html>");
    }
}
```
- 회원 목록
```java
@WebServlet(name = "memberListServlet", urlPatterns = "/servlet/members")
public class MemberListServlet extends HttpServlet {
         
    private MemberRepository memberRepository = MemberRepository.getInstance();
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html"); 
        response.setCharacterEncoding("utf-8");

        List<Member> members = memberRepository.findAll();
        
        PrintWriter w = response.getWriter(); w.write("<html>");
        w.write("<head>");
        w.write(" <meta charset=\"UTF-8\">"); w.write(" <title>Title</title>"); w.write("</head>");
        w.write("<body>");
        w.write("<a href=\"/index.html\">메인</a>");
        w.write("<table>");
        w.write("<thead>");
        w.write("<th>id</th>");
        w.write("<th>username</th>");
        w.write("<th>age</th>");
        w.write("</thead>");
        w.write("<tbody>");

        for (Member member : members) {
        w.write("<tr>");
        w.write("<td>" + member.getId() + "</td>");
        w.write("<td>" + member.getUsername() + "</td>");
        w.write("<td>" + member.getAge() + "</td>");
        w.write("</tr>");
        }

        w.write(" </tbody>"); 
        w.write("</table>");
        w.write("</body>");
        w.write("</html>");
    }
}
```
- 서블릿만으로 회원 관리 웹 애프리케이션을 만들었을 때의 문제
  - 자바 코드로 HTML을 만들어 내는 것이 매우 불편하고 특히 HTML 문서에 동적인 변동 부분은 불가능 -> `템플릿 엔진 필요`

### JSP로 회원 관리 웹 애플리케이션 만들기
- 회원 등록 폼
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %> <html>
<head>
      <title>Title</title>
  </head>
<body>
<form action="/jsp/members/save.jsp" method="post"> username: <input type="text" name="username" /> age: <input type="text" name="age" /> <button type="submit">전송</button>
</form>
  </body>
  </html>
```
- 회원 저장
```jsp
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %> 

<%
// request, response 사용 가능
MemberRepository memberRepository = MemberRepository.getInstance();
System.out.println("save.jsp");
String username = request.getParameter("username");
int age = Integer.parseInt(request.getParameter("age"));
Member member = new Member(username, age); System.out.println("member = " + member); memberRepository.save(member);
%>

<html>
  <head>
<meta charset="UTF-8"> </head>
<body>
<ul>
    <li>id=<%=member.getId()%></li>
    <li>username=<%=member.getUsername()%></li>
    <li>age=<%=member.getAge()%></li>
</ul>
<a href="/index.html">메인</a>
</body>
</html>
```
- 회원 목록
```jsp
<%@ page import="java.util.List" %>
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %> 

<%
MemberRepository memberRepository = MemberRepository.getInstance();
List<Member> members = memberRepository.findAll(); %>
  <html>
  <head>
<meta charset="UTF-8">
      <title>Title</title>
  </head>
   <body>
<a href="/index.html">메인</a>
<table>
    <thead>
    <th>id</th>
    <th>username</th>
    <th>age</th>
    </thead>
<tbody> 
<%
for (Member member : members) {
        out.write("    <tr>");
        out.write("        <td>" + member.getId() + "</td>");
        out.write("        <td>" + member.getUsername() + "</td>");
        out.write("        <td>" + member.getAge() + "</td>");
        out.write("    </tr>");
    }
%>
    </tbody>
</table>
</body>
</html>
```
- 서블릿과 JSP으로 회원 관리 웹 애플리케이션을 만들었을 때의 문제
  - JAVA 코드, 데이터를 조회하는 리포지토리 등등 다양한 코드가 모두 JSP에 노출되어 있으며 JSP가 너무 많은 역할을 함 -> `MVC 패턴 필요`

### MVC 패턴 - 개요
- MVC 패턴
  - MVC 패턴은 지금까지 학습한 것 처럼 하나의 서블릿이나, JSP로 처리하던 것을 컨트롤러(Controller)와 뷰(View)라는 영역으로 서로 역할을 나눈 것
  - 서블릿이나 JSP만으로 비즈니스 로직과 뷰 렌더링까지 모두 처리하게 되면 하나의 코드에서 너무 많은 역할을 하게되고 결과적으로 유지보수가 어려워지기 때문에 생긴 패턴
  - JSP 같은 뷰 템플릿은 화면을 렌더링 하는데 최적화 되어 있기 때문에 이 부분의 업무만 담당하는 것이 가장 효과적
  - `컨트롤러`: HTTP 요청을 받아서 파라미터를 검증하고, 비즈니스 로직을 실행하는 곳으로 뷰에 전달할 결과 데이터를 조회해서 모델에 담음
  - `모델`: 뷰에 출력할 데이터를 담아둔다. 뷰가 필요한 데이터를 모두 모델에 담아서 전달해주는 덕분에 뷰는 비즈니스 로직이나 데이터 접근을 몰라도 되고, 화면을 렌더링 하는 일에 집중할 수 있음
  - `뷰`: 모델에 담겨있는 데이터를 사용해서 화면을 그리는 일에 집중하는 HTML을 생성하는 부분을 말함
- 컨트롤러와 비니니스 로직
  - 컨트롤러에 비즈니스 로직을 둘 수도 있지만, 이렇게 되면 컨트롤러가 너무 많은 역할을 담당하게 되어 일반적으로 `비즈니스 로직은 서비스(Service)`라는 계층을 별도로 만들어서 처리

### MVC 패턴 - 적용
- 회원 등록 폼 컨트롤러
```java
WebServlet(name = "mvcMemberFormServlet", urlPatterns = "/servlet-mvc/members/ new-form")
public class MvcMemberFormServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String viewPath = "/WEB-INF/views/new-form.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
        // 다른 서블릿이나 JSP로 이동할 수 있는 기능으로 redirect와 달라 클라이언트가 알 수 없는 기능
        dispatcher.forward(request, response); 
    } 
}
```

- 회원 등록 폼 뷰
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %> 
<html>
<head>
<meta charset="UTF-8">
        <title>Title</title>
</head>

<body>
<!-- 상대경로 사용, [현재 URL이 속한 계층 경로 + /save] --> 
    <form action="save" method="post">
        username: <input type="text" name="username" /> 
        age: <input type="text" name="age" /> <button type="submit">전송</button>
    </form>
</body>
</html>

```

- 회원 저장 컨트롤러
```java
@WebServlet(name = "mvcMemberSaveServlet", urlPatterns = "/servlet-mvc/members/ save")
public class MvcMemberSaveServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));
        Member member = new Member(username, age); System.out.println("member = " + member); memberRepository.save(member);

    // request는 내부에 데이터 저장소에 Model 데이터를 보관 
    request.setAttribute("member", member);
    String viewPath = "/WEB-INF/views/save-result.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
    dispatcher.forward(request, response);
    } 
}
```

- 회원 저장 뷰
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %> <html>
<head>
<meta charset="UTF-8"> </head>

<body> 성공
    <ul>
        <!-- JSP는 ${} 문법으로 request의 attribute에 담긴 데이터를 편리하게 조회 가능 --> 
        <li>id=${member.id}</li> <li>username=${member.username}</li> <li>age=${member.age}</li>
    </ul>
    <a href="/index.html">메인</a>
</body>
</html>
```

- 회원 목록 조회 컨트롤러
```java
@WebServlet(name = "mvcMemberListServlet", urlPatterns = "/servlet-mvc/ members")
public class MvcMemberListServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        System.out.println("MvcMemberListServlet.service");
        List<Member> members = memberRepository.findAll(); 

        // request는 내부에 데이터 저장소에 Model 데이터를 보관 
        request.setAttribute("members", members);
        String viewPath = "/WEB-INF/views/members.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
        dispatcher.forward(request, response);
    } 
}
```
- 회원 목록 조회 뷰
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %> 
<!-- <c:forEach>를 사용하기 위해 추가하는 import -->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%> 
<html>
<head>
<meta charset="UTF-8">
      <title>Title</title>
</head>
<body>
<a href="/index.html">메인</a>
  <table>
      <thead>
        <th>id</th>
        <th>username</th>
        <th>age</th>
      </thead>
      
      <tbody>

      <!-- JSP의 jstl을 통해서 편리하게 데이터 순회 조회 --> 
      <c:forEach var="item" items="${members}">
         <tr>
          <td>${item.id}</td>
          <td>${item.username}</td>
          <td>${item.age}</td>
         </tr>
      </c:forEach>
      </tbody>

  </table>
  </body>
  </html>
```

### MVC 패턴 - 한계
- 서블릿과 JSP를 활용한 MVC 패턴의 단점
  - MVC 패턴을 적용한 덕분에 컨트롤러의 역할과 뷰를 렌더링 하는 역할을 명확하게 구분이 되었지만 컨트롤러는 딱 봐도 `중복이 많고, 필요하지 않는 코드가 존재`
- 서블릿과 JSP를 활용한 MVC 패턴 한계
  - 포워드 중복
    - View로 이동하는 항상 중복 호출
    ```java
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
    dispatcher.forward(request, response);
    ``` 
  - ViewPath에 중복: String viewPath = "/WEB-INF/views/new-form.jsp";같은 가 중복 코드 존재
  - 사용하지 않는 코드: 파라미터로 받는 HttpServletRequest request, HttpServletResponse response는 사용하지 않을 때가 많음
  - 공통 처리가 어려움: 컨트롤러에서 공통으로 처리해야 하는 부분이 점점 더 많이 증가하는데, 서드를 항상 호출해야 하고, 실수로 호출하지 않으면 문제가 되고 호출하는 것 자체도 중복
- 스프링 MVC
  - `프론트 컨트롤러(Front Controller) 패턴` 구현을 통해서 공통 기능을 처리하는 소위 수문장 역할의 코드를 작성하여 적용

MVC 프레임워크 만들기
=======
### 프론트 컨트롤러 패턴 소개

###### 프론트 컨트롤러 패턴 특징
- 프론트 컨트롤러도 서블릿의 하나로 클라이언트의 요청을 받음
- 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출
- 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 됨

##### 스프링 웹 MVC와 프론트 컨트롤러
- 스프링 웹 MVC의 DispatcherServlet이 FrontController 패턴으로 구현

### 프론트 컨트롤러 도입 v1
##### ControllerV1
- 서블릿과 비슷한 모양의 컨트롤러 인터페이스를 도입
- 프론트 컨트롤러는 이 인터페이스를 호출해서 구현과 관계없이 로직의 일관성을 가져감
```java
public interface ControllerV1 {
      void process(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException;
  }
```

##### MemberFormControllerV1 - 회원 등록 컨트롤러
```java
public class MemberFormControllerV1 implements ControllerV1 {

  @Override
  public void process(HttpServletRequest request, HttpServletResponse  response) throws ServletException, IOException {
    
    String viewPath = "/WEB-INF/views/new-form.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
    dispatcher.forward(request, response);
  }

}
```

##### MemberSaveControllerV1 - 회원 저장 컨트롤러
```java
public class MemberSaveControllerV1 implements ControllerV1 {
  
  private MemberRepository memberRepository = MemberRepository.getInstance();

  Override
  public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));
    Member member = new Member(username, age); memberRepository.save(member);
    
    request.setAttribute("member", member);
    String viewPath = "/WEB-INF/views/save-result.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
    dispatcher.forward(request, response);
  }
}
```

##### MemberListControllerV1 - 회원 목록 컨트롤러
```java
public class MemberListControllerV1 implements ControllerV1 {
  
  private MemberRepository memberRepository = MemberRepository.getInstance();
  
  @Override
  public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    List<Member> members = memberRepository.findAll(); 
    request.setAttribute("members", members);
    String viewPath = "/WEB-INF/views/members.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
    dispatcher.forward(request, response);
  } 
}
```
##### FrontControllerServletV1 - 프론트 컨트롤러
```java
@WebServlet(name = "frontControllerServletV1", urlPatterns = "/front- controller/v1/*")
public class FrontControllerServletV1 extends HttpServlet {

    private Map<String, ControllerV1> controllerMap = new HashMap<>();
    
    public FrontControllerServletV1() { 
      // key는 매핑 URL, value는 호출될 컨트롤러
      controllerMap.put("/front-controller/v1/members/new-form", new MemberFormControllerV1()); controllerMap.put("/front-controller/v1/members/save", new MemberSaveControllerV1()); controllerMap.put("/front-controller/v1/members", new MemberListControllerV1());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
      System.out.println("FrontControllerServletV1.service");

      // 리퀘스트 url를 얻어옴
      String requestURI = request.getRequestURI();

      // 리퀘스트 url을 통해서 그에 맞는 컨트롤러를 찾음
      ControllerV1 controller = controllerMap.get(requestURI); 

      // 컨트롤러가 없는 경우 404
      if (controller == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return; 
      }

      // 다형성을 통해서 프로세스 처리
      controller.process(request, response); 
    }
}
```

### View 분리 v2

##### View 분리
- 아래와 같이 모든 컨트롤러에서 뷰로 이동하는 부분에 중복이 있어서 이를 제거하여 깔끔하게 만들 수 있음
```java
String viewPath = "/WEB-INF/views/new-form.jsp";
RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
dispatcher.forward(request, response);
```

##### MyView v2
- viewPath를 처리하는 데에 특화된 객체 
```java
public class MyView {

  private String viewPath;

  public MyView(String viewPath) {

    // ViewPath 처리
    this.viewPath = viewPath;
  }
  
  public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    // 포워딩 처리
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response); 
  }
}
```

##### ControllerV2
```java
 public interface ControllerV2 {
      MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
  }
```

##### MemberFormControllerV2 - 회원 등록 폼
```java
public class MemberFormControllerV2 implements ControllerV2 {
  
  @Override
  public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 뷰 객체로 뷰 처리
    return new MyView("/WEB-INF/views/new-form.jsp"); 
  }
}
```

##### MemberSaveControllerV2 - 회원 저장
```java
public class MemberSaveControllerV2 implements ControllerV2 {
  
  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));
    Member member = new Member(username, age); memberRepository.save(member);
    request.setAttribute("member", member);

    // 뷰 객체로 뷰 처리
    return new MyView("/WEB-INF/views/save-result.jsp");
  } 
}
```

##### MemberListControllerV2 - 회원 목록
```java
public class MemberListControllerV2 implements ControllerV2 {
  
  
  private MemberRepository memberRepository = MemberRepository.getInstance();
  
  @Override
  public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    List<Member> members = memberRepository.findAll(); 
    request.setAttribute("members", members);
    // 뷰 객체로 뷰 처리
    return new MyView("/WEB-INF/views/members.jsp"); 
  }
}
```

##### FrontControllerServletV2 - 프론트 컨트롤러
```java
 @WebServlet(name = "frontControllerServletV2", urlPatterns = "/front- controller/v2/*")
public class FrontControllerServletV2 extends HttpServlet {
  
  private Map<String, ControllerV2> controllerMap = new HashMap<>();
  
  public FrontControllerServletV2() { 
    controllerMap.put("/front-controller/v2/members/new-form", new MemberFormControllerV2()); controllerMap.put("/front-controller/v2/members/save", new MemberSaveControllerV2()); controllerMap.put("/front-controller/v2/members", new MemberListControllerV2());
  }
  
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 

    String requestURI = request.getRequestURI();
    ControllerV2 controller = controllerMap.get(requestURI); 
    
    if (controller == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return; 
    }

    // 뷰 객체로 뷰 처리
    MyView view = controller.process(request, response);
    view.render(request, response); 
  }
}
```

### Model 추가 v3

##### 서블릿 종속성 제거 및 뷰 이름 중복 제거
- 컨트롤러 입장에서 HttpServletRequest, HttpServletResponse이 필요 없어서 요청 파라미터 정보는 따로 처리 가능
- request 객체를 Model로 사용하는 대신에 `별도의 Model 객체를 만들어서 반환`
- 컨트롤러에서 지정하는 뷰 이름에 중복하므로 `뷰의 논리 이름을 반환`하고, 실제 물리 위치의 이름은 프론트 컨트롤러에서 처리하도록 단순화

##### ModelView v3
- 서블릿에 종속적인 HttpServletRequest 대신에 Model을 직접 만들고, 추가로 View 이름까지 전달하는 객체를 만들어서 간소화
```java
public class ModelView {

    private String viewName
    // 뷰의 이름과 뷰를 렌더링할 때 필요한 model 객체
    private Map<String, Object> model = new HashMap<>();

    public ModelView(String viewName) { 
      this.viewName = viewName;
    }
    
    public String getViewName() {
      return viewName;
    }

    public void setViewName(String viewName) { 
      this.viewName = viewName;
    }
    
    public Map<String, Object> getModel() {
      return model;
    }

    public void setModel(Map<String, Object> model) {
       this.model = model;
    }
}
```

##### ControllerV3
- 서블릿 기술을 사용하지 않는 컨트롤러 인터페이스 정의
```java
public interface ControllerV3 {
  // HttpServletRequest가 제공하는 파라미터는 프론트 컨트롤러가 paramMap에 담아서 호출
  // 응답 결과로 뷰 이름과 뷰에 전달할 Model 데이터를 포함하는 ModelView 객체를 반환
   ModelView process(Map<String, String> paramMap);  
}
```

##### MemberFormControllerV3 - 회원 등록 폼
- ModelView 를 생성할 때 new-form 이라는 view의 논리적인 이름을 지정
```java
public class MemberFormControllerV3 implements ControllerV3 {
  @Override
  public ModelView process(Map<String, String> paramMap) {
    return new ModelView("new-form"); 
  }
}
```

##### MemberSaveControllerV3 - 회원 저장
- 
```java
public class MemberSaveControllerV3 implements ControllerV3 {

  private MemberRepository memberRepository = MemberRepository.getInstance();
      
  @Override
  public ModelView process(Map<String, String> paramMap) {
    
    String username = paramMap.get("username");
    int age = Integer.parseInt(paramMap.get("age"));
    Member member = new Member(username, age); memberRepository.save(member);
    ModelView mv = new ModelView("save-result"); 
    mv.getModel().put("member", member);
    return mv;
  } 
}

```

##### MemberListControllerV3 - 회원 목록
- 
```java
public class MemberListControllerV3 implements ControllerV3 {

  private MemberRepository memberRepository = MemberRepository.getInstance();
    
  @Override
  public ModelView process(Map<String, String> paramMap) {
    List<Member> members = memberRepository.findAll(); 
    ModelView mv = new ModelView("members");
    mv.getModel().put("members", members); return mv;
  } 
}
```
##### FrontControllerServletV3
- 다른 컨트롤러에서 서블릿 기능을 제외하고 FrontControllerServletV3에서 처리
```java
@WebServlet(name = "frontControllerServletV3", urlPatterns = "/front- controller/v3/*")
public class FrontControllerServletV3 extends HttpServlet {
  
      private Map<String, ControllerV3> controllerMap = new HashMap<>();
      
      public FrontControllerServletV3() { 
        controllerMap.put("/front-controller/v3/members/new-form", new MemberFormControllerV3()); controllerMap.put("/front-controller/v3/members/save", new MemberSaveControllerV3()); controllerMap.put("/front-controller/v3/members", new MemberListControllerV3());
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 

      String requestURI = request.getRequestURI();
      ControllerV3 controller = controllerMap.get(requestURI); 
      
      if (controller == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return; 
      }
      
      // 모든 전달 받은 파라미터를 처리하고 저장하는 paramMap
      Map<String, String> paramMap = createParamMap(request);

      // paramMap에서 처리를 통해 ModelView를 생성
      ModelView mv = controller.process(paramMap);

      // ModelView를에서 뷰 이름을 얻어옴
      String viewName = mv.getViewName();
      
      // 뷰 이름을 통해서 리졸빙
      MyView view = viewResolver(viewName); 

      // 랜더링
      view.render(mv.getModel(), request, response);
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
      Map<String, String> paramMap = new HashMap<>();
      request.getParameterNames().asIterator() 
             .forEachRemaining(
               paramName -> paramMap.put(paramName, request.getParameter(paramName))
              ); 
      return paramMap;
    }
    
    private MyView viewResolver(String viewName) {
      // 컨트롤러가 반환한 논리 뷰 이름을 실제 물리 뷰 경로로 변경하고  MyView 객체를 반환
      return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    } 
}
```

##### MyView v3
```java
public class MyView {

    private String viewPath;

    public MyView(String viewPath) { 
      this.viewPath = viewPath;
    }
    
    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
      dispatcher.forward(request, response); 
    }
    
    public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      modelToRequestAttribute(model, request);
      RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath); 
      dispatcher.forward(request, response);
    }

    private void modelToRequestAttribute(Map<String, Object> model, HttpServletRequest request) {
      model.forEach((key, value) -> request.setAttribute(key, value)); 
    }
}
```
### 단순하고 실용적인 컨트롤러 v4
- 컨트톨러 인터페이스를 구현하는 개발자 입장에서 보면, 항상 ModelView 객체를 생성하고 반환해야 하는 부분이 있는데 이를 개선할 수 있음
- 본적인 구조는 V3와 같으나 대신에 컨트롤러가 ModelView 를 반환하지 않고, ViewName 만 반환할 수 있음

##### ControllerV4
```java
public interface ControllerV4 {

      /**
       * @param paramMap
       * @param model
       * @return viewName
       */
       // 인터페이스에 ModelView가 없음
       // model 객체는 파라미터로 전달되기 때문에 그냥 사용하면 되고, 결과로 뷰의 이름만 반환
      String process(Map<String, String> paramMap, Map<String, Object> model);
  }
```



##### MemberFormControllerV4
```java
public class MemberFormControllerV4 implements ControllerV4 {
    
    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
      return "new-form"; 
    }
}
```

##### MemberSaveControllerV4
```java
public class MemberSaveControllerV4 implements ControllerV4 {

  private MemberRepository memberRepository = MemberRepository.getInstance();
  
  @Override
  public String process(Map<String, String> paramMap, Map<String, Object> model) {

    String username = paramMap.get("username");
    int age = Integer.parseInt(paramMap.get("age"));
    
    Member member = new Member(username, age); 
    memberRepository.save(member);
    
    // Front 컨트롤러에서 얻은 모델에 값 추가 
    model.put("member", member); 
    return "save-result";
  } 
}
```

##### MemberListControllerV4
```java
public class MemberListControllerV4 implements ControllerV4 {
  
  private MemberRepository memberRepository = MemberRepository.getInstance();
  
  @Override
  public String process(Map<String, String> paramMap, Map<String, Object> model) {
    
    List<Member> members = memberRepository.findAll(); 

    // Front 컨트롤러에서 얻은 모델에 값 추가
    model.put("members", members);
    return "members";
  }
}
```

##### FrontControllerServletV4
```java
@WebServlet(name = "frontControllerServletV4", urlPatterns = "/front- controller/v4/*")
public class FrontControllerServletV4 extends HttpServlet {

  private Map<String, ControllerV4> controllerMap = new HashMap<>();
  
  public FrontControllerServletV4() { 
    controllerMap.put("/front-controller/v4/members/new-form", new MemberFormControllerV4()); controllerMap.put("/front-controller/v4/members/save", new MemberSaveControllerV4()); controllerMap.put("/front-controller/v4/members", new MemberListControllerV4());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
    String requestURI = request.getRequestURI();
    ControllerV4 controller = controllerMap.get(requestURI); 
    
    if (controller == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return; 
    }
    
    Map<String, String> paramMap = createParamMap(request); 
    
    // 모델 객체를 프론트 컨트롤러에서 생성
    Map<String, Object> model = new HashMap<>(); 
    
    // 모델 객체를 프론트 컨트롤러에서 컨트롤러들로 보내고 뷰의 논리 이름을 직접 반환
    String viewName = controller.process(paramMap, model);
    MyView view = viewResolver(viewName);
    
    view.render(model, request, response); 
  }

    private Map<String, String> createParamMap(HttpServletRequest request) { 
      Map<String, String> paramMap = new HashMap<>(); 
      
      request.getParameterNames().asIterator()
            .forEachRemaining(paramName -> paramMap.put(paramName, request.getParamete(paramName));
      return paramMap;
    }

    private MyView viewResolver(String viewName) {
      return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }
}
```
### 유연한 컨트롤러 v5

##### 어댑터 패턴
- ControllerV3 , ControllerV4 는 완전히 다른 인터페이스인데, 어댑터 패턴을 사용해서 프론트 컨트롤러가 다양한 방식의 컨트롤러를 처리할 수 있도록 변경시킬 수 있음

##### v5에 추가된 개념
- 핸들러 어댑터 목록: 핸들러 어탭더를 조회하는 곳
- `핸들러 어댑터`: 프론트 컨트롤러와 컨트롤러 중간에 어댑터 역할을 하는 핸들러 어댑터가 추가되었고 여기서 어댑터 역할을 해주는 덕분에 `다양한 종류의 컨트롤러를 호출함`
- `핸들러`: `컨트롤러의 이름을 더 넓은 범위`인 핸들러로 변경, 그 이유는 이제 어댑터가 있기 때문에 꼭 컨트롤러의 개념 뿐만 아니라 어떠한 것이든 해당하는 종류의 어댑터만 있으면 다 처리할 수 있음


##### MyHandlerAdapter
```java
public interface MyHandlerAdapter {

  // handler는 컨트롤러
  boolean supports(Object handler);

  // 어댑터는 실제 컨트롤러를 호출하고, 그 결과로 ModelView를 반환
  ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException;
}
```

##### ControllerV3HandlerAdapter
```java
public class ControllerV3HandlerAdapter implements MyHandlerAdapter {
      
    @Override
    public boolean supports(Object handler) {
      // ControllerV3 을 처리할 수 있는 어댑터인지 체크
      return (handler instanceof ControllerV3);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {

      // handler를 컨트롤러 V3로 변환한 다음에 V3 형식에 맞도록 호출
      ControllerV3 controller = (ControllerV3) handler;

      Map<String, String> paramMap = createParamMap(request);
      ModelView mv = controller.process(paramMap);
      return mv; 
    }

    private Map<String, String> createParamMap(HttpServletRequest request) { 
      Map<String, String> paramMap = new HashMap<>(); 
      request.getParameterNames().asIterator()
            .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
      return paramMap;
    }
}
```

##### ControllerV4HandlerAdapter
```java
public class ControllerV4HandlerAdapter implements MyHandlerAdapter {
    
    @Override
    public boolean supports(Object handler) {
      // ControllerV4 을 처리할 수 있는 어댑터인지 체크
      return (handler instanceof ControllerV4);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {

      // handler를 컨트롤러 V4로 변환한 다음에 V4 형식에 맞도록 호출
      ControllerV4 controller = (ControllerV4) handler;
      Map<String, String> paramMap = createParamMap(request);
      Map<String, Object> model = new HashMap<>();
      
      String viewName = controller.process(paramMap, model);

      // V3와 맞춰주기 위해서 ModelView를 만들어서 반환(어뎁팅)
      ModelView mv = new ModelView(viewName); 
      mv.setModel(model);
      return mv; 
    }

    private Map<String, String> createParamMap(HttpServletRequest request) { 
      Map<String, String> paramMap = new HashMap<>(); 
      request.getParameterNames().asIterator()
            .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

##### FrontControllerServletV5
```java
@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front- controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {

    // 값이 ControllerV3 , ControllerV4 같은 인터페이스에서 아무 값이나 받을 수 있는 Object
    private final Map<String, Object> handlerMappingMap = new HashMap<>();
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();

    public FrontControllerServletV5() {
      initHandlerMappingMap(); // 핸들러 매핑 초기화
      initHandlerAdapters(); // 어댑터 초기화
    }

    private void initHandlerMappingMap() { 
      
      // ControllerV3들과 url 매핑
      handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
      handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
      handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());

      // ControllerV4들과 url 매핑
      handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
      handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
      handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());
    }

    private void initHandlerAdapters() { 
      handlerAdapters.add(new ControllerV3HandlerAdapter()); // V3 추가
      handlerAdapters.add(new ControllerV4HandlerAdapter()); // V4 추가
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

      // 핸들러 매핑
      Object handler = getHandler(request);

      if (handler == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return; 
      }

      // 핸들러 어뎁터 조회
      MyHandlerAdapter adapter = getHandlerAdapter(handler); 
      // 어뎁터(컨트롤러) 실행
      ModelView mv = adapter.handle(request, response, handler);

      MyView view = viewResolver(mv.getViewName());
      view.render(mv.getModel(), request, response); 
    }

    private Object getHandler(HttpServletRequest request) { 
      String requestURI = request.getRequestURI(); 
      return handlerMappingMap.get(requestURI);
    }

    private MyHandlerAdapter getHandlerAdapter(Object handler) {

      // handlerAdapters에서 핸들링 가능한 어뎁터를 찾음
      for (MyHandlerAdapter adapter : handlerAdapters) {
        if (adapter.supports(handler)) { 
          return adapter;
        }
      }
      throw new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler=" + handler);
    }

    private MyView viewResolver(String viewName) {
      return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    } 
}
```
스프링 MVC 구조 이해
=======

### 스프링 MVC 전체 구조

##### 직접 만든 프레임워크와 스프링 MVC 비교
<img width="892" alt="스크린샷 2021-05-01 오후 2 38 03" src="https://user-images.githubusercontent.com/38535571/116772560-e5a5f700-aa8a-11eb-8500-4aa0b2b83e94.png">

- FrontController <-> DispatcherServlet 
- handlerMappingMap <-> HandlerMapping 
- MyHandlerAdapter <-> HandlerAdapter 
- ModelView <-> ModelAndView
- viewResolver <-> ViewResolver
- MyView <-> View

##### DispatcherServlet 구조 살펴보기
- DispacherServlet
  - 스프링 MVC의 프론트 컨트롤러가 디스패처 서블릿
  - DispatcherServlet -> FrameworkServlet -> HttpServletBean -> HttpServlet 의 상속 관계를 갖음
- DispacherServlet 서블릿 등록
  - 스프링 부트는 DispacherServlet을 서블릿으로 자동으로 등록하면서 모든 경로( urlPatterns="/" )에 대해서 매핑
- 요청 흐름
  - 서블릿이 호출되면 HttpServlet이 제공하는 serivce()가 호출 (FrameworkServlet.service())
  - `DispacherServlet.doDispatch() 가 호출`
- doDispatch 내부 흐름
```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse
  response) throws Exception {

    HttpServletRequest processedRequest = request;
    HandlerExecutionChain mappedHandler = null;
    ModelAndView mv = null;
    
    // 1. 핸들러 조회
    mappedHandler = getHandler(processedRequest); 
    if (mappedHandler == null) {
        noHandlerFound(processedRequest, response);
        return;
    }

    //2.핸들러 어댑터 조회-핸들러를 처리할 수 있는 어댑터
    HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

    // 3. 핸들러 어댑터 실행 -> 4. 핸들러 어댑터를 통해 핸들러 실행 -> 5. ModelAndView 반환 
    mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

    // 6. 뷰 리졸버를 통해서 뷰 찾기 -> 7.View 반환 -> 8. 뷰 렌더링
    processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
}

private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, HandlerExecutionChain mappedHandler, ModelAndView mv, Exception exception) throws Exception {

  // 렌더링 호출
  render(mv, request, response);
}

protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
  View view;
  String viewName = mv.getViewName(); 
  
  // 뷰 리졸버를 통해서 뷰 찾기 및 View 반환
  view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
  // 뷰 렌더링
  view.render(mv.getModelInternal(), request, response); 
}
```
- 동작 순서
```
1. 핸들러 조회: 핸들러 매핑을 통해 요청 URL에 매핑된 핸들러(컨트롤러)를 조회한다.
2. 핸들러 어댑터 조회: 핸들러를 실행할 수 있는 핸들러 어댑터를 조회한다.
3. 핸들러 어댑터 실행: 핸들러 어댑터를 실행한다.
4. 핸들러 실행: 핸들러 어댑터가 실제 핸들러를 실행한다.
5. ModelAndView 반환: 핸들러 어댑터는 핸들러가 반환하는 정보를 ModelAndView로 변환해서
반환한다.
6. viewResolver 호출: 뷰 리졸버를 찾고 실행한다.
JSP의 경우: InternalResourceViewResolver 가 자동 등록되고, 사용된다.
7. View반환:뷰리졸버는뷰의논리이름을물리이름으로바꾸고,렌더링역할을담당하는뷰객체를
반환한다.
JSP의 경우 InternalResourceView(JstlView) 를 반환하는데, 내부에 forward() 로직이 있다.
8. 뷰렌더링:뷰를통해서뷰를렌더링한다.
```

##### DispatcherServlet 관련 주요 인터페이스 살펴보기
- DispatcherServlet 코드의 변경 없이, 원하는 기능을 변경하거나 확장할 수 있음
- 주요 인터페이스 목록
  - 핸들러 매핑: org.springframework.web.servlet.HandlerMapping
  - 핸들러 어댑터: org.springframework.web.servlet.HandlerAdapter
  - 뷰 리졸버: org.springframework.web.servlet.ViewResolver
  - 뷰: org.springframework.web.servlet.View

### 핸들러 매핑과 핸들러 어댑터

##### 현재 범용적으로 사용되는 스프링 컨트롤러 인터페이스 HttpRequestHandler와 사용자 정의 MyHttpRequestHandler
- org.springframework.web.HttpRequestHandler
```java
 public interface HttpRequestHandler {
   void handleRequest(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException;
}
```
- MyHttpRequestHandler
```java
//  /springmvc/request-handler 라는 이름의 스프링 빈으로 등록
@Component("/springmvc/request-handler")
public class MyHttpRequestHandler implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      System.out.println("MyHttpRequestHandler.handleRequest"); 
    }
}
```


##### 사용자 정의 컨트롤러를 호출하는 방법
- HandlerMapping(핸들러 매핑)
  - 핸들러 매핑에서 이 컨트롤러를 찾을 수 있어야 함
  - 예) 스프링 빈의 이름으로 핸들러를 찾을 수 있는 핸들러 매핑이 필요
- HandlerAdapter(핸들러 어댑터)
  - 핸들러 매핑을 통해서 찾은 핸들러를 실행할 수 있는 핸들러 어댑터가 필요
  - 예) HttpRequestHandler 인터페이스를 실행할 수 있는 핸들러 어댑터를 찾고 실행

##### 스프링 부트가 자주 등록하는 핸들러 매핑과 핸들러 어댑터
- HandlerMapping
  - `0 = RequestMappingHandlerMapping : 애노테이션 기반의 컨트롤러인 @RequestMapping에서 사용`
  - 1 = BeanNameUrlHandlerMapping : 스프링 빈의 이름으로 핸들러를 찾음
- HandlerAdapter
  - `0 = RequestMappingHandlerAdapter : 애노테이션 기반의 컨트롤러인 @RequestMapping에서 사용`
  - 1 = HttpRequestHandlerAdapter : HttpRequestHandler 처리
  - 2 = SimpleControllerHandlerAdapter : Controller 인터페이스(애노테이션X, 과거에 사용) 처리

##### 호출 순서 정리
1. 핸들러 매핑으로 핸들러 조회
  - HandlerMapping 을 순서대로 실행해서, 핸들러를 찾음
  - 빈 이름으로 핸들러를 찾아야하기 때문에 이름 그대로 빈 이름으로 핸들러를찾아주는 BeanNameUrlHandlerMapping가 실행에 성공하고 `핸들러인 MyHttpRequestHandler 반환`
2. 핸들러 어댑터 조회
  - HandlerAdapter 의 supports() 를 순서대로 호출
  - `HttpRequestHandlerAdapter가 HttpRequestHandler 인터페이스를 지원`하므로 대상이 됨
3. 핸들러 어댑터 실행
  - 디스패처 서블릿이 조회한 HttpRequestHandlerAdapter 를 실행하면서 핸들러 정보도 함께 넘겨줌
  - `HttpRequestHandlerAdapter는 핸들러인 MyHttpRequestHandler를 내부에서 실행하고, 그 결과를 반환`

### 뷰 리졸버

##### 현재 범용적으로 사용되는 스프링 뷰 리졸버 인터페이스 InternalResourceViewResolver
- 스프링 부트는 InternalResourceViewResolver 라는 뷰 리졸버를 자동으로 등록하는데, 이때 application.properties 에 등록한 spring.mvc.view.prefix , spring.mvc.view.suffix 설정 정보를 사용해서 등록

##### 스프링 부트가 자동 등록하는 뷰 리졸버
- ViewResolver
  - 1 = BeanNameViewResolver : 빈 이름으로 뷰를 찾아서 반환 (예: 엑셀 파일 생성 기능에 사용)
  - `2 = InternalResourceViewResolver : JSP를 처리할 수 있는 뷰를 반환`
  - 참고) ThymeleafViewResolver: Thymeleaf 뷰 템플릿을 사용


##### 호출 순서 정리
1. 핸들러 어댑터 호출
2. ViewResolver 호출
  - 사용자가 넘긴 뷰 이름으로 viewResolver를 순서대로 호출
  - BeanNameViewResolver는 사용자가 넘긴 뷰 이름의 스프링 빈으로 등록된 뷰를 찾아야 하는데 없음
  - InternalResourceViewResolver가 호출
3. InternalResourceViewResolver가 InternalResourceView 를 반환
  -  InternalResourceView는 JSP처럼 forward()를 호출해서 처리할 수 있는 경우에 사용
4. view.render()가 호출되고 InternalResourceView 는 forward()를 사용해서 JSP를 실행


### 스프링 MVC 시작하기

#####

### 스프링 MVC 컨트롤러 통합

#####

### 스프링 MVC 실용적인 방식

스프링 MVC 기본 기능
=======

스프링 MVC 웹 페이지 만들기
=======
