package simple.restbucks.order.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import simple.restbucks.order.Order;
import simple.restbucks.order.OrderRepository;
import simple.restbucks.order.Payment;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

	private final OrderRepository orderRepository;

	@Autowired
	public OrderController(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<Order>> findAll() {
		List<Order> orders = new ArrayList<>();
		for (Order order : orderRepository.readAll()) {
			orders.add(order);
		}
		return new ResponseEntity<List<Order>>(orders, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Order> createOrder(@RequestBody Order order) {
		order = orderRepository.create(order);

		HttpHeaders httpHeaders = new HttpHeaders();

		String location = String.format("http://localhost:8080/orders/%s", order.getId());

		httpHeaders.setLocation(URI.create(location));

		return new ResponseEntity<>(order, httpHeaders, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{orderId}")
	public ResponseEntity<Order> getOrder(@PathVariable String orderId) {

		Order order = orderRepository.findOne(orderId);
		
		if (order == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(order, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{orderId}")
	public ResponseEntity<Void> cancelOrder(@PathVariable String orderId) {

		Order order = orderRepository.findOne(orderId);
		
		if (order == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if (order.isPaid()) {
			return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
		}
		
		orderRepository.delete(orderId);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/{orderId}/payment")
	public ResponseEntity<Payment> payOrder(@PathVariable String orderId, @RequestBody Payment payment) {

		Order order = orderRepository.findOne(orderId);
		
		if (order == null || order.isPaid()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		// We assume the payment is successful
		order.markAsPaid();
		orderRepository.update(order);

		return new ResponseEntity<>(payment, HttpStatus.CREATED);
	}
}
