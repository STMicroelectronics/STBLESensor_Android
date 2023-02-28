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

import android.util.SparseArray;

import com.st.BlueSTSDK.ExportedFeature;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedAudioOpusConf;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedFeatureAudioOpusMusic;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedFeatureAudioOpusVoice;
import com.st.BlueSTSDK.Features.FeatureAILogging;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureBinaryContent;
import com.st.BlueSTSDK.Features.FeatureColorAmbientLight;
import com.st.BlueSTSDK.Features.FeatureFiniteStateMachine;
import com.st.BlueSTSDK.Features.FeatureGNSS;
import com.st.BlueSTSDK.Features.FeatureNEAIClassClassification;
import com.st.BlueSTSDK.Features.JsonNFCFeature.FeatureJsonNFC;
import com.st.BlueSTSDK.Features.FeatureMachineLearningCore;
import com.st.BlueSTSDK.Features.FeatureMemsNorm;
import com.st.BlueSTSDK.Features.FeatureActivity;
import com.st.BlueSTSDK.Features.Audio.ADPCM.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.Audio.ADPCM.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Features.Audio.Opus.FeatureAudioOpus;
import com.st.BlueSTSDK.Features.Audio.Opus.FeatureAudioOpusConf;
import com.st.BlueSTSDK.Features.FeatureAudioClassification;
import com.st.BlueSTSDK.Features.FeatureBeamforming;
import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Features.FeatureCOSensor;
import com.st.BlueSTSDK.Features.FeatureCarryPosition;
import com.st.BlueSTSDK.Features.FeatureCompass;
import com.st.BlueSTSDK.Features.FeatureDirectionOfArrival;
import com.st.BlueSTSDK.Features.FeatureEulerAngle;
import com.st.BlueSTSDK.Features.FeatureEventCounter;
import com.st.BlueSTSDK.Features.FeatureFitnessActivity;
import com.st.BlueSTSDK.Features.FeatureFreeFall;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureFFTAmplitude;
import com.st.BlueSTSDK.Features.FeatureLuminosity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMemsGesture;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.FeatureMicLevel;
import com.st.BlueSTSDK.Features.FeatureMotionIntensity;
import com.st.BlueSTSDK.Features.FeatureMotorTimeParameter;
import com.st.BlueSTSDK.Features.FeatureNEAIAnomalyDetection;
import com.st.BlueSTSDK.Features.FeatureGestureNavigation;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Features.FeatureMotionAlgorithm;
import com.st.BlueSTSDK.Features.FeaturePiano;
import com.st.BlueSTSDK.Features.FeatureQVAR;
import com.st.BlueSTSDK.Features.FeatureStepperMotor;
import com.st.BlueSTSDK.Features.FeatureToFMultiObject;
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration;
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL;
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig;
import com.st.BlueSTSDK.Features.PnPL.predictive.FeaturePredictiveFrequencyDomainStatus;
import com.st.BlueSTSDK.Features.PnPL.predictive.FeaturePredictiveAccelerationStatus;
import com.st.BlueSTSDK.Features.PnPL.predictive.FeaturePredictiveSpeedStatus;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureProximity;
import com.st.BlueSTSDK.Features.FeatureProximityGesture;
import com.st.BlueSTSDK.Features.FeatureSDLogging;
import com.st.BlueSTSDK.Features.FeatureSwitch;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Features.remote.RemoteFeatureHumidity;
import com.st.BlueSTSDK.Features.remote.RemoteFeaturePressure;
import com.st.BlueSTSDK.Features.remote.RemoteFeatureSwitch;
import com.st.BlueSTSDK.Features.remote.RemoteFeatureTemperature;
import com.st.BlueSTSDK.Features.FeatureSTREDL;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class help to get list of services and characteristics available in the BlueST devices
 * <p>
 * It define the UUID and the name of the services and the characteristic UUID available in the
 * BlueST devices.
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class BLENodeDefines {

    /**
     * all the characteristics handle by this sdk must end with this value
     */
    private static final String COMMON_CHAR_UUID = "-11e1-ac36-0002a5d5c51b";

    /**
     * all the service handle by this sdk must to finish with this value
     */
    private final static String COMMON_UUID_SERVICES = "-11e1-9ab4-0002a5d5c51b";

    /**
     * This class help to get list of services available in the BlueST devices
     * <p>
     * It define the UUID and the name of the services available in the
     * BlueST devices.
     * </p>
     * <p>
     * A valid service UUID must have the form 00000000 -XXXX-11e1-9ab4-0002a5d5c51b,
     * where XXXX is the service id
     * </p>
     *
     * @author STMicroelectronics - Central Labs.
     * @version 1.0
     */
    public static class Services {

        private final static String SERVICE_UUID_FORMAT = "00000000-[0-9a-fA-F]{4}-11e1-9ab4-0002a5d5c51b";

        /**
         * return true if the service is handle by this sdk. It is handle by this sdk if the uuid is
         * 0000 0000-YYYY-11e1-9ab4-0002a5d5c51b
         *
         * @param uuid uuid of the service that we want test
         * @return true i the uuid ends with -11e1-9ab4-0002a5d5c51b
         */
        public static boolean isKnowService(UUID uuid) {
            String uuidString = uuid.toString();
            return uuidString.matches(SERVICE_UUID_FORMAT);
        }//isKnowService

        /**
         * Service for access to the board stdout/err
         *
         * @author STMicroelectronics - Central Labs.
         */
        public static class Debug {

            /**
             * service UUID
             */
            public final static UUID DEBUG_SERVICE_UUID = UUID.fromString("00000000-000E" +
                    COMMON_UUID_SERVICES);

            /**
             * all the characteristics from this service will hand with this value
             */
            private static String COMMON_DEBUG_UUID_CHAR = "-000E" + COMMON_CHAR_UUID;

            /**
             * characteristic where you can write and read output commands
             */
            public final static UUID DEBUG_TERM_UUID = UUID.fromString
                    ("00000001" + COMMON_DEBUG_UUID_CHAR);

            /**
             * characteristic where the node will write error message
             */
            public final static UUID DEBUG_STDERR_UUID = UUID.fromString
                    ("00000002" + COMMON_DEBUG_UUID_CHAR);

            /**
             * true if is a valid debug characteristic
             *
             * @param charUuid characteristic uuid
             * @return true the param is equal to \code{DEBUG_STDERR_UUID} or \code{DEBUG_TERM_UUID}
             */
            public static boolean isDebugCharacteristics(UUID charUuid) {
                return charUuid.equals(DEBUG_STDERR_UUID) ||
                        charUuid.equals(DEBUG_TERM_UUID);
            }//isDebugCharacteristics

            private Debug() {
            }

        }//Debug

        /**
         * Service that permit to configure the board parameters or the features
         *
         * @author STMicroelectronics - Central Labs.
         */
        public static class Config {

            /**
             * Service uuid
             */
            public final static UUID CONFIG_CONTROL_SERVICE_UUID = UUID.fromString("00000000-000F" +
                    COMMON_UUID_SERVICES);

            /**
             * all the characteristics of this service will end with this value
             */
            private static String COMMON_CONFIG_UUID_CHAR = "-000F" + COMMON_CHAR_UUID;

            /**
             * characteristic permit to send a command to a feature
             */
            public final static UUID FEATURE_COMMAND_UUID = UUID.fromString
                    ("00000002" + COMMON_CONFIG_UUID_CHAR);
            /**
             * characteristic permit to Manage register (read or write register values)
             */
            public final static UUID REGISTERS_ACCESS_UUID = UUID.fromString
                    ("00000001" + COMMON_CONFIG_UUID_CHAR);

            private Config() {
            }

        }//Config

        private Services() {
        }

    }//Services

    /**
     * This class define the characteristics associated with the features
     * There are 3 types of features:
     * <li>
     *     <ul>Base Feature: this feature has an uuid in the format  XXXXXXXX-0001-11e1-ac36-0002a5d5c51b,
     *     each bit of the first part tell witch feature is present inside the characteristics, and the presence
     *     of the feature is advertised inside the node feature mask field inside the advertise </ul>
     *     <ul>Extended feature: this feature has an uuid in the format XXXXXXXX-0002-11e1-ac36-0002a5d5c51b,
     *     this feature are not advertised inside the feature mask
     *     </ul>
     *      <ul>The general purpose feature will be created if there is a characteristics with the format
     *      XXXXXXXX-0003-11e1-ac36-0002a5d5c51b, in this case the the feature data are not parsed, but
     *      just notify to the user as an array of byte </ul>
     * </li>
     *
     * @author STMicroelectronics - Central Labs.
     */
    public static class FeatureCharacteristics {
        /**
         * all the valid characteristics have to finish with this value
         */
        public static final String BASE_FEATURE_COMMON_UUID = "0001" + COMMON_CHAR_UUID;
        public static final String EXTENDED_FEATURE_COMMON_UUID = "0002" + COMMON_CHAR_UUID;

        /**
         * extract the fist 32 bits from the characteristics UUID
         *
         * @param uuid characteristics uuid
         * @return feature mask bit, the first 32 bit of the UUID
         */
        public static int extractFeatureMask(UUID uuid) {
            return (int) (uuid.getMostSignificantBits() >> 32);
        }

        /**
         * return true if the UUID can be a valid feature UUID
         *
         * @param uuid characteristics uuid to test
         * @return true if the uuid end with \code{BASE_COMMON_FEATURE_UUID}
         */
        public static boolean isBaseFeatureCharacteristics(UUID uuid) {
            String uuidString = uuid.toString();
            return uuidString.endsWith(BASE_FEATURE_COMMON_UUID);
        }//isKnowService

        public static boolean isExtendedFeatureCharacteristics(UUID uuid) {
            return EXTENDED_FEATURE_MAP.containsKey(uuid);
        }//isKnowService

        public static List<Class<? extends Feature>> getExtendedFeatureFor(UUID uuid) {
            return Collections.unmodifiableList(EXTENDED_FEATURE_MAP.get(uuid));
        }//isKnowService

        public static UUID buildExtendedFeatureCharacteristics(long header) {
            String uuid = String.format("%08X-" + EXTENDED_FEATURE_COMMON_UUID, header);
            return UUID.fromString(uuid);
        }

        /**
         * all the valid general purpose characteristics have to finish with this value
         */
        static final String GP_FEATURE_UUID = "0003" + COMMON_CHAR_UUID;

        /**
         * return true if the UUID can be a valid general purpose characteristics
         *
         * @param uuid characteristics to to test
         * @return true if the uuid end with \code{GP_FEATURE_UUID}
         */
        public static boolean isGeneralPurposeCharacteristics(UUID uuid) {
            String uuidString = uuid.toString();
            return uuidString.endsWith(GP_FEATURE_UUID);
        }

        /**
         * array that map a feature mask with a feature class, for a nucleo devices
         */
        public static final SparseArray<Class<? extends Feature>> DEFAULT_MASK_TO_FEATURE =
                new SparseArray<>();

        public static final SparseArray<Class<? extends Feature>> SENSOR_TILE_BOX_MASK_TO_FEATURE =
                new SparseArray<>();

        /**
         * array that map a feature mask with a feature class, for a nucleo devices
         */
        public static final SparseArray<Class<? extends Feature>> Nucleo_Remote_Features =
                new SparseArray<>();

        static final Map<UUID, List<Class<? extends Feature>>> EXTENDED_FEATURE_MAP = new HashMap<>();


        private static void buildDefaultBaseFeatureMask() {
            //DEFAULT_MASK_TO_FEATURE.put(0x80000000, RFU);
            DEFAULT_MASK_TO_FEATURE.put(0x40000000, FeatureAudioADPCMSync.class);
            DEFAULT_MASK_TO_FEATURE.put(0x20000000, FeatureSwitch.class);
            DEFAULT_MASK_TO_FEATURE.put(0x10000000, FeatureDirectionOfArrival.class);

            DEFAULT_MASK_TO_FEATURE.put(0x08000000, FeatureAudioADPCM.class);
            DEFAULT_MASK_TO_FEATURE.put(0x04000000, FeatureMicLevel.class);
            DEFAULT_MASK_TO_FEATURE.put(0x02000000, FeatureProximity.class);
            DEFAULT_MASK_TO_FEATURE.put(0x01000000, FeatureLuminosity.class);

            DEFAULT_MASK_TO_FEATURE.put(0x00800000, FeatureAcceleration.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00400000, FeatureGyroscope.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00200000, FeatureMagnetometer.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00100000, FeaturePressure.class);

            DEFAULT_MASK_TO_FEATURE.put(0x00080000, FeatureHumidity.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00040000, FeatureTemperature.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00020000, FeatureBattery.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00010000, FeatureTemperature.class);

            DEFAULT_MASK_TO_FEATURE.put(0x00008000, FeatureCOSensor.class);
            //DEFAULT_MASK_TO_FEATURE.put(0x00004000, RSU);
            DEFAULT_MASK_TO_FEATURE.put(0x00002000, FeatureStepperMotor.class); // stm32 ota reboot??
            DEFAULT_MASK_TO_FEATURE.put(0x00001000, FeatureSDLogging.class);

            DEFAULT_MASK_TO_FEATURE.put(0x00000800, FeatureBeamforming.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000400, FeatureAccelerationEvent.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000200, FeatureFreeFall.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000100, FeatureMemsSensorFusionCompact.class);

            DEFAULT_MASK_TO_FEATURE.put(0x00000080, FeatureMemsSensorFusion.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000040, FeatureCompass.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000020, FeatureMotionIntensity.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000010, FeatureActivity.class);

            DEFAULT_MASK_TO_FEATURE.put(0x00000008, FeatureCarryPosition.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000004, FeatureProximityGesture.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000002, FeatureMemsGesture.class);
            DEFAULT_MASK_TO_FEATURE.put(0x00000001, FeaturePedometer.class);
        }

        private static void buildSensorTileBoxFeatureMask() {
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x80000000, FeatureFFTAmplitude.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x40000000, FeatureAudioADPCMSync.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x20000000, FeatureSwitch.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x10000000, FeatureMemsNorm.class);

            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x08000000, FeatureAudioADPCM.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x04000000, FeatureMicLevel.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x02000000, FeatureAudioClassification.class);
            //SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x01000000, FeatureLuminosity.class);

            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00800000, FeatureAcceleration.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00400000, FeatureGyroscope.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00200000, FeatureMagnetometer.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00100000, FeaturePressure.class);

            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00080000, FeatureHumidity.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00040000, FeatureTemperature.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00020000, FeatureBattery.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00010000, FeatureTemperature.class);

            //SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00008000, FeatureGyroscopeNorm.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00004000, FeatureEulerAngle.class);
            //DEFAULT_MASK_TO_FEATURE.put(0x00002000, RFU); // stm32 ota reboot
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00001000, FeatureSDLogging.class);

            //SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000800, FeatureMagnetometerNorm.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000400, FeatureAccelerationEvent.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000200, FeatureEventCounter.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000100, FeatureMemsSensorFusionCompact.class);

            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000080, FeatureMemsSensorFusion.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000020, FeatureMotionIntensity.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000040, FeatureCompass.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000010, FeatureActivity.class);

            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000008, FeatureCarryPosition.class);
            //SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000004, FeatureProximityGesture.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000002, FeatureMemsGesture.class);
            SENSOR_TILE_BOX_MASK_TO_FEATURE.put(0x00000001, FeaturePedometer.class);
        }

        @SafeVarargs
        private static List<Class<? extends Feature>> asFeatureList(Class<? extends Feature>... args) {
            return Arrays.asList(args);
        }

        private static void buildExtendedFeatureMask() {
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x01), asFeatureList(FeatureAudioOpus.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x02), asFeatureList(FeatureAudioOpusConf.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x03), asFeatureList(FeatureAudioClassification.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x04), asFeatureList(FeatureAILogging.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x05), asFeatureList(FeatureFFTAmplitude.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x06), asFeatureList(FeatureMotorTimeParameter.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x07), asFeatureList(FeaturePredictiveSpeedStatus.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x08), asFeatureList(FeaturePredictiveAccelerationStatus.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x09), asFeatureList(FeaturePredictiveFrequencyDomainStatus.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x0A), asFeatureList(FeatureMotionAlgorithm.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x0D), asFeatureList(FeatureEulerAngle.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x0E), asFeatureList(FeatureFitnessActivity.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x0F), asFeatureList(FeatureMachineLearningCore.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x10), asFeatureList(FeatureFiniteStateMachine.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x11), asFeatureList(FeatureHSDataLogConfig.class));
            // the 0x12 is already exposed by HSDataLog even if it's not used....
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x13), asFeatureList(FeatureToFMultiObject.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x14), asFeatureList(FeatureExtConfiguration.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x15), asFeatureList(FeatureColorAmbientLight.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x16), asFeatureList(FeatureQVAR.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x17), asFeatureList(FeatureSTREDL.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x18), asFeatureList(FeatureGNSS.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x19), asFeatureList(FeatureNEAIAnomalyDetection.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x1A), asFeatureList(FeatureNEAIClassClassification.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x1B), asFeatureList(FeaturePnPL.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x1C), asFeatureList(FeaturePiano.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x1D), asFeatureList(FeatureEventCounter.class));
            // the 0x1E is reserved for Quasar
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x1F), asFeatureList(FeatureGestureNavigation.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x20), asFeatureList(FeatureJsonNFC.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x21), asFeatureList(FeatureMemsNorm.class));
            EXTENDED_FEATURE_MAP.put(buildExtendedFeatureCharacteristics(0x22), asFeatureList(FeatureBinaryContent.class));
        }


        @SafeVarargs
        private static List<Class<? extends ExportedFeature>> asExportedFeatureList(Class<? extends ExportedFeature>... args) {
            return Arrays.asList(args);
        }

        private static FeatureCoordinate buildStandardExportedExtendedFeature(long header) {
            return new FeatureCoordinate(
                    UUID.fromString("00000000-0001-11e1-9ab4-0002a5d5c51b"),
                    buildExtendedFeatureCharacteristics(header)
            );
        }

        public static Map<FeatureCoordinate, Class<? extends ExportedFeature>> getDefaultExportedFeature() {
            Map<FeatureCoordinate, Class<? extends ExportedFeature>> map = new HashMap<>();
            map.put(buildStandardExportedExtendedFeature(0x01), ExportedFeatureAudioOpusVoice.class);
            map.put(buildStandardExportedExtendedFeature(0x02), ExportedAudioOpusConf.class);
            map.put(buildStandardExportedExtendedFeature(0x11), ExportedFeatureAudioOpusMusic.class);
            return map;

        }

        static {
            buildDefaultBaseFeatureMask();
            buildExtendedFeatureMask();

            buildSensorTileBoxFeatureMask();

            Nucleo_Remote_Features.put(0x20000000, RemoteFeatureSwitch.class);
            Nucleo_Remote_Features.put(0x00100000, RemoteFeaturePressure.class);
            Nucleo_Remote_Features.put(0x00080000, RemoteFeatureHumidity.class);
            Nucleo_Remote_Features.put(0x00040000, RemoteFeatureTemperature.class);
        }//static

        private FeatureCharacteristics() {
        }

    }//FeatureCharacteristics

    private BLENodeDefines() {
    }
}//BLENodeDefines
