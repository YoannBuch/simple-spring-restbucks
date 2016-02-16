package simple.restbucks.order;

public class Item {

	private String name;
	private int quantity;
	private double price;
	
	public Item(String name, double price) {
		this(name, 1, price);
	}
	
	public Item(String name, int quantity,
			double price) {
		this.name = name;
		this.quantity = quantity;
		this.price = price;
	}
	
	protected Item() {
		this(null, 0.0);
	}

	public String getName() {
		return name;
	}
	public int getQuantity() {
		return quantity;
	}
	
	public double getPrice() {
		return price;
	}
}
