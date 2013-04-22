package uk.co.probablyfine.aoko.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

@Service
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	public static final RedirectStrategy NO_REDIRECT = new RedirectStrategy() {
		@Override
		public void sendRedirect(HttpServletRequest request,
				HttpServletResponse response, String url) throws IOException {
		}
	};
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
			throws IOException, ServletException {

		logger.debug("Successful login - "+auth.getName());
	
		setRedirectStrategy(NO_REDIRECT);
		
		super.onAuthenticationSuccess(request, response, auth);
	}
}