package ch.softappeal.yass.tutorial.server;

import ch.softappeal.yass.core.Interceptor;
import ch.softappeal.yass.core.remote.Request;
import ch.softappeal.yass.core.remote.Server;
import ch.softappeal.yass.serialize.Reader;
import ch.softappeal.yass.serialize.Serializer;
import ch.softappeal.yass.serialize.Writer;
import ch.softappeal.yass.tutorial.contract.Config;
import ch.softappeal.yass.util.Exceptions;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XhrServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static void invoke(final Server server, final Serializer serializer, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        serializer.write(
            server.invocation((Request)serializer.read(Reader.create(request.getInputStream()))).invoke(
                Interceptor.DIRECT // note: we could add a http session interceptor here if needed
            ),
            Writer.create(response.getOutputStream())
        );
    }

    @Override protected void doPost(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            invoke(ServerSetup.SERVER, Config.MESSAGE_SERIALIZER, request, response);
        } catch (final Exception e) {
            throw Exceptions.wrap(e);
        }
    }

}