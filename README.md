# Spring Framework 정리 자료
실전! 스프링 MVC 1편 정리 문서

Table of contents
=================
<!--ts-->
   * [웹 애플리케이션 이해](#웹-애플리케이션-이해)
   * [서블릿](#서블릿)
   * [서블릿, JSP, MVC 패턴](#서블릿,-JSP,-MVC-패턴)
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
    //response.setHeader("Content-Type", "text/plain;charset=utf-8"); 
    response.setContentType("text/plain"); response.setCharacterEncoding("utf-8"); 
    //response.setContentLength(2); //(생략시 자동 생성)
  }
  ```
  - 쿠키 편의 메서드
  ```java
  private void cookie(HttpServletResponse response) {
     //Set-Cookie: myCookie=good; Max-Age=600; 
     //response.setHeader("Set-Cookie", "myCookie=good; Max-Age=600"); 
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
      //response.setStatus(HttpServletResponse.SC_FOUND); //302 
      //response.setHeader("Location", "/basic/hello-form.html"); 
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

서블릿, JSP, MVC 패턴
=======

##### 회원 관리 웹 애플리케이션 로직
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

##### 서블릿으로 회원 관리 웹 애플리케이션 만들기
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

##### JSP로 회원 관리 웹 애플리케이션 만들기
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

##### MVC 패턴 - 개요


##### MVC 패턴 - 적용


##### MVC 패턴 - 한계


MVC 프레임워크 만들기
=======


스프링 MVC 구조 이해
=======

스프링 MVC 기본 기능
=======

스프링 MVC 웹 페이지 만들기
=======
