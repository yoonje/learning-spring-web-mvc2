/*
 * ItemRepository.java 2021. 05. 18
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package com.hello.itemservice.domain.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/18
 */
@Repository
public class ItemRepository {

	private static final Map<Long, Item> store = new HashMap<>(); //static 사용 private static long sequence = 0L; //static 사용
	private static long sequence = 0L; //static 사용

	public Item save(Item item) {
		item.setId(++sequence);
		store.put(item.getId(), item);
		return item;
	}

	public Item findById(Long id) {
		return store.get(id);
	}

	public List<Item> findAll() {
		return new ArrayList<>(store.values());
	}

	public void update(Long itemId, Item updateParam) {
		Item findItem = findById(itemId);
		findItem.setItemName(updateParam.getItemName());
		findItem.setPrice(updateParam.getPrice());
		findItem.setQuantity(updateParam.getQuantity());
	}

	public void clearStore() {
		store.clear();
	}

}
