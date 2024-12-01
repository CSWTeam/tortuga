package de.computerstudienwerkstatt.tortuga.controller.base;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;

import java.io.IOException;

/**
 * @author Mischa Holz
 */
public class ChangeSetDeserializer extends JsonDeserializer<ChangeSet<?>> implements ContextualDeserializer {

    private JavaType type;

    public ChangeSetDeserializer() {
    }

    public ChangeSetDeserializer(JavaType type) {
        this.type = type;
    }

    @Override
    public ChangeSet<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ChangeSet<PersistentEntity> ret = new ChangeSet<>();

        JsonNode node = p.readValueAsTree();

        node.fieldNames().forEachRemaining(n -> ret.getPatchedFields().add(n));

        JsonParser parser = node.traverse(p.getCodec());

        Object patch = parser.readValueAs(type.getRawClass());

        ret.setPatch((PersistentEntity) patch);

        return ret;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new ChangeSetDeserializer(ctxt.getContextualType().containedType(0));
    }
}
