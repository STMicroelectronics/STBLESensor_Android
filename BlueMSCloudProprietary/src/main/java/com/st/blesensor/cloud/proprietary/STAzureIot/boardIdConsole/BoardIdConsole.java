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

package com.st.blesensor.cloud.proprietary.STAzureIot.boardIdConsole;

import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;
import com.st.blesensor.cloud.proprietary.STAzureIot.boardIdConsole.nucleo.BoardIdConsoleNucleo;


/**
 * Class that use the debug console for send/receive information about the license status
 */
public abstract class BoardIdConsole {

    static public @Nullable
    BoardIdConsole getBoardIdConsole(Node node){
        Debug debug = node.getDebug();
        if(debug==null)
            return null;
        //else
        switch (node.getType()) {
            case STEVAL_WESU1:
                return new BoardIdConsoleWesu(debug);
            case NUCLEO:
            case NUCLEO_F401RE:
            case NUCLEO_L476RG:
            case NUCLEO_F446RE:
            case NUCLEO_L053R8:
            case SENSOR_TILE:
            case BLUE_COIN:
                return new BoardIdConsoleNucleo(debug);
        }
        return  null;
    }

    /**
     * console where send the command
     */
    protected Debug mConsole;

    /**
     *
     * @param console console where send the command
     */
    protected BoardIdConsole(Debug console) {
        mConsole = console;
    }

    /**
     * true if the object is waiting an answer from the console
     *
     * @return true if the object is busy
     */
    public abstract boolean isWaitingAnswer();

    /**
     * request to read the board id
     * the board id is notify using the method
     * {@link ReadBoardIdCallback#onBoardIdRead(BoardIdConsole, String)}
     * @return true if the command is correctly send
     */
    public abstract boolean readBoardId(ReadBoardIdCallback callback);

    public interface ReadBoardIdCallback {
        /**
         * function called when we receive the board id.
         *
         * @param console object that request the board id
         * @param uid     board id
         */
        void onBoardIdRead(BoardIdConsole console, String uid);
    }

}
