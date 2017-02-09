#ST BlueMS

This repository contains the ST BlueMS app source code.

Used in conjunction with a firmware compatible with the BlueST protocol, the BlueMS application allow the user to access to all the sensors data, to request and download algorithm licenses, and to update the firmware directly from a mobile device via the Bluetooth® Low Energy protocol.
As an example, [BLUEMICROSYSTEM1](http://www.st.com/content/st_com/en/products/embedded-software/mems-and-sensors-software/open-mems/bluemicrosystem1.html) expansion software package for STM32Cube lets the user read and display real-time inertial (e.g. motion MEMS) and environmental (e.g. humidity, pressure, temperature) sensor data. It also provides a set of Open.Mems library functionalities like motion-sensor data fusion (osxMotionFX), activity recognition (osxMotionAR) and pedometer (osxMotionPM).
The application is also compatible with the ST Open Development Environment compliant [FP-SNS-MOTENV1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-sns-motenv1.html), [FP-SNS-ALLMEMS1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-sns-allmems1.html) and [FP-SNS-FLIGHT1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-sns-flight1.html) function packs.

The application is built on top of BlueST SDK, a library that provides an implementation of BlueST protocol and helps to easily export the data via Bluetooth® Low Energy. The SDK source code is freely available on [github](https://github.com/STMicroelectronics-CentralLabs/)

##Download the source

Since the project uses git submodules, <code>--recursive</code> option must be used to clone the repository:

```Shell
git clone --recursive https://github.com/STMicroelectronics-CentralLabs/STBlueMS_Android
```

or run
```Shell
git clone https://github.com/STMicroelectronics-CentralLabs/STBlueMS_Android
git submodule update --init --recursive
```

##License

Copyright (c) 2017  STMicroelectronics – All rights reserved
The STMicroelectronics corporate logo is a trademark of STMicroelectronics

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions
and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or other materials provided
with the distribution.

- Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
STMicroelectronics company nor the names of its contributors may be used to endorse or
promote products derived from this software without specific prior written permission.

- All of the icons, pictures, logos and other images that are provided with the source code
in a directory whose title begins with st_images may only be used for internal purposes and
shall not be redistributed to any third party or modified in any way.

- Any redistributions in binary form shall not include the capability to display any of the
icons, pictures, logos and other images that are provided with the source code in a directory
whose title begins with st_images.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
