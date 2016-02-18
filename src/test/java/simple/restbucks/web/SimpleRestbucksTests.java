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

		MockHttpServletResponse rootResourceResponse = getRootResource();

		MockHttpServletResponse orderResponse = createOrder(rootResourceResponse);

		cancelOrder(orderResponse);
	}

	@Test
	public void payOrder() throws Exception {

		MockHttpServletResponse rootResourceResponse = getRootResource();

		MockHttpServletResponse orderResponse = createOrder(rootResourceResponse);

		payOrder(orderResponse);
	}

	private MockHttpServletResponse getRootResource() throws Exception {
		return mvc.perform(get("/")). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isOk()). //
				andExpect(jsonPath("_links.orders").exists()). //
				andExpect(jsonPath("_links.menu").exists()). //
				andReturn().getResponse();
	}

	private MockHttpServletResponse createOrder(MockHttpServletResponse rootResourceResponse) throws Exception {

		Link ordersLink = findLink("orders", rootResourceResponse);

		ClassPathResource resource = new ClassPathResource("order.json");
		byte[] data = Files.readAllBytes(resource.getFile().toPath());

		MockHttpServletResponse response = mvc
				.perform(post(ordersLink.getHref()).contentType(MediaType.APPLICATION_JSON).content(data)). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isCreated()). //
				andExpect(header().string("Location", is(notNullValue()))). //
				andExpect(jsonPath("_links.self").exists()). //
				andExpect(jsonPath("_links.payment").exists()). //
				andExpect(jsonPath("_links.cancel").exists()). //
				andReturn().getResponse();

		// Make sure we can get it
		Link selfLink = findLink("self", response);
		
		response = mvc.perform(get(selfLink.getHref())). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isOk()). //
				andExpect(jsonPath("_links.self").exists()). //
				andExpect(jsonPath("_links.payment").exists()). //
				andExpect(jsonPath("_links.cancel").exists()). //
				andReturn().getResponse();

		return response;
	}

	private void cancelOrder(MockHttpServletResponse orderResponse) throws Exception {

		Link cancelLink = findLink("cancel", orderResponse);

		mvc.perform(delete(cancelLink.getHref())). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isNoContent());

		// Make sure order is gone
		Link selfLink = findLink("self", orderResponse);
		
		mvc.perform(get(selfLink.getHref())). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isNotFound());
	}

	private void payOrder(MockHttpServletResponse orderResponse) throws Exception {

		Link paymentLink = findLink("payment", orderResponse);
		Link canceLink = findLink("cancel", orderResponse);

		MockHttpServletResponse paymentResponse = mvc
				.perform(put(paymentLink.getHref()).contentType(MediaType.APPLICATION_JSON) //
						.content("\"1234123412341234\"")). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isCreated()). //
				andExpect(jsonPath("_links.order").exists()). //
				andExpect(jsonPath("_links.receipt").exists()). //
				andReturn().getResponse();
		
		// Make sure no links exist to cancel or pay
		Link orderLink = findLink("order", paymentResponse);
		
		orderResponse = mvc.perform(get(orderLink.getHref())). //
				andDo(MockMvcResultHandlers.print()). //
				andExpect(status().isOk()). //
				andExpect(jsonPath("_links.self").exists()). //
				andExpect(jsonPath("_links.payment").doesNotExist()). //
				andExpect(jsonPath("_links.cancel").doesNotExist()). //
				andReturn().getResponse();
		
		// Make sure the order can't be canceled once it's paid
		mvc.perform(delete(canceLink.getHref())). //
				andExpect(status().isMethodNotAllowed());
	}

	private Link findLink(String relationName, MockHttpServletResponse response) throws Exception {
		LinkDiscoverer discoverer = new HalLinkDiscoverer();
		return discoverer.findLinkWithRel(relationName, response.getContentAsString());
	}
}
