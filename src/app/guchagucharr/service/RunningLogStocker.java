package app.guchagucharr.service;

import java.util.Vector;

import android.location.Location;
import android.util.SparseArray;

// �����j���O���̃f�[�^�𒙂߂�̃N���X
public class RunningLogStocker {

	private final int MAX_LOCATION_LOG_CNT = 72000;	
	// ��ԑ����Ă�0.1�b�����ł������O���擾�ł��Ȃ��͂��Ȃ̂ŁA�ō���2.4���ԕ�
	// �������A���[�^�[�̕���1m�ȏ�͂Ȃ�Ȃ��ƌv������Ȃ����䂪����̂ŁA
	// 0.1�b������������蕨�ɏ���Ă��Ȃ����薳�����Ǝv��
	static long removeMilli( long val )
	{
		return val * 1000;
	}
	// 1970�N����̎���(ms)
	long totalStartTime = 0;
	long totalStopTime = 0;
	double firstCorrectDistance = 0;	// GPS�Ōv������O�̌덷�ƂȂ鋗��
	double totalDistance = 0;	// m
	double totalTime = 0;		// ms
	double totalSpeed = 0;		// m/s(average)
	
	int iRap = 0;	// rap(from0)
	RapData currentRapData = new RapData();
	
	SparseArray<RapData> rapTime = new SparseArray<RapData>();
	Vector<Location> vLocation = new Vector<Location>();
	Location prevLocation = null;
	public RapData getCurrentRapData()
	{
		return currentRapData;
	}

	public RunningLogStocker(long time)
	{
		currentRapData.clear();
		iRap = 0;
		totalStartTime = time;
		currentRapData.setStartTime(time);
	}	
	public void putLocationLog( Location location )
	{
		if( vLocation.isEmpty() )
		{
			// ��̎�=����A����rap1(rap2�ȍ~)
			// ���̎���Speed�ƁA���Ԃŋ������v�Z���AGPS�ł܂��v������Ă��Ȃ��͈͂̋����Ƃ���
			// TODO: ���Ԃ�A���܂萳�m�ł͂Ȃ��̂ŁA�`�����X����������ʂ̂������l��
			long diffTime = location.getTime() - currentRapData.getStartTime();
			firstCorrectDistance = location.getSpeed() * diffTime * removeMilli(diffTime);
			currentRapData.increaseTime(diffTime);
			currentRapData.increaseDistance(firstCorrectDistance);
		}
		else
		{
			// �����ŁA���̃��b�v�̊e�l�ɂ��Čv�Z���邪�A�p�t�H�[�}���X���l�����āA
			// �Ȃ�ׂ��ߋ��̒l�͌��Ȃ��Ă������悤�ɂ���
			float[] result = new float[3];
			//float distance = 0;
			Location.distanceBetween(
					prevLocation.getLatitude(),//vLocation.lastElement().getLatitude(),
					prevLocation.getAltitude(),//vLocation.lastElement().getAltitude(),
					location.getLatitude(),
					location.getAltitude(), result);
			//distance = result[0];
			currentRapData.increaseDistance(result[0]);
			// long time = location.getTime() - vLocation.lastElement().getTime();
			currentRapData.increaseTime(location.getTime() - prevLocation.getTime());//vLocation.lastElement().getTime());
			currentRapData.addSpeedData(location.getSpeed());
		}
        // Log.v("Speed", String.valueOf(location.getSpeed()));
		if( MAX_LOCATION_LOG_CNT < vLocation.size() )
		{
			// �}�b�N�X�l�𒴂�����A�^�񒆂�ւ񂩂甲���Ă���
			vLocation.remove(MAX_LOCATION_LOG_CNT/2);
		}
		// �v�f�ǉ�
		vLocation.add(location);
		prevLocation = new Location(location);
	}
	public void nextRap(Long time)
	{
		iRap++;
		currentRapData.clear();
		prevLocation = new Location( vLocation.lastElement() );
		// ���Ԃ�������������
		prevLocation.setTime(time);
	}
	public void stop( long time )
	{
		totalStopTime = time;
	}
	
	/**
	 * �擾���ꂽ���O�f�[�^�̕ۑ�
	 * @return
	 */
	public boolean save()
	{
		boolean bRet = false;
		
		// gpx�f�[�^�ւ̕ϊ��A�ۑ�
		
		// database�ւ̕ۑ�
		
		
		return bRet;
	}
}
