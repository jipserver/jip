package jip.jobs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultMessage implements Message{

    private Date createDate;
    private MessageType type;
    private String message;

    public DefaultMessage() {
        this(new Date(), MessageType.Info, "");
    }

    public DefaultMessage(Date createDate, MessageType type, String message) {
        if(createDate == null || type == null || message == null) throw new NullPointerException();
        this.createDate = createDate;
        this.type = type;
        this.message = message;
    }

    public DefaultMessage(Map data) {
        createDate = new Date(((Number) data.get("createDate")).longValue());
        type = MessageType.valueOf((String) data.get("type"));
        message = (String) data.get("message");
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public int compareTo(Message message) {
        return getCreateDate().compareTo(message.getCreateDate());
    }

    public static Map<String, Object> toMap(Message message) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("createDate", message.getCreateDate().getTime());
        map.put("message", message.getMessage());
        map.put("type", message.getType().toString());
        return map;
    }


}
