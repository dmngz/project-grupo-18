package com.example.cybercert.controllers;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Agrega automáticamente el token CSRF a todos los modelos de controladores.
 * Las templates pueden acceder al token con {{token}}
 */
@ControllerAdvice
public class CsrfControllerAdvice {

	@ModelAttribute("token")
	public String csrfToken(HttpServletRequest request) {
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			return csrfToken.getToken();
		}
		return "";
	}
}
