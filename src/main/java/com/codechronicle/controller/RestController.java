package com.codechronicle.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codechronicle.entity.Book;

@Controller
@RequestMapping(value="/rest")
public class RestController {
	
	private static Logger log = LoggerFactory.getLogger(RestController.class);

	//@Inject
	//private BookDAO bookDAO;
	
	@RequestMapping(method=RequestMethod.GET, value="/book/{bookId}")
	public @ResponseBody Book getBook (@PathVariable(value="bookId") long bookId, Model model) {
		
		log.info("Searching for book with id = " + bookId);
		Book book = new Book();
		book.setAuthor("JR Tolkein");
		book.setName("Fellowship of the Ring");
		
		return book;
	}
}