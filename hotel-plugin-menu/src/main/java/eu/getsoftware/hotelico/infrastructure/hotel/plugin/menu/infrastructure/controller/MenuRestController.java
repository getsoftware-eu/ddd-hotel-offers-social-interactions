package eu.getsoftware.hotelico.infrastructure.hotel.plugin.menu.infrastructure.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.getsoftware.hotelico.infrastructure.hotel.plugin.menu.domain.model.MenuItem;
import eu.getsoftware.hotelico.infrastructure.hotel.plugin.menu.infrastructure.service.MenuService;

@Controller
@RequestMapping("/menu")
public class MenuRestController
{
	@Autowired
	private MenuService menuService;
	
	@RequestMapping(value = "/items", method = RequestMethod.GET)
	public @ResponseBody List<MenuItem> getItems() {
		return menuService.getItems();
	}
	
}
