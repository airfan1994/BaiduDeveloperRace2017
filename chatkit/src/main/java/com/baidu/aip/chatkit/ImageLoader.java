/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.aip.chatkit;

import android.widget.ImageView;

/**
 * Callback for implementing images loading in message list
 */
public interface ImageLoader {

    void loadImage(ImageView imageView, String url);

}
