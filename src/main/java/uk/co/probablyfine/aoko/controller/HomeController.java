package uk.co.probablyfine.aoko.controller;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.probablyfine.aoko.dao.AccountDao;
import uk.co.probablyfine.aoko.domain.Account;
import uk.co.probablyfine.aoko.domain.QueueItem;
import uk.co.probablyfine.aoko.service.QueueService;

@Controller
public class HomeController {
	
	@Autowired private QueueService queue;
	@Autowired private AccountDao users;
	
	@ResponseBody
	@RequestMapping(value="/", produces="application/json")
	public Map<String,Object> home(Principal p) {
		
		final Map<String,Object> response = new HashMap<String, Object>();
		
		if (p != null) {
			response.put("username", p.getName());
		}
		
		Collection<Collection<QueueItem>> queueLayout = queue.getQueueLayout();
		response.put("queue", queueLayout);
		return response;
	}
	
	@ResponseBody
	@RequestMapping(value="/admins", produces="application/json")
	public Map<String,Object> admins(Principal p) {
		final Map<String,Object> response = new HashMap<String, Object>();
		
		if (p != null) {
			response.put("username", p.getName());
		}
		
		Collection<Account> admins = users.getAdmins();
		response.put("admins", admins);
		
		return response;
	}
	
}