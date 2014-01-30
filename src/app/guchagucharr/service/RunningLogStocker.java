package app.guchagucharr.service;

import java.util.Vector;

import android.location.Location;
import android.util.SparseArray;

// �����j���O���̃f�[�^�𒙂߂�̃N���X
public class RunningLogStocker {

	private final int MAX_LOCATION_LOG_CNT = 5000;
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
					vLocation.lastElement().getLatitude(),
					vLocation.lastElement().getAltitude(),
					location.getLatitude(),
					location.getAltitude(), result);
			//distance = result[0];
			currentRapData.increaseDistance(result[0]);
			// long time = location.getTime() - vLocation.lastElement().getTime();
			currentRapData.increaseTime(location.getTime() - vLocation.lastElement().getTime());
			currentRapData.addSpeedData(location.getSpeed());
		}
        // Log.v("Speed", String.valueOf(location.getSpeed()));
		if( MAX_LOCATION_LOG_CNT < vLocation.size() )
		{
			vLocation.remove(0);
		}
		
		// �v�f�ǉ�
		vLocation.add(location);
	}		
	public void stop( long time )
	{
		totalStopTime = time;
	}
}
