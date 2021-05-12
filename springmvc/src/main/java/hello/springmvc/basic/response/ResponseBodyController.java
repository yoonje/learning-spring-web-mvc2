/*
 * ResponseBodyController.java 2021. 05. 08
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package hello.springmvc.basic.response;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import hello.springmvc.basic.HelloData;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/08
 */
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