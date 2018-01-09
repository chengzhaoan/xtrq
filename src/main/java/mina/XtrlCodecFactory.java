package mina;

import ffrq.FfrqBody;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Hextools;

import java.util.Arrays;


//保留包体，去掉包头，进行加解密
public class XtrlCodecFactory  implements ProtocolCodecFactory {

    int FFRL_PKG_HEADER_LEN = 7+4+6+7;

    private final static Logger log = LoggerFactory.getLogger(XtrlCodecFactory.class);
    private ProtocolDecoder decoder;
    private ProtocolEncoder encoder;

    public XtrlCodecFactory() {
        encoder = new XtrlEncoder();
        decoder = new XtrlDecoder();
    }

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }

    private class XtrlEncoder extends ProtocolEncoderAdapter {
        private final  Logger log = LoggerFactory.getLogger(this.getClass());
        public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
            FfrqBody xtrlBody = (FfrqBody)o;

            IoBuffer ioBuffer = IoBuffer.allocate(FFRL_PKG_HEADER_LEN+ xtrlBody.getXtrlbody().length);
            ioBuffer.setAutoExpand(true);
            ioBuffer.put("PKHDGAS".getBytes());
            ioBuffer.put(xtrlBody.getBodylen().getBytes());
            ioBuffer.put(xtrlBody.getCheckCode());
            ioBuffer.put(xtrlBody.getXtrlbody());
            ioBuffer.put("PKEDGAS".getBytes("iso8859-1"));
            ioBuffer.flip();
            protocolEncoderOutput.write(ioBuffer);

            log.debug("向tulip发送收数据\n{}", Hextools.Hexlog(ioBuffer.array(),"GBK"));
        }
    }

    private class XtrlDecoder extends CumulativeProtocolDecoder {
        private final  Logger log = LoggerFactory.getLogger(this.getClass());
        protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {

            log.debug("从 燃气服务器 {}, 收到字节数{} :",ioSession.getRemoteAddress(),ioBuffer.remaining());

            log.debug("HEX DUMP\n{}",Hextools.Hexlog(ioBuffer.array(),"iso8859-1"));

            int strat_position = ioBuffer.position();

            if(ioBuffer.remaining()<11)
                return false;

            byte[] strlen = new byte[4];
            ioBuffer.position(strat_position + 7);
            ioBuffer.get(strlen);

            int bodylen = Integer.parseInt(new String(strlen,"ISO8859-1"));

            if(ioBuffer.remaining() < bodylen + 7){
                ioBuffer.position(strat_position);
                return false;
            }
            byte[] checkCode = new byte[6];

            ioBuffer.get(checkCode);

            byte[] ffrqbody = new byte[bodylen];

            ioBuffer.get(ffrqbody);

            FfrqBody ffrqBody = new FfrqBody();

            ffrqBody.setXtrlbody(ffrqbody);

            if(!Arrays.equals(ffrqBody.getCheckCode(),checkCode)){
                log.error("ffrqBody count {}, and checkCode receive {} is not equal",Arrays.toString(ffrqBody.getCheckCode()),Arrays.toString(checkCode));
            }
            log.debug("通知报文文件名称为\n{}",Hextools.Hexlog(ffrqBody.getXtrlbody(),"GBK"));
            protocolDecoderOutput.write(ffrqBody);
            return  true;
        }
    }



}
