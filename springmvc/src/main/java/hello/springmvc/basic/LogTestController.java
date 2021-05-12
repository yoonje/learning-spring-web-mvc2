/*
 * LogTestController.java 2021. 05. 07
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package hello.springmvc.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/07
 */
@RestController
public class LogTestController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@RequestMapping("/log-test")
	public String logTest() {
		String name = "Spring";
		log.trace("trace log={}", name);
		log.debug("debug log={}", name);
		log.info(" info log={}", name);
		log.warn(" warn log={}", name);
		log.error("error log={}", name);

		//로그를 사용하지 않아도 a+b 계산 로직이 먼저 실행됨, 이런 방식으로 사용하면 X
		log.debug("String concat log=" + name);
		return "ok";
	}
}
