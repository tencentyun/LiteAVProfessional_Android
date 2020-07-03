## 目录结构说明

本目录包含 Android 版 专业版(Professional) SDK 的Demo 源代码，主要演示接口如何调用以及最基本的功能。

```
├─ Demo // 企业版Demo，包括演示直播、点播、短视频、RTC 在内的多项功能
|  ├─ app                   // 程序入口界面
|  ├─ audioeffectsettingkit // 音效面板，包含BGM播放，变声，混响，变调等效果
|  ├─ beautysettingkit      // 美颜面板，包含美颜，滤镜，动效等效果
|  ├─ debug                 // 包含 GenerateTestUserSig，用于本地生成测试用的 UserSig
|  ├─ login                 // 一个演示性质的简单登录界面
|  ├─ trtcmeetingdemo       // 场景一：多人会议，类似腾讯会议，包含屏幕分享
|  ├─ trtcvoiceroomdemo     // 场景二：语音聊天室，也叫语聊房，多人音频聊天场景
|  ├─ trtcliveroomdemo      // 场景三：互动直播，包含连麦、PK、聊天、点赞等特性
|  ├─ trtcaudiocalldemo     // 场景四：音频通话，展示双人音频通话，有离线通知能力
|  ├─ trtcvideocalldemo     // 场景五：视频通话，展示双人视频通话，有离线通知能力
|  ├─ liveplayerdemo        // 直播播放，可以扫码播放地址进行播放
|  ├─ livepusherdemo        // 直播推流，包含推流时，设置美颜，音效，等基础操作
|  ├─ mlvbliveroomdemo      // 互动直播，包含连麦、聊天、点赞等特性
|  ├─ ugckit                // UGC 组件，包含视频录制，编辑，合成，发布上传等基础功能
|  ├─ ugcvideorecorddemo    // 视频录制 Demo
|  ├─ ugcvideojoindemo      // 视频合成 Demo
|  ├─ ugcvideoeditdemo      // 视频编辑 Demo
|  ├─ ugcvideouploaddemo    // 视频发布上传 Demo
|  ├─ superplayerkit        // 超级播放器组件
|  ├─ superplayerdemo       // 超级播放器 Demo
|  
├─ SDK 
|  ├─ LiteAVSDK_Professional_x.y.zzzz.aar // 如果您下载的是专业版 zip 包，解压后将出现此文件夹，其中 x.y.zzzz 表示 SDK 版本号 
|  ├─ LiteAVSDK_Enterprise_x.y.zzzz.aar   // 如果您下载的是企业版 zip 包，解压后将出现此文件夹，其中 x.y.zzzz 表示 SDK 版本号 
```

## SDK 分类和下载

腾讯云 专业版(Professional) SDK 基于 LiteAVSDK 统一框架设计和实现，该框架包含直播、点播、短视频、RTC、AI美颜在内的多项功能：

- 如果您需要使用多个功能而不希望打包多个 SDK，可以下载专业版：[TXLiteAVSDK_Professional.zip](https://cloud.tencent.com/document/product/647/32689#Professional)
- 如果您已经通过腾讯云商务购买了 AI 美颜 License，可以下载企业版：[TXLiteAVSDK_Enterprise.zip](https://cloud.tencent.com/document/product/647/32689#Enterprise)

## 相关文档链接

- [SDK 的版本更新历史](https://github.com/tencentyun/LiteAVProfessional_Android/releases)
- [实时音视频（TRTC） API文档](http://doc.qcloudtrtc.com/md_introduction_trtc_Android_%E6%A6%82%E8%A7%88.html)
- [播放器（Player） API文档](https://github.com/tencentyun/SuperPlayer_Android/wiki)
- [移动直播（MLVB） API文档](https://cloud.tencent.com/document/product/454/34766)
- [短视频（UGSV） API文档](http://doc.qcloudtrtc.com/group__TXUGCRecord__android.html)
- [Demo体验](https://cloud.tencent.com/document/product/454/6555#.E7.B2.BE.E7.AE.80.E7.89.88-demo)