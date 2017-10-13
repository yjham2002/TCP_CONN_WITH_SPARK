package server.engine;

import agent.RealtimeAgent;
import configs.ServerConfig;
import databases.DBManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import javafx.application.Platform;
import models.ByteSerial;
import models.TIDBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.RealtimePOJO;
import redis.ICallback;
import server.ProtocolResponder;
import server.SohaDecoder;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import utils.HexUtil;
import utils.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static constants.ConstProtocol.SOCKET_TIMEOUT_LIMIT;
import static constants.ConstRest.REQUEST_RETRY_COUNT;
import static spark.route.HttpMethod.get;

/**
 * @author 함의진
 * @version 1.0.0
 * 가변 스레딩을 위한 운영 개체 스레드셋이 운영되는 곳으로, 이곳으로부터
 * 각 클라이언트에 따른 인스턴스를 생성하고 해시맵을 통해 운영한다.
 * 맵의 키는 클라이언트에 따른 유니크한 스트링을 통해 처리(정수형 농장 코드 및 특정 설정 문자열 기반)
 */
public class ServiceProvider extends ServerConfig{

    /**
     * SLF4J 로거
     */
    Logger log;
    /**
     * 싱글턴 패턴을 사용하기 위한 인스턴스 레퍼런스
     */
    private static ServiceProvider instance;

    /**
     * 메인 서버 소켓 운영을 위한 스레드
     */
    private Thread thread;

    /**
     * 클라이언트 소켓 집합
     */
    private HashMap<String, ProtocolResponder> clients;

    /**
     * 서버 소켓 채널 바인딩 포트
     */
    private int port;

    public static ConcurrentHashMap<String, Long> idleStateTime = new ConcurrentHashMap<>();

    /**
     * 포트를 매개변수로 입력받아 인스턴스를 생성하는 내부 접근 지정 생성자
     * @param port
     */
    private ServiceProvider(int port){
        this.port = port;

        log = LoggerFactory.getLogger(this.getClass());
        Log.i("Initiating Service Provider");
        thread = getProviderInstance();
        d("Server is ready to respond");

    }

    private Thread getProviderInstance(){

        Thread ret = new Thread(() -> {

            if(clients == null) clients = new HashMap<>();

            EventLoopGroup parentGroup = new NioEventLoopGroup(1);
            EventLoopGroup childGroup = new NioEventLoopGroup();
            try{
                ServerBootstrap sb = new ServerBootstrap();

                sb.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) ;
                sb.option(ChannelOption.TCP_NODELAY,true) ;

                sb.option(ChannelOption.SO_REUSEADDR, true);
                sb.option(ChannelOption.SO_LINGER, 0);
                sb.childOption(ChannelOption.SO_LINGER, 0);
                sb.childOption(ChannelOption.SO_REUSEADDR, true);
                sb.childOption(ChannelOption.SO_KEEPALIVE, true);
                sb.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 10*65536);
                sb.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 2*65536);

                sb.option(ChannelOption.SO_KEEPALIVE ,true) ;    // 소켓 KEEP ALIVE
                sb.option(ChannelOption.SO_RCVBUF, Integer.MAX_VALUE);

                sb.childOption(ChannelOption.SO_RCVBUF, 1048576);
                sb.childOption(ChannelOption.SO_SNDBUF, 1048576);

                sb.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) ;

