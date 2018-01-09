package mina;

import ffrq.FfrqBody;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Hextools;
//4字节长度 + 包体内容
public class TulipCodecFactory implements ProtocolCodecFactory {
    private final static Logger log = LoggerFactory.getLogger(XtrlCodecFactory.class);
    private ProtocolDecoder decoder;
    private ProtocolEncoder encoder;

    public TulipCodecFactory() {
        encoder = new TulipEncoder();
        decoder = new TulipDecoder();
    }

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
    private class TulipEncoder extends ProtocolEncoderAdapter {
        private final  Logger log = LoggerFactory.getLogger(this.getClass());
        public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {

            FfrqBody xtrlBody = (FfrqBody)o;
            IoBuffer ioBuffer = IoBuffer.allocate(4 + xtrlBody.getXtrlbody().length);
            ioBuffer.setAutoExpand(true);
            ioBuffer.put(xtrlBody.getBodylen().getBytes());
            ioBuffer.put(xtrlBody.getXtrlbody());
            protocolEncoderOutput.write(ioBuffer);

            log.debug("向tulip发送收数据\n{}", Hextools.Hexlog(ioBuffer.array(),"GBK"));
        }
    }
    private class TulipDecoder extends CumulativeProtocolDecoder {

        private final  Logger log = LoggerFactory.getLogger(this.getClass());

        protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
            log.debug("从TULIP 服务器 {}, 收到字节数{}, :",ioSession.getRemoteAddress(),ioBuffer.remaining());


            log.debug("收到字节\n{}",ioBuffer.getHexDump());

            int start_position = ioBuffer.position();

            if(ioBuffer.remaining()<4)
                return false;

            byte[] strlen = new byte[4];

            ioBuffer.get(strlen);

            int bodylen = Integer.parseInt(new String(strlen,"ISO8859-1"));

            log.debug("bodylen = {}" , bodylen);

            if(ioBuffer.remaining() < bodylen  ){
                ioBuffer.position(start_position);
                return false;
            }

            byte[] ffrqbody = new byte[bodylen];

            ioBuffer.get(ffrqbody);

            FfrqBody ffrqBody = new FfrqBody();
            ffrqBody.setXtrlbody(ffrqbody);

            log.debug("收到的峰峰燃气报文体为\n{}",Hextools.Hexlog(ffrqBody.getXtrlbody(),"GBK"));

            protocolDecoderOutput.write(ffrqBody);

            return  true;
        }
    }
}
