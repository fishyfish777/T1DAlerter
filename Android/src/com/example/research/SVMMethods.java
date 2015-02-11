package com.example.research;
import java.io.*;
import java.util.*;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.classification.Classifier;
import libsvm.LibSVM;
import net.sf.javaml.classification.evaluation.EvaluateDataset;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.classification.AbstractClassifier;
class SVMMethods
{
    public SVMMethods()
    {
    }

    public DexComReading produceReading(String readStr)
    {
        DexComReading garbage = new DexComReading(0, "empty", 0);
        int position = readStr.indexOf("dateString");
        int sgvVal = garbage.strToSgvVal(readStr, position);
        String slope = garbage.strToDirection(readStr, position);
        try
        {
            int time = garbage.strToTime(readStr, position);
            DexComReading reading = new DexComReading(sgvVal, slope, time);
            return reading;
        }
        catch(NumberFormatException e)
        {
            System.out.println("date format is different");
        }
        System.out.println("should never get here");
        System.exit(1);
        return garbage;
    }

    public Vector<Dataset> produceDataSets(Vector<String> all, int tooHigh, int tooLow)
    {
        DexComReading garbage = new DexComReading(0,"empty", 0);
        Vector<DexComReading> dexReadings = new Vector<DexComReading>();
        for(String curString : all)
        {
            DexComReading newRead = produceReading(curString);
            dexReadings.add(newRead);
        }
        Vector<Boolean> dangerListHigh = new Vector<Boolean>();
        Vector<Boolean> dangerListLow = new Vector<Boolean>(); 
        for(int i=0; i<12; ++i)
        {
            dangerListHigh.add(false);
            dangerListLow.add(false);
        }
        for(int i=12; i<dexReadings.size()-6; ++i)
        { 
            if(dexReadings.get(i+6).getSgv()>tooHigh)
            {
                dangerListHigh.add(true);
                dangerListLow.add(false);
            }
            else if(dexReadings.get(i+6).getSgv()>tooLow)
            {
                dangerListHigh.add(false);
                dangerListLow.add(false);
            }
            else if(dexReadings.get(i+6).getSgv()<tooLow)
            {
                dangerListLow.add(true);
                dangerListHigh.add(false);
            }
        }
        double [][] sets13 = new double[dangerListHigh.size()][13];
        for(int i=12; i<dexReadings.size()-6;++i)
        {
            double [] set13 = new double[13];
            for(int j=0; j<12; ++j)
            {
                set13[11-j]=dexReadings.get(i-j).getDoubleSgv();
            }
            set13[12]=dexReadings.get(i).getTime();
            sets13[i-12]=set13;
        }

        Dataset dataHigh = new DefaultDataset();
        Dataset dataLow = new DefaultDataset();
        for(int i=0; i<sets13.length; ++i)
        {
            Instance instanceWClassValueHigh=new DenseInstance(sets13[i], 
                    dangerListHigh.get(i));
            Instance instanceWClassValueLow = new DenseInstance(sets13[i], 
                    dangerListLow.get(i));
            dataHigh.add(instanceWClassValueHigh);
            dataLow.add(instanceWClassValueLow);
        }
        Vector<Dataset> dataSets = new Vector<Dataset>();
        dataSets.add(dataHigh);
        dataSets.add(dataLow);
        return dataSets;
    }
    public Vector<Classifier> trainSVM(Dataset dataHigh, Dataset dataLow)
    {
        Classifier svmHigh = new LibSVM();
        svmHigh.buildClassifier(dataHigh);
        Classifier svmLow = new LibSVM();
        svmLow.buildClassifier(dataLow);
        
        Vector<Classifier> toReturn = new Vector<Classifier>();
        toReturn.add(svmHigh);
        toReturn.add(svmLow);
        return toReturn;
    }
    public void testSVMs(Vector<Classifier> SVMsToTest, Dataset dataHigh, Dataset dataLow)
    { 
        Map<Object, PerformanceMeasure> pmHigh = EvaluateDataset.testDataset(SVMsToTest.get(0),
                dataHigh);
        Map<Object, PerformanceMeasure> pmLow = EvaluateDataset.testDataset(SVMsToTest.get(1), 
                dataLow);
        for(Object o : pmHigh.keySet())
        {
            System.out.println(o + ": " + pmHigh.get(o).tp + " "+ pmHigh.get(o).fp);
            System.out.println("f measure: "+ pmHigh.get(o).getFMeasure());
        }
        for(Object o: pmLow.keySet())
        { 
            System.out.println(o + ": " + pmLow.get(o).tp + " "+ pmLow.get(o).fp);   
            System.out.println("f measure: "+ pmLow.get(o).getFMeasure());
        }
    }

    //!!THE LAST 11 should be in order from 5 minutes back to 55 minutes back
    Instance makeInstance(Vector<String> last11, String mostRecent)
    {
        double [] set13 = new double[13];
        for(int i=0; i<11; --i)
        {
            dexComReading tempReading = produceReading(last11.get(i));
            set13[i+1]=tempReading.getDoubleSgv();
        }
        dexComReading tempReading = produceReading(mostRecent);
        set13[0]=tempReading.getDoubleSgv(); 
        set13[12]=tempReading.getTime();

    }

    public Object classify(Classifier svm, Instance aRead)
    {
        return svm.classify(aRead);
    } 
}
