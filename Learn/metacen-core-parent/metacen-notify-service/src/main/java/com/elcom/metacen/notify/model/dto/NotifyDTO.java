/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

/**
 * @author Admin
 */
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
public class NotifyDTO implements Serializable {

    protected String id;
    protected String title;
    protected String content;
    protected String icon;
    protected String url;
    protected int statusView;
    protected int type;
    protected long timeSendNotify;
    protected String objectId;
}
