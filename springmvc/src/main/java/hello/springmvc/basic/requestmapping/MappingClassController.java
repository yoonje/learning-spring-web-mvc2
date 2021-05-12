/*
 * MappingClassController.java 2021. 05. 07
 *
 * Copyright 2021 WorksMobile Corp. All rights Reserved.
 * WorksMobile PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package hello.springmvc.basic.requestmapping;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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