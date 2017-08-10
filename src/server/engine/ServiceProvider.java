package server.engine;

import configs.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import javafx.application.Platform;
import models.ByteSerial;
import models.TIDBlock;
import mysql.DBManager;
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

    private int customTime = -1;

    /**
     * 메인 서버 소켓 운영을 위한 스레드
     */
    private Thread thread;

    /**
     * 서버 소켓 채널 인스턴스
     */
    private ServerSocketChannel socket;

    private Selector selector;

    private List<ICallback> jobs;

    /**
     * 클라이언트 소켓 집합
     */
    private HashMap<String, ProtocolResponder> clients;

    /**
     * 간단한 배치 작업을 위한 스레드로 이후 설계 변경 시 삭제 예정
     * @deprecated
     */
    private Thread batch;

    /**
     * 서버 소켓 채널 바인딩 포트
     */
    private int port;

    private void startServer(){
        try {
            resetServer();
/*
		NioEventLoop는 I/O 동작을 다루는 멀티스레드 이벤트 루프입니다.
		네티는 다양한 이벤트 루프를 제공합니다.
		이 예제에서는 두개의 Nio 이벤트 루프를 사용합니다.
		첫번째 'parent' 그룹은 인커밍 커넥션(incomming connection)을 액세스합니다.
		두번째 'child' 그룹은 액세스한 커넥션의 트래픽을 처리합니다.
		만들어진 채널에 매핑하고 스레드를 얼마나 사용할지는 EventLoopGroup 구현에 의존합니다.
		그리고 생성자를 통해서도 구성할 수 있습니다.
	*/


        }catch(IOException e){
            e.printStackTrace();
            d("Socket Creation failed - The port set in const is in use or internet connection is not established.");
        }
    }

    private void resetServer() throws IOException{
//        if(clients != null){
//            Iterator<String> iterator = clients.keySet().iterator();
//            while(iterator.hasNext()){
//                String key = iterator.next();
//                clients.get(key).getSocket().close();
//            }
//        }
        if(socket != null){
            if(socket.isOpen()) {
                socket.close();
            }
        }
        if(selector != null){
            if(selector.isOpen()) selector.close();
        }
    }

    /**
     * 포트를 매개변수로 입력받아 인스턴스를 생성하는 내부 접근 지정 생성자
     * @param port
     */
    private ServiceProvider(int port){
        this.port = port;

        jobs = new ArrayList<>();

        log = LoggerFactory.getLogger(this.getClass());

        log.info("Initiating Service Provider");

        startServer();

        batch = new Thread(() -> {
            migrateDirectly();
            // Test
//            while(true) {
//                try {
//                    int time = BATCH_TIME;
//                    if(customTime != -1 && customTime > 0) time = customTime;
//                    Thread.sleep(time);
//                    for(ICallback callback : jobs){
//                        callback.postExecuted();
//                    }
//                } catch (InterruptedException e) {
//                    d("Batch Thread Interrupted");
//                }
//
//                d("Batch Executed");
//
//            }
        });

        batch.setPriority(Thread.NORM_PRIORITY);

        thread = getProviderInstance();

        d("Server is ready to respond");

    }

    public static BlockingQueue<RealtimePOJO> offerList = new LinkedBlockingQueue<>();

    public boolean migrateDirectly(){
        while(true){
            try {
                RealtimePOJO r = offerList.take();
                String sql = r.getInsertSQL();
                DBManager.getInstance().execute(sql);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
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

    private void accept(SelectionKey selectionKey) {
//
//        try {
//            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
//            SocketChannel socketChannel = serverSocketChannel.accept();
//
//            d("STATUS :: [Connection Requested from [" + socketChannel.getRemoteAddress() + "]]");
//
//            ProtocolResponder protocolResponder = new ProtocolResponder(socketChannel, clients, selector);
//
//        }catch (NullPointerException e){
//            d("WARN :: Couldn't get Remote Address");
//        } catch (Exception e) {
//            e.printStackTrace();
//            d("ERROR :: NOT ABLE TO GENERATE SOCKET CHANNEL AND AN ERROR OCCURED WHILE ACCEPTING");
//        }
    }


    public void offer(ICallback callback){
        this.jobs.add(callback);
    }

    /**
     * 인스턴스 소켓 운영을 위한 스레드와 간이 배치 작업 스레드 호출
     * 명시적 호출이 필요함
     */
    public ServiceProvider start(){
        thread.start();
        batch.start();

        return instance;
    }

    public static ConcurrentHashMap<Long, TIDBlock> blockMap = new ConcurrentHashMap<>();

    public ByteSerial send(String client, ByteSerial msg, int length){
        final long tid = ByteSerial.bytesToLong(Arrays.copyOfRange(msg.getProcessed(), 8, 16));
        TIDBlock tidBlock = new TIDBlock(tid);
        tidBlock.setByteSerial(null);
        blockMap.put(tid, tidBlock);
        ByteSerial ret = null;
        try {

            if(clients.containsKey(client)) {
               clients.get(client).sendBlock(msg, length);
                try{
                    synchronized (tidBlock){
                        tidBlock.wait(REQUEST_TIMEOUT);

                        if(tidBlock.getByteSerial() == null) throw new InterruptedException();
                    }
                }catch (InterruptedException ee){
                    System.out.println("SEND BLOCK IS INTERRUPTED ::::::::::::::::::::::::::::::::::::::::::::::::");
                    blockMap.remove(tid);
                    tidBlock.setByteSerial(null);
//                    ee.printStackTrace();
                }
            }
            else{
                log.info("Client just requested does not exist. [KEY : " + client + "]");
            }
        }catch(Exception e){
            if(e instanceof InterruptedException){
                System.out.println("SEND BLOCK IS INTERRUPTED ::::::::::::::::::::::::::::::::::::::::::::::::");
            }
//            e.printStackTrace();
            blockMap.remove(tid);
            tidBlock.setByteSerial(null);
        }finally {

            return tidBlock.getByteSerial();
        }
    }

    public ByteSerial send(String client, byte[] msg, int length){
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
                    System.out.println("[WARN] BULK SEND RECV IS NULL :: [" + Thread.currentThread().getName() + "]");
                }else {
                    if (entry.isLoss()) System.out.println("[WARN] ENTRY LOSS :::: " + Arrays.toString(entry.getProcessed()));
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
            log.info(getTime() + " " + message);
        }
    }

    /**
     * 배치 작업 주기 시간을 커스텀 설정함
    * @param customTime
     */
    public void setCustomTime(int customTime) {
        this.customTime = customTime;
    }
}
