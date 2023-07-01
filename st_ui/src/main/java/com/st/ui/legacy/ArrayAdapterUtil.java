/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ui.legacy;

import android.content.Context;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;

public class ArrayAdapterUtil {

    public static android.widget.ArrayAdapter<CharSequence> createAdapterFromArray(@NonNull Context context, @ArrayRes int res) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        android.widget.ArrayAdapter<CharSequence> adapter = android.widget.ArrayAdapter.createFromResource(context,
                res, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

}
