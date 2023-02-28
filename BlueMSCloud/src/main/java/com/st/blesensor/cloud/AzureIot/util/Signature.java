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

/**
 * A signature that is used in the SAS token to authenticate the mqtt.
 */
public final class Signature
{
    private String sig;

    /**
     * Constructs a {@code Signature} instance from the given resource URI,
     * expiry time and device key.
     * @param resourceUri the resource URI.
     * @param expiryTime the time, as a UNIX timestamp, after which the token
     * will become invalid.
     * @param deviceKey the device key.
     */
    public Signature(String resourceUri, long expiryTime, String deviceKey)
    {
        // Codes_SRS_SIGNATURE_11_001: [The signature shall be computed from a composition of functions as such: encodeSignatureWebSafe(encodeSignatureUtf8(encodeSignatureBase64(encryptSignatureHmacSha256(buildRawSignature(scope, expiryTime))))).]
        byte[] rawSig = SignatureHelper.buildRawSignature(resourceUri,
                expiryTime);
        // Codes_SRS_SIGNATURE_11_002: [The device key shall be decoded using Base64 before the signature computation begins, excluding buildRawSignature().]
        byte[] decodedDeviceKey = SignatureHelper.decodeDeviceKeyBase64(
                deviceKey);
        byte[] encryptedSig =
                SignatureHelper.encryptSignatureHmacSha256(rawSig,
                        decodedDeviceKey);
        byte[] encryptedSigBase64 = SignatureHelper.encodeSignatureBase64(
                encryptedSig);
        // Codes_SRS_SIGNATURE_11_003: [The signature string shall be encoded using charset UTF-8.]
        String utf8Sig = SignatureHelper.encodeSignatureUtf8(encryptedSigBase64);
        this.sig = SignatureHelper.encodeSignatureWebSafe(utf8Sig);
    }

    /**
     * Returns the string representation of the signature.
     *
     * @return the string representation of the signature.
     */
    @Override
    public String toString()
    {
        // Codes_SRS_SIGNATURE_11_005: [The function shall return the string representation of the signature.]
        return this.sig;
    }
}
