package com.sqber.filemgr.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FileMgrController {
	
	@GetMapping("/filemgr/index")
	public String index() {
		
		return "filemgr/index";
	}
}
