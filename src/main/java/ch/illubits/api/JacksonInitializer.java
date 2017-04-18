package ch.illubits.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

@Provider
public class JacksonInitializer implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonInitializer() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {git
                jsonGenerator.writeNumber(localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond());
            }
        });
        mapper = new ObjectMapper()
                .registerModule(module)
                .configure(FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

    public ObjectWriter getPrettyPrinter() {
        return mapper.writer(new CustomPrettyPrinter());
    }

    private static class CustomPrettyPrinter extends DefaultPrettyPrinter {

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(": ");
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new CustomPrettyPrinter();
        }
    }
}
