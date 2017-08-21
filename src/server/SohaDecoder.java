package server;

import constants.ConstProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import models.ByteSerial;
import utils.HexUtil;
import utils.SohaProtocolUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 채팅 프로토콜의 기본이 되는 공통적인 부분을 Decoding 한다.
 * out 에 add 되지 전까지 decode 메쏘드가 계속적으로 호출된다.(From 이용자 가이드 4.0)
 * @author Administrator
 *
 */
public class SohaDecoder extends ByteToMessageDecoder {

    private SohaDecoder server = null ;

    // 바디 길이
    private int blen = 0 ;

    public SohaDecoder(){}

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        try {
            final int HEADER_PREFIX_LEN = 2 ; // ST
            final int HEADER_PREFIX_BODYLEN_LEN = 4 ; // 바디 길이값(4byte)

            // S , T , 4 바이트
            if( in.readableBytes() < HEADER_PREFIX_LEN + HEADER_PREFIX_BODYLEN_LEN + this.blen ) return ;

            in.markReaderIndex() ;

//            in.readShort() ;     // ST
            byte ch1 = in.readByte();
            byte ch2;

            if(ch1 == 0x53){
                ch2 = in.readByte();
            }else{
                return;
            }

            ByteBuf bbLen = in.readBytes(HEADER_PREFIX_BODYLEN_LEN) ;
            byte[] bytes = new byte[HEADER_PREFIX_BODYLEN_LEN];
            bbLen.readBytes(bytes);

            blen = Integer.parseInt(HexUtil.getNumericStringFromAscii(bytes));

            if( in.readableBytes() < blen ) {
                in.resetReaderIndex() ;
                return ;
            }else {
                in.resetReaderIndex();
                ByteBuf byteBuf = in.readBytes(HEADER_PREFIX_LEN + HEADER_PREFIX_BODYLEN_LEN + blen);
                out.add(byteBuf);
            }

            blen = 0 ;

        }
        catch(Exception e) {
            e.printStackTrace() ;
        }
        finally {}
    }

}