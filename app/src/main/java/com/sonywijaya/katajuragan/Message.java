package com.sonywijaya.katajuragan;

/**
 * Created by Sony Surya on 28/05/2017.
 */

public class Message {
    String id, updated_at, partner_id, partner_name, partner_avatar, last_message, last_message_sent;
    Boolean last_message_read;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(String partner_id) {
        this.partner_id = partner_id;
    }

    public String getPartner_name() {
        return partner_name;
    }

    public void setPartner_name(String partner_name) {
        this.partner_name = partner_name;
    }

    public String getPartner_avatar() {
        return partner_avatar;
    }

    public void setPartner_avatar(String partner_avatar) {
        this.partner_avatar = partner_avatar;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getLast_message_sent() {
        return last_message_sent;
    }

    public void setLast_message_sent(String last_message_sent) {
        this.last_message_sent = last_message_sent;
    }

    public Boolean getLast_message_read() {
        return last_message_read;
    }

    public void setLast_message_read(Boolean last_message_read) {
        this.last_message_read = last_message_read;
    }
}
