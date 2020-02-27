package org.glassfish.jersey.netty.httpserver;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class NettyHttpContainerProviderTest {

    private ResourceConfig resourceConfig = new ResourceConfig(TestResource.class);
    private URI baseUri = URI.create("http://localhost:" + getPort());

    @Test
    public void createHttp2Server() {
        testChannelHandlerContextInjection(NettyHttpContainerProvider.createHttp2Server(baseUri, resourceConfig, null));
    }

    @Test
    public void createServer() {
        testChannelHandlerContextInjection(NettyHttpContainerProvider.createServer(baseUri, resourceConfig, false));
    }

    private void testChannelHandlerContextInjection(Channel channel) {
        Response response = ClientBuilder.newClient()
                .target(baseUri)
                .request()
                .get();

        assertThat(response.getStatus(), equalTo(200));
        String responseBody = response.readEntity(String.class);
        assertThat(responseBody, startsWith("/127.0.0.1"));

        Response asyncResponse = ClientBuilder.newClient()
                .target(baseUri)
                .path("async")
                .request()
                .get();

        assertThat(asyncResponse.getStatus(), equalTo(200));
        String asyncResponseBody = asyncResponse.readEntity(String.class);
        assertThat(asyncResponseBody, startsWith("/127.0.0.1"));

        channel.close();
    }

    private int getPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Path("/")
    public static class TestResource {

        @Inject
        private Provider<ChannelHandlerContext> channelHandlerContextProvider;

        @GET
        public String getRemoteAddress() {
            return channelHandlerContextProvider.get()
                    .channel()
                    .remoteAddress()
                    .toString();
        }

        @GET
        @Path("/async")
        public void getRemoteAddressAsync(@Suspended final AsyncResponse asyncResponse) {
            new Thread(() -> asyncResponse.resume(getRemoteAddress())).start();
        }

    }


}