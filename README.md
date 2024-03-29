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

### Servlet

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

##### Servlet
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

##### HTTP 메시지 바디의 JSON 조회 메소드
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
    
    Member member = new Member(username, age); 
    memberRepository.save(member);
    
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
    mv.getModel().put("members", members); 
    return mv;
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
    
    // 1.핸들러 조회
    mappedHandler = getHandler(processedRequest); 
    if (mappedHandler == null) {
        noHandlerFound(processedRequest, response);
        return;
    }

    //2.핸들러 어댑터 조회-핸들러를 처리할 수 있는 어댑터
    HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

    // 3.핸들러 어댑터 실행 -> 4.핸들러 어댑터를 통해 핸들러 실행 -> 5.ModelAndView 반환 
    mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

    // 6.뷰 리졸버를 통해서 뷰 찾기 -> 7.View 반환 -> 8.뷰 렌더링
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

##### @RequestMapping
- 스프링은 애노테이션을 활용한 매우 유연하고, 실용적인 컨트롤러를 만들었는데 이것이 바로 `@RequestMapping 애노테이션`을 사용하는 컨트롤러
- 메서드 단위로 어노테이션을 적용하여 매우 편리
- 가장 우선순위가 높은 핸들러 매핑과 핸들러 어댑터는 @RequestMapping 기반의 RequestMappingHandlerMapping, RequestMappingHandlerAdapter
- SpringMemberFormControllerV1
```java
// 스프링이 자동으로 스프링 빈으로 등록하게 하는 @Controller 
// RequestMappingHandlerMapping 은 스프링 빈 중에서 @RequestMapping 또는 @Controller 가 클래스 레벨에 붙어 있는 경우에 매핑 정보로 인식
@Controller
public class SpringMemberFormControllerV1 {

  // 요청 정보를 매핑, URL이 호출되면 이 메서드가 호출됨 (메서드의 이름은 임의로 지으면 됨)
  @RequestMapping("/springmvc/v1/members/new-form") 
  public ModelAndView process() {

    // 모델과 뷰 정보를 담아서 반환
    return new ModelAndView("new-form"); 
  }
}
```
- SpringMemberSaveControllerV1 - 회원 저장
```java
@Controller
public class SpringMemberSaveControllerV1 {
  
  private MemberRepository memberRepository = MemberRepository.getInstance();
  
  @RequestMapping("/springmvc/v1/members/save")
  public ModelAndView process(HttpServletRequest request, HttpServletResponse response) {
    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));
 
    Member member = new Member(username, age);

    System.out.println("member = " + member); 
    memberRepository.save(member);

    // 스프링이 제공하는 ModelAndView 를 통해 Model 데이터를 추가할 때는 addObject() 를 사용, 데이터는 이후 뷰를 렌더링 할 때 사용
    ModelAndView mv = new ModelAndView("save-result"); 
    mv.addObject("member", member);
    return mv;
  } 
}
```
- SpringMemberListControllerV1 - 회원 목록
```java
@Controller
public class SpringMemberListControllerV1 {

  private MemberRepository memberRepository = MemberRepository.getInstance();
      
  @RequestMapping("/springmvc/v1/members")
  public ModelAndView process() {

    List<Member> members = memberRepository.findAll();
    ModelAndView mv = new ModelAndView("members"); 
    mv.addObject("members", members);
    return mv; 
  }
}
```

### 스프링 MVC 컨트롤러 통합

##### SpringMemberControllerV2
- 클래스 레벨과 메서드 레벨을 분리하여 url path를 통합하여 컨트롤러를 통합할 수 있음
- 클래스 레벨 @RequestMapping("/springmvc/v2/members")
  - 메서드 레벨 @RequestMapping("/new-form") /springmvc/v2/members/new-form 
  - 메서드 레벨 @RequestMapping("/save") /springmvc/v2/members/save
  - 메서드 레벨 @RequestMapping /springmvc/v2/members
```java
/**
* 클래스 단위->메서드 단위
* @RequestMapping 클래스 레벨과 메서드 레벨 조합 
*/
@Controller
// 클래스 레벨에 다음과 같이 @RequestMapping 을 두면 메서드 레벨과 조합
@RequestMapping("/springmvc/v2/members")
public class SpringMemberControllerV2 {

   private MemberRepository memberRepository = MemberRepository.getInstance();
   
   @RequestMapping("/new-form") 
   public ModelAndView newForm() {
     return new ModelAndView("new-form"); 
   }
      
    @RequestMapping("/save")
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
      String username = request.getParameter("username");
      int age = Integer.parseInt(request.getParameter("age"));
      Member member = new Member(username, age); memberRepository.save(member);
      ModelAndView mav = new ModelAndView("save-result"); mav.addObject("member", member);
      return mav;
    }
     
    @RequestMapping
    public ModelAndView members() {
      List<Member> members = memberRepository.findAll();
      ModelAndView mav = new ModelAndView("members"); 
      mav.addObject("members", members);
      return mav;
    } 
}
```
### 스프링 MVC 실용적인 방식

