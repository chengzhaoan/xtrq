package ffrq;

import utils.Hextools;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

//峰峰燃气报文体
public class FfrqBody {

    byte[]  xtrqbody  = null;

    byte[]  checkCode = null;

    public void setXtrlbody(byte[] xtrlbody) {
        this.xtrqbody = xtrlbody;
    }


    public byte[] getXtrlbody(){
        return xtrqbody;
    }

    public  String getBodylen(){
        return  String.format("%04d",xtrqbody.length);
    }
    //得到校验码
    public byte[] checkcCode(){
        return  checkCode;
    }

    public byte[] getCheckCode() {

        int checkcode = 0;

        for(int i =0 ;i< xtrqbody.length;i++){
            int tmp =0xff &((int)xtrqbody[i]);
            checkcode += tmp;
        }
        this.checkCode = String.format("%06d",checkcode).getBytes();
        return this.checkCode;
    }

    //加密
    public void  CBCEncrypt(){

        //不足8的倍数的补齐
        if(xtrqbody.length % 8 != 0){
            int block = xtrqbody.length / 8 ;
            block++;
            byte[] tmp = new byte[block*8];
            System.arraycopy(xtrqbody,0,tmp,0,xtrqbody.length);
            xtrqbody = tmp;
        }

        byte[] enxtrqbody = DES.CBCEncrypt(xtrqbody, DES.key, DES.iv);
        xtrqbody = enxtrqbody;

    }


    //解密
    public void CBCDecrypt(){

        //不足8的倍数的补齐
        if(xtrqbody.length % 8 != 0){
            int block = xtrqbody.length / 8 ;
            block++;
            byte[] tmp = new byte[block*8];
            System.arraycopy(xtrqbody,0,tmp,0,xtrqbody.length);
            xtrqbody = tmp;
        }

        byte[] dextrqbody =  DES.CBCDecrypt(xtrqbody ,DES.key, DES.iv);
        xtrqbody = dextrqbody;
    }

    @Override
    public String toString() {

        String str = null;

        if( xtrqbody != null){
            str = "FfrqBody{" +
                    "xtrqbody=" + Hextools.Hexlog(xtrqbody,"GBK");
        }
        if(checkCode != null){
            str += " checkCode = "+new String(checkCode)+"}";
        }

        return str;
    }
}
