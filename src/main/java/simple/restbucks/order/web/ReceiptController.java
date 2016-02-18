package simple.restbucks.order.web;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/receipts")
public class ReceiptController {

	@RequestMapping(method = RequestMethod.GET, path = "/{receiptId}")
	public ReceiptResource getReceipt(@PathVariable String receiptId) {
		return new ReceiptResource();
	}

	private static class ReceiptResource extends ResourceSupport {
		private String message;

		public ReceiptResource() {
			this.message = "the receipt";
		}

		public String getMessage() {
			return message;
		}
	}
}
