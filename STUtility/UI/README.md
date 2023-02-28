# UI_Android

UI is a Android library that allows easy access to common graphic resources shared between the various applications of ST. The aim of this module is to reduce the redundancy of the resource files and have a single point in which the main graphical resources that a STMicroelectronics application needs are collected.

## Content
Files are divided into three main folders:

1. _drawable_resources_
2. _values_resources_
3. _layout_resources_

### Drawable Resources
Here are contained all the files that, in the Android Studio project, are classified as drawable.
A drawable resource is a general concept for a graphic that can be drawn to the screen.
The explanation of the subfolders is given below:

|Name              | Description
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------
|st_boards         | contains the ST board's image (like SensorTile.box, SmarTag, IoT Lora Tracker) 
|st_icons          | contains specific icons (such as the Asset Tracking application logo) that can also be used by other applications
|bluestsdk_icons   | contains icons to use to display information about BLE devices such as battery charge level, icon that identifies the board in advertise, etc... 
|st_sensor_icons   | contains all the icons associated with the sensors of a board (such as icons for temperature, humidity, accelerometric sensors, etc...)
|connectivity_logo | contains all icons for various connectivity technologies (BLE, NFC, Lora, Sigfox)
|menu_icons        | contains the main icons (derived from the vector asset of Android) often used in applications (such as back button, cloud icon, etc... )
|graphics_element  | contains specific graphic elements such as the border style of a textview

### Value Resources
Here are defined the colors to use within the application.

### Layout Resources
Here are defined layouts common to the various applications.

## How to install the library
### As an external library
1. Clone the repository
  ```Shell
  $ git clone https://github.com/SW-Platforms/UI_Android.git
  ```
2. Add the UI directory as a submodule of your project: File->Import Module
3. To access the resource files contained in the module:
```Java
dependencies {
    implementation project(path: ':UI')
}
```


## License
COPYRIGHT(c) 2015 STMicroelectronics

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
   1. Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
   3. Neither the name of STMicroelectronics nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

