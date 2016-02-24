package simple.restbucks.order;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

	private volatile AtomicInteger currentId = new AtomicInteger(0);

	private ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

	@PostConstruct
	public void initialize() {
		Item javaChip = new Item("Java Chip", 4.20);
		Item cappuchino = new Item("Cappuchino", 3.20);

		Order javaChipOrder = new Order(javaChip);
		Order cappuchinoOrder = new Order(cappuchino);

		create(javaChipOrder);
		create(cappuchinoOrder);
	}

	public Order create(Order order) {

		order.setId(nextId());

		orders.put(order.getId(), order);
		
		return order;
	}
	
	public Order findOne(String orderId) {
		return orders.get(orderId);
	}
	
	public void update(Order order) {
		orders.put(order.getId(), order);
	}
	
	public void delete(String orderId) {
		orders.remove(orderId);
	}

	private String nextId() {
		return String.valueOf(currentId.incrementAndGet());
	}

	public Collection<Order> readAll() {
		return orders.values();
	}
}
