package utils;



import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


public class SocketUtils {

    public static final String TLP_SESSION = "TLP_SESSION";
    public static final String CONNECTER   = "CONNECTER";
    public static final int DefaultConnectTimeOut = 1000;
    public static final int MAX_TIMEOUT =90;



    private static Logger LOGGER= LoggerFactory.getLogger(SocketUtils.class);

    public  static String LISTEN_IP;
    public  static int LISTEN_PORT;

    public static String THIRD_IP;
    public static int THIRD_PORT;


    //socket connection
    public static IoSession  conn(InetSocketAddress addr, IoHandler ioHandler, ProtocolCodecFactory protocolCodecFactory){
        NioSocketConnector conn =null;
        IoSession session = null;
        ConnectFuture connectFuture = null;
        try{
            conn   = new NioSocketConnector();
            DefaultIoFilterChainBuilder chain = conn.getFilterChain();
            chain.addLast("logger", new LoggingFilter());
            chain.addLast("mdc",    new MdcInjectionFilter());
            chain.addLast("codec",  new ProtocolCodecFilter(protocolCodecFactory));
            conn.setHandler(ioHandler);
            //重复三次

            conn.setConnectTimeoutMillis(DefaultConnectTimeOut);


            connectFuture = conn.connect(addr);
            connectFuture.awaitUninterruptibly();

            if  (connectFuture.isDone()) {
                if  (!connectFuture.isConnected()) {  //若在指定时间内没连接成功，则抛出异常
                    LOGGER.info("fail to connect "  );
                    conn.dispose();    //不关闭的话会运行一段时间后抛出，too many open files异常，导致无法连接
                    throw   new  Exception();
                }
            }
            session = connectFuture.getSession();
            session.setAttribute(CONNECTER,conn);

            LOGGER.debug("链接成功{},{}",session.getId(),session.getRemoteAddress());
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.warn("链接燃气服务器失败"+e.toString());
        }finally {
//            if(conn!= null){
//                conn.dispose();
//                LOGGER.debug("connection dispose");
//            }
            //最后超时或者异常关闭，不能连上了直接关，否则会收不到东西
        }
        return session;
    }

}
