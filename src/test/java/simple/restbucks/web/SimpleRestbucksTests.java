package simple.restbucks.web;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import simple.restbucks.SimpleRestbucksApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SimpleRestbucksApplication.class)
@WebAppConfiguration
public class SimpleRestbucksTests {

	private @Autowired WebApplicationContext context;
	private MockMvc mvc;

	@Before
	public void before() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void cancelOrderBeforePayment() throws Exception {

		String orderResourceUrl = createOrder();

		cancelOrder(orderResourceUrl);
	}

	@Test
	public void payOrder() throws Exception {

		String orderResourceUrl = createOrder();

		payOrder(orderResourceUrl);
	}

	private String createOrder() throws Exception {

		ClassPathResource resource = new ClassPathResource("order.json");
		byte[] data = Files.readAllBytes(resource.getFile().toPath());
		
		// PROBLEM: "/orders" is hard-coded

		MockHttpServletResponse response = mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(data)). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isCreated()). //
				andExpect(header().string("Location", is(notNullValue()))). //
				andReturn().getResponse();
		
		String orderResourceUrl = response.getRedirectedUrl();
		
		mvc.perform(get(orderResourceUrl)). //
			andDo(MockMvcResultHandlers.print()). //
			andExpect(status().isOk());
		
		return orderResourceUrl;
	}

	private void cancelOrder(String orderResourceUrl) throws Exception {

		mvc.perform(delete(orderResourceUrl)). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isNoContent()). //
				andReturn().getResponse();

		mvc.perform(get(orderResourceUrl)). //
			andDo(MockMvcResultHandlers.print()). //
			andExpect(status().isNotFound());
	}

	private void payOrder(String orderResourceUrl) throws Exception {

		// PROBLEM: "/orders/1/payment" is hard-coded
		
		String paymentUrl = String.format("%s/payment", orderResourceUrl);

		mvc.perform(put(paymentUrl).contentType(MediaType.APPLICATION_JSON).content("\"1234123412341234\"")). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isCreated()). //
				andReturn().getResponse();
		
		// Make sure the order can't be canceled once it's paid
		mvc.perform(delete(orderResourceUrl)). //
			andExpect(status().isMethodNotAllowed());
	}
}
