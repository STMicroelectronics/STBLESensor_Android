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
package com.st.BlueSTSDK.Config;


/**
 * Class that represent an abstraction of the device Register
 * <p>
 * The class maintain the information like type, access,
 * address and size for the single register
 * </p>
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class Register {

    /**
     * Constructor for Register class
     * @param address filed unit
     * @param size field size
     * @param access register mode access
     * @param target register target
     */
    public Register(int address, int size, Access access, Target target) {
        this.setAddress(address);
        this.setAccess(access);
        this.setSize(size);
        this.setTarget(target);
    }

    /**
     * Constructor for Register class
     * @param address filed unit
     * @param size field size
     * @param access register mode access
     */
    public Register(int address, int size, Access access) {
        this(address,size,access,Target.BOTH);
    }

    /**
     * Constructor for Register class
     * @param address filed unit
     * @param size field size
     * @param target register target
     */
    public Register(int address, int size,  Target target) {
        this(address,size,Access.RW,target);
    }
    /**
     * Constructor for Register class starting
     * @param buffer buffer containing the register information
     */
    public Register(byte [] buffer) {
        this(getAddress(buffer), getSize(buffer), getTarget(buffer));
    }

    /**
     * Type used for register store target
     */
    public enum Target {
        PERSISTENT,
        SESSION,
        BOTH
    }

    /**
     * Type used for register access type
     */
    public enum Access {
        R,
        W,
        RW
    }

    /**
     * fill buffer header for the mode with proper write/read options and ack
     * @param header buffer to fill
     * @param target Target memory Persistent/Session
     * @param write write or read
     * @param ack ack required
     */
    private void setHeader(byte[] header, Target target,  boolean write, boolean ack){
        //CTRL byte
        header[0] = (byte) (0x80 |                          //Exec op
                ((target == Target.PERSISTENT) ? 0x40 : 0x00) | //target register session vs persistent
                (write ? 0x20 : 0x00) |                     //Write or Read operation
                (ack ? 0x08 : 0x00));                       //Ack required
        //ADDR
        header[1] = (byte) getAddress();
        //ERR
        header[2] = (byte) 0;
        //LEN
        header[3] = (byte) getSize();
    }

    /**
     * Get the buffer for read the register
     * @param target Mode Persistent/Session
     *
     * @return the packet (buffer) to send to the device to read the register
     */
    public byte [] ToReadPacket(Target target){
        byte [] toBuffer = null;

        if (this.target == Target.BOTH || target == this.target) {
            if (getAccess() == Access.R || getAccess() == Access.RW) {
                toBuffer = new byte[4];
                setHeader(toBuffer, target, false, true);
            }
        }
        return toBuffer;
    }

    /**
     * Get the buffer for write the register
     * @param target Mode Persistent/Session
     * @param payload data to write in the device register
     *
     * @return the packet (buffer) to send to the device to write the register with the value
     * defined in the payload
     */
    public byte [] ToWritePacket(Target target, byte [] payload){

        byte [] toBuffer = null;

        if (payload != null) {
            if (this.target == Target.BOTH || target == this.target) {
                if ((getAccess() == Access.RW || getAccess() == Access.W) &&
                        payload.length <= getSize() * 2) {
                    toBuffer = new byte[4 + payload.length];
                    setHeader(toBuffer, target, true, true);

                    System.arraycopy(payload, 0, toBuffer, 4, payload.length);
                }
            }
        }
        return toBuffer;
    }

    /**
     * Get the payload of the read register
     * @param buffer packet received from the device
     *
     * @return the value(s) of the read registers
     */
    public static byte [] getPayload(byte [] buffer) {
        byte [] toPayload = null;

        if (buffer.length > 4) {
            toPayload = new byte[buffer.length-4]; //skip header
            System.arraycopy(buffer, 4, toPayload, 0, toPayload.length);
        }

        return  toPayload;
    }

    /**
     * Get the Target of the received register
     * @param buffer packet received from the device
     *
     * @return the Mode of the read registers
     */
    public static Target getTarget(byte [] buffer){
         return ((buffer[0] & 0x40) == 0x40 )? Target.PERSISTENT : Target.SESSION;
    }

    /**
     * Check if the buffer is for a write operation
     * @param buffer packet received from the device
     *
     * @return true if write bit is set
     */
    static public boolean isWriteOperation(byte [] buffer){
        return ((buffer[0] & 0x20) == 0x20 );
    }

    /**
     * Check if the buffer is for a write operation
     * @param buffer packet received from the device
     *
     * @return true if Read bit is set
     */
    static public boolean isReadOperation(byte [] buffer){
        return !isWriteOperation(buffer);
    }


    /**
     * Get the address of the received register
     * @param buffer packet received from the device
     *
     * @return the address of the register read or write operation
     */
    public static int getAddress(byte [] buffer){
        return buffer[1] & 0xFF;
    }

    /**
     * Get the error of the received register
     * @param buffer packet received from the device
     *
     * @return the error code of the register read or write operation
     */
    public  static int getError(byte [] buffer){
        return buffer[2] & 0xFF;
    }

    /**
     * Get the size of the received register
     * @param buffer packet received from the device
     *
     * @return the size of the register read or write operation
     */
    public  static int getSize(byte [] buffer){
        return buffer[3] & 0xFF;
    }

    /**
     * get Register address
     * @return register address
     */
    public int getAddress() {
        return address;
    }

    /**
     * get Register access type
     * @return register type
     */
    public Access getAccess() {
        return access;
    }

    /**
     * get Register target
     * @return register target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * get Register size
     * @return register size
     */
    public int getSize() {
        return size;
    }

    /**
     * set Register address
     */
    private void setAddress(int address) {
        this.address = address;
    }

    /**
     * set Register access type
     */
    private void setAccess(Access accessType) {
        this.access = accessType;
    }

    /**
     * set Register size
     */
    private void setSize(int size) {
        this.size = size;
    }

    /**
     * set Register target
     */
    private void setTarget(Target target) {
        this.target = target;
    }

    @Override
    protected Object clone() {
        return new Register(this.getAddress(), this.getSize(), this.getAccess(), this.getTarget());
    }

    @Override
    public boolean equals(Object o) {
        boolean bRet = false;
        if (o instanceof Register) {
            bRet = (o == this) || ((this.getAddress() == ((Register) o).getAddress()) && (this.getSize() == ((Register) o).getSize()));
        }
        return bRet;
    }

    /**
     * The register address
     */
    private int address;

    /**
     * The register size (number of short to be read or write from the register)
     */
    private int size;

    /**
     * The register access (read/ write or both)
     */
    private Access access;

    /**
     * The register targed (session , persistent or both)
     */
    private Target target;
}
