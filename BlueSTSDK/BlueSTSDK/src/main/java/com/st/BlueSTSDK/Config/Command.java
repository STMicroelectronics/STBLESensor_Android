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

import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Utils.NumberConversion;

/**
 * Class that represent an abstraction of the device command to set/get the proper register
 * <p>
 * The class maintain the information  of the register {@link com.st.BlueSTSDK.Config.Register},
 * the target and the payload to write to the device or read from the device.
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class Command implements Cloneable{

    private final Register register;
    private final Register.Target target;
    private byte[] data;

    /**
     * Constructor for Command class
     * @param r Register to manage with this command
     * @param t Memory target for the register
     * @param d value as byte array to set
     */
    public Command(Register r, Register.Target t, byte[] d) {
        register = r;
        target =t;
        data = d;
    }

    /**
     * Constructor for Command class
     * @param r Register to manage with this command
     * @param t Memory target for the register
     */
    public Command(Register r, Register.Target t) {
        this(r, t, (byte [])null);
    }

    /**
     * Constructor for Command class
     * @param r Register to manage with this command
     * @param t Memory target for the register
     * @param val long value to set
     * @param type the type of the val parameter
     */
    public Command(Register r, Register.Target t, long val, Field.Type type) {
        this(r, t);
        byte [] dataReg = null;

        switch (type) {
            case Float:
                dataReg = NumberConversion.LittleEndian.uint32ToBytes(val);
                break;
            case Int8:
                dataReg = new byte[1];
                dataReg[0] = (byte)val;
                break;
            case UInt8:
                dataReg = new byte[1];
                dataReg[0] = (byte)(val & 0xFF);
                break;
            case Int16:
                dataReg = NumberConversion.LittleEndian.int16ToBytes((short) val);
                break;
            case UInt16:
                dataReg = NumberConversion.LittleEndian.uint16ToBytes((int) val);
                break;
            case Int32:
                dataReg = NumberConversion.LittleEndian.int32ToBytes((int) val);
                break;
            case UInt32:
                dataReg = NumberConversion.LittleEndian.uint32ToBytes(val);
                break;
            case Int64:
                dataReg = NumberConversion.LittleEndian.uint32ToBytes(val);
                break;

        }

        data = dataReg;
    }

    /**
     * Constructor for Command class
     * @param r Register to manage with this command
     * @param t Memory target for the register
     * @param f float value to set
     */
    public Command(Register r, Register.Target t, float f) {
        this(r, t, NumberConversion.LittleEndian.floatToBytes(f));
    }

    /**
     * Constructor for Command class
     * @param r Register to manage with this command
     * @param t Memory target for the register
     * @param s String value to set
     */
    public Command(Register r, Register.Target t, String s) {
        this(r, t, s.getBytes());
    }

    /**
     * Constructor for Command class
     * @param dataReceived buffer received from control registers access characteristic
     */
    public Command(byte[] dataReceived) {
        this(new Register(dataReceived), Register.getTarget(dataReceived), Register.getPayload(dataReceived));
    }

    /**
     * Get the Register for this command
     *
     * @return the Register object managed from this command
     */
    public Register getRegister() {
        return register;
    }

    /**
     * Get the buffer managed from this command
     *
     * @return the data buffer to send or received from this command
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the short value received from this command
     *
     * @return the short value received from this command
     */
    public short getDataShort() {
        if (data.length == 2) {
            return NumberConversion.LittleEndian.bytesToInt16(data);
        }
        return -1; //Error condition
    }

    /**
     * Get the buffer managed from this command
     *
     * @return the data buffer to send or received from this command
     */
    public char[] getDataChar() {
        char [] cRet = null;
        if (data != null) {
            cRet = new char[data.length];
            for (int i = 0; i < data.length; i++)
                cRet[i] = (char) data[i];
        }//if
        return cRet;
    }

    /**
     * Get the target register from this command
     *
     * @return the Target set for this command (persistent or session)
     */
    public Register.Target getTarget(){
        return target;
    }

    /**
     * Get the packet to write the data payload to the target register of this command
     *
     * @return the data packet to sent through the config control characteristic
     */
    public byte [] ToWritePacket(){
        return register.ToWritePacket(getTarget(), data);
    }

    /**
     * Get the packet to read target register of this command
     *
     * @return the data packet to sent through the config control characteristic
     */
    public byte [] ToReadPacket(){
        return register.ToReadPacket(getTarget());
    }

    /**
     * Creates and returns a copy of this {@code Object}. The default
     * implementation returns a so-called "shallow" copy: It creates a new
     * instance of the same class and then copies the field values (including
     * object references) from this instance to the new instance. A "deep" copy,
     * in contrast, would also recursively clone nested objects. A subclass that
     * needs to implement this kind of cloning should call {@code super.clone()}
     * to create the new instance and then create deep copies of the nested,
     * mutable objects.
     *
     * @return a copy of this object.
     * @throws CloneNotSupportedException if this object's class does not implement the {@code
     *                                    Cloneable} interface.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException{
        return new Command((Register)register.clone(), this.target, this.data);
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must represent the same object
     * as this instance using a class-specific comparison. The general contract
     * is that this comparison should be reflexive, symmetric, and transitive.
     * Also, no object reference other than null is equal to null.
     * <p>The default implementation returns {@code true} only if {@code this ==
     * o}. See <a href="{@docRoot}reference/java/lang/Object.html#writing_equals">Writing a correct
     * {@code equals} method</a>
     * if you intend implementing your own {@code equals} method.
     * <p>The general contract for the {@code equals} and {@link
     * #hashCode()} methods is that if {@code equals} returns {@code true} for
     * any two objects, then {@code hashCode()} must return the same value for
     * these objects. This means that subclasses of {@code Object} usually
     * override either both methods or neither of them.
     *
     * @param o the object to compare this instance with.
     * @return {@code true} if the specified object is equal to this {@code
     * Object}; {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object o) {
        boolean bRet = false;
        if (o instanceof Command) {
            bRet = (o == this) || ((this.getRegister().equals(((Command) o).getRegister())) && (this.getTarget() == ((Command) o).getTarget()));
        }
        return bRet;
    }

}
