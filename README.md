# NineImageView
仿新浪微博九宫图排版展示(一图，二图，四图，多图,超过9图时的排版处理),支持GIF，长图。点击查看图片详情，可缩放。
项目中要实现微博的图文排版，想到用Glide加载图片（Glide直接支持Gif非常Nice其他图片加载库是不行的），所以就用到了
这个https://github.com/sfsheng0322/GlideImageView ，这是一个对Glide4.0+的很好的封装库，这个库里的Sample作者还有一个NineImageView，
很巧的是实现微博九宫图排版就是需要这个类似的控件，我很高兴地将其拿来使用，但是我在使用中发现了众多bug，断断续续地花了一个多礼拜的时间，
我修复了其存在的众多bug，在作者NineImageView基础上的做了一些修改(其实作者这个NineImageView也是在另外一个NineGridImageView的基础上修改得来的)
因为是持续的发现bug修改，到后面项目并添加了一些其他细节功能实现，具体修改了什么 实在是记不清了（只记得图片尺寸的限制修改（不同的手机设备，
对ImageView能加载的图片最大尺寸的限制不一样），避免重复加载的问题等等）.
具体的内容，有兴趣的朋友自己clone demo 去运行看看效果吧
