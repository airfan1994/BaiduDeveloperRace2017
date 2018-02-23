/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.aip.chatkit.model;


/*
 * Created by troy379 on 28.03.17.
 */

/**
 * Interface used to mark messages as custom content types. For its representation see {@link MessageHolders}
 */

public interface MessageContentType extends IMessage {

    /**
     * Default media type for image message.
     */
    interface Image extends IMessage {
        String getImageUrl();
    }

    // other default types will be here

}
