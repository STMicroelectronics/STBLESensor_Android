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

import android.bluetooth.BluetoothAdapter;
import android.util.SparseArray;

import com.st.BlueSTSDK.TestUtil.TestUtil;
import com.st.BlueSTSDK.Utils.advertise.InvalidBleAdvertiseFormat;
import com.st.BlueSTSDK.Utils.InvalidFeatureBitMaskException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBluetoothAdapter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,manifest = "src/main/AndroidManifest.xml", sdk = 23)
public class ManagerTest {

    private static int NOTIFICATION_TIMEOUT_MS = 200;

    private ShadowBluetoothAdapter mBtAdapter;
    private Manager mManager;


    @Before
    public void setUp() {
        mBtAdapter = Shadows.shadowOf(BluetoothAdapter.getDefaultAdapter());
        mManager = Manager.getSharedInstance();
        mBtAdapter.setEnabled(true);
    }

    @After
    public void tearDown() {
        mManager.resetDiscovery();
        Class manager = mManager.getClass();
        java.lang.reflect.Field instance = null;
        try {
            instance = manager.getDeclaredField("sInstance");
            instance.setAccessible(true);
            //set to null for force the re creation of the object
            instance.set(null, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private int nBuildNode=0;
    private Node buildMockNode() {
        Node n = mock(Node.class);
        when(n.isBounded()).thenReturn(false);
        when(n.getTag()).thenReturn(""+nBuildNode++);
        return n;
    }

    @Test
    public void testBleAdapterEnabled() {
        mBtAdapter.setEnabled(false);
        Assert.assertEquals(false, mManager.startDiscovery());
        Assert.assertEquals(false, mManager.stopDiscovery());

        mBtAdapter.setEnabled(true);
        Assert.assertEquals(true, mManager.startDiscovery());
        Assert.assertEquals(true, mManager.stopDiscovery());

    }

    @Test
    public void testStatusListener() {

        Manager.ManagerListener listener = mock(Manager.ManagerListener.class);

        mManager.addListener(listener);

        Assert.assertEquals(true, mManager.startDiscovery());

        verify(listener,timeout(NOTIFICATION_TIMEOUT_MS)).onDiscoveryChange(mManager, true);

        Assert.assertEquals(true, mManager.stopDiscovery());

        verify(listener,timeout(NOTIFICATION_TIMEOUT_MS)).onDiscoveryChange(mManager, false);

        mManager.removeListener(listener);
    }


    static private final int TIMEOUT_MS = 50;

    @Test
    public void testStatusListenerTimeout() {

        Manager.ManagerListener listener = mock(Manager.ManagerListener.class);

        mManager.addListener(listener);

        Assert.assertEquals(true, mManager.startDiscovery(TIMEOUT_MS));
        Assert.assertEquals(true, mManager.isDiscovering());

        verify(listener,timeout(NOTIFICATION_TIMEOUT_MS)).onDiscoveryChange(mManager, true);
        TestUtil.execAllAsyncTask();

        verify(listener,timeout(NOTIFICATION_TIMEOUT_MS)).onDiscoveryChange(mManager, false);
        Assert.assertEquals(false, mManager.isDiscovering());

        mManager.removeListener(listener);
    }

    @Test
    public void notifyNewNodeTest() {
        Node node = null;
        try {
            node = new NodeEmulator();
        } catch (InvalidBleAdvertiseFormat invalidBleAdvertiseFormat) {
            //it s never throw
            invalidBleAdvertiseFormat.printStackTrace();
            Assert.fail("Impossible build a node emulator: " + invalidBleAdvertiseFormat);
        }

        Manager.ManagerListener listener = mock(Manager.ManagerListener.class);

        mManager.addListener(listener);

        mManager.addNode(node);

        verify(listener,timeout(NOTIFICATION_TIMEOUT_MS)).onNodeDiscovered(mManager, node);

        mManager.removeListener(listener);
    }


    @Test
    public void addNodeTest() {
        Node node = null;
        try {
            node = new NodeEmulator();
        } catch (InvalidBleAdvertiseFormat invalidBleAdvertiseFormat) {
            //it s never throw
            invalidBleAdvertiseFormat.printStackTrace();
            Assert.fail("Impossible build a node emulator: " + invalidBleAdvertiseFormat);
        }

        mManager.addNode(node);

        Assert.assertEquals(1, mManager.getNodes().size());
        Assert.assertEquals(node, mManager.getNodes().get(0));

    }


    @Test
    public void TestGetNodeByTag() {
        final String nodeTag1 = "TestNode1";
        final String nodeTag2 = "TestNode2";
        Node node1 = buildMockNode();
        Node node2 = buildMockNode();

        when(node1.getTag()).thenReturn(nodeTag1);
        when(node2.getTag()).thenReturn(nodeTag2);

        mManager.addNode(node1);
        mManager.addNode(node2);

        Assert.assertEquals(node1, mManager.getNodeWithTag(nodeTag1));
        Assert.assertEquals(node2, mManager.getNodeWithTag(nodeTag2));
    }

    @Test
    public void TestNonExistingTag() {
        final String nodeTag = "TestNode";

        Assert.assertEquals(null, mManager.getNodeWithTag(nodeTag));
    }

    @Test
    public void TestGetNodeByName() {
        final String nodeName1 = "TestNode1";
        final String nodeName2 = "TestNode2";
        Node node1 = buildMockNode();
        Node node2 = buildMockNode();

        when(node1.getName()).thenReturn(nodeName1);
        when(node2.getName()).thenReturn(nodeName2);

        mManager.addNode(node1);
        mManager.addNode(node2);

        Assert.assertEquals(node1, mManager.getNodeWithName(nodeName1));
        Assert.assertEquals(node2, mManager.getNodeWithName(nodeName2));
    }

    @Test
    public void TestNonExistingName() {
        final String nodeName = "TestNode";
        mManager.resetDiscovery();
        Assert.assertEquals(null, mManager.getNodeWithName(nodeName));
    }

    @Test
    public void keepBoundedNode() {
        Node unBoundedNode = buildMockNode();
        Node boundedNode = buildMockNode();

        when(boundedNode.isBounded()).thenReturn(true);

        mManager.addNode(unBoundedNode);
        mManager.addNode(boundedNode);

        //both node are inserted
        Assert.assertEquals(2, mManager.getNodes().size());

        mManager.resetDiscovery();

        Assert.assertEquals(1, mManager.getNodes().size());
        Assert.assertEquals(true, mManager.getNodes().contains(boundedNode));

        //change the status for permit a correct cleaning
        when(boundedNode.isBounded()).thenReturn(false);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void nodeListIsReadOnly() {

        Node n1 = buildMockNode();
        Node n2 = buildMockNode();

        mManager.addNode(n1);
        mManager.addNode(n2);

        mManager.getNodes().clear();

    }

    @Test(expected = InvalidFeatureBitMaskException.class)
    public void addInvalidFeatureMask() throws InvalidFeatureBitMaskException{
        SparseArray<Class<? extends Feature>> invalidMask = new SparseArray<>(1);
        invalidMask.append(3, Feature.class); //not a power of 2

        Manager.addFeatureToNode((byte) 0x00, invalidMask);
    }

    private static Node buildNodeWithId(byte deviceID) throws InvalidBleAdvertiseFormat {
        return new Node(null, 10,
                new byte[]{0x07, (byte) 0xFF, (byte) 0x01, deviceID, (byte)
                        0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
    }

    @Test
    public void checkNewDeviceType() throws InvalidFeatureBitMaskException, InvalidBleAdvertiseFormat {
        SparseArray<Class<? extends Feature>> featureList = new SparseArray<>(1);
        featureList.append(2, Feature.class);

        Manager.addFeatureToNode((byte) 0xFF, featureList);

        Node node = buildNodeWithId((byte) 0xFF);

        Assert.assertEquals(Node.Type.NUCLEO, node.getType());

        Manager.addFeatureToNode((byte) 0x79, featureList);

        node = buildNodeWithId((byte) 0x79);

        Assert.assertEquals(Node.Type.GENERIC, node.getType());


    }

}
