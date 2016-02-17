package simple.restbucks.menu;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menu")
public class MenuController {

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> getMenu() {
		return new ResponseEntity<String>("the menu", HttpStatus.OK);
	}
}
