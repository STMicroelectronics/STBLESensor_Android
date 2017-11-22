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
package com.st.BlueMS.preference.nucleo;

import android.util.Log;

import com.st.BlueSTSDK.Debug;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NucleoConsole {

    private static final String SET_NAME_COMMAND_FORMAT ="setName %s\n";
    private static final DateFormat SET_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
    private static final String SET_TIME_COMMAND_FORMAT= "setTime %s\n";
    //when the u parameters is available convert to:
    // private static final DateFormat SET_DATE_FORMAT = new SimpleDateFormat("uu/dd/MM/yy",Locale.getDefault());
    // private static final String SET_DATE_COMMAND_FORMAT= "setDate %s\n";
    // and remove the getDayOfTheWeek method
    private static final DateFormat SET_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy",Locale.getDefault());
    private static final String SET_DATE_COMMAND_FORMAT= "setDate %02d/%s\n";

    private Debug mConsole;

    public NucleoConsole(Debug console){
        mConsole = console;
    }

    public void setName(String newName) {
        if(newName.length()>7){
            throw  new IllegalArgumentException("Name must be shorter than 7 chars");
        }
        if(newName.isEmpty()){
            throw  new IllegalArgumentException("Name must not be empty");
        }

        String command = String.format(Locale.getDefault(),SET_NAME_COMMAND_FORMAT,newName);
        mConsole.write(command);
    }


    public void setTime(Date date) {
        String timeStr = SET_TIME_FORMAT.format(date);
        String command = String.format(Locale.getDefault(),SET_TIME_COMMAND_FORMAT,timeStr);
        mConsole.write(command);
    }

    /**
     * Convert the range Sunday=1 ... saturday=7 to monday =1 ... sunday=7
     * @param dayOfTheWeek dat og the week, 1 = sunday, 2 = monday ... 7= saturday
     * @return change the week where moday is the first day of the week
     */
    private static int toMondayFirst(int dayOfTheWeek){
        if(dayOfTheWeek==Calendar.SUNDAY)
            return 7;
        else
            return dayOfTheWeek-1;
    }

    /**
     * get the day of the week, in a specifc date
     * @param d date to query
     * @return 1 = monday .. 7 = sunday
     */
    private static int getDayOfTheWeek(Date d){
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return toMondayFirst(cal.get(Calendar.DAY_OF_WEEK));
    }

    public void setDate(Date date) {
        String dateStr = SET_DATE_FORMAT.format(date);
        int dayOfTheWeek = getDayOfTheWeek(date);
        String command = String.format(Locale.getDefault(),SET_DATE_COMMAND_FORMAT,dayOfTheWeek,dateStr);
        mConsole.write(command);
    }

    public void setDateAndTime(Date date){
        setDate(date);
        setTime(date);
    }
}
