package com.codechronicle.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codechronicle.EnvironmentHelper;
import com.codechronicle.dao.ImageDAO;
import com.codechronicle.entity.Book;
import com.codechronicle.entity.Image;
import com.codechronicle.messaging.AsyncMessage;
import com.codechronicle.messaging.MessageQueue;
import com.codechronicle.messaging.ProcessImageMessage;

@Controller
@RequestMapping(value="/rest")
public class RestController {
	
	private static Logger log = LoggerFactory.getLogger(RestController.class);

	@Inject
	private ImageDAO imageDAO;
	
	@Resource(name="messageQueue")	
	private MessageQueue messageQueue;
	
	/*@RequestMapping(method=RequestMethod.GET, value="/book/{bookId}")
	public @ResponseBody Book getBook (@PathVariable(value="bookId") long bookId, Model model) {
		
		log.info("Searching for book with id = " + bookId);
		Book book = new Book();
		book.setAuthor("JR Tolkein");
		book.setName("Fellowship of the Ring");
		
		return book;
	}*/
	
	
	@RequestMapping(method=RequestMethod.POST, value="/image")
	public @ResponseBody Image postSingleImage (
			@RequestParam(value="origUrl") String originalUrl, 
			@RequestParam(value="localFile") String localFilePath,
			@RequestParam(value="hostOriginal",required=false) boolean hostOriginal) {
		
		Image image = new Image();
		image.setOriginalUrl(originalUrl);
		image.setLocalPath(localFilePath);
		
		image = imageDAO.saveOrUpdate(image);
		
		ProcessImageMessage msg = new ProcessImageMessage();
		msg.setImageId(image.getId());
		msg.setHostOriginal(hostOriginal);
		messageQueue.enqueue(EnvironmentHelper.PROCESS_IMAGE_QUEUE, msg);
		
		return image;
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/image")
	public @ResponseBody Image postSingleImageTest () {
		
		String localPath = "/tmp/DSC_6420.JPG";
		String origUrl = "http://saptarshi.homedns.org/nas/pics/kai.jpg";
		
		//String origUrl = "http://www.citypictures.net/data/media/227/Monch_and_Eiger_Grosse_Scheidegg_Switzerland.jpg";
		//String origUrl = "http://www.zastavki.com/pictures/1024x768/2008/Movies_Movies_U_Underworld__Evolution_010690_.jpg";

		// ************* END artificial setup ****************
		
		// Check to see if we have this already. If we do, just retrieve the record and send it back.
		List<Image> existingImages = imageDAO.findByOrigUrl(origUrl);
		if (existingImages.size() > 0) {
			Image existingImage = existingImages.get(0);
			log.info("Requested image already in database, id = " + existingImage.getId() + ", original URL = " + existingImage.getOriginalUrl());
			return existingImage;
		}
		
		// Otherwise, create a new image, save the record, and then queue up post processing.
		Image image = new Image();
		image.setLocalPath(localPath);
		image.setOriginalUrl(origUrl);
		image = imageDAO.saveOrUpdate(image);
		
		ProcessImageMessage msg = new ProcessImageMessage();
		msg.setImageId(image.getId());
		msg.setHostOriginal(false);
		messageQueue.enqueue(EnvironmentHelper.PROCESS_IMAGE_QUEUE, msg);
		
		return image;
	}
}