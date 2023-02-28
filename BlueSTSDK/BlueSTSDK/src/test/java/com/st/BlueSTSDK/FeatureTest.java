/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/

package com.st.BlueSTSDK;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Features.Field;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FeatureTest {

    class EmptyFeatureTest extends Feature{

        public EmptyFeatureTest() {
            this("",null,null);
        }

        public EmptyFeatureTest(String name, Node n, Field dataDesc[]){
            super(name, n, dataDesc);
        }

        @Override
        protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {

            return new ExtractResult(new Sample(timestamp, new Number[]{data[dataOffset]}, new Field[]{}),1);
        }

        public int getFeatureListenerSize(){
            return mFeatureListener.size();
        }

        public List<FeatureListener> getFeatureListener(){
            return mFeatureListener;
        }

        public void execAllTask(){
            try {
                sThreadPool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private static final String FEATURE_NAME = "Name";

    @Test
    public void getName(){
        EmptyFeatureTest feature = new EmptyFeatureTest(FEATURE_NAME,null,null);
        Assert.assertEquals(FEATURE_NAME,feature.getName());
    }

    @Test
    public void getNode(){
        Node n = mock(Node.class);
        EmptyFeatureTest feature = new EmptyFeatureTest(FEATURE_NAME,n,null);
        Assert.assertEquals(n,feature.getParentNode());
    }

    @Test
    public void setEnable(){
        EmptyFeatureTest feature = new EmptyFeatureTest(FEATURE_NAME,null,null);
        Assert.assertEquals(false, feature.isEnabled());

        feature.setEnable(true);
        Assert.assertEquals(true,feature.isEnabled());
    }

    @Test
    public void insertFeatureListener() throws Exception {
        Feature.FeatureListener listener1 = new Feature.FeatureListener() {
            @Override
            public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample s) {

            }
        };

        Feature.FeatureListener listener2 = new Feature.FeatureListener() {
            @Override
            public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample s) {

            }
        };

        EmptyFeatureTest f = new EmptyFeatureTest();
        f.addFeatureListener(listener1);
        Assert.assertTrue(f.getFeatureListener().contains(listener1));
        //avoid to insert 2 time the same listener
        f.addFeatureListener(listener1);
        Assert.assertEquals(1, f.getFeatureListenerSize());

        f.addFeatureListener(listener2);
        Assert.assertTrue(f.getFeatureListener().contains(listener2));
        Assert.assertEquals(2, f.getFeatureListenerSize());

        f.addFeatureListener(null);
        Assert.assertEquals(2, f.getFeatureListenerSize());


    }

    @Test
    public  void removeFeatureListener(){
        Feature.FeatureListener listener1 = new Feature.FeatureListener() {
            @Override
            public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample s) {

            }
        };

        Feature.FeatureListener listener2 = new Feature.FeatureListener() {
            @Override
            public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample s) {

            }
        };

        EmptyFeatureTest f = new EmptyFeatureTest();
        f.addFeatureListener(listener1);
        f.addFeatureListener(listener2);

        f.removeFeatureListener(listener2);
        Assert.assertFalse(f.getFeatureListener().contains(listener2));
        Assert.assertEquals(1, f.getFeatureListenerSize());

        f.removeFeatureListener(listener1);
        Assert.assertFalse(f.getFeatureListener().contains(listener1));
        Assert.assertEquals(0,f.getFeatureListenerSize());
    }

    @Test
    public void notifyChange(){

        Feature.FeatureListener listener = mock(Feature.FeatureListener.class);
        EmptyFeatureTest f = new EmptyFeatureTest();
        f.addFeatureListener(listener);

        int timestamp = 1000;

        byte[] data = new byte[]{10,4};

        f.update(timestamp, data, 0);
        f.execAllTask();
        verify(listener).onUpdate(f, new Feature.Sample(timestamp, new Number[]{data[0]}, new Field[]{}));
    }

    @Test
    public void getSample(){

        EmptyFeatureTest f = new EmptyFeatureTest();

        final int timestamp = 1000;
        final byte[] data = new byte[]{10,4};

        f.update(timestamp, data, 0);

        Assert.assertEquals(new Feature.Sample(timestamp, new Number[]{data[0]}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void getSampleNoChange(){

        EmptyFeatureTest f = new EmptyFeatureTest();

        final int timestamp = 1000;
        final byte[] data = new byte[]{10,4};

        f.update(timestamp, data, 0);

        Feature.Sample s = f.getSample();

        Assert.assertEquals(new Feature.Sample(timestamp, new Number[]{data[0]}, new Field[]{}),
                s);

        s.data[0]=127-s.data[0].byteValue();

        Assert.assertNotEquals(s, f.getSample());
    }

    @Test
    public void getLastUpdate(){
        final long UPDATE_TIME_DIFF=1000; //1s

        EmptyFeatureTest f = new EmptyFeatureTest();

        int timestamp = 1000;
        byte[] data = new byte[]{10,4};


        Date now = new Date();
        f.update(timestamp, data, 0);
        Date updateTime = f.getLastUpdate();

        long diff = Math.abs(now.getTime()-updateTime.getTime());

        Assert.assertTrue(diff < UPDATE_TIME_DIFF);
    }


    @Test
    public void sampleEquals(){

        Feature.Sample s1 = new Feature.Sample(100,new Number[]{1,2,3,4}, new Field[]{});
        Assert.assertEquals(s1, s1);
        Feature.Sample s2 = new Feature.Sample(100,new Number[]{1,2,3,4}, new Field[]{});
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void sampleEqualsTimestamp(){

        Feature.Sample s1 = new Feature.Sample(100,new Number[]{1,2,3,4}, new Field[]{});
        Feature.Sample s2 = new Feature.Sample(101,new Number[]{1,2,3,4}, new Field[]{});
        Assert.assertNotEquals(s1, s2);
    }

    @Test
    public void sampleEqualsData(){

        Feature.Sample s1 = new Feature.Sample(100,new Number[]{1,2,3,4}, new Field[]{});
        Feature.Sample s2 = new Feature.Sample(100,new Number[]{1,-2,3,4}, new Field[]{});
        Assert.assertNotEquals(s1,s2);
    }

    @Test
    public void sampleEqualsDataLength(){

        Feature.Sample s1 = new Feature.Sample(100,new Number[]{1,2,3,4}, new Field[]{});
        Feature.Sample s2 = new Feature.Sample(100,new Number[]{1,2,3}, new Field[]{});
        Assert.assertNotEquals(s1,s2);
    }

    @Test
    public void sampleEqualsDiffObj(){

        Feature.Sample s1 = new Feature.Sample(100,new Number[]{1,2,3,4}, new Field[]{});
        Assert.assertNotEquals(s1,new Object());
    }



}