                sb.group(parentGroup, childGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 100)
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel sc) throws Exception {
                                ChannelPipeline cp = sc.pipeline();
                                cp.addLast("decoder", new SohaDecoder());
                                cp.addLast(new ProtocolResponder(clients));
                            }
                        });

                // 인커밍 커넥션을 액세스하기 위해 바인드하고 시작합니다.
                ChannelFuture cf = sb.bind(this.port).sync();

                // 서버 소켓이 닫힐때까지 대기합니다.
                cf.channel().closeFuture().sync();
            }catch(Exception e){
                e.printStackTrace();
            }
            finally{
                parentGroup.shutdownGracefully();
                childGroup.shutdownGracefully();
            }

        });

        return ret;
    }

    public HashMap<String, ProtocolResponder> getClients() {
        return clients;
    }

    /**
     * 인스턴스 소켓 운영을 위한 스레드와 간이 배치 작업 스레드 호출
     * 명시적 호출이 필요함
     */
    public ServiceProvider start(){
        thread.start();
//        batch.start();
        RealtimeAgent.getInstance().start(20);

        return instance;
    }

    public static ConcurrentHashMap<Long, TIDBlock> blockMap = new ConcurrentHashMap<>();

    public ByteSerial send(String client, ByteSerial msg, int length){
        final long tid = ByteSerial.bytesToLong(Arrays.copyOfRange(msg.getProcessed(), 8, 16));
        TIDBlock tidBlock = new TIDBlock(tid);
        tidBlock.setByteSerial(null);
        blockMap.put(tid, tidBlock);
        try {

            if(clients.containsKey(client)) {

                try{
                    for(int e = 0; e < REQUEST_RETRY_COUNT; e++) {
                        Log.i("SENDING TRY COUNT :: " + (e + 1));
                        clients.get(client).sendBlock(msg, length);
                        synchronized (tidBlock) {
                            tidBlock.wait(REQUEST_TIMEOUT);
                            if (tidBlock.getByteSerial() == null) {
                                if(e + 1 >= REQUEST_RETRY_COUNT){
                                    throw new InterruptedException();
                                }else{
                                    continue;
                                }

                            }
                            else break;
                        }
                    }
                }catch (InterruptedException ee){
                    Log.i("SEND BLOCK IS INTERRUPTED ::::::::::::::::::::::::::::::::::::::::::::::::");
                    blockMap.remove(tid);
                    tidBlock.setByteSerial(null);
//                    ee.printStackTrace();
                }
            } else{
                Log.i("Client just requested does not exist. [KEY : " + client + "]");
            }
        }catch(Exception e){
            if(e instanceof InterruptedException){
                Log.i("SEND BLOCK IS INTERRUPTED ::::::::::::::::::::::::::::::::::::::::::::::::");
            }
//            e.printStackTrace();
            blockMap.remove(tid);
            tidBlock.setByteSerial(null);
        }finally {

            return tidBlock.getByteSerial();
        }
    }

    public ByteSerial send(String client, byte[] msg, int length){
        Log.i("SEND :: " + Arrays.toString(msg));
        return send(client, new ByteSerial(msg, ByteSerial.TYPE_NONE), length);
    }

    public List<ByteSerial> send(String client, byte[][] msgs, int[] lengths){

        List<ByteSerial> byteSerials = new ArrayList<>();

        for(int e = 0; e < msgs.length; e++) {

            ByteSerial entry = send(client, new ByteSerial(msgs[e], ByteSerial.TYPE_NONE), lengths[e]);

            if( entry != null && !entry.isLoss() ) {
                byteSerials.add(entry) ;
            }
            else
            {
                if(entry == null) {
                    Log.i("[WARN] BULK SEND RECV IS NULL :: [" + Thread.currentThread().getName() + "]");
                    Log.i("ERROR OCCURRED ON " + Arrays.toString(msgs[e]));
                }else {
                    if (entry.isLoss()) Log.i("[WARN] ENTRY LOSS :::: " + Arrays.toString(entry.getProcessed()));
                }
                return null;
            }
        }

        return byteSerials;
    }

    /**
     * 싱글턴 패턴을 위한 인스턴스 레퍼런스 메소드
     * @param port
     * @return
     */
    public static ServiceProvider getInstance(int port){
        if(instance == null) instance = new ServiceProvider(port);
        return instance;
    }

    public static ServiceProvider getInstance(){
        return getInstance(SOCKET_PORT);
    }

    /**
     * 개발시 디버깅을 위한 로깅 메소드 단축
     * @param message
     */
    private void d(String message){
        if(DEBUG_MODE) {
            Log.i(getTime() + " " + message);
        }
    }

}
