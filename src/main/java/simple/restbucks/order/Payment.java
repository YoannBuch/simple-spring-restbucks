package simple.restbucks.order;

public class Payment {

	private String creditCardNumber;
	
	public Payment(String number) {
		this.creditCardNumber = number;
	}
	
	public String getNumber() {
		return creditCardNumber;
	}
}
