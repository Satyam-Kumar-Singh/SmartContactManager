package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private UserRepository userRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("Username " + userName);

		// get the user using username(email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		model.addAttribute("user", user);
	}

//	User dashboardUser handler

	@RequestMapping("/index")
	public String dashboard(Model model) {

		model.addAttribute("tittle", "User Dashboard");

		return "normal/user_dashboard";
	}

//	 add contact form handler

	@GetMapping("/addcontact")
	public String openAddContactForm(Model model) {

		model.addAttribute("tittle", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add-contact-form";
	}

//	receiving addcontact form data

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			contact.setUser(user);
			user.getContacts().add(contact);

			// processing and uploading file

			if (file.isEmpty()) {

				System.out.println("File is empty");
				contact.setImage("contact.png");

			} else {
				// file ka name ko set kr dega database me
				contact.setImage(file.getOriginalFilename());

				// kha upload kha krna hai uska path
				File file2 = new ClassPathResource("static/image").getFile();

				// path hai jha data ko write krna hai
				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());

				// copy all the bytes from file to an output stream(source se sare byte ko
				// destination me copy kr dega)
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image uploading");

			}

			// Save data in database
			this.userRepository.save(user);

			System.out.println("Data " + contact);
			System.out.println("Data " + user);
			System.out.println("Added data successfully");

			// message success
			session.setAttribute("message", new Message("Your contact is added successfilly.", "alert-success"));

		} catch (Exception e) {
			System.out.println("Error" + e.getMessage());
			e.printStackTrace();
			// message error
			session.setAttribute("message", new Message("Something went wrong !! Try again", "alert-danger"));

		}

		return "normal/add-contact-form";
	}

//  View contacts page handler

	@RequestMapping("/view-contact/{page}")
	public String showContactPage(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("tittle", "View Contact");

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

//		take two argument 1. current page number  2. pagesize
		Pageable pageable = PageRequest.of(page, 7);
		Page<Contact> Contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

		model.addAttribute("contacts", Contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", Contacts.getTotalPages());

		return "normal/view_contact";
	}

//	show single contact details handler

	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		System.out.println("cid " + cId);

		Optional<Contact> findById = this.contactRepository.findById(cId);
		Contact contact = findById.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("tittle", contact.getName());
		}

		return "normal/show_contact_details";
	}

//	delete contact handler

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		Contact contact = this.contactRepository.findById(cId).get();

		User user = this.userRepository.getUserByUserName(principal.getName());

		user.getContacts().remove(contact);
		this.userRepository.save(user);

		session.setAttribute("message", new Message("Your contact is deleted succesfully...", "alert-success"));

		return "redirect:/user/view-contact/0";
	}

//	open update form handler

	@GetMapping("/update/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("tittle", "Update contact");

		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

//	processing update form handler
	@PostMapping("/process-update")
	public String processUpdateForm(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			//old contact details

			Optional<Contact> findById = this.contactRepository.findById(contact.getcId());
			Contact oldContactDetails = findById.get();



			// image..
			if(!file.isEmpty()) {
				// delete old photo
				File deleteFile  = new ClassPathResource("static/image").getFile();
				File file2=new File(deleteFile,oldContactDetails.getImage() );
				file2.delete();

				// update new photo
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			}else {
				contact.setImage(oldContactDetails.getImage());

			}

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			contact.setUser(user);
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact is updated..","alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}

//	youe profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model,Principal principal) {
		model.addAttribute("tittle", "Profile");

		User user = this.userRepository.getUserByUserName(principal.getName());
		model.addAttribute("user", user);

		return "normal/profile";
	}

}
