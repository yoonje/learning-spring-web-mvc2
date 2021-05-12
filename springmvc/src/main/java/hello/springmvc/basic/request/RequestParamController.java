/*
 * ReqeustParamController.java 2021. 05. 07
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package hello.springmvc.basic.request;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hello.springmvc.basic.HelloData;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/07
 */
@Slf4j
@Controller
public class RequestParamController {
	/**
	 *  HttpServletRequest가 제공하는 방식으로 요청 파라미터를 조회
	 * 반환 타입이 없으면서 이렇게 응답에 값을 직접 집어넣으면, view 조회X
	 */
	@RequestMapping("/request-param-v1")
	public void requestParamV1(HttpServletRequest request, HttpServletResponse
		response) throws IOException {
		String username = request.getParameter("username");
		int age = Integer.parseInt(request.getParameter("age"));
		log.info("username={}, age={}", username, age);
		response.getWriter().write("ok");
	}

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
}
