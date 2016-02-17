package simple.restbucks.order.web;

import java.util.Set;

import org.springframework.hateoas.ResourceSupport;

import simple.restbucks.order.Item;
import simple.restbucks.order.Order;
import simple.restbucks.order.Order.Status;

public class OrderResource extends ResourceSupport {

	private Set<Item> items;
	private Status status;
	private double price;

	public OrderResource(Set<Item> items, Status status, double price) {
		this.items = items;
		this.status = status;
		this.price = price;
	}

	public Set<Item> getItems() {
		return items;
	}

	public Status getStatus() {
		return status;
	}

	public double getPrice() {
		return price;
	}
}
