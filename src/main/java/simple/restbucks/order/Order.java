package simple.restbucks.order;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Order {

	private String id;

	private Status status;

	private final Set<Item> items = new HashSet<Item>();

	public Order(Collection<Item> lineItems) {
		this.status = Status.PAYMENT_EXPECTED;
		this.items.addAll(lineItems);
	}

	public Order(Item... items) {
		this(Arrays.asList(items));
	}
	
	protected Order() {
		this(new Item[0]);
	}
	
	public void setId(String id) {
		this.id = id;
	}

	// Important: we don't want internal ids to leak into the client
	@JsonIgnore
	public String getId() {
		return id;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public double getPrice() {

		double price = 0;

		for (Item item : items) {
			price += item.getPrice();
		}

		return price;
	}
	
	public Set<Item> getItems() {
		return items;
	}

	public void markAsPaid() {

		if (isPaid()) {
			throw new IllegalStateException("Already paid order cannot be paid again!");
		}

		this.status = Status.PREPARING;
	}

	@JsonIgnore
	public boolean isPaid() {
		return !this.status.equals(Status.PAYMENT_EXPECTED);
	}

	public static enum Status {

		PAYMENT_EXPECTED,

		PREPARING
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