##### SpringMemberControllerV3
- ModelView를 개발자가 직접 생성해서 반환했기 때문에 불편하므로 Model을 파라미터로 받고 String을 반환하는 방식으로 처리가 가능
- DispatcherServlet이 제공하는 공통 기능이 있어서 서블릿을 코드에서 컨트롤러 코드에서 지울 수 있음
- HTTP 메서드를 구체적으로 구분하는 것을 어노테이션으로 처리할 수 있음
```java
/**
* v3
* Model 도입
* ViewName 직접 반환
* @RequestParam 사용
* @RequestMapping -> @GetMapping, @PostMapping */
@Controller
@RequestMapping("/springmvc/v3/members")
public class SpringMemberControllerV3 {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @GetMapping("/new-form") 
  public String newForm() {
    return "new-form"; 
  }
  
  @PostMapping("/save")
  public String save(@RequestParam("username") String username, @RequestParam("age") int age, Model model) {
    
    Member member = new Member(username, age); 
    memberRepository.save(member);
    model.addAttribute("member", member); 
    return "save-result";
  }
      
  @GetMapping
  public String members(Model model) {
    List<Member> members = memberRepository.findAll(); 
    model.addAttribute("members", members);
    return "members";
  } 
}
```

스프링 MVC 기본 기능
=======

### 스프링 MVC 기본 기능 - 로깅

##### 로깅 간단히 알아보기
- 운영 시스템에서는 System.out.println() 같은 시스템 콘솔을 사용해서 필요한 정보를 출력하지 않고, 별도의 로깅 라이브러리를 사용해서 로그를 출력
- 쓰레드 정보, 클래스 이름 같은 부가 정보를 함께 볼 수 있고, 출력 모양을 조정이 가능
- 로그 레벨에 따라 개발 서버에서는 모든 로그를 출력하고, 운영서버에서는 출력하지 않는 등 로그를 상황에 맞게 조절 가능
- 파일이나 네트워크 등, 로그를 별도의 위치에 남길 수 있음
- 성능도 일반 System.out 보다 좋음

##### 로깅 라이브러리
- 스프링 부트 라이브러리를 사용하면 스프링 부트 로깅 라이브러리(spring-boot-starter-logging)가 함께 포함
  - SLF4J(인터페이스): Logback, Log4J, Log4J2 등등 수 많은 라이브러리를 통합해서 `인터페이스`로 제공하는 것
  - Logback(구현체)
- 로그 선언
```java
// 방법1
private Logger log = LoggerFactory.getLogger(getClass());
// 방법2
private static final Logger log = LoggerFactory.getLogger(클래스이름.class)
// 방법3
@Slf4j
```
- 로그 호출
```java
// 5단계의 로깅 레벨 선택 가능(TRACE > DEBUG > INFO > WARN > ERROR)
log.trace("trace log={}", name); 
log.debug("debug log={}", name); 
log.info(" info log={}", name); 
log.warn(" warn log={}", name); 
log.error("error log={}", name);
```
- 로그 레벨 설정(application.properties)
```yml
# 전체 로그 레벨 설정(기본 info) 
logging.level.root=warn

# hello.springmvc 패키지와 그 하위 로그 레벨 설정 
logging.level.hello.springmvc=debug
```

##### 올바른 로그 사용법
- log.debug("data="+data)
  - 로그 출력 레벨을 info로 설정해도 해당 코드에 있는 "data="+data가 실제 실행이 되어 결과적으로 문자 더하기 연산이 발생
- log.debug("data={}", data)
  - 로그 출력 레벨을 info로 설정하면 아무일도 발생하지 않아서 앞과 같은 의미없는 연산이 발생하지 않음

### 스프링 MVC 기본 기능 - 요청 매핑

##### 매핑 정보
- @RestController
  - `@Controller 는 반환 값이 String 이면 뷰 이름으로 인식`하므로 뷰를 찾고 뷰가 랜더링 됨
  - `@RestController 는 반환 값으로 뷰를 찾는 것이 아니라, HTTP 메시지 바디에 바로 입력`
- @RequestMapping()는 
  - 맞는 URL 호출이 오면 이 메서드가 실행되도록 매핑
  - 배열[]로 제공하므로 {} 안에 선언해서 다중 설정 가능
  - method 속성으로 HTTP 메서드를 지정하지 않으면 HTTP 메서드와 무관하게 호출
- MappingController
```java
@RestController
public class MappingController {

	private Logger log = LoggerFactory.getLogger(getClass());

	/**
  * 기본 요청
  * 둘다 허용 /hello-basic, /hello-basic/
  * HTTP 메서드 모두 허용 GET, HEAD, POST, PUT, PATCH, DELETE
  */
	@RequestMapping("/hello-basic") 
  public String helloBasic() {
		log.info("helloBasic");
		return "ok";
	}

  /**
  * method 특정 HTTP 메서드 요청만 허용
  * GET, HEAD, POST, PUT, PATCH, DELETE
  */
  @RequestMapping(value = "/mapping-get-v1", method = RequestMethod.GET)
  public String mappingGetV1() {
    log.info("mappingGetV1");
    return "ok";
  }

  /**
  * 편리한 축약 애노테이션 (코드보기) * @GetMapping
  * @PostMapping
  * @PutMapping
  * @DeleteMapping
  * @PatchMapping
  */
  @GetMapping(value = "/mapping-get-v2")
  public String mappingGetV2() {
    log.info("mapping-get-v2");
    return "ok";
  }
}
```

