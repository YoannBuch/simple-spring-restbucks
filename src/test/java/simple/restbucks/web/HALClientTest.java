package simple.restbucks.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import simple.restbucks.SimpleRestbucksApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SimpleRestbucksApplication.class)
@WebIntegrationTest
public class HALClientTest {

	private static String API_ENTRY_POINT = "http://localhost:8080";

	private static String ORDERS_REL = "orders";

	private static String CANCEL_REL = "cancel";

	private RestTemplate template;

	@Before
	public void before() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new Jackson2HalModule());

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
		converter.setObjectMapper(mapper);

		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(converter);

		template = new RestTemplate(converters);
	}

	@Test
	public void cancelOrder() throws Exception {

		// Ideally we'd use Traverson
		// (http://docs.spring.io/spring-hateoas/docs/0.19.0.RELEASE/reference/html/#client.traverson)
		// but it only supports GET method.

		// #1 Get root resource
		Resource<?> rootResource = template.getForObject(API_ENTRY_POINT, Resource.class);

		// #2 Create order
		Order newOrder = createOrder();
		URI newOrderURI = template.postForLocation(rootResource.getLink(ORDERS_REL).getHref(), newOrder);

		// #3 Get order
		Resource<Order> orderResource = getOrder(newOrderURI).getBody();

		// #4 Cancel order if it's possible
		if (orderResource.hasLink(CANCEL_REL)) {
			
			template.delete(orderResource.getLink(CANCEL_REL).getHref());

			try {
				getOrder(newOrderURI);
			} catch (HttpStatusCodeException e) {
				
				// Make sure order is gone
				Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
				return;
			}

			Assert.fail();
		}
	}

	private ResponseEntity<Resource<Order>> getOrder(URI orderURI) {

		ResponseEntity<Resource<Order>> getOrderResponse = template.exchange(orderURI, HttpMethod.GET, null,
				new ParameterizedTypeReference<Resource<Order>>() {
				});

		return getOrderResponse;
	}

	private Order createOrder() {

		Item hotChocolate = new Item("hot chocolate", 1, 3.40);
		Item coffee = new Item("coffee", 2, 2.40);

		Order order = new Order(Arrays.asList(hotChocolate, coffee));

		return order;
	}

	private static class Order {
		private Set<Item> items;

		protected Order() {
		}

		public Order(List<Item> items) {
			this.items = new HashSet<>();
			this.items.addAll(items);
		}

		public Set<Item> getItems() {
			return items;
		}
	}

	private static class Item {
		private String name;
		private int quantity;
		private double price;

		protected Item() {
		}

		public Item(String name, int quantity, double price) {
			this.name = name;
			this.quantity = quantity;
			this.price = price;
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
}
