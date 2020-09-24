/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE MEDIAFILE_REQUEST PACKING
package com.MAVLink.DLink;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Request a mediafile's information, thumbnail, preview or raw data.
*/
public class msg_mediafile_request extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_MEDIAFILE_REQUEST = 152;
    public static final int MAVLINK_MSG_LENGTH = 8;
    private static final long serialVersionUID = MAVLINK_MSG_ID_MEDIAFILE_REQUEST;


      
    /**
    * The index of the request page.
    */
    public long page_index;
      
    /**
    * Index of the requested file on the target storage location.
    */
    public int index;
      
    /**
    * The storage location, SD card or the internal storage.
    */
    public short storage_location;
      
    /**
    * The type of the current request.
    */
    public short request_type;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 1;
        packet.compid = 1;
        packet.msgid = MAVLINK_MSG_ID_MEDIAFILE_REQUEST;
              
        packet.payload.putUnsignedInt(page_index);
              
        packet.payload.putUnsignedShort(index);
              
        packet.payload.putUnsignedByte(storage_location);
              
        packet.payload.putUnsignedByte(request_type);
        
        return packet;
    }

    /**
    * Decode a mediafile_request message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.page_index = payload.getUnsignedInt();
              
        this.index = payload.getUnsignedShort();
              
        this.storage_location = payload.getUnsignedByte();
              
        this.request_type = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_mediafile_request(){
        msgid = MAVLINK_MSG_ID_MEDIAFILE_REQUEST;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_mediafile_request(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MEDIAFILE_REQUEST;
        unpack(mavLinkPacket.payload);        
    }

            
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_MEDIAFILE_REQUEST - sysid:"+sysid+" compid:"+compid+" page_index:"+page_index+" index:"+index+" storage_location:"+storage_location+" request_type:"+request_type+"";
    }
}
        