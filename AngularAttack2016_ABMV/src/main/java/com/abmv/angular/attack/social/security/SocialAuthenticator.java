package com.abmv.angular.attack.social.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import com.abmv.angular.attack.util.Tuple;

@Component
public class SocialAuthenticator {
	protected static final Logger log = LoggerFactory.getLogger(SocialAuthenticator.class);

    public void authenticate(Connection<?> connection, HttpServletRequest httpRequest) {
    	Tuple<String, String> userDetails = justAuthenticate(connection);
        if(httpRequest!=null){
        	HttpSession session = httpRequest.getSession();
        	session.setAttribute("userDisplayName", userDetails.getFirst());
        	session.setAttribute("username", userDetails.getSecond());
        	session.setAttribute("socialType", justAuthenticate(connection));
        	session.setAttribute("userConnection", connection);
        }
        log.info("User {} connected.", userDetails.getFirst());
    }

	public void authenticate(Connection<?> connection, NativeWebRequest request) {
		 Tuple<String, String> userDetails = justAuthenticate(connection);
        if(request!=null){
        	request.setAttribute("userDisplayName", userDetails.getFirst(), NativeWebRequest.SCOPE_GLOBAL_SESSION);
        	request.setAttribute("username", userDetails.getSecond(), NativeWebRequest.SCOPE_GLOBAL_SESSION);
        	request.setAttribute("socialType", justAuthenticate(connection), NativeWebRequest.SCOPE_GLOBAL_SESSION);
        	request.setAttribute("userConnection", connection, NativeWebRequest.SCOPE_GLOBAL_SESSION);
        }
        log.info("User {} connected.", userDetails);
	}
	
	public String getTypeOfSocial(Connection<?> connection){
		if (connection.getApi() instanceof Facebook) {
			return "Facebook";
		}else if (connection.getApi() instanceof Twitter) {
			return "Twitter";
		}else{
			return "NotAvailable";
		}
	}
	
	private Tuple<String, String> justAuthenticate(Connection<?> connection) {
		UserProfile userProfile = connection.fetchUserProfile();
		String username = userProfile.getUsername()==null ? userProfile.getEmail() : userProfile.getUsername();
		username = username==null ? connection.getDisplayName() : username;
		String userDisplayName = userProfile.getFirstName() +" "+ userProfile.getLastName();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, null);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Tuple<String, String> tuple = new Tuple<String, String>(userDisplayName, username);
		return tuple;
	}
}
