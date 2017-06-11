package utils;

/**
 * @author Anonymous
 *
 * 모드버스 프로토콜의 CRC16 무결성 코드를 추출하기 위한 메소드를 정의하는 클래스
 */
public class Modbus  {

    public Modbus()
    {
        System.out.println("Modbus instanciated.");
    }

    Htypecast htype = new Htypecast() ;

    int[] CRC = new int[256];

    /** **********************************************************************************
     * CRC16 테이블초기화 (modbus RTU 시리얼통신에 사용)
     * 생성자 함수
     * **********************************************************************************/
    int[] table =
            {
                    0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
                    0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
                    0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
                    0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
                    0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
                    0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
                    0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
                    0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
                    0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
                    0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
                    0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
                    0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
                    0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
                    0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
                    0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
                    0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
                    0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
                    0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
                    0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
                    0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
                    0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
                    0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
                    0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
                    0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
                    0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
                    0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
                    0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
                    0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
                    0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
                    0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
                    0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
                    0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
            };

    /************************************************************************************
     * 헥사값 배열의 CRC16 을 구하는 함수
     *
     * 파라메터에 해당하는 값의 CRC16 체크섬값을 만들어 리턴
     * @param   bytes: 프로토콜 6byte (일반적인 파라메터)
     * @return  byte[2]형 16bit CRC값 리턴
     * *********************************************************************************/
    public byte[] fn_makeCRC16(byte[] bytes)
    {
        int icrc = 0xFFFF;
        for (byte b : bytes) {
            icrc = (icrc >>> 8) ^ table[(icrc ^ b) & 0xff];
        }
        //bytes = new byte[bytes.length];
        //System.out.println("Integer " + Integer.toHexString(icrc)); // test
        return htype.fnShortToBytes((short)icrc,0);//HtypeCast의 short를 byte로 변환해주는 함수
        //byte[] results = Integer.toHexString(icrc).getBytes();
        //return results;
    }
    /** *********************************************************************************
     * 헥사값 배열의 CRC16 을 구하는 함수
     *
     * 파라메터에 해당하는 값의 CRC16 체크섬값을 만들어 리턴
     * @param   bytes: 프로토콜 6byte (일반적인 파라메터)
     * @return  short형 16bit CRC값 리턴
     * *********************************************************************************/
    public short fn_makeCRC16toShort(byte[] bytes)
    {
        int icrc = 0xFFFF;
        for (byte b : bytes) {
            icrc = (icrc >>> 8) ^ CRC[(icrc ^ b) & 0xff];
        }

        return (short)icrc;
    }
    /** *********************************************************************************
     * 인자를 이용한 MODBUS 프로토콜 생성함수 (CRC가 있는 시리얼용)
     *
     * @param   id:     장비번호 (모두 십진수)
     *          fnr:    펑션코드
     *          rst:    레지스터 시작주소
     *          rcnt:   레지스터 갯수
     *
     * @return 8byte의 프로토콜 리턴 (CRC16포함)
     * *********************************************************************************/
    public byte[] fn_makePTcom(int id, int fnr, int rst, int rcnt )
    {
        byte[] btmp;
        byte[] bytes = new byte[6];
        byte[] rbyte = new byte[8];

        bytes[0] = (byte) id;   //장치번호
        bytes[1] = (byte) fnr;  //펑션코드

        //if(fnr == 3)    rst = rst - 40001;

        //레지스터 시작주소
        btmp = htype.fnShortToBytes((short)rst,1);
        bytes[2] = btmp[0];
        bytes[3] = btmp[1];

        //가져올 레지스터 갯수
        btmp = htype.fnShortToBytes((short)rcnt,1);
        bytes[4] = btmp[0];
        bytes[5] = btmp[1];

        btmp = fn_makeCRC16(bytes);// CRC구함
        for(int i=0;i<6;i++) rbyte[i] = bytes[i];
        rbyte[6] = btmp[0];
        rbyte[7] = btmp[1];
        //System.out.println("CRC16 = " + Integer.toHexString(btmp[0])); //test
        return rbyte;
    }
    /** *********************************************************************************
     * 인자를 이용한 MODBUS 프로토콜 생성함수 (CRC가 없고 Header가 있는 TCP/IP용)
     *
     * @param   id:     장비번호 (모두 십진수)
     *          fnr:    펑션코드
     *          rst:    레지스터 시작주소
     *          rcnt:   레지스터 갯수
     *
     * @return 12byte의 프로토콜 리턴 (헤더포함)
     * *********************************************************************************/
    public byte[] fn_makePTsock(int id, int fnr, int rst, int rcnt )
    {
        byte[] btmp;
        byte[] rbyte = new byte[12];
        //HEADER
        //카운터
        rbyte[0] = 0x00;
        rbyte[1] = 0x00;
        //프로토콜아이디 0x0000 고정
        rbyte[2] = 0x00;
        rbyte[3] = 0x00;
        //길이
        rbyte[4] = 0x00;
        rbyte[5] = 0x06;
        //
        rbyte[6] = (byte) id;   //유닛번호
        rbyte[7] = (byte) fnr;  //펑션코드

        //if(fnr == 3)    rst = rst - 40001;

        //레지스터 시작주소
        btmp = htype.fnShortToBytes((short)rst,1);
        rbyte[8] = btmp[0];
        rbyte[9] = btmp[1];

        //가져올 레지스터 갯수
        btmp = htype.fnShortToBytes((short)rcnt,1);
        rbyte[10] = btmp[0];
        rbyte[11] = btmp[1];
        return rbyte;
    }
    /** *********************************************************************************
     * [사용예]
     * *********************************************************************************/
    /*public static void main(String[] args) {
        //byte[] bytes = {0x01,0x03,0x01,0x00,0x00,0x17};   //0x04 0x38
        byte[] bytes = {0x01,0x03,0x00,0x64,0x00,0x01};
        Modbus t = new Modbus();


        System.out.println("[유닛번호:1, 펑션코드:3, 레지스터시작:100, 레지스터갯수:6개 일때] ");
        System.out.println("Modbus프로토콜의 헥사코드는 아래와 같다.");
        byte r[] = t.fn_makePTcom(1,3,100,1);
        for(byte b:r) {
            System.out.format("0x%02X ", b);
        }

        byte crc1[] = t.fn_makeCRC16(bytes);
        System.out.println("\n\n[CRC16 예]");
        System.out.format("byte[0] = %02X\n",crc1[0]);
        System.out.format("byte[1] = %02X\n",crc1[1]);
        System.out.println(crc1[1]);

        short crc2 = t.fn_makeCRC16toShort(bytes);
        System.out.format("\nshort = %02X\n",crc2);

    }*/

}