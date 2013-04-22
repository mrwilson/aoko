package uk.co.probablyfine.aoko.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.probablyfine.aoko.dao.AccountDao;
import uk.co.probablyfine.aoko.domain.Account;

@Controller
public class LoginController {

	@Autowired private AccountDao users;
	@Autowired private PasswordEncoder pass;

	@ResponseBody
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public Map<String, String> processNewUser(@RequestParam String j_username,
			@RequestParam String j_password) {

		Map<String, String> response = new HashMap<String, String>();

		if (j_username.length() == 0) {
			response.put("error", "Please actually give a username.");
		} else if (j_password.length() == 0) {
			response.put("error", "Please actually give a password.");
		} else if (!j_username.matches("[a-zA-Z_0-9]+")) {
			response.put("error",
					"Illegal character in username, alphanumeric and _ only.");
		} else if (users.getFromUsername(j_username) != null) {
			response.put("error", "Username already exists, pick another!");
		} else {
			Account user = new Account(j_username, pass.encode(j_password), "ROLE_USER");
			users.merge(user);
			response.put("success", "Succesfully registered, can now log in");
		}

		return response;

	}

}
