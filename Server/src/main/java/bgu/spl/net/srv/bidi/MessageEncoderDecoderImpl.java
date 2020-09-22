package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl<T> implements MessageEncoderDecoder {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private int lenArrEncode= 0;
    private short zeroCounter = -1;
    private int phase = 0;
    private short opCode = 0;
    private T messageTDecoded;
    private String messageDecoded = "";

    /**
     * add the next byte to the decoding process
     * *
     *
     * @param nextByte the next byte to consider for the currently decoded
     *                 message
     * @return a message if this byte completes one or null if it doesnt.
     */
    public T decodeNextByte(byte nextByte) {
        pushByte(nextByte);
        if (len == 2) {
            opCode = bytesToShort(bytes);
        }

        switch (opCode) {
            case 1: {
                if (phase == 0) {
                    phase = 1;
                    zeroCounter = 2;
                }

                decodeRegister(nextByte);

                if (phase == 2) {
                    rebootEncoderDecoder();
                    return messageTDecoded;
                }
                break;
            }

            case 2: {
                if (phase == 0) {
                    phase = 1;
                    zeroCounter = 2;
                }

                decodeLogin(nextByte);

                if (phase == 2) {
                    rebootEncoderDecoder();
                    return messageTDecoded;
                }
                break;
            }

            case 3: {
                decodeLogout(nextByte);
                rebootEncoderDecoder();
                return messageTDecoded;
            }
            case 4: {
                if (len == 5 && phase == 0) {
                    phase = 1;
                    byte[] bytesFollowers = {bytes[3], bytes[4]};
                    zeroCounter = bytesToShort(bytesFollowers);
                }
                if(len > 5)
                    decodeFollow(nextByte);

                if (phase == 2) {
                    rebootEncoderDecoder();
                    return messageTDecoded;
                }
                break;
            }
            case 5: {
                if (phase == 0) {
                    phase = 1;
                    zeroCounter = 1;
                }

                decodePost(nextByte);

                if (phase == 2) {
                    rebootEncoderDecoder();
                    return messageTDecoded;
                }
                break;
            }
            case 6: {
                if (phase == 0) {
                    phase = 1;
                    zeroCounter = 2;
                }
                deCodePM(nextByte);
                if (phase == 2) {
                    rebootEncoderDecoder();
                    return messageTDecoded;
                }
                break;
            }
            case 7: {
                deCodeUserList(nextByte);
                rebootEncoderDecoder();
                return messageTDecoded;
            }
            case 8: {
                if (phase == 0) {
                    phase = 1;
                    zeroCounter = 1;
                }

                decodeStat(nextByte);

                if (phase == 2) {
                    rebootEncoderDecoder();
                    return messageTDecoded;
                }
                break;
            }
        }
        return null;
    }

    private void decodeRegister(byte nextByte) {
        if (nextByte == '\0') {
            zeroCounter--;
        }
        if (zeroCounter == 0) {
            for (int i = 0; i < len; i++) {
                if (bytes[i] == 0) {
                    bytes[i] = 0x20;
                }
            }
            messageDecoded = popString();
            messageDecoded = "REGISTER " + messageDecoded.substring(2);
            messageTDecoded = (T) messageDecoded;
            phase = 2;
        }
    }

    private void decodeLogin(byte nextByte) {
        if (nextByte == '\0') {
            zeroCounter--;
        }
        if (zeroCounter == 0) {
            for (int i = 0; i < len; i++) {
                if (bytes[i] == 0) {
                    bytes[i] = 0x20;
                }
            }
            messageDecoded = popString();
            messageDecoded = "LOGIN " + messageDecoded.substring(2);
            messageTDecoded = (T) messageDecoded;
            phase = 2;
        }
    }

    private void decodeLogout(byte nextByte) {
        messageDecoded = "LOGOUT";
        messageTDecoded = (T) messageDecoded;
    }


    private void decodeFollow(byte nextByte) {
        if (nextByte == '\0') {
            zeroCounter--;
        }

        if (zeroCounter == 0) {
            byte[] bytesZeroOrOne = {bytes[2]};
            int intZeroOrOne;

            if(bytesZeroOrOne[0] == 0){
                intZeroOrOne = 0;
            }
            else
                intZeroOrOne = 1;


            byte[] bytesFollowersToMessage = {bytes[3], bytes[4]};
            short followersToMessage = bytesToShort(bytesFollowersToMessage);

            for (int i = 5; i < len; i++) {
                if (bytes[i] == 0) {
                    bytes[i] = 0x20;
                }
            }
            messageDecoded = popString();
            messageDecoded = "FOLLOW " + intZeroOrOne + " " + followersToMessage + " " + messageDecoded.substring(5);
            messageTDecoded = (T) messageDecoded;
            phase = 2;
        }

    }

    private void decodePost(byte nextByte) {
        if (nextByte == '\0') {
            zeroCounter--;
        }
        if (zeroCounter == 0) {
            for (int i = 0; i < len; i++) {
                if (bytes[i] == 0) {
                    bytes[i] = 0x20;
                }
            }
            messageDecoded = popString();
            messageDecoded = "POST " + messageDecoded.substring(2);
            messageTDecoded = (T) messageDecoded;
            phase = 2;
        }
    }

    private void deCodePM(byte nextByte) {
        if (nextByte == '\0') {
            zeroCounter--;
        }
        if (zeroCounter == 0) {
            for (int i = 0; i < len; i++) {
                if (bytes[i] == 0) {
                    bytes[i] = 0x20;
                }
            }
            messageDecoded = popString();
            messageDecoded = "PM " + messageDecoded.substring(2);
            messageTDecoded = (T) messageDecoded;
            phase = 2;
        }
    }

    private void deCodeUserList(byte nextByte) {
        messageDecoded = "USERLIST";
        messageTDecoded = (T) messageDecoded;
    }

    private void decodeStat(byte nextByte) {
        if (nextByte == '\0') {
            zeroCounter--;
        }
        if (zeroCounter == 0) {
            for (int i = 0; i < len; i++) {
                if (bytes[i] == 0) {
                    bytes[i] = 0x20;
                }
            }
            messageDecoded = popString();
            messageDecoded = "STAT " + messageDecoded.substring(2);
            messageTDecoded = (T) messageDecoded;
            phase = 2;
        }
    }

    /**
     * encodes the given message to bytes array
     *
     * @param message the message to encode
     * @return the encoded bytes
     */

    @Override
    public byte[] encode(Object message) {
        String messageUse = (String) message;
        String messageType = messageUse.substring(0,messageUse.indexOf(" "));
        messageUse = messageUse.substring(messageUse.indexOf(" ")+1);
        if (messageType.equals("NOTIFICATION"))
            return encodeNotification(messageUse);
        else if (messageType.equals("ACK"))
            return encodeACK(messageUse);
        else
            return encodeError(messageUse);
    }

    private byte[] encodeError(String messageUse) {
        byte[] bytesEncode = new byte[1<<10];
        short opCode = 11;
        byte[] opCodeBytes = shortToBytes(opCode);
        short messageOpcode = Short.parseShort(messageUse);
        byte[] messageOpCodeBytes = shortToBytes(messageOpcode);
        bytesEncode = addBytes(bytesEncode,opCodeBytes);
        bytesEncode = addBytes(bytesEncode, messageOpCodeBytes);
        return bytesEncode;
    }

    private byte[] encodeACK(String messageUse){
        byte[] bytesEncode = new byte[1<<10];
        short opCode = 10;
        short messageOpcode;
        if(messageUse.indexOf(" ")!=-1) {
            messageOpcode = Short.parseShort(messageUse.substring(0,messageUse.indexOf(" ")));
        }
        else {
            messageOpcode = Short.parseShort(messageUse);
        }
        messageUse = messageUse.substring(messageUse.indexOf(" ")+1);

        bytesEncode = addBytes(bytesEncode,shortToBytes(opCode));
        bytesEncode = addBytes(bytesEncode,shortToBytes(messageOpcode));

        if(messageOpcode == 4){
            short NumOfUsers = Short.parseShort(messageUse.substring(0,messageUse.indexOf(" ")));
            messageUse = messageUse.substring(messageUse.indexOf(" ")+1);
            String UserNameList = messageUse;
            bytesEncode = addBytes(bytesEncode,shortToBytes(NumOfUsers));
            bytesEncode = addBytes(bytesEncode,UserNameList.getBytes());
            bytesEncode = pushByte(bytesEncode,(byte)'\0');
        }
        if(messageOpcode == 7){
            short numOfUsers = Short.parseShort(messageUse.substring(0,messageUse.indexOf(" ")));
            bytesEncode = addBytes(bytesEncode,shortToBytes(numOfUsers));
            messageUse = messageUse.substring(messageUse.indexOf(" ")+1);
            String listOfUsers = messageUse;
            bytesEncode = addBytes(bytesEncode,listOfUsers.getBytes());
            bytesEncode = pushByte(bytesEncode,(byte)'\0');
        }
        if(messageOpcode == 8){
            short numPosts = Short.parseShort(messageUse.substring(0,messageUse.indexOf(" ")));
            bytesEncode = addBytes(bytesEncode,shortToBytes(numPosts));
            messageUse = messageUse.substring(messageUse.indexOf(" ")+1);
            short numFollowers = Short.parseShort(messageUse.substring(0,messageUse.indexOf(" ")));
            bytesEncode = addBytes(bytesEncode,shortToBytes(numFollowers));
            messageUse = messageUse.substring(messageUse.indexOf(" ")+1);
            short numFollowing = Short.parseShort(messageUse);
            bytesEncode = addBytes(bytesEncode,shortToBytes(numFollowing));
        }
        rebootEncoderDecoder();
        return bytesEncode;
    }

    private byte[] encodeNotification(String messageUse) {
        byte[] bytesEncode = new byte[1<<10];
        String typeStr = messageUse.substring(0,messageUse.indexOf(" "));
        messageUse = messageUse.substring(messageUse.indexOf(" ")+1);
        String userStr = messageUse.substring(0,messageUse.indexOf(" "));
        messageUse = messageUse.substring(messageUse.indexOf(" ")+1);
        String contentStr = messageUse;

        short opCode = 9;
        byte[] opCodeBytes = shortToBytes(opCode);
        bytesEncode = addBytes(bytesEncode,opCodeBytes);
        byte type;

        if(typeStr.equals("PM"))
            type = '0';
        else
            type = '1';

        bytesEncode = pushByte(bytesEncode,type);
        byte[] user = userStr.getBytes();
        bytesEncode = addBytes(bytesEncode,user);
        bytesEncode = pushByte(bytesEncode,(byte)'\0');
        byte[] content = contentStr.trim().getBytes();
        bytesEncode = addBytes(bytesEncode,content);
        bytesEncode = pushByte(bytesEncode,(byte)'\0');

        return bytesEncode;
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private byte[] pushByte(byte[] bytesEncode, byte nextByte) {
            if (lenArrEncode >= bytesEncode.length) {
                bytesEncode = Arrays.copyOf(bytesEncode, lenArrEncode * 2);
            }
            bytesEncode[lenArrEncode++] = nextByte;
            return bytesEncode;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }

    private void rebootEncoderDecoder(){
        bytes = new byte[1 << 10]; //start with 1k
        len = 0;
        lenArrEncode=0;
        zeroCounter = -1;
        phase = 0;
        opCode = 0;
        messageDecoded = "";
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public byte[] addBytes(byte[] bytesEncode,byte[] bytesToadd){
        for(byte b:bytesToadd){
            bytesEncode = pushByte(bytesEncode,b);
        }
        return bytesEncode;
    }
}
