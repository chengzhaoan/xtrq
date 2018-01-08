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
            for(int conn_count = 0 ;conn_count < 4;conn_count++){
                connectFuture = conn.connect(addr);
                connectFuture.awaitUninterruptibly();
                if(connectFuture.isConnected())
                    session= connectFuture.getSession();
                    session.setAttribute(CONNECTER,conn);
                    //conHashMap.put(session.getId(),conn);//注意这个bug的处理
                    break;
            }
            LOGGER.debug("链接成功{},{}",session.getId(),session.getRemoteAddress());
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.warn("链接房管局段失败"+e.toString());
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
