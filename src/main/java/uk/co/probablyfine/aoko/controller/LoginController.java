package uk.co.probablyfine.aoko.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
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
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private SecurityContextRepository repository;

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password,
                                     HttpServletRequest req, HttpServletResponse res) {

        Map<String, String> response = new HashMap<String, String>();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        token.setDetails(users.getFromUsername(username).toUser());

        try {
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            repository.saveContext(SecurityContextHolder.getContext(), req, res);
            res.sendRedirect("/");
        } catch (Exception e) {
            res.setStatus(401);
            response.put("Failure", e.getMessage());
        }

        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Map<String, String> processNewUser(@RequestParam String j_username, @RequestParam String j_password) {

        Map<String, String> response = new HashMap<String, String>();

        if (j_username.length() == 0) {
            response.put("error", "Please actually give a username.");
        } else if (j_password.length() == 0) {
            response.put("error", "Please actually give a password.");
        } else if (!j_username.matches("[a-zA-Z_0-9]+")) {
            response.put("error", "Illegal character in username, alphanumeric and _ only.");
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