##### 경로 변수 매핑
- @PathVariable
  - 최근 HTTP API는 다음과 같이 리소스 경로에 식별자를 넣는 스타일을 선호
  - URL 경로에서 @PathVariable을 사용하면 매칭 되는 부분을 편리하게 조회
  - @PathVariable의 이름과 파라미터 이름이 같으면 생략
```java
/**
* PathVariable 사용
* 변수명이 같으면 생략 가능
* @PathVariable("userId") String userId -> @PathVariable userId */
@GetMapping("/mapping/{userId}")
public String mappingPath(@PathVariable("userId") String data) {
  log.info("mappingPath userId={}", data);
  return "ok";
}

/**
* PathVariable 사용 다중
*/
@GetMapping("/mapping/users/{userId}/orders/{orderId}")
public String mappingPath(@PathVariable String userId, @PathVariable Long orderId) {
  log.info("mappingPath userId={}, orderId={}", userId, orderId);
  return "ok";
}
```

##### 특정 파라미터 조건 매핑
- 특정 파라미터에 해당하는 조건에만 매핑되도록 설정할 수 있음
```java
/**
* 파라미터로 추가 매핑
* params="mode" -> mode 파라미터가 있어야 매핑이 됨
* params="!mode" -> mode 파라미터가 없어야 매핑이 됨
* params="mode=debug" -> mode가 debug여야 매핑이 됨
*/
@GetMapping(value = "/mapping-param", params = "mode=debug") 
public String mappingParam() {
  log.info("mappingParam");
  return "ok";
}
```

##### 특정 헤더 조건 매핑
- 파라미터 매핑과 비슷하지만, HTTP 헤더 값에 조건을 매핑하도록 설정할 수 있음
- Content-Type과 Accept는 MediaType에 정의된 것 사용 가능
```java
/**
* 특정 헤더로 추가 매핑
* headers="mode", -> 요청 헤더의 mode 키 값이 있어야 매핑이 됨
* headers="!mode" -> 요청 헤더의 mode 키 값이 있어야 매핑이 됨
* headers="mode=debug" -> 요청 헤더의 mode 키 값이 debug여야 매핑이 됨
*/
@GetMapping(value = "/mapping-header", headers = "mode=debug")
public String mappingHeader() {
  log.info("mappingHeader");
  return "ok";
}
 

/**
* Content-Type 헤더 기반 매핑 (Media Type)
* consumes="application/json" -> 요청 헤더의 Content-Type이 application/json이어야 매핑
* consumes="!application/json" -> 요청 헤더의 Content-Type이 application/json아니어야 매핑
* consumes="application/*" -> 요청 헤더의 Content-Type이 application/*이면 매핑
* consumes="*\/*" -> 전페 다 매핑 
*/
@PostMapping(value = "/mapping-consume", consumes = "application/json") 
public String mappingConsumes() {
  log.info("mappingConsumes");
  return "ok";
}
/**
* Accept 헤더 기반 (Media Type) 
* produces = "text/html" -> 요청 헤더의 Accept이 text/html이어야 매핑
* produces = "!text/html" -> 요청 헤더의 Accept이 text/html아니어야 매핑
* produces = "text/*" -> 요청 헤더의 Accept이 text/*이어야 매핑
* produces = "*\/*" -> 전체 다 매핑
*/
@PostMapping(value = "/mapping-produce", produces = "text/html") 
public String mappingProduces() {
  log.info("mappingProduces");
  return "ok";
}
```

### 스프링 MVC 기본 기능 - 요청 매핑 API 예시

##### 회원 관리 API
- 회원 목록 조회: GET /users
- 회원 등록: POST /users
- 회원 조회: GET /users/{userId} 
- 회원수정: PATCH /users/{userId} 
- 회원 삭제: DELETE /users/{userId}
```java
@RestController
@RequestMapping("/mapping/users")
public class MappingClassController {
	/**
	 * GET /mapping/users
	 */
	@GetMapping
	public String users() {
		return "get users";
	}

	/**
	 * POST /mapping/users
	 */
	@PostMapping
	public String addUser() {
		return "post user";
	}

	/**
	 * GET /mapping/users/{userId}
	 */
	@GetMapping("/{userId}")
	public String findUser(@PathVariable String userId) {
		return "get userId=" + userId;
	}

	/**
	 * PATCH /mapping/users/{userId}
	 */
	@PatchMapping("/{userId}")
	public String updateUser(@PathVariable String userId) {
		return "update userId=" + userId;
	}

	/**
	 * DELETE /mapping/users/{userId}
	 */
	@DeleteMapping("/{userId}")
	public String deleteUser(@PathVariable String userId) {
		return "delete userId=" + userId;
	}
}
```

