package uk.co.probablyfine.aoko.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.probablyfine.aoko.dao.QueueItemDao;

@Controller
@RequestMapping("/a/")
@PreAuthorize("isAuthenticated()")
public class QueueController {

	private final Logger log = LoggerFactory.getLogger(QueueController.class);
	
	@Autowired private QueueItemDao queue;
	
	@ResponseBody
	@RequestMapping("move/{direction}/{bucketId}")
	public Map<String,Object> moveSong(@PathVariable String direction, @PathVariable int bucketId, Principal p) {
		final Map<String,Object> response = new HashMap<String, Object>();
		log.debug("User = {}, Bucket = {}, Direction = {}", new Object[] {p.getName(),bucketId,direction });

		if (direction.matches("up")) {
			queue.shiftUp(p.getName(), bucketId);
		} else if (direction.matches("down")) {
			queue.shiftDown(p.getName(), bucketId);
		} else {
			response.put("error", "Invalid direction to move song");
		}
		
		return response;
	}
	
	@ResponseBody
	@RequestMapping("delete/{id}")
	public Map<String, Object> deleteSong(@PathVariable("id") int bucketId, Principal p) {
		Map<String,Object> response = new HashMap<String, Object>();
		queue.deleteItem(bucketId,p.getName());
		return response;
	}
	
}