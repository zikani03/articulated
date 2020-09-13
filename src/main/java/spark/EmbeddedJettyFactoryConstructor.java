package spark;

import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ThreadPool;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.JettyServerFactory;

public class EmbeddedJettyFactoryConstructor {
    AbstractNCSARequestLog requestLog;

    public EmbeddedJettyFactoryConstructor(AbstractNCSARequestLog requestLog) {
        this.requestLog = requestLog;
    }

    public EmbeddedJettyFactory create() {
        return new EmbeddedJettyFactory(new JettyServerFactory() {
            @Override
            public Server create(int i, int i1, int i2) {
                var server = new Server(new LoomThreadPool());
                ServerConnector connector = new ServerConnector(server);
                server.setConnectors(new Connector[]{connector});
                // server.setRequestLog(requestLog);
                return server;
            }

            @Override
            public Server create(ThreadPool threadPool) {
               return create(1, 1, 1);
            }
        });
    }
}