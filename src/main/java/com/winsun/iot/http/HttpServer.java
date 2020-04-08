package com.winsun.iot.http;

import com.winsun.iot.config.Config;
import com.winsun.iot.http.common.HttpServerInitializer;
import com.winsun.iot.iocmodule.Ioc;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private static HttpServer httpServer;

    private HttpServer() {
    }

    public static synchronized HttpServer getInstance() {
        if (httpServer == null) {
            httpServer = new HttpServer();
        }
        return httpServer;
    }

    public void start() {
       Thread r = new Thread(new Runnable() {
            @Override
            public void run() {
                initHttpServer();
            }
        });
        r.setName("http-server");
        r.start();
    }

    public void initHttpServer() {

        Config config = Ioc.getInjector().getInstance(Config.class);

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(20);
        EventLoopGroup workerGroup = new NioEventLoopGroup(20);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
//					.option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_KEEPALIVE, Boolean.FALSE)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)// 30S
                    .childHandler(new HttpServerInitializer());                    //闁板秶鐤嗛張宥呭閸掓繂顫愰崠锟?

            logger.info("http port:" + config.getHttpServerPort());
            logger.info("Http server bind port : " + config.getHttpServerPort());
            ChannelFuture f = b.bind(config.getHttpServerPort()).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("Http server start err:" + e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("Http server finally");
        }

    }
}
