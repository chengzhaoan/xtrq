import ffrq.DES;
import mina.TulipCodecFactory;
import mina.TulipHander;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SocketUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
//        try {
//            byte[] key = "11111111".getBytes();
//            byte[] iv = "22222222".getBytes();
//            byte[] data = DES.encrypt("ebc mode test".getBytes(), key);
//            System.out.print("EBC mode:");
//            System.out.println(new String(DES.decrypt(data, key)));
//            System.out.print("CBC mode:");
//            data = DES.CBCEncrypt("cbc mode test".getBytes(), key, iv);
//            System.out.println(new String(DES.CBCDecrypt(data, key, iv)));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        LoadConfig();

        //开启监听
        NioSocketAcceptor acceptor_tlp = new NioSocketAcceptor();
        DefaultIoFilterChainBuilder chain = acceptor_tlp.getFilterChain();

        MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter();
        chain.addLast("mdc", mdcInjectionFilter);
        chain.addLast("codec", new ProtocolCodecFilter(new TulipCodecFactory()));
        chain.addLast("logger", new LoggingFilter());
        logger.info("Logging ON");
        acceptor_tlp.setHandler(new TulipHander());//
        acceptor_tlp.bind(new InetSocketAddress(SocketUtils.LISTEN_IP,SocketUtils.LISTEN_PORT));

    }

    public static void LoadConfig() throws IOException {

        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream("C:\\Users\\chengzhaoan\\workspace\\xtrq\\src\\main\\resources\\config.properties");

        defaultProps.load(in);
//        iv=!@#hd#@!
//        key=!@#hd#@!
//        tulipip=10.63.64.8
//        tulipport=10000
//        thirdip=15000
//        thirdport=8000

        DES.iv = defaultProps.getProperty("iv").getBytes();
        DES.key =defaultProps.getProperty("key").getBytes();
        String listenip =  defaultProps.getProperty("listenip");
        System.out.println("listenip ip ="+listenip);
        String listenport = defaultProps.getProperty("listenport");
        System.out.println("listenport port ="+listenport);
        String thirdip =  defaultProps.getProperty("thirdip");
        System.out.println("third ip ="+thirdip);

        String thirdport = defaultProps.getProperty("thirdport");
        System.out.println("third port="+thirdport);

        in.close();
        SocketUtils.LISTEN_IP = listenip;
        SocketUtils.LISTEN_PORT = Integer.parseInt(listenport);

        SocketUtils.THIRD_IP = thirdip;
        SocketUtils.THIRD_PORT =Integer.parseInt(thirdport);


    }
}
