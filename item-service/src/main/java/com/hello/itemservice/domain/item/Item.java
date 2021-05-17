/*
 * Item.java 2021. 05. 18
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package com.hello.itemservice.domain.item;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * Created By Yoonje Choi
 * Date : 2021/05/18
 */
@Getter
@Setter
public class Item {

	private Long id;
	private String itemName;
	private Integer price;
	private Integer quantity;

	public Item() {
	}

	public Item(String itemName, Integer price, Integer quantity) {
		this.itemName = itemName;
		this.price = price;
		this.quantity = quantity;
	}

}
