package simple.restbucks.order;

public class Payment {

	private String creditCardNumber;
	
	public Payment(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}
	
	public String getCreditCardNumber() {
		return creditCardNumber;
	}
}
