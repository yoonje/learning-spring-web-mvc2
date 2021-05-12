/*
 * MappingController.java 2021. 05. 07
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package hello.springmvc.basic.requestmapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/07
 */
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

	/**
	 * 파라미터로 추가 매핑
	 * params="mode",
	 * params="!mode"
	 * params="mode=debug"
	 * params="mode!=debug" (! = )
	 * params = {"mode=debug","data=good"}
	 */
	@GetMapping(value = "/mapping-param", params = "mode=debug")
	public String mappingParam() {
		log.info("mappingParam");
		return "ok";
	}

	/**
	 *특정 헤더로 추가 매핑
	 * headers="mode",
	 * headers="!mode"
	 * headers="mode=debug"
	 * headers="mode!=debug" (! = )
	 */
	@GetMapping(value = "/mapping-header", headers = "mode=debug")
	public String mappingHeader() {
		log.info("mappingHeader");
		return "ok";
	}


}
