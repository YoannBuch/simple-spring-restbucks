package simple.restbucks;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import simple.restbucks.menu.MenuController;
import simple.restbucks.order.web.OrderController;

@RestController
@RequestMapping(path="/")
public class RootController {
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<ResourceSupport> root() {
		
		ResourceSupport rootResource = new ResourceSupport();
		
		Link orderLink = linkTo(OrderController.class).withRel("orders");
		rootResource.add(orderLink);
		
		Link menuLink = linkTo(MenuController.class).withRel("menu");
		rootResource.add(menuLink);
		
		return new ResponseEntity<>(rootResource, HttpStatus.OK);
	}
}