### 스프링 MVC 기본 기능 - HTTP 요청 

##### HTTP 요청 - 기본, 헤더 조회
- 서블릿을 활
```java
@Slf4j
@RestController
public class RequestHeaderController {

	@RequestMapping("/headers")
	public String headers(HttpServletRequest request, HttpServletResponse response, 
    HttpMethod httpMethod, Locale locale,
    // HTTP 헤더를 하나의 키에 여러 값이 들어가는 MultiValueMap로 조회
		@RequestHeader MultiValueMap<String, String> headerMap, 
    // 특정 HTTP 헤더를 조회
		@RequestHeader("host") String host,
    // 특정 쿠키를 조회
		@CookieValue(value = "myCookie", required = false) String cookie) { 
		log.info("request={}", request);
		log.info("response={}", response);
		log.info("httpMethod={}", httpMethod);
		log.info("locale={}", locale);
		log.info("headerMap={}", headerMap);
		log.info("header host={}", host);
		log.info("myCookie={}", cookie);
		return "ok";
	}
}
```
### 스프링 MVC 기본 기능 - HTTP 요청 파라미터

##### HTTP 요청 파라미터 - 쿼리 파라미터, HTML Form
- 클라이언트에서 서버로 요청 데이터를 전달할 때는 주로 다음 3가지 방법
  - `GET - 쿼리 파라미터`
    - /url?username=hello&age=20
    - 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달 
    - 예) 검색, 필터, 페이징등에서 많이 사용하는 방식
  - `POST - HTML Form`
    - username=hello&age=20
    - content-type: application/x-www-form-urlencoded
    - 메시지 바디에 쿼리 파리미터 형식으로 전달 
    - 예) 회원 가입, 상품 주문, HTML Form 사용
  - HTTP message body에 데이터를 직접 담아서 요청 HTTP API에서 주로 사용
    - 데이터 형식은 주로 JSON 사용
    - POST, PUT, PATCH

##### HTTP 요청 파라미터 - @RequestParam
- @RequestParam
  - `파라미터 이름으로 바인딩`
  - name(value) 속성이 파라미터 이름으로 사용
  - 서블릿의 request.getParameter()와 비슷
  - HTTP 파라미터 이름이 변수 이름과 같으면 @RequestParam(name="xx") 생략 가능
  - String , int , Integer 등의 단순 타입이면 `@RequestParam도 생략 가능하나 직관적이지 않음`
```java
/**
* @RequestParam 사용 - 파라미터 이름으로 바인딩
* @ResponseBody 추가 - @Controller여도 String이여도 뷰 이름을 찾지 않고 HTTP 응답 메시지에 넣어줌
*/
@ResponseBody
@RequestMapping("/request-param-v2")
public String requestParamV2(@RequestParam("username") String memberName, @RequestParam("age") int memberAge) {
  log.info("username={}, age={}", memberName, memberAge);
  return "ok";
}

/**
* @RequestParam 사용
* HTTP 파라미터 이름이 변수 이름과 같으면 @RequestParam(name="xx") 생략 가능 
*/
@ResponseBody
@RequestMapping("/request-param-v3")
public String requestParamV3(@RequestParam String username, @RequestParam int age) {
  log.info("username={}, age={}", username, age);
  return "ok";
}

/**
* @RequestParam 사용
* String, int 등의 단순 타입이면 @RequestParam 도 생략 가능 
*/
@ResponseBody
@RequestMapping("/request-param-v4")
public String requestParamV4(String username, int age) {
  log.info("username={}, age={}", username, age);
  return "ok";
}
```
- RequestParam.required
  - 파라미터 필수 여부
  - 기본값이 파라미터 필수(true)
  - 기본형(primitive)에 null 입력되므로 주의해야함
```java
/**
* @RequestParam.required
* /request-param -> username이 없으므로 예외
* /request-param?username= -> 빈문자로 통과
* /request-param?username=yoonje -> null을 int에 입력하는 것은 불가능, 따라서 Integer 변경해야 함(또는 defaultValue를 사용)
*/
@ResponseBody
@RequestMapping("/request-param-required")
public String requestParamRequired(@RequestParam(required = true) String username, @RequestParam(required = false) Integer age) {
  log.info("username={}, age={}", username, age);
  return "ok";
}
```
- defaultValue
  - 파라미터에 값이 없는 경우 defaultValue 를 사용하면 기본 값을 적용 가능
  - 기본 값이 있기 때문에 required 는 의미가 없음
