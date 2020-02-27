package org.glassfish.jersey.netty.httpserver;

import org.glassfish.jersey.internal.PropertiesDelegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class NettyPropertiesDelegate implements PropertiesDelegate {

    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public void setProperty(String name, Object object) {
        properties.put(name, object);
    }

    @Override
    public void removeProperty(String name) {
        properties.remove(name);
    }

}
