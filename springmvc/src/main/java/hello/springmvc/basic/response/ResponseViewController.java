/*
 * ResponseViewController.java 2021. 05. 08
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package hello.springmvc.basic.response;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/08
 */
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