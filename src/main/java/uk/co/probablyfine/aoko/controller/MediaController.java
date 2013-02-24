package uk.co.probablyfine.aoko.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.probablyfine.aoko.dao.MusicFileDao;
import uk.co.probablyfine.aoko.domain.MusicFile;

@Controller
public class MediaController {

	@Autowired private MusicFileDao musicFiles;
	
	@ResponseBody
	@RequestMapping(value="/media/",produces="application/json")
	public Map<String,Object> getAllQueuedTracks(Model m, Principal p) {
		final Map<String,Object> response = new HashMap<String, Object>();
				
		if(p != null) {
			response.put("username",p.getName());
		}
		
		List<MusicFile> allQueuedTracks = musicFiles.getAll();
		response.put("allTracks",allQueuedTracks);
		return response;
	}
}
