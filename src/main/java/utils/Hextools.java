package utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Hextools {

    public static String DEFAULT_CHARSET ="GBK";

    public static String Hexlog(byte[] buf,String charsetName){

        if(charsetName==""){
            charsetName=DEFAULT_CHARSET;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);

        int lines = buf.length % 16== 0 ? buf.length/16 : buf.length/16 + 1;
        int fulllines = buf.length/16;

        byte[] temp = new byte[16];

        String title = String.format("%4d : %02X %02X %02X %02X %02X %02X %02X %02X    %02X %02X %02X %02X %02X %02X %02X %02X : %s\n",0,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,"123456789ABCDEF");

        StringBuffer sb = new StringBuffer();
        sb.append(String.format("本次DUMP数据【%10d】字节\n",buf.length));
        sb.append(title);
        try{

        for(int i = 0 ;i< fulllines ;i++ ) {
            byteBuffer.get(temp);
            sb.append(String.format("%04d : %02X %02X %02X %02X %02X %02X %02X %02X    %02X %02X %02X %02X %02X %02X %02X %02X :",i,temp[0],temp[1],temp[2],temp[3],temp[4],temp[5],temp[6],temp[7],temp[8],temp[9],temp[10],temp[11],temp[12],temp[13],temp[14],temp[15]));

            sb.append(String.format("%s\n",new String(temp,charsetName)));
            String str = new String(temp,charsetName);

        }
        if(lines!= fulllines){
            byteBuffer.get(temp,0,buf.length % 16);
            sb.append(String.format("%04d :",lines));
            for(int i = 0 ;i<buf.length % 16;i++){
                sb.append(String.format(" %02X",temp[i]));
                if(i==7)
                    sb.append("   ");
            }
            for(int i=buf.length % 16;i<16;i++){
                sb.append("   ");
            }
            sb.append(" : ");

            sb.append(String.format("%s\n",new String(temp,0,buf.length % 16,"GBK")));


        }
        }catch (UnsupportedEncodingException e){
             System.out.println("不支持的字符类型"+charsetName);
        }
        return sb.toString();

    }

    public static String dumpMemory(byte [] buf){
        StringBuffer dump = new StringBuffer();

        int lines = buf.length % 8 == 0 ? buf.length/8 : buf.length/8 + 1;
        int j = 0;
        int k = 0;
        int i = 0;
        byte c ;
        int WID = 16;
        //System.out.println("data_length" + buf.length);
        while(j*WID < buf.length){
            dump.append(String.format("%04X: ", j*WID));
            //System.out.printf(" %04X: ", j*WID);
            for(i = 0; i < WID; i++){
                if((i + j * WID)>=buf.length) break;
                c = buf[i + j * WID];
                dump.append(String.format("%02X ",c));
                //System.out.printf("%02X ",c);
                if((i+1) % 8 == 0) //System.out.print(" ");
                    dump.append(String.format(" "));
            }
            for(k=i; k<WID; k++){
                System.out.printf(" ");
                if((k+1) % 8 == 0)
                    //System.out.printf(" ");
                    dump.append(String.format(" "));
            }
            System.out.printf(" ");
            for(i=0; i<WID; i++){
                if((i+j*WID) >= buf.length) break;
                c = buf[i+j*WID];
                if(c >= 0x30 && c <= 0x7a){
                    dump.append(String.format("%c",c));
                    //System.out.printf("%c",c);
                }else{
                    dump.append(String.format("%c",'.'));
                    //System.out.printf("%c",'.');
                }
            }
            dump.append(String.format("\n"));
            //System.out.printf("\n");
            j++;
        }

        return dump.toString();
    }

        /**
         * Convert hex string to byte[]
       * @param hexString the hex string
       * @return byte[]
          */
            public static byte[] hexStringToBytes(String hexString) {
             if (hexString == null || hexString.equals("")) {
                        return null;
             }

            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
             char[] hexChars = hexString.toCharArray();
             byte[] d = new byte[length];
             for (int i = 0; i < length; i++) {
                  int pos = i * 2;
                  d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
              }
              return d;
          }

    /**
        * Convert char to byte
        * @param c char
        * @return byte
    */

          private static byte charToByte(char c) {
              return (byte) "0123456789ABCDEF".indexOf(c);
          }

}
