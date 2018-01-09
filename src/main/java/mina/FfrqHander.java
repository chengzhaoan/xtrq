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

public class FfrqHander extends IoHandlerAdapter {

    private static Logger LOGGER  =  LoggerFactory.getLogger(FfrqHander.class);

    public void sessionCreated(IoSession session) throws Exception {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, SocketUtils.MAX_TIMEOUT);
        LOGGER.debug("sessionCreated {}"+session.getId());
    }

    public void sessionOpened(IoSession session) throws Exception {
        LOGGER.debug("sessionOpened {}"+session.getId());
    }

    public void sessionClosed(IoSession session) throws Exception {
        LOGGER.debug("sessionClosed {}"+session.getId());
        NioSocketConnector conn = (NioSocketConnector)session.getAttribute("connection");
        if(conn != null && conn.isActive()){
            conn.dispose();
            LOGGER.debug("connection is closed");
        }
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        LOGGER.debug("sessionIdle {}"+session.getId());
        session.closeOnFlush();
    }

    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("EXCEPTION, please implement " + this.getClass().getName() + ".exceptionCaught() for proper handling:", cause);
        }
        NioSocketConnector conn =(NioSocketConnector) session.getAttribute(SocketUtils.CONNECTER);
        if( conn != null && conn.isActive()){
            conn.dispose();
            LOGGER.debug("connection is closed");
        }
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        LOGGER.info("从预售房系统收到消息：");
        LOGGER.info("从如下地址收到消息  :"+session.getRemoteAddress());

        FfrqBody ffrqBody = (FfrqBody) message;

        IoSession tlp_session = (IoSession)session.getAttribute(SocketUtils.TLP_SESSION);


        ffrqBody.CBCDecrypt();
        LOGGER.debug(ffrqBody.toString());
        tlp_session.write(ffrqBody);

        tlp_session.closeOnFlush();

        session.closeOnFlush();

        LOGGER.debug("打印返回tulip的字节序列\n{}", Hextools.Hexlog( ffrqBody.getXtrlbody() ,"GBK"));
    }

    public void messageSent(IoSession session, Object message) throws Exception {
        LOGGER.warn("消息发往峰峰燃气服务器");
    }

    public void inputClosed(IoSession session) throws Exception {
        NioSocketConnector conn = (NioSocketConnector)session.getAttribute(SocketUtils.CONNECTER);
        if(conn!=null)
            conn.dispose();
        session.closeNow();
    }


}
