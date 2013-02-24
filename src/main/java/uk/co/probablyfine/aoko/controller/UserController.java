package uk.co.probablyfine.aoko.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.probablyfine.aoko.dao.AccountDao;
import uk.co.probablyfine.aoko.dao.QueueItemDao;
import uk.co.probablyfine.aoko.domain.Account;
import uk.co.probablyfine.aoko.domain.QueueItem;

@Controller
public class UserController {

	@Autowired private QueueItemDao queue;
	@Autowired private AccountDao accounts;
	
	@ResponseBody
	@RequestMapping("/user/{username}")
	public Map<String,Object> getAllFromUser(@PathVariable("username") String username) {
		final Map<String,Object> response = new HashMap<String, Object>();
		
		Account userAccount = accounts.getFromUsername(username);
		
		if (userAccount == null) {
			response.put("error", "No user with that username");
		} else {
			List<QueueItem> queued = queue.allQueuedByUser(userAccount);
			response.put("queued",queued);
		}
		
		return response;
	}
	
}