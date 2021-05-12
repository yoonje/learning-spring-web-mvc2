/*
 * HelloData.java 2021. 05. 07
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package hello.springmvc.basic;

import lombok.Data;

@Data
public class HelloData {
	private String username;
	private int age;
}