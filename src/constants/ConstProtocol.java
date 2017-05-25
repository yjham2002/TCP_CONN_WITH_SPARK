package constants;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author 함의진
 * @version 1.0
 * @apiNote 소하테크 프로토콜 정의 컨스턴트
 */
public class ConstProtocol {

    public static final CharSequence STX = "ST"; // Start Of Text
    public static final CharSequence ETX = "\r\n"; // End Of Text

    public static final CharSequence COMMAND_SENDING_TERM = "F3"; // 서버에서 TCP/IP 장치로 실시간 데이터 전송 주기를 보낸다는 명령코드
    public static final CharSequence COMMAND_REALTIME = "F4"; // TCP/IP 장치에서 서버로 실시간 데이터를 보낸다는 명령코드
    public static final CharSequence COMMAND_NOTIFY_WRITE = "F5"; // 서버에서 TCP/IP 장치로 데이터를 쓸 것이라는 명령코드
    public static final CharSequence COMMAND_NOTIFY_READ = "F6"; // 서버에서 클라이언트로 데이터를 읽을 것이라는 명령코드
    public static final CharSequence COMMAND_NO_RESPONSE = "F7"; // TCP/IP 장치에서 서버로 재배동의 데이터가 호출이 되지 않는 재배동을 알려주는 명령코드

    public static final CharSequence RESPONSE_WRITE_REQ = "W0"; // 쓰기 요청
    public static final CharSequence RESPONSE_READ_REQ = "R0"; // 읽기 요청
    public static final CharSequence RESPONSE_WRITE_SUCC = "WP"; // 쓰기 성공
    public static final CharSequence RESPONSE_WRITE_FAIL = "WF"; // 쓰기 실패
    public static final CharSequence RESPONSE_READ_SUCC = "RP"; // 읽기 성공
    public static final CharSequence RESPONSE_READ_FAIL = "RF"; // 일기 실패

}
