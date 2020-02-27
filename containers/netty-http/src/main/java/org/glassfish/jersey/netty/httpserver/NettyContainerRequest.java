package org.glassfish.jersey.netty.httpserver;

import io.netty.channel.ChannelHandlerContext;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.ContainerUtils;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;

import javax.ws.rs.core.GenericType;
import java.lang.reflect.Type;
import java.net.URI;

class NettyContainerRequest {

    static final Type CHANNEL_HANDLER_TYPE = (new GenericType<Ref<ChannelHandlerContext>>() {}).getType();

    static ContainerRequest create(
            ChannelHandlerContext ctx,
            String method,
            String path,
            URI baseUri,
            ResourceConfig resourceConfig
    ) {
        String s = path.startsWith("/") ? path.substring(1) : path;
        URI uri = URI.create(baseUri + ContainerUtils.encodeUnsafeCharacters(s));

        ContainerRequest containerRequest = new ContainerRequest(
                baseUri,
                uri,
                method,
                new NettySecurityContext(ctx),
                new NettyPropertiesDelegate(),
                resourceConfig
        );

        containerRequest.setRequestScopedInitializer(injectionManager -> {
            injectionManager.<Ref<ChannelHandlerContext>>getInstance(CHANNEL_HANDLER_TYPE).set(ctx);
        });

        return containerRequest;
    }

}
