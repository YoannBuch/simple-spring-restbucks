package simple.restbucks.order.web;

import org.springframework.hateoas.ResourceSupport;

public class PaymentResource extends ResourceSupport {

	private String creditCardNumber;
	
	public PaymentResource(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}
}
