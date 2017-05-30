package utils;


public class Htypecast {
	{
		System.out.println("HtypeCast 인스턴스가 생성되었습니다.");
	}

	short Value;

	byte [] temp = new byte[2];
	int idx;
	public byte[] fnShortToBytes(short Value, int Order) {
		temp [0]=  (byte)((Value & 0xFF00) >> 8);
		temp [1]= (byte)(Value & 0x00FF);
		temp = ChangeByteOrder(temp,Order);
		return temp;
	}

	/**
	 * 상위, 하위 변환 (내부적으로 사용하는 함수)
	 * */
	private byte[] ChangeByteOrder(byte[] value,int Order) {
		idx = value.length;
		byte[] Temp  = new byte [idx];
		//BIG_EDIAN
		if(Order == 1) {
			Temp = value;
		}
		//Little_EDIAN
		else if(Order == 0) {
			for(int i=0;i<idx;i++) {
				Temp[i] = value[idx-(i+1)];
			}
		}
		return Temp;
	}

}
