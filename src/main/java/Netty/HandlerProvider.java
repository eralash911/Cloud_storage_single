package Netty;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandlerProvider {

    private final UserNameService nameService;
    private final ContextStoreService contextStoreService;

    public HandlerProvider(UserNameService nameService, ContextStoreService contextStoreService) {
        this.nameService = nameService;
        this.contextStoreService = contextStoreService;
    }

        public ChannelHandler[] getSerializePipeline(){
            return new ChannelHandler[]{
                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                    new ObjectEncoder(),
                    new AbstractMessageHandler()
            };
        }
    }

