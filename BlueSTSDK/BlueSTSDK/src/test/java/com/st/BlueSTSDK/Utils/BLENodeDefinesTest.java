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
package com.st.BlueSTSDK.Utils;

import com.st.BlueSTSDK.BuildConfig;
import com.st.BlueSTSDK.Node;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        manifest = "src/main/AndroidManifest.xml",
        sdk = 23)
public class BLENodeDefinesTest {

    @Test
    public void testIsKnowServices(){
        Assert.assertTrue(BLENodeDefines.Services.isKnowService(BLENodeDefines
                .Services.Debug.DEBUG_SERVICE_UUID));
        Assert.assertTrue(BLENodeDefines.Services.isKnowService(BLENodeDefines
                .Services.Config.CONFIG_CONTROL_SERVICE_UUID));
        Assert.assertTrue(BLENodeDefines.Services.isKnowService(
                UUID.fromString("00000000-FFFF-11e1-9ab4-0002a5d5c51b")));
        Assert.assertTrue(BLENodeDefines.Services.isKnowService(
                UUID.fromString("00000000-F00F-11e1-9ab4-0002a5d5c51b")));

    }

    @Test
    public void testIsUnKnowServices(){
        Assert.assertFalse(BLENodeDefines.Services.isKnowService(
                UUID.fromString("00000001-0000-11e1-9ab4-0002a5d5c51b")));
        Assert.assertFalse(BLENodeDefines.Services.isKnowService(
                UUID.fromString("00000000-0010-11e1-9ab4-000000000000")));
    }

    @Test
    public void testIsDebugCharacteristics(){
        Assert.assertTrue(BLENodeDefines.Services.Debug.isDebugCharacteristics(
                BLENodeDefines.Services.Debug.DEBUG_STDERR_UUID
        ));
        Assert.assertTrue(BLENodeDefines.Services.Debug.isDebugCharacteristics(
                BLENodeDefines.Services.Debug.DEBUG_TERM_UUID
        ));
    }

    @Test
    public void testIsNotDebugCharacteristics(){
        Assert.assertFalse(BLENodeDefines.Services.Debug.isDebugCharacteristics(
                BLENodeDefines.Services.Debug.DEBUG_SERVICE_UUID
        ));
        Assert.assertFalse(BLENodeDefines.Services.Debug.isDebugCharacteristics(
                BLENodeDefines.Services.Config.FEATURE_COMMAND_UUID
        ));
        Assert.assertFalse(BLENodeDefines.Services.Debug.isDebugCharacteristics(
                UUID.fromString("00000000-0000-FFFF-FFFA-000000000000")
        ));
    }

    @Test
    public  void testExtractFeatureMask(){
        Assert.assertEquals(0, BLENodeDefines.FeatureCharacteristics.extractFeatureMask(
                UUID.fromString("00000000-0001-11e1-ac36-0002a5d5c51b"))
        );
        Assert.assertEquals(0x12345678, BLENodeDefines.FeatureCharacteristics.extractFeatureMask(
                        UUID.fromString("12345678-0001-11e1-ac36-0002a5d5c51b"))
        );
        Assert.assertEquals(0xFFFFFFFF, BLENodeDefines.FeatureCharacteristics.extractFeatureMask(
                        UUID.fromString("FFFFFFFF-0001-11e1-ac36-0002a5d5c51b"))
        );
    }

    @Test
    public void theHeaderIsCorrectlyInsertIntoanExtendedFeature(){
        UUID uuid = BLENodeDefines.FeatureCharacteristics.buildExtendedFeatureCharacteristics(0);
        UUID expected = UUID.fromString("00000000-0002-11e1-ac36-0002a5d5c51b");
        Assert.assertEquals(expected,uuid);

        uuid = BLENodeDefines.FeatureCharacteristics.buildExtendedFeatureCharacteristics(1);
        expected = UUID.fromString("00000001-0002-11e1-ac36-0002a5d5c51b");
        Assert.assertEquals(expected,uuid);

        uuid = BLENodeDefines.FeatureCharacteristics.buildExtendedFeatureCharacteristics(0xFFFFFFFFL);
        expected = UUID.fromString("FFFFFFFF-0002-11e1-ac36-0002a5d5c51b");
        Assert.assertEquals(expected,uuid);

    }

}
