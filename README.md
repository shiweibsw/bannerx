# BannerX

这是一款Android平台下的轮播控件，支持图片，视频以及图片视频混合轮播，支持自定义Item，自定义指示器，自定义切换动画以及自定义视频播放引擎。

## Preview

![图片](screenshot/img.gif)
![视频](screenshot/video.gif)
![图片&视频](screenshot/mix.gif)

## Functions

#### 1.支持自定义轮播内容

轮播内容(图片,视频, 图片视频混合),三种轮播内容均支持 自动轮播，无限轮播，视频默认播放完毕切换下一个

#### 2. 支持自定义Item

#### 3.支持自定义指示器

#### 4.支持自定义Item切换效果

#### 5.支持自定义视频播放引擎

#### 6. 支持自定义Item圆角

## Installation

```
 repositories {
        mavenCentral()
 }
 
 implementation 'io.github.shiwebsw:bannerx:1.0.0'
```

## Usage

xml

```
        <com.android.view.bannerx.library.BannerX
            android:id="@+id/bannerX"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent" />        
```

kotlin

```
            val banners = mutableListOf<String>(
                "https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF",
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                "https://t7.baidu.com/it/u=825057118,3516313570&fm=193&f=GIF"
            )
            bannerX.apply {
                val indicatorView = IndicatorView(this@MainActivity)
                    .setIndicatorColor(Color.BLACK)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(INDICATOR_CIRCLE_RECT)
                setIndicator(indicatorView)
                setAdapter(object : DefaultVideoAdapter(bannerX.getPlayer(), banners) {
                    override fun onBindViewHolder(holder: DefaultVideoHolder, position: Int) {
                        super.onBindViewHolder(holder, position)
                        if (getItemViewType(position) == TYPE_IMG)
                            Glide.with(holder.itemView).load(banners[position]).into(holder.ivImg!!)
                    }
                })
                start()
            }
```

## APIS

[(Back to top)](#table-of-contents)

<!-- This is the place where you give instructions to developers on how to modify the code.

You could give **instructions in depth** of **how the code works** and how everything is put together.

You could also give specific instructions to how they can setup their development environment.

Ideally, you should keep the README simple. If you need to add more complex explanations, use a wiki. Check out [this wiki](https://github.com/navendu-pottekkat/nsfw-filter/wiki) for inspiration. -->

# License

```
   Copyright (c) 2021-present, shiweibsw.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

```
