/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.blesensor.cloud.AzureIot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class containing the parameters need to connect to an Azure iot hub
 */
public class ConnectionParameters {

    // regexp needed to extract the parameters from a standard connection string
    private static Pattern CONNECTION_STRING_PATTERN = Pattern.compile("HostName=(.*);DeviceId=(.*);SharedAccessKey=(.*)");

    /**
     * validate the string
     * @param connectionString string to analyze
     * @return true if the string is a valid conneciton string
     */
    public static boolean hasValidFormat(CharSequence connectionString){
        return hasValidFormat(CONNECTION_STRING_PATTERN.matcher(connectionString));
    }

    /**
     * tell if the match has found a valid connection string
     * @param match regexp match to test
     * @return true if the string has matched the regexp and all the 3 parameters are present
     */
    private static boolean hasValidFormat(Matcher match){
        return (match.matches() && match.groupCount()==3);
    }

    /**
     * create a connectionParameters object from a string
     * @param connectionString string containing the connection parameters
     * @return the connection parameters contained in the string
     * @throws IllegalArgumentException throw if the string isn't a valid connection string
     */
    public static ConnectionParameters parse(CharSequence connectionString) throws IllegalArgumentException{

        Matcher match = CONNECTION_STRING_PATTERN.matcher(connectionString);
        if(!hasValidFormat(match)){
            throw new IllegalArgumentException("Connection string must match:"+ CONNECTION_STRING_PATTERN.pattern());
        }
        return new ConnectionParameters(match.group(1),match.group(2),match.group(3));
    }


    /**
     * azure iot hub
     */
    public final String hostName;

    /**
     * azure iot device id
     */
    public final String deviceId;

    /**
     * device shared key
     */
    public final String sharedAccessKey;


    public ConnectionParameters(String hostName, String deviceId, String sharedAccessKey) {
        this.hostName = hostName;
        this.deviceId = deviceId;
        this.sharedAccessKey = sharedAccessKey;
    }

}
