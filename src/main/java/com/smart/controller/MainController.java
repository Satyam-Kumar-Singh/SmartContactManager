package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class MainController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

// 		home handler
	@RequestMapping("/")
	public String home(Model model) {

		model.addAttribute("tittle", "Home-Smart Contact Manager");

		return "home";
	}

//		about handler
	@RequestMapping("/about")
	public String about(Model model) {

		model.addAttribute("tittle", "About-Smart Contact Manager");

		return "about";
	}

//		signup page handler
	@RequestMapping("/signup")
	public String signup(Model model) {

		model.addAttribute("tittle", "Register-Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

//    form handler
	@PostMapping("/do_register")
	public String userRegister(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {

			if (!agreement) {
				System.out.println("You have not agreed terms and conditions");
				throw new Exception("You have not agreed terms and conditions");
			}

			if (result.hasErrors()) {
				System.out.println("errors" + result.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("agreement " + agreement);
			System.out.println(user);

			User result1 = this.userRepository.save(user);
			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfilly Registered !!", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Somthing went wrong !!" + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

//   login handler
	@RequestMapping("/signin")
	public String login(Model model) {
		model.addAttribute("tittle", "Login page");
		return "login";
	}

}
