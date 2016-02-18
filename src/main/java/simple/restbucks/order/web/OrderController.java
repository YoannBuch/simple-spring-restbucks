package simple.restbucks.order.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import simple.restbucks.order.Payment;
import simple.restbucks.order.Order;
import simple.restbucks.order.OrderRepository;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

	private final OrderRepository orderRepository;

	@Autowired
	public OrderController(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<OrderResource>> findAll() {

		List<OrderResource> resources = new ArrayList<>();
		for (Order order : orderRepository.readAll()) {
			resources.add(createOrderResource(order));
		}

		return new ResponseEntity<List<OrderResource>>(resources, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<OrderResource> createOrder(@RequestBody Order order) {
		order = orderRepository.create(order);

		HttpHeaders httpHeaders = new HttpHeaders();

		Link selfLink = createSelfLink(order.getId());

		httpHeaders.setLocation(URI.create(selfLink.getHref()));

		OrderResource orderResource = createOrderResource(order);

		return new ResponseEntity<>(orderResource, httpHeaders, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{orderId}")
	public ResponseEntity<OrderResource> getOrder(@PathVariable String orderId) {

		Order order = orderRepository.findOne(orderId);

		if (order == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		OrderResource orderResource = createOrderResource(order);

		return new ResponseEntity<>(orderResource, HttpStatus.OK);
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
	public ResponseEntity<PaymentResource> payOrder(@PathVariable String orderId, @RequestBody Payment payment) {

		Order order = orderRepository.findOne(orderId);

		if (order == null || order.isPaid()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		// We assume given credit card number leads to a successful payment
		order.markAsPaid();
		orderRepository.update(order);

		Link orderLink = linkTo(methodOn(OrderController.class).getOrder(orderId)).withRel("order");

		// We assume we created a corresponding receipt
		String receiptId = "4uyfhbj";
		Link receiptLink = linkTo(methodOn(ReceiptController.class).getReceipt(receiptId)).withRel("receipt");

		PaymentResource paymentResource = new PaymentResource(payment.getCreditCardNumber());
		
		// Clients can either get the receipt or go back to the order
		paymentResource.add(orderLink, receiptLink);

		return new ResponseEntity<>(paymentResource, HttpStatus.CREATED);
	}

	private OrderResource createOrderResource(Order order) {
		OrderResource orderResource = new OrderResource(order.getItems(), order.getStatus(), order.getPrice());

		orderResource.add(createSelfLink(order.getId()));

		if (!order.isPaid()) {
			orderResource.add(createCancelLink(order.getId()));
			orderResource.add(createPaymentLink(order.getId()));
		}

		return orderResource;
	}

	private Link createSelfLink(String orderId) {
		return linkTo(methodOn(OrderController.class).getOrder(orderId)).withSelfRel();
	}

	private Link createCancelLink(String orderId) {
		return linkTo(methodOn(OrderController.class).cancelOrder(orderId)).withRel("cancel");
	}

	private Link createPaymentLink(String orderId) {
		return linkTo(methodOn(OrderController.class).payOrder(orderId, null)).withRel("payment");
	}
}
