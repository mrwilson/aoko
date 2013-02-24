package uk.co.probablyfine.aoko.controller;

import java.security.Principal;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.probablyfine.aoko.dao.YoutubeDao;
import uk.co.probablyfine.aoko.domain.YoutubeDownload;

@Controller
@RequestMapping("/youtube/")
public class YoutubeController {

	@Autowired private YoutubeDao videos;
	
	@RequestMapping(value="/", produces="application/json")
	public @ResponseBody Collection<YoutubeDownload> getAllQueued(Model m) {
		return videos.getAllQueued();
	}
	
	@ResponseBody
	@RequestMapping(value="delete/{id}", produces="application/json")
	@PreAuthorize("isAuthenticated()")
	public String deleteSong(@PathVariable int id, Principal p) {
		videos.delete(id,p.getName());
		return "redirect:/";
	}
	
}