package com.smart.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

//  this class is created due to session are removed from thymeleaf version 3 &
//  upper version so for removing message from session we have to create class like this.

@Component
public class SessionHelper {

	public void removeMessageFromSession() {
		try {
			System.out.println("removing session message.");

			HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getSession();
			session.removeAttribute("message");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
