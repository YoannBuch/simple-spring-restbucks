package simple.restbucks.web;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
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

		MockHttpServletResponse orderResourceUrl = createOrder();

		cancelOrder(orderResourceUrl);
	}

	@Test
	public void payOrder() throws Exception {

		MockHttpServletResponse reponse = createOrder();

		payOrder(reponse);
	}

	private MockHttpServletResponse createOrder() throws Exception {

		ClassPathResource resource = new ClassPathResource("order.json");
		byte[] data = Files.readAllBytes(resource.getFile().toPath());
		
		MockHttpServletResponse response =  mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(data)). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isCreated()). //
				andExpect(header().string("Location", is(notNullValue()))). //
				andExpect(jsonPath("_links.self").exists()). // 
				andExpect(jsonPath("_links.payment").exists()). // 
				andExpect(jsonPath("_links.cancel").exists()). //
				andReturn().getResponse();
		
		Link selfLink = findLink("self", response);	
		
		// Make sure we can get it
		response =  mvc.perform(get(selfLink.getHref())). //
			andDo(MockMvcResultHandlers.print()). //
			andExpect(status().isOk()). //
			andExpect(jsonPath("_links.self").exists()). // 
			andExpect(jsonPath("_links.payment").exists()). // 
			andExpect(jsonPath("_links.cancel").exists()). //
			andReturn().getResponse();
		
		return response;
	}

	private void cancelOrder(MockHttpServletResponse getOrderResponse) throws Exception {

		Link cancelLink = findLink("cancel", getOrderResponse);

		mvc.perform(delete(cancelLink.getHref())). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isNoContent());

		Link selfLink = findLink("self", getOrderResponse);
		
		// Make sure order is gone
		mvc.perform(get(selfLink.getHref())). //
			andDo(MockMvcResultHandlers.print()). //
			andExpect(status().isNotFound());
	}

	private void payOrder(MockHttpServletResponse getOrderResponse) throws Exception {

		Link paymentLink = findLink("payment", getOrderResponse);

		MockHttpServletResponse paymentResponse = mvc.perform(put(paymentLink.getHref()).contentType(MediaType.APPLICATION_JSON).content("\"1234123412341234\"")). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isCreated()). //
				andExpect(jsonPath("_links.self").exists()). //
				andExpect(jsonPath("_links.payment").doesNotExist()). // 
				andExpect(jsonPath("_links.cancel").doesNotExist()). //
				andReturn().getResponse();
		
		Link canceLink = findLink("cancel", getOrderResponse);
		
		// Make sure the order can't be canceled once it's paid
		mvc.perform(delete(canceLink.getHref())). //
			andExpect(status().isMethodNotAllowed());
		
		Link selfLink = findLink("self", paymentResponse); 
		
		// Make sure no links exist to cancel or pay
		mvc.perform(get(selfLink.getHref())). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isOk()). //
				andExpect(jsonPath("_links.self").exists()). // 
				andExpect(jsonPath("_links.payment").doesNotExist()). // 
				andExpect(jsonPath("_links.cancel").doesNotExist());
	}
	
	private Link findLink(String relationName, MockHttpServletResponse response) throws Exception {
		LinkDiscoverer discoverer = new HalLinkDiscoverer();
		return discoverer.findLinkWithRel(relationName, response.getContentAsString());		
	}
}
