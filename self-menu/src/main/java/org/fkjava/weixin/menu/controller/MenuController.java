package org.fkjava.weixin.menu.controller;

import org.fkjava.weixin.menu.domain.SelfMenu;
import org.fkjava.weixin.menu.service.SelfMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/fhb_self_menu")
public class MenuController {


	@Autowired
	private SelfMenuService selfMenuService;

	@GetMapping
	public ModelAndView index() {
		// 默认只会在/META-INF/resources/目录下去找JSP文件。
		// 由于使用了JSP文件，所以必须要加入JSP的解析器（修改pom.xml），否则JSP文件找不到！
		return new ModelAndView("/WEB-INF/views/self-menu/index.jsp");
	}

	// 此时方法就会返回JSON数据
	@GetMapping(produces = "application/json")
	@ResponseBody
	public SelfMenu data() {
		return selfMenuService.findMenus();
	}

	@PostMapping(produces = "application/json")
	@ResponseBody
	// @RequestBody表示把整个请求体转换为Java对象
	public String save(@RequestBody SelfMenu menu) {
		this.selfMenuService.save(menu);
		return "保存成功";
	}
}