```java
/**
* @RequestParam
* defaultValue 사용
* /request-param?username= -> defaultValue는 빈 문자의 경우에도 적용
*/
@ResponseBody
@RequestMapping("/request-param-default")
public String requestParamDefault(@RequestParam(required = true, defaultValue = "guest") String username, @RequestParam(required = false, defaultValue = "-1") int age) {
  log.info("username={}, age={}", username, age);
  return "ok";
}
```
- 파라미터를 Map으로 조회하기
  - @RequestParam Map -> Map(key=value)
  - @RequestParam MultiValueMap -> MultiValueMap(key=[value1, value2, ...]
```java
/**
* @RequestParam Map, MultiValueMap
* Map(key=value)
* MultiValueMap(key=[value1, value2, ...]
*/
@ResponseBody
@RequestMapping("/request-param-map")
public String requestParamMap(@RequestParam Map<String, Object> paramMap) {
  log.info("username={}, age={}", paramMap.get("username"), paramMap.get("age"));
  return "ok";
}
```
##### HTTP 요청 파라미터 - @ModelAttribute
- @ModelAttribute
  - 요청 파라미터를 받아서 필요한 객체를 만들고 그 객체에 값을 넣어줌
  - 객체를 생성 -> 요청 파라미터의 이름으로 HelloData 객체의 프로퍼티를 찾음 -> 프로퍼티의 setter를 호출해서 파라미터의 값을 입력(바인딩)
  - `ModelAttribute 는 생략 가능하나 직관적이지 않음`
```java
/**
* @ModelAttribute 사용
* 참고: model.addAttribute(helloData) 코드도 함께 자동 적용됨
*/
@ResponseBody
@RequestMapping("/model-attribute-v1")
public String modelAttributeV1(@ModelAttribute HelloData helloData) {
  log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
  return "ok";
}

/**
* @ModelAttribute 생략 가능
* String, int 같은 단순 타입 = @RequestParam
* argument resolver 로 지정해둔 타입 외 = @ModelAttribute */
@ResponseBody
@RequestMapping("/model-attribute-v2")
public String modelAttributeV2(HelloData helloData) {
  log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
  return "ok";
}
```
### 스프링 MVC 기본 기능 - HTTP 요청 메시지(Body)

##### HTTP 요청 메시지(Body) - 단순 텍스트
- HTTP message body에 데이터를 직접 담아서 요청하는 경우
  - HTTP API에서 주로 사용, JSON, XML, TEXT 
  - 데이터 형식은 주로 JSON 사용
  - POST, PUT, PATCH(GET에도 Body를 넣을 수 잇지만 현재 그렇게 사용하진 않음)
  - HTTP 메시지 바디를 통해 데이터가 직접 데이터가 넘어오는 경우는 `@RequestParam , @ModelAttribute 를 사용할 수 없음`
  - HTML Form 형식으로 전달되는 경우는 요청 파라미터로 인정
```java
@PostMapping("/request-body-string-v1")
public void requestBodyString(HttpServletRequest request,
  HttpServletResponse response) throws IOException {
  ServletInputStream inputStream = request.getInputStream();
  // 바이트 정보인 Stream의 경우는 항상 인코딩을 해줘야함
  String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
  log.info("messageBody={}", messageBody);
  response.getWriter().write("ok");
}
```
- InputStream(Reader)와 OutputStream(Writer)
  - InputStream(Reader): HTTP 요청 메시지 바디의 내용을 직접 조회
  - OutputStream(Writer): HTTP 응답 메시지의 바디에 직접 결과 출력
```java
@PostMapping("/request-body-string-v2")
public void requestBodyStringV2(InputStream inputStream, Writer responseWriter) throws IOException{
  String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
  log.info("messageBody={}", messageBody);
  responseWriter.write("ok");
}
```
- `HttpEntity`: HTTP 요청 및 응답에서 HTTP header, body 정보를 편리하게 조회 가능
  - 요청
    - 메시지 바디 정보를 직접 조회
    - 요청 파라미터를 조회하는 기능과 관계 없음(@RequestParam X, @ModelAttribute X)
  - 응답
    - 메시지 바디 정보 직접 반환
    - 헤더 정보 포함 가능
    - view 조회 X
```java
/**
* HttpEntity: HTTP header, body 정보를 편라하게 조회(응답에서도 HttpEntity 사용 가능)
* - 메시지 바디 정보를 직접 조회(@RequestParam X, @ModelAttribute X
* - 메시지 바디 정보 직접 반환 (view 조회 x)
*/
@PostMapping("/request-body-string-v3")
public HttpEntity<String> requestBodyStringV3(HttpEntity<String> httpEntity) {
  String messageBody = httpEntity.getBody(); 
  log.info("messageBody={}", messageBody);
  return new HttpEntity<>("ok");
}
```
- `RequestEntity와 ResponseEntity`: HttpEntity 를 상속받은 다음 객체
  - RequestEntity: HttpMethod, url 정보가 추가, 요청에서 사용
  - ResponseEntity: HTTP 상태 코드 설정 가능, 응답에서 사용
```java
@PostMapping("/request-body-string-v3_2")
public HttpEntity<String> requestBodyStringV3_2(RequestEntity<String> httpEntity) {
  String messageBody = httpEntity.getBody();
  log.info("messageBody={}", messageBody);
  return new ResponseEntity<String>("ok", HttpStatus.ACCEPTED);
}
```
- `@RequestBody와 @ResponseBody`
  - @RequestBody
    - RequestBody를 사용하면 `HTTP 메시지 바디 정보를 편리하게 조회`
    - 헤더 정보가 필요하다면 HttpEntity 를 사용하거나 @RequestHeader를 사용
    - @RequestParam와 @ModelAttribute 와는 전혀 관계가 없으므로 요청 파라미터를 조회하는 기능은 @RequestParam와 @ModelAttribute를 사용하고 HTTP 메시지 바디를 직접 조회하는 기능은 @RequestBody 사용
  - @ResponseBody
    - @ResponseBody 를 사용하면 응답 결과를 HTTP 메시지 바디에 직접 담아서 전달
    - view를 사용 X
```java
/**
* @RequestBody
* - 메시지 바디 정보를 직접 조회(@RequestParam X, @ModelAttribute X)
* @ResponseBody
* - 메시지 바디 정보 직접 반환(view 조회X)
*/
@ResponseBody
@PostMapping("/request-body-string-v4")
public String requestBodyStringV4(@RequestBody String messageBody) {
  log.info("messageBody={}", messageBody);
  return "ok";
}
```

##### HTTP 요청 메시지(Body) - JSON
- 예전 서블릿 방식의 JSON 처리
  - HttpServletRequest를 사용해서 직접 HTTP 메시지 바디에서 데이터를 읽어와서, 문자로 변환
  - JSON 데이터를 Jackson 라이브러리인 objectMapper 를 사용해서 자바 객체로 변환
```java
@PostMapping("/request-body-json-v1")
public void requestBodyJsonV1(HttpServletRequest request,
  HttpServletResponse response) throws IOException {
  ServletInputStream inputStream = request.getInputStream();
  String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
  log.info("messageBody={}", messageBody);
  HelloData data = objectMapper.readValue(messageBody, HelloData.class);
  log.info("username={}, age={}", data.getUsername(), data.getAge());
  response.getWriter().write("ok");
}
```
- HttpEntity
```java
/**
* HttpEntity: HTTP header, body 정보를 편라하게 조회(응답에서도 HttpEntity 사용 가능)
* - 메시지 바디 정보를 직접 조회(@RequestParam X, @ModelAttribute X
* - 메시지 바디 정보 직접 반환 (view 조회 x)
*/
@ResponseBody 
@PostMapping("/request-body-json-v2")
 public String requestBodyJsonV2(HttpEntity<HelloData> httpEntity) { 
   HelloData data = httpEntity.getBody();
   log.info("username={}, age={}", data.getUsername(), data.getAge()); 
   return "ok";
}
```
- @RequestBody 문자 변환
  - 이전에 학습했던 @RequestBody 를 사용해서 HTTP 메시지에서 데이터를 꺼내고 messageBody에 저장
  - JSON 데이터인 messageBody 를 objectMapper 를 통해서 자바 객체로 변환
```java
@ResponseBody
@PostMapping("/request-body-json-v3")
public String requestBodyJsonV3(@RequestBody String messageBody) throws IOException {
  HelloData data = objectMapper.readValue(messageBody, HelloData.class);
  log.info("username={}, age={}", data.getUsername(), data.getAge());
  return "ok";
}
```
- @RequestBody 객체 변환
  - @RequestBody는 생략 불가능
  - @RequestBody 에 직접 만든 객체를 지정하여 매핑
```java
@ResponseBody
@PostMapping("/request-body-json-v4")
public String requestBodyJsonV4(@RequestBody HelloData data) {
  log.info("username={}, age={}", data.getUsername(), data.getAge());
  return "ok";
}
```
- `@RequestBody과 @ResponseBody 객체 변환`
```java
/**
* @RequestBody 생략 불가능(@ModelAttribute 가 적용되어 버림)
* - 메시지 바디 정보를 직접 조회(@RequestParam X, @ModelAttribute X)
* @ResponseBody 적용
* - 메시지 바디 정보 직접 반환(view 조회X)
*/
@ResponseBody
@PostMapping("/request-body-json-v5")
public HelloData requestBodyJsonV5(@RequestBody HelloData data) {
  log.info("username={}, age={}", data.getUsername(), data.getAge());
  return data;
}
```

### 스프링 MVC 기본 기능 - HTTP 응답

##### 정적 리소스, 뷰 템플릿
- 서버에서 클라이언트로 응답 데이터를 전달할 때는 주로 다음 3가지 방법
  - 정적 리소스: 정적인 HTML, css, js 제공
  - 뷰 템플릿 사용: 동적인 HTML을 제공
  - HTTP 메시지 사용: HTTP 메시지 바디에 JSON 같은 형식으로 데이터를 실어 보냄
- `정적 리소스`: 스프링 부트는 클래스패스의 특정 디렉토리에 있는 정적 리소스를 제공
  - 경로: /static, /public, /resources, /META-INF/resource
  - src/main/resources 는 리소스를 보관하는 곳이고, 또 클래스패스의 시작 경로
- `뷰 템플릿`: 뷰 템플릿을 거쳐서 HTML이 생성되고, 뷰가 응답을 만들어서 전달
  - 경로 src/main/resources/templates
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Title</title>
</head>
  <body>
  <p th:text="${data}">empty</p>
  </body>
</html>
```
```java
@Controller
public class ResponseViewController {

	@RequestMapping("/response-view-v1")
	public ModelAndView responseViewV1() {
		ModelAndView mav = new ModelAndView("response/hello").addObject("data", "hello!");
		return mav;
	}

	/**
	 * String을 반환하는 경우 @ResponseBody 가 없으면 response/hello 로 뷰 리졸버가 실행되어서 뷰를 찾고 렌더링
	 * @param model
	 * @return
	 */
	@RequestMapping("/response-view-v2")
	public String responseViewV2(Model model) {
		model.addAttribute("data", "hello!!");
		return "response/hello";
	}

	/**
	 * Void를 반환하는 경우 @Controller가 없고 HTTP 바디가 없으면 요청 URL을 참고해서 논리 뷰 이름으로 사용 (권장되지 않는 방법)
	 * @param model
	 */  
	@RequestMapping("/response/hello")
	public void responseViewV3(Model model) {
		model.addAttribute("data", "hello!!");
	}
}
```
- `HTTP 메시지`
  - ResponseBody , HttpEntity 를 사용하면, 뷰 템플릿을 사용하는 것이 아니라, HTTP 메시지 바디에 직접 응답 데이터를 출력

##### HTTP 응답 API와 응답 메시지(Body)
- HTTP API를 제공하는 경우에는 HTML이 아니라 데이터를 전달해야 하므로, HTTP 메시지 바디에 JSON 같은 형식으로 데이터를 실어 보냄
- @ResponseBody + @Controller -> @RestController
```java
@Slf4j
@Controller
//@RestController
public class ResponseBodyController {

	@GetMapping("/response-body-string-v1")
	public void responseBodyV1(HttpServletResponse response) throws IOException {
		response.getWriter().write("ok");
	}

	/**
	 * HttpEntity, ResponseEntity(Http Status 추가)
	 * @return
	 */
	@GetMapping("/response-body-string-v2")
	public ResponseEntity<String> responseBodyV2() {
		return new ResponseEntity<>("ok", HttpStatus.OK);
	}

	/**
	 * String 반환을 문자열 리턴
	 * @return
	 */
	@ResponseBody
	@GetMapping("/response-body-string-v3")
	public String responseBodyV3() {
		return "ok";
	}

	/**
	 * ResponseEntity에 HelloData가 들어감
	 * @return
	 */
	@GetMapping("/response-body-json-v1")
	public ResponseEntity<HelloData> responseBodyJsonV1() {
		HelloData helloData = new HelloData();
		helloData.setUsername("userA");
		helloData.setAge(20);
		return new ResponseEntity<>(helloData, HttpStatus.OK);
	}

	/**
	 * 어노테이션을 보고 ResponseEntity에 자동으로 helloData가 들어감
	 * @return
	 */
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@GetMapping("/response-body-json-v2")
	public HelloData responseBodyJsonV2() {
		HelloData helloData = new HelloData();
		helloData.setUsername("userA");
		helloData.setAge(20);
		return helloData;
	}
}
```

### 스프링 MVC 기본 기능 - HTTP 메시지 컨버터

##### @ResponseBody
<img width="819" alt="스크린샷 2021-05-08 오후 11 28 01" src="https://user-images.githubusercontent.com/38535571/117542803-0ce56100-b055-11eb-9032-60b6c897559e.png">

- HTTP API처럼 JSON 데이터를 `HTTP 메시지 바디에서 직접 읽거나 쓰는 경우` HTTP 메시지 컨버터를 사용
- viewResolver 대신에 `HttpMessageConverter`가 동작
- `HttpMessageConverter`
  - 기본 바이트: ByteArrayHttpMessageConverter (0순위 체크)
  - 기본 객체처리: MappingJackson2HttpMessageConverter (1순위 체크)
  - 기본 문자처리: StringHttpMessageConverter (2순위 체크)
  - byte 처리 등등 기타 여러 HttpMessageConverter가 기본으로 등록되어 있음
- `HTTP Accept 해더와 서버의 컨트롤러 반환 타입 정보를` 조합해서 HttpMessageConverter가 선택

##### Spring에서 제공하는 주요 메시지 컨버터
- `ByteArrayHttpMessageConverter`: byte[] 데이터를 처리
  - 클래스 타입: byte[] , 미디어타입: */* ,
  - 요청 예) @RequestBody byte[] data
  - 응답 예) @ResponseBody return byte[], 미디어타입: application/octet-stream
- `MappingJackson2HttpMessageConverter`: application/json
  - 클래스 타입: 객체 또는 HashMap, 미디어타입: application/json 
  - 요청 예) @RequestBody HelloData data
  - 응답 예) @ResponseBody return helloData, 미디어타입: application/json 
- `StringHttpMessageConverter`: String 문자로 데이터를 처리
  - 클래스 타입: String , 미디어타입: */*
  - 요청 예) @RequestBody String data
  - 응답 예) @ResponseBody return "ok", 미디어타입: text/plain

##### HTTP 요청 데이터 읽기 과정
- HTTP 요청이 오고, 컨트롤러에서 @RequestBody, HttpEntity 파라미터를 사용
- 메시지 컨버터가 메시지를 읽을 수 있는지 확인하기 위해 canRead()를 호출
  - `대상 클래스 타입을 지원하는지 체크` 예) @RequestBody 의 대상 클래스 ( byte[] , String , HelloData )
  - `HTTP 요청의 Content-Type 미디어 타입을 지원하는지 체크` 예) text/plain , application/json , */*
- canRead() 조건을 만족하면 read()를 호출해서 객체 생성하고, 반환

##### HTTP 응답 데이터 생성 과정
- 요청에 대한 처리가 어느 정도 되고, 컨트롤러에서 @ResponseBody, HttpEntity 로 값이 반환
- 메시지 컨버터가 메시지를 쓸 수 있는지 확인하기 위해 canWrite()를 호출
  - `대상 클래스 타입을 지원하는지 체크` 예) return의 대상 클래스 ( byte[] , String , HelloData )
  - `HTTP 요청의 Accept 미디어 타입을 지원하는지 체크(@RequestMapping 의 produces)` 예) text/plain , application/json , */*
- canWrite() 조건을 만족하면 write()를 호출해서 HTTP 응답 메시지 바디에 데이터를 생성

### 스프링 MVC 기본 기능 - 요청 매핑 헨들러 어뎁터 구조

##### Sprin MVC 구조와 RequestMappingHandlerAdapter 동작
<img width="890" alt="스크린샷 2021-05-08 오후 11 29 50" src="https://user-images.githubusercontent.com/38535571/117542847-4f0ea280-b055-11eb-8988-fc68cf119ccd.png">

<img width="806" alt="스크린샷 2021-05-09 오전 4 01 59" src="https://user-images.githubusercontent.com/38535571/117550434-52b52000-b07b-11eb-91ef-6b6be5bc05ec.png">

- @RequestMapping을 처리하는 핸들러 어댑터인 RequestMappingHandlerAdapter(요청 매핑 헨들러 어뎁터)에서 ArgumentResolver와 ReturnValueHandler 실행 도중 HTTP 메시지 컨버팅이 일어남

##### ArgumentResolver
- 풀네임은 `HandlerMethodArgumentResolver`
- 애노테이션 기반의 컨트롤러는 HttpServletRequest, Model과 @RequestParam, @ModelAttribute 같은 애노테이션 그리고 @RequestBody, HttpEntity 같은 HTTP 메시지를 처리하는 부분까지 처리
- 동작 방식
  - 애노테이션 기반 컨트롤러를 처리하는 RequestMappingHandlerAdaptor는 바로 이 ArgumentResolver를 호출해서 컨트롤러(핸들러)가 필요로 하는 다양한 파라미터의 값(객체)을 생성 하고 파리미터의 값이 모두 준비되면 컨트롤러를 호출하면서 값을 넘겨줌
  - ArgumentResolver의 supportsParameter()를 호출해서 해당 파라미터를 지원하는지 체크하고, 지원하면 resolveArgument()를 호출해서 실제 객체를 생성하고 그리고 이렇게 생성된 객체가 컨트롤러 호출시 넘겨줌

##### ReturnValueHandler
- 풀네임은 `HandlerMethodReturnValueHandler`
- 동작 방식은 ArgumentResolver와 비슷한데, 이것은 응답 값을 변환하고 처리하여 String으로 뷰 이름만 반환해도 동작하도록 해줌

##### HTTP 메시지 컨버터 위치
- 요청의 경우 `@RequestBody를 처리하는` ArgumentResolver가 있고, HttpEntity 를 처리하는 ArgumentResolver가 있어 ArgumentResolver 들이 HTTP 메시지 컨버터를 사용해서 필요한 객체를 생성하는 것
- 응답의 경우 `@ResponseBody와 HttpEntity를 처리하는` ReturnValueHandler 가 있어 여기에서 HTTP 메시지 컨버터를 호출해서 응답 결과를 만듦


##### 확장
- 확장 인터페이스
  - HandlerMethodArgumentResolver
  - HandlerMethodReturnValueHandler
  - HttpMessageConverter
- 기능 확장은 WebMvcConfigurer를 상속 받아서 스프링 빈으로 등록
```java
@Bean
public WebMvcConfigurer webMvcConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
          // ...
        }
        
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
          //...
        } 
  };
}
```
### 부록
- 스프링 MVC 기본 기능 코드
[springmvc](./springmvc)

- 스프링 MVC를 이용한 웹 페이지 만들기 코드
[iterm-service](./item-service)
