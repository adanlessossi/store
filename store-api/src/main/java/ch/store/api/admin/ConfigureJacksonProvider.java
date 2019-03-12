package ch.store.api.admin;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * @author: B. Kanli
 *
 */
@Provider
public class ConfigureJacksonProvider implements ContextResolver<ObjectMapper> {

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}
}
