package com.example.research;

import java.util.ArrayList;
import java.util.Vector;

import net.sf.javaml.classification.Classifier;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class MyResultReceiver extends ResultReceiver {

	private Vector<String> last11;
	private Vector<Classifier> SVMs;
	private SVMMethods methodObject;

	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	public MyResultReceiver(Handler handler) {
		super(handler);
		last11 = new Vector<String>();
		SVMs = new Vector<Classifier>();
		methodObject = new SVMMethods();
		// TODO Auto-generated constructor stub
	}

	private MyResultReceiver mReceiver;

	public void setReceiver(MyResultReceiver receiver) {
		mReceiver = receiver;
	}

	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (mReceiver != null) {
			
			Vector temp = new Vector<String>(resultData.getStringArrayList("results"));  
			//CLASSIFY IF 1, no training here, the vector here should contain 1 element
            //at index 0, the most recent reading
			if (resultCode == 1) {
			    Instance toClassify = makeInstance(last11, temp.get(0));
                //I'm not sure what you want to do with these values, 
                //they are either true or false, an alert would happen if one returned true
                Object classificationHigh = methodObject.classify(SVMs.get(0));
                Object classificationLow = methodObject.classify(SVMs.get(1));
                //increment the last11
                for(int i=11; i>=0; --i)
                {
                    last11.get(i)=last11.get(i-1);
                }
                last11.get(0)=temp.get(0);
			}
			//TRAIN / RETRAIN if 2
			else if (resultCode == 2) {
				Vector<Dataset> newDatasets = methodObject.produceDataSets(temp, 80, 160);
			    SVMs = methodObject.trainSVM(newDatasets.get(0), newDatasets.get(1));	
			}

		}
	}
}
