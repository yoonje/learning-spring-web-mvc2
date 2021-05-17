/*
 * BasicItemController.java 2021. 05. 18
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package com.hello.itemservice.web.basic;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hello.itemservice.domain.item.Item;
import com.hello.itemservice.domain.item.ItemRepository;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/18
 */
@Controller
@RequestMapping("/basic/items")
// final 이 붙은 멤버변수만 사용해서 생성자를 자동으로 만들어 주는 @RequiredArgsConstructor 사용해서 생성자 정의 없이
// 스프링에게서 생성자에 @Autowired 로 의존관계를 주입을 받을 수 있음 (생성자가 1개인 경우)
public class BasicItemController {
	private final ItemRepository itemRepository;

	@Autowired
	public BasicItemController(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@GetMapping
	public String items(Model model) {
		List<Item> items = itemRepository.findAll();
		model.addAttribute("items", items);
		return "basic/items";
	}

	@GetMapping("/{itemId}")
	public String item(@PathVariable Long itemId, Model model) {
		Item item = itemRepository.findById(itemId);
		model.addAttribute("item", item);
		return "basic/item";
	}

	@GetMapping("/add")
	public String addFrom() {
		return "basic/addForm";
	}

	/**
	 * HTML Form의 바디에 들어 있는 itemName=itemA&price=10000&quantity=10를 @RequestParam으로 처리
	 * @param itemName
	 * @param price
	 * @param quantity
	 * @param model
	 * @return
	 */
	@PostMapping("/add")
	public String addItemV1(@RequestParam String itemName,
		@RequestParam int price,
		@RequestParam Integer quantity,
		Model model) {
		Item item = new Item();
		item.setItemName(itemName);
		item.setPrice(price);
		item.setQuantity(quantity);
		itemRepository.save(item);
		model.addAttribute("item", item);
		return "basic/item";
	}

	/**
	 * @ModelAttribute("item") Item item
	 * @ModelAttribute 에 지정한 name(value) 속성 -> model.addAttribute(value) 에 영향
	 * name(value) 속성을 지정하지 않으면 첫문자가 소문자인 클래스 명을 그대로 사용
	 * model.addAttribute("item", item); 자동 추가
	 */
	@PostMapping("/add")
	public String addItemV2(@ModelAttribute("myItem") Item item, Model model) {
		itemRepository.save(item);
		//model.addAttribute("myItem", item); // 자동 추가, 생략 가능
		return "basic/item";
	}

	/**
	 * @ModelAttribute name 생략 가능
	 * model.addAttribute(item); 자동 추가, 생략 가능
	 * 생략시 model에 저장되는 name은 클래스명 첫글자만 소문자로 등록 Item -> item
	 */
	@PostMapping("/add")
	public String addItemV3(@ModelAttribute Item item) {
		itemRepository.save(item);
		return "basic/item";
	}

	/**
	 * @ModelAttribute 자체 생략 가능
	 * model.addAttribute(item) 자동 추가
	 */
	@PostMapping("/add")
	public String addItemV4(Item item) {
		itemRepository.save(item);
		return "basic/item";
	}

	@GetMapping("/{itemId}/edit")
	public String editForm(@PathVariable Long itemId, Model model) {
		Item item = itemRepository.findById(itemId);
		model.addAttribute("item", item);
		return "basic/editForm";
	}

	@PostMapping("/{itemId}/edit")
	public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
		itemRepository.update(itemId, item);
		return "redirect:/basic/items/{itemId}"; // 상품 상세 화면으로 이동하도록 리다이렉트, pathVarible 값도 사용 가능
	}

	/**
	 * 리다이렉트는 마지막 요청을 다시 한번 하는 것이므로 POST 이후 GET 처리를 해야함
	 * PRG - Post/Redirect/Get 패턴이 적용된 상품 등록
	 */
	@PostMapping("/add")
	public String addItemV5(Item item) {
		itemRepository.save(item);
		return "redirect:/basic/items/" + item.getId();
	}

	/**
	 * RedirectAttributes URL 인코딩도 해주고, pathVarible , 쿼리 파라미터까지 처리해줌
	 */
	@PostMapping("/add")
	public String addItemV6(Item item, RedirectAttributes redirectAttributes) {
		Item savedItem = itemRepository.save(item);
		redirectAttributes.addAttribute("itemId", savedItem.getId());
		redirectAttributes.addAttribute("status", true);
		return "redirect:/basic/items/{itemId}";
	}

	// 테스트용 더미 데이터 추가
	@PostConstruct
	public void init() {
		itemRepository.save(new Item("testA", 10000, 10));
		itemRepository.save(new Item("testB", 20000, 20));
	}

}
