package mina;

import ffrq.FfrqBody;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Hextools;
import utils.SocketUtils;

import java.net.InetSocketAddress;


public class TulipHander extends IoHandlerAdapter {

    private static Logger LOGGER  =  LoggerFactory.getLogger(FfrqHander.class);

    public void sessionCreated(IoSession session) throws Exception {

        LOGGER.debug("TULIP sessionCreated {}"+session.getId());
    }

    public void sessionOpened(IoSession session) throws Exception {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, SocketUtils.MAX_TIMEOUT);
        LOGGER.debug("TULIP sessionOpened {}"+session.getId());
    }

    public void sessionClosed(IoSession session) throws Exception {
        LOGGER.debug("TULIP sessionClosed {}"+session.getId());
        NioSocketConnector conn = (NioSocketConnector)session.getAttribute(SocketUtils.CONNECTER);
        if(conn != null && conn.isActive()){
            conn.dispose();
            LOGGER.debug("TULIP connection is closed");
        }
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        LOGGER.debug("TULIP sessionIdle {}"+session.getId());
        session.closeNow();
    }

    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("EXCEPTION, please implement " + this.getClass().getName() + ".exceptionCaught() for proper handling:", cause);
        }
        NioSocketConnector conn =(NioSocketConnector) session.getAttribute(SocketUtils.CONNECTER);
        if( conn != null && conn.isActive()){//tulip session里应该不用写这这个代码吧
            conn.dispose();
            LOGGER.debug("TULIP connection is closed");
        }
        session.closeNow();
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        LOGGER.debug("TULIP messageReceived {}"+session.getId());
        LOGGER.info("从Tulip收到如下消息：");
        LOGGER.info("从如下地址收到消息  :"+session.getRemoteAddress());

        FfrqBody ffrqBody = (FfrqBody)message;

        IoSession thirdSession = SocketUtils.conn(new InetSocketAddress(SocketUtils.THIRD_IP,SocketUtils.THIRD_PORT),new FfrqHander(),new XtrlCodecFactory());

        if(thirdSession== null){
            LOGGER.error("得不到通向峰峰燃气费终端的链接");
            return;
        }
        thirdSession.setAttribute(SocketUtils.TLP_SESSION,session);
        LOGGER.debug(ffrqBody.toString());
        ffrqBody.CBCEncrypt();
        LOGGER.info("加密完成后  :"+ Hextools.Hexlog(ffrqBody.getXtrlbody(),"iso8859-1"));
        thirdSession.write(ffrqBody);

    }

    public void messageSent(IoSession session, Object message) throws Exception {
        LOGGER.info("TULIP messageSent {}",session.getId());
    }

}
