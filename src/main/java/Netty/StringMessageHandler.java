package Netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringMessageHandler extends SimpleChannelInboundHandler<String> {
    private final UserNameService userNameService;
    private ContextStoreService contextStoreService;
    private String name;

    public StringMessageHandler(UserNameService userNameService, ContextStoreService contextStoreService) {
        this.userNameService = userNameService;
        this.contextStoreService = contextStoreService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        userNameService.userConnect();
        log.debug("client connected");
        contextStoreService.registerContext(ctx);
        name = userNameService.getUserName();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        log.debug("received: {}", s);
        for(ChannelHandlerContext context : contextStoreService.getContext()){
            context.writeAndFlush(name + ": " + s);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userNameService.userDisconnect();
        log.debug("Client disconnected....");
        contextStoreService.removeContext(ctx);
    }
}
